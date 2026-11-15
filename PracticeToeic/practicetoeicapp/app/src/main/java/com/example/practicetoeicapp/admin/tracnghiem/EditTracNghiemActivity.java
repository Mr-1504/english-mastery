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
import com.example.practicetoeicapp.tracnghiem.CauTracNghiem;
import com.example.practicetoeicapp.ui.UiFeedback;

import java.util.ArrayList;

public class EditTracNghiemActivity extends AppCompatActivity {
    View imgBack, imgEdit;
    EditText edtNoiDung, edtDapAnA, edtDapAnB, edtDapAnC, edtDapAnD;
    Spinner spnDapAnTrue;
    QuestionRepository repository;
    int idTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tracnghiem);
        imgBack = findViewById(R.id.imgBackEditTN);
        imgEdit = findViewById(R.id.imgEditTN);
        edtNoiDung = findViewById(R.id.edtCauHoiEditTN);
        edtDapAnA = findViewById(R.id.edtDapAnAEditTN);
        edtDapAnB = findViewById(R.id.edtDapAnBEditTN);
        edtDapAnC = findViewById(R.id.edtDapAnCEditTN);
        edtDapAnD = findViewById(R.id.edtDapAnDEditTN);
        spnDapAnTrue = findViewById(R.id.spnDapAnTrueEditTN);
        repository = QuestionRepository.getInstance();
        ArrayList<String> listDapAn = new ArrayList<>();
        listDapAn.add("A");
        listDapAn.add("B");
        listDapAn.add("C");
        listDapAn.add("D");
        ArrayAdapter<String> dapAnAdapter = new ArrayAdapter<>(EditTracNghiemActivity.this,
                R.layout.support_simple_spinner_dropdown_item, listDapAn);
        spnDapAnTrue.setAdapter(dapAnAdapter);
        idTN = getIntent().getIntExtra("ID_TN", -1);
        loadTracNghiem();
    }

    private void loadTracNghiem() {
        repository.loadTracNghiemById(idTN, new QuestionRepository.DataCallback<CauTracNghiem>() {
            @Override
            public void onSuccess(CauTracNghiem tracNghiem) {
                edtNoiDung.setText(tracNghiem.getNoidung());
                edtDapAnA.setText(tracNghiem.getDapanA());
                edtDapAnB.setText(tracNghiem.getDapanB());
                edtDapAnC.setText(tracNghiem.getDapanC());
                edtDapAnD.setText(tracNghiem.getDapanD());
                int position = 0;
                try {
                    position = Integer.parseInt(tracNghiem.getDapanTrue()) - 1;
                } catch (NumberFormatException ignored) {
                }
                spnDapAnTrue.setSelection(Math.max(0, Math.min(3, position)));
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
                        String dapanA = edtDapAnA.getText().toString().trim();
                        String dapanB = edtDapAnB.getText().toString().trim();
                        String dapanC = edtDapAnC.getText().toString().trim();
                        String dapanD = edtDapAnD.getText().toString().trim();
                        if (noidung.isEmpty() || dapanA.isEmpty() || dapanB.isEmpty()
                                || dapanC.isEmpty() || dapanD.isEmpty()) {
                            UiFeedback.showTransient(EditTracNghiemActivity.this,
                                    getString(R.string.feedback_required_fields), false);
                            return;
                        }
                        String dapanTrue = String.valueOf(spnDapAnTrue.getSelectedItemPosition() + 1);
                        final AlertDialog loading = UiFeedback.showLoading(EditTracNghiemActivity.this,
                                R.string.feedback_saving);
                        repository.updateTracNghiem(tracNghiem.getIdcau(), tracNghiem.getIdbo(),
                                noidung, dapanA, dapanB, dapanC, dapanD, dapanTrue,
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        UiFeedback.dismiss(loading);
                                        UiFeedback.showTransient(EditTracNghiemActivity.this,
                                                getString(R.string.feedback_update_success), true);
                                        finish();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        UiFeedback.dismiss(loading);
                                        UiFeedback.showTransient(EditTracNghiemActivity.this,
                                                getString(R.string.feedback_update_failed), false);
                                    }
                                });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                UiFeedback.showInfo(EditTracNghiemActivity.this,
                        getString(R.string.feedback_load_failed),
                        EditTracNghiemActivity.this::finish);
            }
        });
    }
}
