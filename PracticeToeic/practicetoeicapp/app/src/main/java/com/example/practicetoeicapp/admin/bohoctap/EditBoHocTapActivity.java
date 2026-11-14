package com.example.practicetoeicapp.admin.bohoctap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.data.QuestionRepository;

public class EditBoHocTapActivity extends AppCompatActivity {
    ImageView imgBack, imgEdit;
    EditText edtBoHocTap;
    QuestionRepository repository;
    int idBHT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bohoctap);
        imgBack = findViewById(R.id.imgBackEditBHT);
        imgEdit = findViewById(R.id.imgEditBHT);
        edtBoHocTap = findViewById(R.id.edtEditBoHocTap);
        repository = QuestionRepository.getInstance();
        idBHT = getIntent().getIntExtra("ID_BHT", -1);
        loadBoHocTap();
    }

    private void loadBoHocTap() {
        repository.loadBoHocTapById(idBHT, new QuestionRepository.DataCallback<BoHocTap>() {
            @Override
            public void onSuccess(BoHocTap boHocTap) {
                edtBoHocTap.setText(boHocTap.getTenBo());
                imgBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(EditBoHocTapActivity.this, AdminBoHocTapActivity.class));
                    }
                });
                imgEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String ten = edtBoHocTap.getText().toString().trim();
                        if (ten.isEmpty()) {
                            Toast.makeText(EditBoHocTapActivity.this,
                                    "Chưa điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        repository.updateBoHocTap(boHocTap.getIdBo(), boHocTap.getStt(), ten,
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        Toast.makeText(EditBoHocTapActivity.this,
                                                "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(EditBoHocTapActivity.this,
                                                AdminBoHocTapActivity.class));
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(EditBoHocTapActivity.this,
                                                "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditBoHocTapActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
