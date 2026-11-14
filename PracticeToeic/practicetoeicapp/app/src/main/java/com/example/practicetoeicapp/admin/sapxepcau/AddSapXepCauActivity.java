package com.example.practicetoeicapp.admin.sapxepcau;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;

public class AddSapXepCauActivity extends AppCompatActivity {
    View imgBack, imgAdd;
    EditText edtNoiDung, edtPart1, edtPart2, edtPart3, edtPart4;
    QuestionRepository repository;
    int idBSXC = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sapxepcau);
        imgBack = findViewById(R.id.imgBackAddSXC);
        imgAdd = findViewById(R.id.imgAddSXC);
        edtNoiDung = findViewById(R.id.edtCauHoiAddSXC);
        edtPart1 = findViewById(R.id.edtPart1AddSXC);
        edtPart2 = findViewById(R.id.edtPart2AddSXC);
        edtPart3 = findViewById(R.id.edtPart3AddSXC);
        edtPart4 = findViewById(R.id.edtPart4AddSXC);
        repository = QuestionRepository.getInstance();
        idBSXC = getIntent().getIntExtra("idBoSapXepCau", -1);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddSapXepCauActivity.this, AdminSapXepCauActivity.class);
                intent.putExtra("idBoSapXepCau", idBSXC);
                startActivity(intent);
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dapan = edtNoiDung.getText().toString().trim();
                String part1 = edtPart1.getText().toString().trim();
                String part2 = edtPart2.getText().toString().trim();
                String part3 = edtPart3.getText().toString().trim();
                String part4 = edtPart4.getText().toString().trim();
                if (dapan.isEmpty() || part1.isEmpty() || part2.isEmpty()
                        || part3.isEmpty() || part4.isEmpty()) {
                    Toast.makeText(AddSapXepCauActivity.this,
                            "Chưa điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!checkPartInResult(dapan, part1, part2, part3, part4)) {
                    Toast.makeText(AddSapXepCauActivity.this,
                            "Vui lòng nhập đúng nội dung tách của câu", Toast.LENGTH_SHORT).show();
                    return;
                }
                repository.createSapXepCau(idBSXC, dapan, part1, part2, part3, part4,
                        new QuestionRepository.DataCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer data) {
                                Toast.makeText(AddSapXepCauActivity.this,
                                        "Thêm thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddSapXepCauActivity.this,
                                        AdminSapXepCauActivity.class);
                                intent.putExtra("idBoSapXepCau", idBSXC);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(AddSapXepCauActivity.this,
                                        "Thêm thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private boolean checkPartInResult(String dapan, String part1, String part2,
                                      String part3, String part4) {
        String part = String.join(" ", part1, part2, part3, part4);
        return dapan.equals(part);
    }
}
