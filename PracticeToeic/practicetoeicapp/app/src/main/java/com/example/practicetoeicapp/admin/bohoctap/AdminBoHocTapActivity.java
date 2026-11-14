package com.example.practicetoeicapp.admin.bohoctap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminActivity;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.data.QuestionRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminBoHocTapActivity extends AppCompatActivity {
    ArrayList<BoHocTap> listBHT;
    AdminBoHocTapAdapter adapter;
    ImageView imgBack, imgAdd;
    ListView listViewBHT;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bohoctap);
        listViewBHT = findViewById(R.id.listviewAdminBHT);
        imgBack = findViewById(R.id.imgBackAdminBHT);
        imgAdd = findViewById(R.id.imgAddBHT);
        repository = QuestionRepository.getInstance();
        listBHT = new ArrayList<>();
        adapter = new AdminBoHocTapAdapter(AdminBoHocTapActivity.this, listBHT);
        listViewBHT.setAdapter(adapter);
        loadBoHocTap();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminBoHocTapActivity.this, AdminActivity.class));
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminBoHocTapActivity.this, AddBoHocTapActivity.class));
            }
        });
    }

    private void loadBoHocTap() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                listBHT.clear();
                listBHT.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminBoHocTapActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
