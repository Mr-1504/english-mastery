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
import com.example.practicetoeicapp.ui.AudioPreviewPlayer;
import com.example.practicetoeicapp.ui.UiFeedback;

import java.util.ArrayList;

public class AddTuVungActivity extends AppCompatActivity {
    View imgBack, imgAdd;
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
    ArrayList<String> listTuLoai = new ArrayList<>();
    int idBTV = 0;
    Uri imageUri;
    Uri audioUri;
    boolean saving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tuvung);
        imgBack = findViewById(R.id.imgBackAddTV);
        imgHinh = findViewById(R.id.imgHinhAddTV);
        imgAdd = findViewById(R.id.imgAddTV);
        btnChonHinh = findViewById(R.id.btnChonHinhAddTV);
        btnChonAudio = findViewById(R.id.btnChonAudioAddTV);
        btnPlayAudio = findViewById(R.id.btnPlayAudioAddTV);
        edtTuVung = findViewById(R.id.edtTuVungAddTV);
        edtNghia = findViewById(R.id.edtNghiaAddTV);
        tvAudioStatus = findViewById(R.id.tvAudioStatusAddTV);
        spnTuLoai = findViewById(R.id.spnLoaiTuAddTV);
        repository = QuestionRepository.getInstance();
        storageRepository = SupabaseStorageRepository.getInstance();
        audioPreviewPlayer = new AudioPreviewPlayer(this);
        audioPreviewPlayer.setOnStateListener(new AudioPreviewPlayer.OnStateListener() {
            @Override
            public void onStateChanged(boolean playing) {
                btnPlayAudio.setImageResource(playing
                        ? R.drawable.ic_stop
                        : R.drawable.ic_play_arrow);
                tvAudioStatus.setText(playing
                        ? R.string.form_audio_playing
                        : audioUri != null ? R.string.form_audio_selected : R.string.form_audio_none);
            }

            @Override
            public void onError() {
                UiFeedback.showTransient(AddTuVungActivity.this,
                        getString(R.string.form_audio_error), false);
            }
        });
        listTuLoai.add("Danh từ");
        listTuLoai.add("Động từ");
        listTuLoai.add("Tính từ");
        listTuLoai.add("Trạng từ");
        listTuLoai.add("Giới từ");
        ArrayAdapter<String> tuLoaiAdapter = new ArrayAdapter<>(AddTuVungActivity.this,
                R.layout.support_simple_spinner_dropdown_item, listTuLoai);
        spnTuLoai.setAdapter(tuLoaiAdapter);
        idBTV = getIntent().getIntExtra("idBoTuVung", -1);
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
                audioPreviewPlayer.toggle(audioUri);
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    private void save() {
        if (saving) {
            return;
        }
        String dapan = edtTuVung.getText().toString().trim();
        String nghia = edtNghia.getText().toString().trim();
        String loaitu = listTuLoai.get(spnTuLoai.getSelectedItemPosition());
        if (dapan.isEmpty() || nghia.isEmpty()) {
            UiFeedback.showTransient(AddTuVungActivity.this,
                    getString(R.string.feedback_required_fields), false);
            return;
        }
        if (imageUri == null) {
            UiFeedback.showTransient(AddTuVungActivity.this,
                    getString(R.string.form_image_required), false);
            return;
        }
        if (audioUri == null) {
            UiFeedback.showTransient(AddTuVungActivity.this,
                    getString(R.string.form_audio_required), false);
            return;
        }
        saving = true;
        loadingDialog = UiFeedback.showLoading(this, R.string.feedback_uploading_image);
        storageRepository.uploadImage(this, imageUri, MediaTypeUtil.PREFIX_VOCABULARY_IMAGES,
                new SupabaseStorageRepository.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        UiFeedback.setLoadingMessage(loadingDialog,
                                R.string.feedback_uploading_audio);
                        uploadAudio(imageUrl, dapan, nghia, loaitu);
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        UiFeedback.dismiss(loadingDialog);
                        UiFeedback.showError(AddTuVungActivity.this,
                                getString(R.string.feedback_upload_failed));
                    }
                });
    }

    private void uploadAudio(String imageUrl, String dapan, String nghia, String loaitu) {
        storageRepository.uploadAudio(this, audioUri, MediaTypeUtil.PREFIX_VOCABULARY_AUDIO,
                new SupabaseStorageRepository.UploadCallback() {
                    @Override
                    public void onSuccess(String audioUrl) {
                        UiFeedback.setLoadingMessage(loadingDialog, R.string.feedback_saving);
                        createTuVung(imageUrl, dapan, nghia, loaitu, audioUrl);
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        UiFeedback.dismiss(loadingDialog);
                        UiFeedback.showError(AddTuVungActivity.this,
                                getString(R.string.feedback_upload_failed));
                    }
                });
    }

    private void createTuVung(String imageUrl, String dapan, String nghia,
                              String loaitu, String audioUrl) {
        repository.createTuVung(idBTV, dapan, nghia, loaitu, audioUrl, imageUrl,
                new QuestionRepository.DataCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer data) {
                        saving = false;
                        UiFeedback.dismiss(loadingDialog);
                        UiFeedback.showTransient(AddTuVungActivity.this,
                                getString(R.string.feedback_add_success), true);
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        UiFeedback.dismiss(loadingDialog);
                        UiFeedback.showTransient(AddTuVungActivity.this,
                                getString(R.string.feedback_add_failed), false);
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
