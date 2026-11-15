package com.example.practicetoeicapp.admin.sapxepcau;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminListController;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.sapxepcau.CauSapXep;

import java.util.ArrayList;
import java.util.List;

public class AdminSapXepCauActivity extends AppCompatActivity {
    ArrayList<CauSapXep> listSapXepCau;
    AdminSapXepCauAdapter adapter;
    AdminListController listController;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sapxepcau);
        idbo = getIntent().getIntExtra("idBoSapXepCau", 0);
        listController = new AdminListController(this,
                R.string.admin_sapxepcau_content,
                R.string.admin_list_subtitle,
                true,
                R.string.admin_list_empty_add);
        repository = QuestionRepository.getInstance();
        listSapXepCau = new ArrayList<>();
        adapter = new AdminSapXepCauAdapter(AdminSapXepCauActivity.this, listSapXepCau);
        listController.listView.setAdapter(adapter);

        listController.bindBack(v -> finish());
        listController.bindAdd(v -> {
            Intent intent = new Intent(AdminSapXepCauActivity.this, AddSapXepCauActivity.class);
            intent.putExtra("idBoSapXepCau", idbo);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSapXepCau();
    }

    private void loadSapXepCau() {
        listController.showLoading();
        repository.loadSapXepCau(idbo, new QuestionRepository.DataCallback<List<CauSapXep>>() {
            @Override
            public void onSuccess(List<CauSapXep> data) {
                listSapXepCau.clear();
                listSapXepCau.addAll(data);
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
