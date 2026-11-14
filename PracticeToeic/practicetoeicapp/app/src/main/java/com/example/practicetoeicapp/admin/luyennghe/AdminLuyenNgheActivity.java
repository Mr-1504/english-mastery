package com.example.practicetoeicapp.admin.luyennghe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.luyennghe.CauLuyenNghe;

import java.util.ArrayList;
import java.util.List;

public class AdminLuyenNgheActivity extends AppCompatActivity {
    ImageView imgBack, imgAdd;
    ArrayList<CauLuyenNghe> listLuyenNghe;
    AdminLuyenNgheAdapter adapter;
    ListView lvLuyenNghe;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_luyennghe);
        idbo = getIntent().getIntExtra("idBoLuyenNghe", 0);
        lvLuyenNghe = findViewById(R.id.listviewAdminLN);
        imgBack = findViewById(R.id.imgBackAdminLN);
        imgAdd = findViewById(R.id.imgAddAdminLN);
        repository = QuestionRepository.getInstance();
        listLuyenNghe = new ArrayList<>();
        adapter = new AdminLuyenNgheAdapter(AdminLuyenNgheActivity.this, listLuyenNghe);
        lvLuyenNghe.setAdapter(adapter);
        loadLuyenNghe();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminLuyenNgheActivity.this, AdminBoLuyenNgheActivity.class));
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLuyenNgheActivity.this, AddLuyenNgheActivity.class);
                intent.putExtra("idBoLuyenNghe", idbo);
                startActivity(intent);
            }
        });
    }

    private void loadLuyenNghe() {
        repository.loadLuyenNghe(idbo, new QuestionRepository.DataCallback<List<CauLuyenNghe>>() {
            @Override
            public void onSuccess(List<CauLuyenNghe> data) {
                listLuyenNghe.clear();
                listLuyenNghe.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminLuyenNgheActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
