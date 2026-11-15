package com.example.practicetoeicapp.admin.luyennghe;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminListController;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.luyennghe.CauLuyenNghe;

import java.util.ArrayList;
import java.util.List;

public class AdminLuyenNgheActivity extends AppCompatActivity {
    ArrayList<CauLuyenNghe> listLuyenNghe;
    AdminLuyenNgheAdapter adapter;
    AdminListController listController;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_luyennghe);
        idbo = getIntent().getIntExtra("idBoLuyenNghe", 0);
        listController = new AdminListController(this,
                R.string.admin_luyennghe_content,
                R.string.admin_list_subtitle,
                true,
                R.string.admin_list_empty_add);
        repository = QuestionRepository.getInstance();
        listLuyenNghe = new ArrayList<>();
        adapter = new AdminLuyenNgheAdapter(AdminLuyenNgheActivity.this, listLuyenNghe);
        listController.listView.setAdapter(adapter);

        listController.bindBack(v -> finish());
        listController.bindAdd(v -> {
            Intent intent = new Intent(AdminLuyenNgheActivity.this, AddLuyenNgheActivity.class);
            intent.putExtra("idBoLuyenNghe", idbo);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLuyenNghe();
    }

    private void loadLuyenNghe() {
        listController.showLoading();
        repository.loadLuyenNghe(idbo, new QuestionRepository.DataCallback<List<CauLuyenNghe>>() {
            @Override
            public void onSuccess(List<CauLuyenNghe> data) {
                listLuyenNghe.clear();
                listLuyenNghe.addAll(data);
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
