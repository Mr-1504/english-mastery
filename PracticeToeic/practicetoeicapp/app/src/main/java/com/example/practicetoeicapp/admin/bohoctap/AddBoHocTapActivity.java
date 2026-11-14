package com.example.practicetoeicapp.admin.bohoctap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;

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
                startActivity(new Intent(AddBoHocTapActivity.this, AdminBoHocTapActivity.class));
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ten = edtBoHocTap.getText().toString().trim();
                if (ten.isEmpty()) {
                    Toast.makeText(AddBoHocTapActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    return;
                }
                repository.createBoHocTap(ten, new QuestionRepository.DataCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer data) {
                        Toast.makeText(AddBoHocTapActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddBoHocTapActivity.this, AdminBoHocTapActivity.class));
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(AddBoHocTapActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
