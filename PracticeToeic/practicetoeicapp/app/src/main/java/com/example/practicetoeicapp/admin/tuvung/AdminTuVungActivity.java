package com.example.practicetoeicapp.admin.tuvung;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminListController;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.hoctuvung.TuVung;

import java.util.ArrayList;
import java.util.List;

public class AdminTuVungActivity extends AppCompatActivity {
    ArrayList<TuVung> listTuVung;
    AdminTuVungAdapter adapter;
    AdminListController listController;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tuvung);
        idbo = getIntent().getIntExtra("idBoTuVung", 0);
        listController = new AdminListController(this,
                R.string.admin_tuvung_content,
                R.string.admin_list_subtitle,
                true,
                R.string.admin_list_empty_add);
        repository = QuestionRepository.getInstance();
        listTuVung = new ArrayList<>();
        adapter = new AdminTuVungAdapter(AdminTuVungActivity.this, listTuVung);
        listController.listView.setAdapter(adapter);
        loadTuVung();

        listController.bindBack(v -> finish());
        listController.bindAdd(v -> {
            Intent intent = new Intent(AdminTuVungActivity.this, AddTuVungActivity.class);
            intent.putExtra("idBoTuVung", idbo);
            startActivity(intent);
        });
    }

    private void loadTuVung() {
        repository.loadTuVung(idbo, new QuestionRepository.DataCallback<List<TuVung>>() {
            @Override
            public void onSuccess(List<TuVung> data) {
                listTuVung.clear();
                listTuVung.addAll(data);
                adapter.notifyDataSetChanged();
                listController.showContent(data.isEmpty());
            }

            @Override
            public void onError(Exception e) {
                listController.showError();
            }
        });
    }
}
