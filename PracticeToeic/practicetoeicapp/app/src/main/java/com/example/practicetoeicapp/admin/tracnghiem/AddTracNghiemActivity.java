package com.example.practicetoeicapp.admin.tracnghiem;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.ui.UiFeedback;

import java.util.ArrayList;

public class AddTracNghiemActivity extends AppCompatActivity {
    View imgBack, imgAdd;
    EditText edtNoiDung, edtDapAnA, edtDapAnB, edtDapAnC, edtDapAnD;
    Spinner spnDapAnTrue;
    QuestionRepository repository;
    int idBTN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tracnghiem);
        imgBack = findViewById(R.id.imgBackAddTN);
        imgAdd = findViewById(R.id.imgAddTN);
        edtNoiDung = findViewById(R.id.edtCauHoiAddTN);
        edtDapAnA = findViewById(R.id.edtDapAnAAddTN);
        edtDapAnB = findViewById(R.id.edtDapAnBAddTN);
        edtDapAnC = findViewById(R.id.edtDapAnCAddTN);
        edtDapAnD = findViewById(R.id.edtDapAnDAddTN);
        spnDapAnTrue = findViewById(R.id.spnDapAnTrueAddTN);
        repository = QuestionRepository.getInstance();
        ArrayList<String> listDapAn = new ArrayList<>();
        listDapAn.add("A");
        listDapAn.add("B");
        listDapAn.add("C");
        listDapAn.add("D");
        ArrayAdapter<String> dapAnAdapter = new ArrayAdapter<>(AddTracNghiemActivity.this,
                R.layout.support_simple_spinner_dropdown_item, listDapAn);
        spnDapAnTrue.setAdapter(dapAnAdapter);
        idBTN = getIntent().getIntExtra("idBoTracNghiem", -1);
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
                String dapanA = edtDapAnA.getText().toString().trim();
                String dapanB = edtDapAnB.getText().toString().trim();
                String dapanC = edtDapAnC.getText().toString().trim();
                String dapanD = edtDapAnD.getText().toString().trim();
                if (noidung.isEmpty() || dapanA.isEmpty() || dapanB.isEmpty()
                        || dapanC.isEmpty() || dapanD.isEmpty()) {
                    UiFeedback.showTransient(AddTracNghiemActivity.this,
                            getString(R.string.feedback_required_fields), false);
                    return;
                }
                String dapanTrue = String.valueOf(spnDapAnTrue.getSelectedItemPosition() + 1);
                final AlertDialog loading = UiFeedback.showLoading(AddTracNghiemActivity.this,
                        R.string.feedback_saving);
                repository.createTracNghiem(idBTN, noidung, dapanA, dapanB, dapanC, dapanD,
                        dapanTrue, new QuestionRepository.DataCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer data) {
                                UiFeedback.dismiss(loading);
                                UiFeedback.showTransient(AddTracNghiemActivity.this,
                                        getString(R.string.feedback_add_success), true);
                                finish();
                            }

                            @Override
                            public void onError(Exception e) {
                                UiFeedback.dismiss(loading);
                                UiFeedback.showTransient(AddTracNghiemActivity.this,
                                        getString(R.string.feedback_add_failed), false);
                            }
                        });
            }
        });
    }
}
