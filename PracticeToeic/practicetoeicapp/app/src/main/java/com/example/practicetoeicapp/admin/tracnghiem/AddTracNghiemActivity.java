package com.example.practicetoeicapp.admin.tracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;

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
                Intent intent = new Intent(AddTracNghiemActivity.this, AdminTracNghiemActivity.class);
                intent.putExtra("idBoTracNghiem", idBTN);
                startActivity(intent);
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
                    Toast.makeText(AddTracNghiemActivity.this,
                            "Chưa điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                String dapanTrue = String.valueOf(spnDapAnTrue.getSelectedItemPosition() + 1);
                repository.createTracNghiem(idBTN, noidung, dapanA, dapanB, dapanC, dapanD,
                        dapanTrue, new QuestionRepository.DataCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer data) {
                                Toast.makeText(AddTracNghiemActivity.this,
                                        "Thêm thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddTracNghiemActivity.this,
                                        AdminTracNghiemActivity.class);
                                intent.putExtra("idBoTracNghiem", idBTN);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(AddTracNghiemActivity.this,
                                        "Thêm thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
