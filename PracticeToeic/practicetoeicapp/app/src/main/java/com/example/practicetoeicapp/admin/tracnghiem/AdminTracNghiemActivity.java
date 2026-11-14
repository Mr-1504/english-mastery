package com.example.practicetoeicapp.admin.tracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.tracnghiem.CauTracNghiem;

import java.util.ArrayList;
import java.util.List;

public class AdminTracNghiemActivity extends AppCompatActivity {
    ImageView imgBack, imgAdd;
    ArrayList<CauTracNghiem> listTracNghiem;
    AdminTracNghiemAdapter adapter;
    ListView lvTracNghiem;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tracnghiem);
        idbo = getIntent().getIntExtra("idBoTracNghiem", 0);
        lvTracNghiem = findViewById(R.id.listviewAdminTN);
        imgBack = findViewById(R.id.imgBackAdminTN);
        imgAdd = findViewById(R.id.imgAddAdminTN);
        repository = QuestionRepository.getInstance();
        listTracNghiem = new ArrayList<>();
        adapter = new AdminTracNghiemAdapter(AdminTracNghiemActivity.this, listTracNghiem);
        lvTracNghiem.setAdapter(adapter);
        loadTracNghiem();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminTracNghiemActivity.this, AdminBoTracNghiemActivity.class));
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminTracNghiemActivity.this, AddTracNghiemActivity.class);
                intent.putExtra("idBoTracNghiem", idbo);
                startActivity(intent);
            }
        });
    }

    private void loadTracNghiem() {
        repository.loadTracNghiem(idbo, new QuestionRepository.DataCallback<List<CauTracNghiem>>() {
            @Override
            public void onSuccess(List<CauTracNghiem> data) {
                listTracNghiem.clear();
                listTracNghiem.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminTracNghiemActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
