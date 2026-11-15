package com.example.practicetoeicapp.admin.luyennghe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.practicetoeicapp.luyennghe.CauLuyenNghe;
import com.example.practicetoeicapp.ui.AudioPreviewPlayer;
import com.example.practicetoeicapp.ui.UiFeedback;

import java.util.ArrayList;

public class EditLuyenNgheActivity extends AppCompatActivity {
    View imgBack, imgEdit;
    ImageView imgHinh;
    ImageButton btnPlayAudio;
    Button btnChonHinh, btnChonAudio;
    TextView tvAudioStatus;
    Spinner spnDapAnTrue;
    final int REQUEST_CHOOSE_PHOTO = 321;
    final int REQUEST_CHOOSE_AUDIO = 322;
    QuestionRepository repository;
    SupabaseStorageRepository storageRepository;
    AudioPreviewPlayer audioPreviewPlayer;
    AlertDialog loadingDialog;
    CauLuyenNghe luyenNghe;
    Uri imageUri;
    Uri audioUri;
    String existingImageUrl = "";
    String existingAudioUrl = "";
    boolean saving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_luyennghe);
        imgBack = findViewById(R.id.imgBackEditLN);
        imgHinh = findViewById(R.id.imgHinhEditLN);
        imgEdit = findViewById(R.id.imgEditLN);
        btnChonHinh = findViewById(R.id.btnChonHinhEditLN);
        btnChonAudio = findViewById(R.id.btnChonAudioEditLN);
        btnPlayAudio = findViewById(R.id.btnPlayAudioEditLN);
        tvAudioStatus = findViewById(R.id.tvAudioStatusEditLN);
        spnDapAnTrue = findViewById(R.id.spnDapAnTrueEditLN);
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
                UiFeedback.showTransient(EditLuyenNgheActivity.this,
                        getString(R.string.form_audio_error), false);
            }
        });
        ArrayList<String> listDapAn = new ArrayList<>();
        listDapAn.add("A");
        listDapAn.add("B");
        listDapAn.add("C");
        listDapAn.add("D");
        ArrayAdapter<String> dapAnAdapter = new ArrayAdapter<>(EditLuyenNgheActivity.this,
                R.layout.support_simple_spinner_dropdown_item, listDapAn);
        spnDapAnTrue.setAdapter(dapAnAdapter);
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
        int idLN = getIntent().getIntExtra("ID_LN", -1);
        loadLuyenNghe(idLN);
    }

    private void loadLuyenNghe(int idLN) {
        repository.loadLuyenNgheById(idLN, new QuestionRepository.DataCallback<CauLuyenNghe>() {
            @Override
            public void onSuccess(CauLuyenNghe data) {
                luyenNghe = data;
                existingImageUrl = data.getHinhAnhUrl() == null ? "" : data.getHinhAnhUrl();
                existingAudioUrl = data.getAudio() == null ? "" : data.getAudio();
                tvAudioStatus.setText(existingAudioUrl.isEmpty()
                        ? R.string.form_audio_none
                        : R.string.form_audio_current);
                Glide.with(EditLuyenNgheActivity.this).load(existingImageUrl).into(imgHinh);
                int position = 0;
                try {
                    position = Integer.parseInt(data.getDapanTrue()) - 1;
                } catch (NumberFormatException ignored) {
                }
                spnDapAnTrue.setSelection(Math.max(0, Math.min(3, position)));
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
                UiFeedback.showInfo(EditLuyenNgheActivity.this,
                        getString(R.string.feedback_load_failed),
                        EditLuyenNgheActivity.this::finish);
            }
        });
    }

    private void save() {
        if (saving || luyenNghe == null) {
            return;
        }
        if (imageUri == null && existingImageUrl.isEmpty()) {
            UiFeedback.showTransient(EditLuyenNgheActivity.this,
                    getString(R.string.form_image_required), false);
            return;
        }
        if (audioUri == null && existingAudioUrl.isEmpty()) {
            UiFeedback.showTransient(EditLuyenNgheActivity.this,
                    getString(R.string.form_audio_required), false);
            return;
        }
        saving = true;
        String dapanTrue = String.valueOf(spnDapAnTrue.getSelectedItemPosition() + 1);
        if (imageUri != null) {
            loadingDialog = UiFeedback.showLoading(this, R.string.feedback_uploading_image);
            storageRepository.uploadImage(this, imageUri, MediaTypeUtil.PREFIX_LISTENING_IMAGES,
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
                            afterImage(imageUrl, dapanTrue);
                        }

                        @Override
                        public void onError(Exception e) {
                            saving = false;
                            UiFeedback.dismiss(loadingDialog);
                            UiFeedback.showError(EditLuyenNgheActivity.this,
                                    getString(R.string.feedback_upload_failed));
                        }
                    });
        } else {
            if (audioUri != null) {
                loadingDialog = UiFeedback.showLoading(this, R.string.feedback_uploading_audio);
            } else {
                loadingDialog = UiFeedback.showLoading(this, R.string.feedback_saving);
            }
            afterImage(existingImageUrl, dapanTrue);
        }
    }

    private void afterImage(String imageUrl, String dapanTrue) {
        if (audioUri != null) {
            storageRepository.uploadAudio(this, audioUri, MediaTypeUtil.PREFIX_LISTENING_AUDIO,
                    new SupabaseStorageRepository.UploadCallback() {
                        @Override
                        public void onSuccess(String audioUrl) {
                            UiFeedback.setLoadingMessage(loadingDialog, R.string.feedback_saving);
                            updateLuyenNghe(imageUrl, dapanTrue, audioUrl);
                        }

                        @Override
                        public void onError(Exception e) {
                            saving = false;
                            UiFeedback.dismiss(loadingDialog);
                            UiFeedback.showError(EditLuyenNgheActivity.this,
                                    getString(R.string.feedback_upload_failed));
                        }
                    });
        } else {
            updateLuyenNghe(imageUrl, dapanTrue, existingAudioUrl);
        }
    }

    private void updateLuyenNghe(String imageUrl, String dapanTrue, String audioUrl) {
        repository.updateLuyenNghe(luyenNghe.getIdbai(), luyenNghe.getIdbo(),
                "A", "B", "C", "D", dapanTrue, imageUrl, audioUrl,
                new QuestionRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        saving = false;
                        UiFeedback.dismiss(loadingDialog);
                        UiFeedback.showTransient(EditLuyenNgheActivity.this,
                                getString(R.string.feedback_update_success), true);
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        UiFeedback.dismiss(loadingDialog);
                        UiFeedback.showTransient(EditLuyenNgheActivity.this,
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
