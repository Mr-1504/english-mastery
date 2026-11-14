package com.example.practicetoeicapp.admin.luyennghe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.MediaTypeUtil;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.data.SupabaseStorageRepository;
import com.example.practicetoeicapp.luyennghe.CauLuyenNghe;

import java.util.ArrayList;

public class EditLuyenNgheActivity extends AppCompatActivity {
    View imgBack, imgEdit;
    ImageView imgHinh;
    Button btnChonHinh, btnChonAudio;
    EditText edtAudio;
    Spinner spnDapAnTrue;
    final int REQUEST_CHOOSE_PHOTO = 321;
    final int REQUEST_CHOOSE_AUDIO = 322;
    QuestionRepository repository;
    SupabaseStorageRepository storageRepository;
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
        edtAudio = findViewById(R.id.edtAudioEditLN);
        spnDapAnTrue = findViewById(R.id.spnDapAnTrueEditLN);
        repository = QuestionRepository.getInstance();
        storageRepository = SupabaseStorageRepository.getInstance();
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
        int idLN = getIntent().getIntExtra("ID_LN", -1);
        loadLuyenNghe(idLN);
    }

    private void loadLuyenNghe(int idLN) {
        repository.loadLuyenNgheById(idLN, new QuestionRepository.DataCallback<CauLuyenNghe>() {
            @Override
            public void onSuccess(CauLuyenNghe data) {
                luyenNghe = data;
                existingImageUrl = data.getHinhAnhUrl();
                existingAudioUrl = data.getAudio();
                edtAudio.setText(existingAudioUrl);
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
                        Intent intent = new Intent(EditLuyenNgheActivity.this,
                                AdminLuyenNgheActivity.class);
                        intent.putExtra("idBoLuyenNghe", luyenNghe.getIdbo());
                        startActivity(intent);
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
                Toast.makeText(EditLuyenNgheActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void save() {
        if (saving || luyenNghe == null) {
            return;
        }
        String audio = edtAudio.getText().toString().trim();
        if (imageUri == null && existingImageUrl.isEmpty()) {
            Toast.makeText(EditLuyenNgheActivity.this,
                    "Chưa chọn hình", Toast.LENGTH_SHORT).show();
            return;
        }
        if (audioUri == null && audio.isEmpty()) {
            Toast.makeText(EditLuyenNgheActivity.this,
                    "Chưa chọn audio", Toast.LENGTH_SHORT).show();
            return;
        }
        saving = true;
        String dapanTrue = String.valueOf(spnDapAnTrue.getSelectedItemPosition() + 1);
        if (imageUri != null) {
            storageRepository.uploadImage(this, imageUri, MediaTypeUtil.PREFIX_LISTENING_IMAGES,
                    new SupabaseStorageRepository.UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            afterImage(imageUrl, dapanTrue, audio);
                        }

                        @Override
                        public void onError(Exception e) {
                            saving = false;
                            Toast.makeText(EditLuyenNgheActivity.this,
                                    "Tải hình thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            afterImage(existingImageUrl, dapanTrue, audio);
        }
    }

    private void afterImage(String imageUrl, String dapanTrue, String audio) {
        if (audioUri != null) {
            storageRepository.uploadAudio(this, audioUri, MediaTypeUtil.PREFIX_LISTENING_AUDIO,
                    new SupabaseStorageRepository.UploadCallback() {
                        @Override
                        public void onSuccess(String audioUrl) {
                            updateLuyenNghe(imageUrl, dapanTrue, audioUrl);
                        }

                        @Override
                        public void onError(Exception e) {
                            saving = false;
                            Toast.makeText(EditLuyenNgheActivity.this,
                                    "Tải audio thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            updateLuyenNghe(imageUrl, dapanTrue, audio);
        }
    }

    private void updateLuyenNghe(String imageUrl, String dapanTrue, String audioUrl) {
        repository.updateLuyenNghe(luyenNghe.getIdbai(), luyenNghe.getIdbo(),
                "A", "B", "C", "D", dapanTrue, imageUrl, audioUrl,
                new QuestionRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        saving = false;
                        Toast.makeText(EditLuyenNgheActivity.this,
                                "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditLuyenNgheActivity.this,
                                AdminLuyenNgheActivity.class);
                        intent.putExtra("idBoLuyenNghe", luyenNghe.getIdbo());
                        startActivity(intent);
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        Toast.makeText(EditLuyenNgheActivity.this,
                                "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
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
            audioUri = data.getData();
            edtAudio.setText("");
            edtAudio.setHint("Đã chọn file audio");
        }
    }
}
