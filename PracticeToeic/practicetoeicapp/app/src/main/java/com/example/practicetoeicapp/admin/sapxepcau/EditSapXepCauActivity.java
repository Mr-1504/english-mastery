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
import com.example.practicetoeicapp.sapxepcau.CauSapXep;

public class EditSapXepCauActivity extends AppCompatActivity {
    ImageView imgBack, imgEdit;
    EditText edtNoiDung, edtPart1, edtPart2, edtPart3, edtPart4;
    QuestionRepository repository;
    int idSXC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sapxepcau);
        imgBack = findViewById(R.id.imgBackEditSXC);
        imgEdit = findViewById(R.id.imgEditSXC);
        edtNoiDung = findViewById(R.id.edtCauHoiEditSXC);
        edtPart1 = findViewById(R.id.edtPart1EditSXC);
        edtPart2 = findViewById(R.id.edtPart2EditSXC);
        edtPart3 = findViewById(R.id.edtPart3EditSXC);
        edtPart4 = findViewById(R.id.edtPart4EditSXC);
        repository = QuestionRepository.getInstance();
        idSXC = getIntent().getIntExtra("ID_SXC", -1);
        loadSapXepCau();
    }

    private void loadSapXepCau() {
        repository.loadSapXepCauById(idSXC, new QuestionRepository.DataCallback<CauSapXep>() {
            @Override
            public void onSuccess(CauSapXep sapxepCau) {
                edtNoiDung.setText(sapxepCau.getDapan());
                edtPart1.setText(sapxepCau.getPart1());
                edtPart2.setText(sapxepCau.getPart2());
                edtPart3.setText(sapxepCau.getPart3());
                edtPart4.setText(sapxepCau.getPart4());
                imgBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(EditSapXepCauActivity.this,
                                AdminSapXepCauActivity.class);
                        intent.putExtra("idBoSapXepCau", sapxepCau.getIdbo());
                        startActivity(intent);
                    }
                });
                imgEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String dapan = edtNoiDung.getText().toString().trim();
                        String part1 = edtPart1.getText().toString().trim();
                        String part2 = edtPart2.getText().toString().trim();
                        String part3 = edtPart3.getText().toString().trim();
                        String part4 = edtPart4.getText().toString().trim();
                        if (dapan.isEmpty() || part1.isEmpty() || part2.isEmpty()
                                || part3.isEmpty() || part4.isEmpty()) {
                            Toast.makeText(EditSapXepCauActivity.this,
                                    "Chưa điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!checkPartInResult(dapan, part1, part2, part3, part4)) {
                            Toast.makeText(EditSapXepCauActivity.this,
                                    "Vui lòng nhập đúng nội dung tách của câu", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        repository.updateSapXepCau(sapxepCau.getIdcau(), sapxepCau.getIdbo(),
                                dapan, part1, part2, part3, part4,
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        Toast.makeText(EditSapXepCauActivity.this,
                                                "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(EditSapXepCauActivity.this,
                                                AdminSapXepCauActivity.class);
                                        intent.putExtra("idBoSapXepCau", sapxepCau.getIdbo());
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(EditSapXepCauActivity.this,
                                                "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditSapXepCauActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private boolean checkPartInResult(String dapan, String part1, String part2,
                                      String part3, String part4) {
        String part = String.join(" ", part1, part2, part3, part4);
        return dapan.equals(part);
    }
}
