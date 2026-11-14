package com.example.practicetoeicapp.admin.tuvung;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.hoctuvung.TuVung;

import java.util.ArrayList;
import java.util.List;

public class AdminTuVungActivity extends AppCompatActivity {
    ImageView imgBack, imgAdd;
    ArrayList<TuVung> listTuVung;
    AdminTuVungAdapter adapter;
    ListView lvTuVung;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tuvung);
        idbo = getIntent().getIntExtra("idBoTuVung", 0);
        lvTuVung = findViewById(R.id.listviewAdminTV);
        imgBack = findViewById(R.id.imgBackAdminTV);
        imgAdd = findViewById(R.id.imgAddAdminTV);
        repository = QuestionRepository.getInstance();
        listTuVung = new ArrayList<>();
        adapter = new AdminTuVungAdapter(AdminTuVungActivity.this, listTuVung);
        lvTuVung.setAdapter(adapter);
        loadTuVung();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminTuVungActivity.this, AdminBoTuVungActivity.class));
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminTuVungActivity.this, AddTuVungActivity.class);
                intent.putExtra("idBoTuVung", idbo);
                startActivity(intent);
            }
        });
    }

    private void loadTuVung() {
        repository.loadTuVung(idbo, new QuestionRepository.DataCallback<List<TuVung>>() {
            @Override
            public void onSuccess(List<TuVung> data) {
                listTuVung.clear();
                listTuVung.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminTuVungActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
