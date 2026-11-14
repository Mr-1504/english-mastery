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

import java.util.ArrayList;

public class AddLuyenNgheActivity extends AppCompatActivity {
    ImageView imgBack, imgHinh, imgAdd;
    Button btnChonHinh, btnChonAudio;
    EditText edtAudio;
    Spinner spnDapAnTrue;
    final int REQUEST_CHOOSE_PHOTO = 321;
    final int REQUEST_CHOOSE_AUDIO = 322;
    QuestionRepository repository;
    SupabaseStorageRepository storageRepository;
    int idBLN = 0;
    Uri imageUri;
    Uri audioUri;
    boolean saving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_luyennghe);
        imgBack = findViewById(R.id.imgBackAddLN);
        imgHinh = findViewById(R.id.imgHinhAddLN);
        imgAdd = findViewById(R.id.imgAddLN);
        btnChonHinh = findViewById(R.id.btnChonHinhAddLN);
        btnChonAudio = findViewById(R.id.btnChonAudioAddLN);
        edtAudio = findViewById(R.id.edtAudioAddLN);
        spnDapAnTrue = findViewById(R.id.spnDapAnTrueAddLN);
        repository = QuestionRepository.getInstance();
        storageRepository = SupabaseStorageRepository.getInstance();
        ArrayList<String> listDapAn = new ArrayList<>();
        listDapAn.add("A");
        listDapAn.add("B");
        listDapAn.add("C");
        listDapAn.add("D");
        ArrayAdapter<String> dapAnAdapter = new ArrayAdapter<>(AddLuyenNgheActivity.this,
                R.layout.support_simple_spinner_dropdown_item, listDapAn);
        spnDapAnTrue.setAdapter(dapAnAdapter);
        idBLN = getIntent().getIntExtra("idBoLuyenNghe", -1);
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
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddLuyenNgheActivity.this, AdminLuyenNgheActivity.class);
                intent.putExtra("idBoLuyenNghe", idBLN);
                startActivity(intent);
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
        String audio = edtAudio.getText().toString().trim();
        if (imageUri == null) {
            Toast.makeText(AddLuyenNgheActivity.this,
                    "Chưa chọn hình", Toast.LENGTH_SHORT).show();
            return;
        }
        if (audioUri == null && audio.isEmpty()) {
            Toast.makeText(AddLuyenNgheActivity.this,
                    "Chưa chọn audio", Toast.LENGTH_SHORT).show();
            return;
        }
        String dapanTrue = String.valueOf(spnDapAnTrue.getSelectedItemPosition() + 1);
        saving = true;
        storageRepository.uploadImage(this, imageUri, MediaTypeUtil.PREFIX_LISTENING_IMAGES,
                new SupabaseStorageRepository.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        if (audioUri != null) {
                            uploadAudio(imageUrl, dapanTrue);
                        } else {
                            createLuyenNghe(imageUrl, dapanTrue, audio);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        Toast.makeText(AddLuyenNgheActivity.this,
                                "Tải hình thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadAudio(String imageUrl, String dapanTrue) {
        storageRepository.uploadAudio(this, audioUri, MediaTypeUtil.PREFIX_LISTENING_AUDIO,
                new SupabaseStorageRepository.UploadCallback() {
                    @Override
                    public void onSuccess(String audioUrl) {
                        createLuyenNghe(imageUrl, dapanTrue, audioUrl);
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        Toast.makeText(AddLuyenNgheActivity.this,
                                "Tải audio thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createLuyenNghe(String imageUrl, String dapanTrue, String audioUrl) {
        repository.createLuyenNghe(idBLN, "A", "B", "C", "D", dapanTrue, imageUrl, audioUrl,
                new QuestionRepository.DataCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer data) {
                        saving = false;
                        Toast.makeText(AddLuyenNgheActivity.this,
                                "Thêm thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddLuyenNgheActivity.this,
                                AdminLuyenNgheActivity.class);
                        intent.putExtra("idBoLuyenNghe", idBLN);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        Toast.makeText(AddLuyenNgheActivity.this,
                                "Thêm thất bại", Toast.LENGTH_SHORT).show();
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
