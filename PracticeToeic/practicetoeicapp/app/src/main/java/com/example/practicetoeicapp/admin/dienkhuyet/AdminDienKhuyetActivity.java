package com.example.practicetoeicapp.admin.dienkhuyet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.dienkhuyet.CauDienKhuyet;

import java.util.ArrayList;
import java.util.List;

public class AdminDienKhuyetActivity extends AppCompatActivity {
    ImageView imgBack, imgAdd;
    ArrayList<CauDienKhuyet> listDienKhuyet;
    AdminDienKhuyetAdapter adapter;
    ListView lvDienKhuyet;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dienkhuyet);
        idbo = getIntent().getIntExtra("idBoDienKhuyet", 0);
        lvDienKhuyet = findViewById(R.id.listviewAdminDK);
        imgBack = findViewById(R.id.imgBackAdminDK);
        imgAdd = findViewById(R.id.imgAddAdminDK);
        repository = QuestionRepository.getInstance();
        listDienKhuyet = new ArrayList<>();
        adapter = new AdminDienKhuyetAdapter(AdminDienKhuyetActivity.this, listDienKhuyet);
        lvDienKhuyet.setAdapter(adapter);
        loadDienKhuyet();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDienKhuyetActivity.this, AdminBoDienKhuyetActivity.class));
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDienKhuyetActivity.this, AddDienKhuyetActivity.class);
                intent.putExtra("idBoDienKhuyet", idbo);
                startActivity(intent);
            }
        });
    }

    private void loadDienKhuyet() {
        repository.loadDienKhuyet(idbo, new QuestionRepository.DataCallback<List<CauDienKhuyet>>() {
            @Override
            public void onSuccess(List<CauDienKhuyet> data) {
                listDienKhuyet.clear();
                listDienKhuyet.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminDienKhuyetActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
