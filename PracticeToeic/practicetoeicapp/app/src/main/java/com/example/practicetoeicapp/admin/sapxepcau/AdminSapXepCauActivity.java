package com.example.practicetoeicapp.admin.sapxepcau;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.sapxepcau.CauSapXep;

import java.util.ArrayList;
import java.util.List;

public class AdminSapXepCauActivity extends AppCompatActivity {
    ImageView imgBack, imgAdd;
    ArrayList<CauSapXep> listSapXepCau;
    AdminSapXepCauAdapter adapter;
    ListView lvSapXepCau;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sapxepcau);
        idbo = getIntent().getIntExtra("idBoSapXepCau", 0);
        lvSapXepCau = findViewById(R.id.listviewAdminSXC);
        imgBack = findViewById(R.id.imgBackAdminSXC);
        imgAdd = findViewById(R.id.imgAddAdminSXC);
        repository = QuestionRepository.getInstance();
        listSapXepCau = new ArrayList<>();
        adapter = new AdminSapXepCauAdapter(AdminSapXepCauActivity.this, listSapXepCau);
        lvSapXepCau.setAdapter(adapter);
        loadSapXepCau();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminSapXepCauActivity.this, AdminBoSapXepCauActivity.class));
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminSapXepCauActivity.this, AddSapXepCauActivity.class);
                intent.putExtra("idBoSapXepCau", idbo);
                startActivity(intent);
            }
        });
    }

    private void loadSapXepCau() {
        repository.loadSapXepCau(idbo, new QuestionRepository.DataCallback<List<CauSapXep>>() {
            @Override
            public void onSuccess(List<CauSapXep> data) {
                listSapXepCau.clear();
                listSapXepCau.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminSapXepCauActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
