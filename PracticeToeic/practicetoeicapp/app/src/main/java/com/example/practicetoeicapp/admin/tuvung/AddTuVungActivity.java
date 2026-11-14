package com.example.practicetoeicapp.admin.tuvung;

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

public class AddTuVungActivity extends AppCompatActivity {
    ImageView imgBack, imgHinh, imgAdd;
    Button btnChonHinh, btnChonAudio;
    EditText edtTuVung, edtNghia, edtAudio;
    Spinner spnTuLoai;
    final int REQUEST_CHOOSE_PHOTO = 321;
    final int REQUEST_CHOOSE_AUDIO = 322;
    QuestionRepository repository;
    SupabaseStorageRepository storageRepository;
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
        edtTuVung = findViewById(R.id.edtTuVungAddTV);
        edtNghia = findViewById(R.id.edtNghiaAddTV);
        edtAudio = findViewById(R.id.edtAudioAddTV);
        spnTuLoai = findViewById(R.id.spnLoaiTuAddTV);
        repository = QuestionRepository.getInstance();
        storageRepository = SupabaseStorageRepository.getInstance();
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
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddTuVungActivity.this, AdminTuVungActivity.class);
                intent.putExtra("idBoTuVung", idBTV);
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
        String dapan = edtTuVung.getText().toString().trim();
        String nghia = edtNghia.getText().toString().trim();
        String audio = edtAudio.getText().toString().trim();
        String loaitu = listTuLoai.get(spnTuLoai.getSelectedItemPosition());
        if (dapan.isEmpty() || nghia.isEmpty()) {
            Toast.makeText(AddTuVungActivity.this,
                    "Chưa điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUri == null) {
            Toast.makeText(AddTuVungActivity.this,
                    "Chưa chọn hình", Toast.LENGTH_SHORT).show();
            return;
        }
        if (audioUri == null && audio.isEmpty()) {
            Toast.makeText(AddTuVungActivity.this,
                    "Chưa chọn audio", Toast.LENGTH_SHORT).show();
            return;
        }
        saving = true;
        storageRepository.uploadImage(this, imageUri, MediaTypeUtil.PREFIX_VOCABULARY_IMAGES,
                new SupabaseStorageRepository.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        if (audioUri != null) {
                            uploadAudio(imageUrl, dapan, nghia, loaitu);
                        } else {
                            createTuVung(imageUrl, dapan, nghia, loaitu, audio);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        Toast.makeText(AddTuVungActivity.this,
                                "Tải hình thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadAudio(String imageUrl, String dapan, String nghia, String loaitu) {
        storageRepository.uploadAudio(this, audioUri, MediaTypeUtil.PREFIX_VOCABULARY_AUDIO,
                new SupabaseStorageRepository.UploadCallback() {
                    @Override
                    public void onSuccess(String audioUrl) {
                        createTuVung(imageUrl, dapan, nghia, loaitu, audioUrl);
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        Toast.makeText(AddTuVungActivity.this,
                                "Tải audio thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(AddTuVungActivity.this,
                                "Thêm thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddTuVungActivity.this, AdminTuVungActivity.class);
                        intent.putExtra("idBoTuVung", idBTV);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        Toast.makeText(AddTuVungActivity.this,
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
