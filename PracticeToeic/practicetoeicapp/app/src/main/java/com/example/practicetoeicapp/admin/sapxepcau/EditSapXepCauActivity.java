package com.example.practicetoeicapp.admin.sapxepcau;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.sapxepcau.CauSapXep;
import com.example.practicetoeicapp.ui.UiFeedback;

public class EditSapXepCauActivity extends AppCompatActivity {
    View imgBack, imgEdit;
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
                        finish();
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
                            UiFeedback.showTransient(EditSapXepCauActivity.this,
                                    getString(R.string.feedback_required_fields), false);
                            return;
                        }
                        if (!checkPartInResult(dapan, part1, part2, part3, part4)) {
                            UiFeedback.showTransient(EditSapXepCauActivity.this,
                                    getString(R.string.feedback_invalid_parts), false);
                            return;
                        }
                        final AlertDialog loading = UiFeedback.showLoading(EditSapXepCauActivity.this,
                                R.string.feedback_saving);
                        repository.updateSapXepCau(sapxepCau.getIdcau(), sapxepCau.getIdbo(),
                                dapan, part1, part2, part3, part4,
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        UiFeedback.dismiss(loading);
                                        UiFeedback.showTransient(EditSapXepCauActivity.this,
                                                getString(R.string.feedback_update_success), true);
                                        finish();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        UiFeedback.dismiss(loading);
                                        UiFeedback.showTransient(EditSapXepCauActivity.this,
                                                getString(R.string.feedback_update_failed), false);
                                    }
                                });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                UiFeedback.showInfo(EditSapXepCauActivity.this,
                        getString(R.string.feedback_load_failed),
                        EditSapXepCauActivity.this::finish);
            }
        });
    }

    private boolean checkPartInResult(String dapan, String part1, String part2,
                                      String part3, String part4) {
        String part = String.join(" ", part1, part2, part3, part4);
        return dapan.equals(part);
    }
}
