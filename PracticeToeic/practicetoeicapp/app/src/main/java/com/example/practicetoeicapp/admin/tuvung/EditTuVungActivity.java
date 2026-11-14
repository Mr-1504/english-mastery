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
import com.example.practicetoeicapp.hoctuvung.TuVung;

import java.util.ArrayList;

public class EditTuVungActivity extends AppCompatActivity {
    ImageView imgBack, imgHinh, imgEdit;
    Button btnChonHinh, btnChonAudio;
    EditText edtTuVung, edtNghia, edtAudio;
    Spinner spnTuLoai;
    final int REQUEST_CHOOSE_PHOTO = 321;
    final int REQUEST_CHOOSE_AUDIO = 322;
    QuestionRepository repository;
    SupabaseStorageRepository storageRepository;
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
        edtTuVung = findViewById(R.id.edtTuVungEditTV);
        edtNghia = findViewById(R.id.edtNghiaEditTV);
        edtAudio = findViewById(R.id.edtAudioEditTV);
        spnTuLoai = findViewById(R.id.spnLoaiTuEditTV);
        repository = QuestionRepository.getInstance();
        storageRepository = SupabaseStorageRepository.getInstance();
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
        int idTV = getIntent().getIntExtra("ID_TV", -1);
        loadTuVung(idTV);
    }

    private void loadTuVung(int idTV) {
        repository.loadTuVungById(idTV, new QuestionRepository.DataCallback<TuVung>() {
            @Override
            public void onSuccess(TuVung data) {
                tuVung = data;
                existingImageUrl = data.getAnhUrl();
                existingAudioUrl = data.getAudio();
                edtTuVung.setText(data.getDapan());
                edtNghia.setText(data.getDichnghia());
                edtAudio.setText(existingAudioUrl);
                Glide.with(EditTuVungActivity.this).load(existingImageUrl).into(imgHinh);
                spnTuLoai.setSelection(Math.max(0, listTuLoai.indexOf(data.getLoaitu())));
                imgBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(EditTuVungActivity.this, AdminTuVungActivity.class);
                        intent.putExtra("idBoTuVung", tuVung.getIdbo());
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
                Toast.makeText(EditTuVungActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void save() {
        if (saving || tuVung == null) {
            return;
        }
        String dapan = edtTuVung.getText().toString().trim();
        String nghia = edtNghia.getText().toString().trim();
        String audio = edtAudio.getText().toString().trim();
        String loaitu = listTuLoai.get(spnTuLoai.getSelectedItemPosition());
        if (dapan.isEmpty() || nghia.isEmpty()) {
            Toast.makeText(EditTuVungActivity.this,
                    "Chưa điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUri == null && existingImageUrl.isEmpty()) {
            Toast.makeText(EditTuVungActivity.this,
                    "Chưa chọn hình", Toast.LENGTH_SHORT).show();
            return;
        }
        if (audioUri == null && audio.isEmpty()) {
            Toast.makeText(EditTuVungActivity.this,
                    "Chưa chọn audio", Toast.LENGTH_SHORT).show();
            return;
        }
        saving = true;
        if (imageUri != null) {
            storageRepository.uploadImage(this, imageUri, MediaTypeUtil.PREFIX_VOCABULARY_IMAGES,
                    new SupabaseStorageRepository.UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            afterImage(imageUrl, dapan, nghia, loaitu, audio);
                        }

                        @Override
                        public void onError(Exception e) {
                            saving = false;
                            Toast.makeText(EditTuVungActivity.this,
                                    "Tải hình thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            afterImage(existingImageUrl, dapan, nghia, loaitu, audio);
        }
    }

    private void afterImage(String imageUrl, String dapan, String nghia,
                            String loaitu, String audio) {
        if (audioUri != null) {
            storageRepository.uploadAudio(this, audioUri, MediaTypeUtil.PREFIX_VOCABULARY_AUDIO,
                    new SupabaseStorageRepository.UploadCallback() {
                        @Override
                        public void onSuccess(String audioUrl) {
                            updateTuVung(imageUrl, dapan, nghia, loaitu, audioUrl);
                        }

                        @Override
                        public void onError(Exception e) {
                            saving = false;
                            Toast.makeText(EditTuVungActivity.this,
                                    "Tải audio thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            updateTuVung(imageUrl, dapan, nghia, loaitu, audio);
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
                        Toast.makeText(EditTuVungActivity.this,
                                "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditTuVungActivity.this, AdminTuVungActivity.class);
                        intent.putExtra("idBoTuVung", tuVung.getIdbo());
                        startActivity(intent);
                    }

                    @Override
                    public void onError(Exception e) {
                        saving = false;
                        Toast.makeText(EditTuVungActivity.this,
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
