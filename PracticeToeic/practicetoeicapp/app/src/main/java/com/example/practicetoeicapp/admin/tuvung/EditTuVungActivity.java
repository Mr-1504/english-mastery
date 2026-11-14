package com.example.practicetoeicapp.admin.tuvung;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.MediaTypeUtil;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.data.SupabaseStorageRepository;
import com.example.practicetoeicapp.hoctuvung.TuVung;
import com.example.practicetoeicapp.ui.AudioPreviewPlayer;
import com.example.practicetoeicapp.ui.UiFeedback;

import java.util.ArrayList;

public class EditTuVungActivity extends AppCompatActivity {
    View imgBack, imgEdit;
    ImageView imgHinh;
    ImageButton btnPlayAudio;
    Button btnChonHinh, btnChonAudio;
    EditText edtTuVung, edtNghia;
    TextView tvAudioStatus;
    Spinner spnTuLoai;
    final int REQUEST_CHOOSE_PHOTO = 321;
    final int REQUEST_CHOOSE_AUDIO = 322;
    QuestionRepository repository;
    SupabaseStorageRepository storageRepository;
    AudioPreviewPlayer audioPreviewPlayer;
    AlertDialog loadingDialog;
    TuVung tuVung;
    Uri imageUri;
    Uri audioUri;
    String existingImageUrl = "";
    String existingAudioUrl = "";
    boolean saving = false;
    ArrayList<String> listTuLoai = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tuvung);
        imgBack = findViewById(R.id.imgBackEditTV);
        imgHinh = findViewById(R.id.imgHinhEditTV);
        imgEdit = findViewById(R.id.imgEditTV);
        btnChonHinh = findViewById(R.id.btnChonHinhEditTV);
        btnChonAudio = findViewById(R.id.btnChonAudioEditTV);
        btnPlayAudio = findViewById(R.id.btnPlayAudioEditTV);
        edtTuVung = findViewById(R.id.edtTuVungEditTV);
        edtNghia = findViewById(R.id.edtNghiaEditTV);
        tvAudioStatus = findViewById(R.id.tvAudioStatusEditTV);
        spnTuLoai = findViewById(R.id.spnLoaiTuEditTV);
        repository = QuestionRepository.getInstance();
        storageRepository = SupabaseStorageRepository.getInstance();
        audioPreviewPlayer = new AudioPreviewPlayer(this);
        audioPreviewPlayer.setOnStateListener(new AudioPreviewPlayer.OnStateListener() {
            @Override
            public void onStateChanged(boolean playing) {
                btnPlayAudio.setImageResource(playing
                        ? R.drawable.ic_stop
                        : R.drawable.ic_play_arrow);
                if (playing) {
                    tvAudioStatus.setText(R.string.form_audio_playing);
                } else if (audioUri != null) {
                    tvAudioStatus.setText(R.string.form_audio_selected);
                } else {
                    tvAudioStatus.setText(existingAudioUrl.isEmpty()
                            ? R.string.form_audio_none
                            : R.string.form_audio_current);
                }
            }

            @Override
            public void onError() {
                UiFeedback.showTransient(EditTuVungActivity.this,
                        getString(R.string.form_audio_error), false);
            }
        });
        listTuLoai.add("Danh từ");
        listTuLoai.add("Động từ");
        listTuLoai.add("Tính từ");
        listTuLoai.add("Trạng từ");
        listTuLoai.add("Giới từ");
        ArrayAdapter<String> tuLoaiAdapter = new ArrayAdapter<>(EditTuVungActivity.this,
                R.layout.support_simple_spinner_dropdown_item, listTuLoai);
        spnTuLoai.setAdapter(tuLoaiAdapter);
        btnChonHinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });
        btnChonAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseAudio();
            }
        });
        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = audioUri;
                if (uri == null && !existingAudioUrl.isEmpty()) {
                    uri = Uri.parse(existingAudioUrl);
                }
                audioPreviewPlayer.toggle(uri);
            }
        });
        int idTV = getIntent().getIntExtra("ID_TV", -1);
        loadTuVung(idTV);
    }

    private void loadTuVung(int idTV) {
        repository.loadTuVungById(idTV, new QuestionRepository.DataCallback<TuVung>() {
            @Override
            public void onSuccess(TuVung data) {
                tuVung = data;
                existingImageUrl = data.getAnhUrl() == null ? "" : data.getAnhUrl();
                existingAudioUrl = data.getAudio() == null ? "" : data.getAudio();
                edtTuVung.setText(data.getDapan());
                edtNghia.setText(data.getDichnghia());
                tvAudioStatus.setText(existingAudioUrl.isEmpty()
                        ? R.string.form_audio_none
                        : R.string.form_audio_current);
                Glide.with(EditTuVungActivity.this).load(existingImageUrl).into(imgHinh);
                spnTuLoai.setSelection(Math.max(0, listTuLoai.indexOf(data.getLoaitu())));
                imgBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                imgEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        save();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                UiFeedback.showInfo(EditTuVungActivity.this,
                        getString(R.string.feedback_load_failed),
                        EditTuVungActivity.this::finish);
            }
        });
    }

    private void save() {
        if (saving || tuVung == null) {
            return;
        }
        String dapan = edtTuVung.getText().toString().trim();
        String nghia = edtNghia.getText().toString().trim();
        String loaitu = listTuLoai.get(spnTuLoai.getSelectedItemPosition());
        if (dapan.isEmpty() || nghia.isEmpty()) {
            UiFeedback.showTransient(EditTuVungActivity.this,
                    getString(R.string.feedback_required_fields), false);
            return;
        }
        if (imageUri == null && existingImageUrl.isEmpty()) {
            UiFeedback.showTransient(EditTuVungActivity.this,
                    getString(R.string.form_image_required), false);
            return;
        }
        if (audioUri == null && existingAudioUrl.isEmpty()) {
            UiFeedback.showTransient(EditTuVungActivity.this,
                    getString(R.string.form_audio_required), false);
            return;
        }
        saving = true;
        if (imageUri != null) {
            loadingDialog = UiFeedback.showLoading(this, R.string.feedback_uploading_image);
            storageRepository.uploadImage(this, imageUri, MediaTypeUtil.PREFIX_VOCABULARY_IMAGES,
                    new SupabaseStorageRepository.UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            if (audioUri != null) {
                                UiFeedback.setLoadingMessage(loadingDialog,
                                        R.string.feedback_uploading_audio);
                            } else {
                                UiFeedback.setLoadingMessage(loadingDialog,
                                        R.string.feedback_saving);
                            }
                            afterImage(imageUrl, dapan, nghia, loaitu);
                        }

                        @Override
                        public void onError(Exception e) {
                            saving = false;
                            UiFeedback.dismiss(loadingDialog);
                            UiFeedback.showError(EditTuVungActivity.this,
                                    getString(R.string.feedback_upload_failed));
                        }
                    });
        } else {
            if (audioUri != null) {
                loadingDialog = UiFeedback.showLoading(this, R.string.feedback_uploading_audio);
            } else {
                loadingDialog = UiFeedback.showLoading(this, R.string.feedback_saving);
            }
            afterImage(existingImageUrl, dapan, nghia, loaitu);
        }
    }

    private void afterImage(String imageUrl, String dapan, String nghia, String loaitu) {
        if (audioUri != null) {
            storageRepository.uploadAudio(this, audioUri, MediaTypeUtil.PREFIX_VOCABULARY_AUDIO,
                    new SupabaseStorageRepository.UploadCallback() {
                        @Override
                        public void onSuccess(String audioUrl) {
                            UiFeedback.setLoadingMessage(loadingDialog, R.string.feedback_saving);
                            updateTuVung(imageUrl, dapan, nghia, loaitu, audioUrl);
                        }

                        @Override
                        public void onError(Exception e) {
                            saving = false;
                            UiFeedback.dismiss(loadingDialog);
                            UiFeedback.showError(EditTuVungActivity.this,
                                    getString(R.string.feedback_upload_failed));
                        }
                    });
        } else {
            updateTuVung(imageUrl, dapan, nghia, loaitu, existingAudioUrl);
        }
    }

    private void updateTuVung(String imageUrl, String dapan, String nghia,
                              String loaitu, String audioUrl) {
        repository.updateTuVung(tuVung.getIdtu(), tuVung.getIdbo(), dapan, nghia,
                loaitu, audioUrl, imageUrl,
                new QuestionRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        saving = false;
                        UiFeedback.dismiss(loadingDialog);
                        UiFeedback.showTransient(EditTuVungActivity.this,
                                getString(R.string.feedback_update_success), true);
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        UiFeedback.dismiss(loadingDialog);
                        UiFeedback.showTransient(EditTuVungActivity.this,
                                getString(R.string.feedback_update_failed), false);
                    }
                });
    }

    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CHOOSE_PHOTO);
    }

    private void chooseAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CHOOSE_AUDIO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        if (requestCode == REQUEST_CHOOSE_PHOTO) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgHinh);
        } else if (requestCode == REQUEST_CHOOSE_AUDIO) {
            audioPreviewPlayer.stop();
            audioUri = data.getData();
            btnPlayAudio.setImageResource(R.drawable.ic_play_arrow);
            tvAudioStatus.setText(R.string.form_audio_selected);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPreviewPlayer != null) {
            audioPreviewPlayer.release();
        }
        UiFeedback.dismiss(loadingDialog);
    }
}
