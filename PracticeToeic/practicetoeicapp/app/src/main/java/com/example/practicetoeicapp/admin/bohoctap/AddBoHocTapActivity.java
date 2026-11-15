package com.example.practicetoeicapp.admin.bohoctap;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.ui.UiFeedback;

public class AddBoHocTapActivity extends AppCompatActivity {
    View imgBack, imgAdd;
    EditText edtBoHocTap;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bohoctap);
        imgBack = findViewById(R.id.imgBackAddBHT);
        imgAdd = findViewById(R.id.imgAddBHT);
        edtBoHocTap = findViewById(R.id.edtAddBoHocTap);
        repository = QuestionRepository.getInstance();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ten = edtBoHocTap.getText().toString().trim();
                if (ten.isEmpty()) {
                    UiFeedback.showTransient(AddBoHocTapActivity.this,
                            getString(R.string.feedback_required_fields), false);
                    return;
                }
                final AlertDialog loading = UiFeedback.showLoading(AddBoHocTapActivity.this,
                        R.string.feedback_saving);
                repository.createBoHocTap(ten, new QuestionRepository.DataCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer data) {
                        UiFeedback.dismiss(loading);
                        UiFeedback.showTransient(AddBoHocTapActivity.this,
                                getString(R.string.feedback_add_success), true);
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        UiFeedback.dismiss(loading);
                        UiFeedback.showTransient(AddBoHocTapActivity.this,
                                getString(R.string.feedback_add_failed), false);
                    }
                });
            }
        });
    }
}
