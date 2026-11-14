package com.example.practicetoeicapp.admin.tracnghiem;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminListController;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.tracnghiem.CauTracNghiem;

import java.util.ArrayList;
import java.util.List;

public class AdminTracNghiemActivity extends AppCompatActivity {
    ArrayList<CauTracNghiem> listTracNghiem;
    AdminTracNghiemAdapter adapter;
    AdminListController listController;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tracnghiem);
        idbo = getIntent().getIntExtra("idBoTracNghiem", 0);
        listController = new AdminListController(this,
                R.string.admin_tracnghiem_content,
                R.string.admin_list_subtitle,
                true,
                R.string.admin_list_empty_add);
        repository = QuestionRepository.getInstance();
        listTracNghiem = new ArrayList<>();
        adapter = new AdminTracNghiemAdapter(AdminTracNghiemActivity.this, listTracNghiem);
        listController.listView.setAdapter(adapter);
        loadTracNghiem();

        listController.bindBack(v -> finish());
        listController.bindAdd(v -> {
            Intent intent = new Intent(AdminTracNghiemActivity.this, AddTracNghiemActivity.class);
            intent.putExtra("idBoTracNghiem", idbo);
            startActivity(intent);
        });
    }

    private void loadTracNghiem() {
        repository.loadTracNghiem(idbo, new QuestionRepository.DataCallback<List<CauTracNghiem>>() {
            @Override
            public void onSuccess(List<CauTracNghiem> data) {
                listTracNghiem.clear();
                listTracNghiem.addAll(data);
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
