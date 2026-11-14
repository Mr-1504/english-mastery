package com.example.practicetoeicapp.admin.dienkhuyet;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.ui.UiFeedback;

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
                finish();
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noidung = edtNoiDung.getText().toString().trim();
                String dapan = edtDapAn.getText().toString().trim();
                String goiy = edtGoiY.getText().toString().trim();
                if (noidung.isEmpty() || dapan.isEmpty() || goiy.isEmpty()) {
                    UiFeedback.showTransient(AddDienKhuyetActivity.this,
                            getString(R.string.feedback_required_fields), false);
                    return;
                }
                if (!checkDapAnInGoiY(dapan, goiy)) {
                    UiFeedback.showTransient(AddDienKhuyetActivity.this,
                            getString(R.string.feedback_invalid_answer), false);
                    return;
                }
                final AlertDialog loading = UiFeedback.showLoading(AddDienKhuyetActivity.this,
                        R.string.feedback_saving);
                repository.createDienKhuyet(idBDK, noidung, dapan, goiy,
                        new QuestionRepository.DataCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer data) {
                                UiFeedback.dismiss(loading);
                                UiFeedback.showTransient(AddDienKhuyetActivity.this,
                                        getString(R.string.feedback_add_success), true);
                                finish();
                            }

                            @Override
                            public void onError(Exception e) {
                                UiFeedback.dismiss(loading);
                                UiFeedback.showTransient(AddDienKhuyetActivity.this,
                                        getString(R.string.feedback_add_failed), false);
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
