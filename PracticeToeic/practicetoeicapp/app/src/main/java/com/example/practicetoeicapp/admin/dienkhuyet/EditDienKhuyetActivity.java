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
import com.example.practicetoeicapp.dienkhuyet.CauDienKhuyet;

import java.util.Arrays;

public class EditDienKhuyetActivity extends AppCompatActivity {
    ImageView imgBack, imgEdit;
    EditText edtNoiDung, edtGoiY, edtDapAn;
    QuestionRepository repository;
    int idDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dienkhuyet);
        imgBack = findViewById(R.id.imgBackEditDK);
        imgEdit = findViewById(R.id.imgEditDK);
        edtNoiDung = findViewById(R.id.edtCauHoiEditDK);
        edtGoiY = findViewById(R.id.edtGoiYEditDK);
        edtDapAn = findViewById(R.id.edtDapAnEditDK);
        repository = QuestionRepository.getInstance();
        idDK = getIntent().getIntExtra("ID_DK", -1);
        loadDienKhuyet();
    }

    private void loadDienKhuyet() {
        repository.loadDienKhuyetById(idDK, new QuestionRepository.DataCallback<CauDienKhuyet>() {
            @Override
            public void onSuccess(CauDienKhuyet dienKhuyet) {
                edtNoiDung.setText(dienKhuyet.getNoidung());
                edtGoiY.setText(dienKhuyet.getGoiy());
                edtDapAn.setText(dienKhuyet.getDapan());
                imgBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(EditDienKhuyetActivity.this, AdminDienKhuyetActivity.class);
                        intent.putExtra("idBoDienKhuyet", dienKhuyet.getIdbo());
                        startActivity(intent);
                    }
                });
                imgEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String noidung = edtNoiDung.getText().toString().trim();
                        String dapan = edtDapAn.getText().toString().trim();
                        String goiy = edtGoiY.getText().toString().trim();
                        if (noidung.isEmpty() || dapan.isEmpty() || goiy.isEmpty()) {
                            Toast.makeText(EditDienKhuyetActivity.this,
                                    "Chưa điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!checkDapAnInGoiY(dapan, goiy)) {
                            Toast.makeText(EditDienKhuyetActivity.this,
                                    "Vui lòng nhập đúng đáp án", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        repository.updateDienKhuyet(dienKhuyet.getIdcau(), dienKhuyet.getIdbo(),
                                noidung, dapan, goiy,
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        Toast.makeText(EditDienKhuyetActivity.this,
                                                "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(EditDienKhuyetActivity.this,
                                                AdminDienKhuyetActivity.class);
                                        intent.putExtra("idBoDienKhuyet", dienKhuyet.getIdbo());
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(EditDienKhuyetActivity.this,
                                                "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditDienKhuyetActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
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
