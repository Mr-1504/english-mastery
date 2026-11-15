package com.example.practicetoeicapp.admin.sapxepcau;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.ui.UiFeedback;

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
                finish();
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
                    UiFeedback.showTransient(AddSapXepCauActivity.this,
                            getString(R.string.feedback_required_fields), false);
                    return;
                }
                if (!checkPartInResult(dapan, part1, part2, part3, part4)) {
                    UiFeedback.showTransient(AddSapXepCauActivity.this,
                            getString(R.string.feedback_invalid_parts), false);
                    return;
                }
                final AlertDialog loading = UiFeedback.showLoading(AddSapXepCauActivity.this,
                        R.string.feedback_saving);
                repository.createSapXepCau(idBSXC, dapan, part1, part2, part3, part4,
                        new QuestionRepository.DataCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer data) {
                                UiFeedback.dismiss(loading);
                                UiFeedback.showTransient(AddSapXepCauActivity.this,
                                        getString(R.string.feedback_add_success), true);
                                finish();
                            }

                            @Override
                            public void onError(Exception e) {
                                UiFeedback.dismiss(loading);
                                UiFeedback.showTransient(AddSapXepCauActivity.this,
                                        getString(R.string.feedback_add_failed), false);
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
