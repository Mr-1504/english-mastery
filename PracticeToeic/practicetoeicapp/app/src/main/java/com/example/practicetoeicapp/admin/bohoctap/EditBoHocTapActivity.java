package com.example.practicetoeicapp.admin.bohoctap;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.ui.UiFeedback;

public class EditBoHocTapActivity extends AppCompatActivity {
    View imgBack, imgEdit;
    EditText edtBoHocTap;
    QuestionRepository repository;
    int idBHT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bohoctap);
        imgBack = findViewById(R.id.imgBackEditBHT);
        imgEdit = findViewById(R.id.imgEditBHT);
        edtBoHocTap = findViewById(R.id.edtEditBoHocTap);
        repository = QuestionRepository.getInstance();
        idBHT = getIntent().getIntExtra("ID_BHT", -1);
        loadBoHocTap();
    }

    private void loadBoHocTap() {
        repository.loadBoHocTapById(idBHT, new QuestionRepository.DataCallback<BoHocTap>() {
            @Override
            public void onSuccess(BoHocTap boHocTap) {
                edtBoHocTap.setText(boHocTap.getTenBo());
                imgBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                imgEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String ten = edtBoHocTap.getText().toString().trim();
                        if (ten.isEmpty()) {
                            UiFeedback.showTransient(EditBoHocTapActivity.this,
                                    getString(R.string.feedback_required_fields), false);
                            return;
                        }
                        final AlertDialog loading = UiFeedback.showLoading(EditBoHocTapActivity.this,
                                R.string.feedback_saving);
                        repository.updateBoHocTap(boHocTap.getIdBo(), boHocTap.getStt(), ten,
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        UiFeedback.dismiss(loading);
                                        UiFeedback.showTransient(EditBoHocTapActivity.this,
                                                getString(R.string.feedback_update_success), true);
                                        finish();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        UiFeedback.dismiss(loading);
                                        UiFeedback.showTransient(EditBoHocTapActivity.this,
                                                getString(R.string.feedback_update_failed), false);
                                    }
                                });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                UiFeedback.showInfo(EditBoHocTapActivity.this,
                        getString(R.string.feedback_load_failed),
                        EditBoHocTapActivity.this::finish);
            }
        });
    }
}
