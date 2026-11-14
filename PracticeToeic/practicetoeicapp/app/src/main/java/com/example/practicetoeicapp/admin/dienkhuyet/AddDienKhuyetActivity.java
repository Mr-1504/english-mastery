package com.example.practicetoeicapp.admin.dienkhuyet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;

import java.util.Arrays;

public class AddDienKhuyetActivity extends AppCompatActivity {
    View imgBack, imgAdd;
    EditText edtNoiDung, edtGoiY, edtDapAn;
    QuestionRepository repository;
    int idBDK = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dienkhuyet);
        imgBack = findViewById(R.id.imgBackAddDK);
        imgAdd = findViewById(R.id.imgAddDK);
        edtNoiDung = findViewById(R.id.edtCauHoiAddDK);
        edtGoiY = findViewById(R.id.edtGoiYAddDK);
        edtDapAn = findViewById(R.id.edtDapAnAddDK);
        repository = QuestionRepository.getInstance();
        idBDK = getIntent().getIntExtra("idBoDienKhuyet", -1);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddDienKhuyetActivity.this, AdminDienKhuyetActivity.class);
                intent.putExtra("idBoDienKhuyet", idBDK);
                startActivity(intent);
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noidung = edtNoiDung.getText().toString().trim();
                String dapan = edtDapAn.getText().toString().trim();
                String goiy = edtGoiY.getText().toString().trim();
                if (noidung.isEmpty() || dapan.isEmpty() || goiy.isEmpty()) {
                    Toast.makeText(AddDienKhuyetActivity.this,
                            "Chưa điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!checkDapAnInGoiY(dapan, goiy)) {
                    Toast.makeText(AddDienKhuyetActivity.this,
                            "Vui lòng nhập đúng đáp án", Toast.LENGTH_SHORT).show();
                    return;
                }
                repository.createDienKhuyet(idBDK, noidung, dapan, goiy,
                        new QuestionRepository.DataCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer data) {
                                Toast.makeText(AddDienKhuyetActivity.this,
                                        "Thêm thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddDienKhuyetActivity.this,
                                        AdminDienKhuyetActivity.class);
                                intent.putExtra("idBoDienKhuyet", idBDK);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(AddDienKhuyetActivity.this,
                                        "Thêm thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private boolean checkDapAnInGoiY(String dapan, String goiy) {
        goiy = goiy.replaceAll("\\W", " ");
        goiy = goiy.trim().replaceAll("\\s{2,}", " ");
        String[] dapAn = goiy.split(" ");
        return Arrays.asList(dapAn).contains(dapan);
    }
}
