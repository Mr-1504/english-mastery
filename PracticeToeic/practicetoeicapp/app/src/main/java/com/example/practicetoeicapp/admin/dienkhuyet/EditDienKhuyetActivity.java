package com.example.practicetoeicapp.admin.dienkhuyet;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.dienkhuyet.CauDienKhuyet;
import com.example.practicetoeicapp.ui.UiFeedback;

import java.util.Arrays;

public class EditDienKhuyetActivity extends AppCompatActivity {
    View imgBack, imgEdit;
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
                        finish();
                    }
                });
                imgEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String noidung = edtNoiDung.getText().toString().trim();
                        String dapan = edtDapAn.getText().toString().trim();
                        String goiy = edtGoiY.getText().toString().trim();
                        if (noidung.isEmpty() || dapan.isEmpty() || goiy.isEmpty()) {
                            UiFeedback.showTransient(EditDienKhuyetActivity.this,
                                    getString(R.string.feedback_required_fields), false);
                            return;
                        }
                        if (!checkDapAnInGoiY(dapan, goiy)) {
                            UiFeedback.showTransient(EditDienKhuyetActivity.this,
                                    getString(R.string.feedback_invalid_answer), false);
                            return;
                        }
                        final AlertDialog loading = UiFeedback.showLoading(EditDienKhuyetActivity.this,
                                R.string.feedback_saving);
                        repository.updateDienKhuyet(dienKhuyet.getIdcau(), dienKhuyet.getIdbo(),
                                noidung, dapan, goiy,
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        UiFeedback.dismiss(loading);
                                        UiFeedback.showTransient(EditDienKhuyetActivity.this,
                                                getString(R.string.feedback_update_success), true);
                                        finish();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        UiFeedback.dismiss(loading);
                                        UiFeedback.showTransient(EditDienKhuyetActivity.this,
                                                getString(R.string.feedback_update_failed), false);
                                    }
                                });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                UiFeedback.showInfo(EditDienKhuyetActivity.this,
                        getString(R.string.feedback_load_failed),
                        EditDienKhuyetActivity.this::finish);
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
