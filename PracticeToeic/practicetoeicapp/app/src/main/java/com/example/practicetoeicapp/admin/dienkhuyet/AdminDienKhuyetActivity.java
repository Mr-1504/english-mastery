package com.example.practicetoeicapp.admin.dienkhuyet;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminListController;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.dienkhuyet.CauDienKhuyet;

import java.util.ArrayList;
import java.util.List;

public class AdminDienKhuyetActivity extends AppCompatActivity {
    ArrayList<CauDienKhuyet> listDienKhuyet;
    AdminDienKhuyetAdapter adapter;
    AdminListController listController;
    QuestionRepository repository;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dienkhuyet);
        idbo = getIntent().getIntExtra("idBoDienKhuyet", 0);
        listController = new AdminListController(this,
                R.string.admin_dienkhuyet_content,
                R.string.admin_list_subtitle,
                true,
                R.string.admin_list_empty_add);
        repository = QuestionRepository.getInstance();
        listDienKhuyet = new ArrayList<>();
        adapter = new AdminDienKhuyetAdapter(AdminDienKhuyetActivity.this, listDienKhuyet);
        listController.listView.setAdapter(adapter);

        listController.bindBack(v -> finish());
        listController.bindAdd(v -> {
            Intent intent = new Intent(AdminDienKhuyetActivity.this, AddDienKhuyetActivity.class);
            intent.putExtra("idBoDienKhuyet", idbo);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDienKhuyet();
    }

    private void loadDienKhuyet() {
        listController.showLoading();
        repository.loadDienKhuyet(idbo, new QuestionRepository.DataCallback<List<CauDienKhuyet>>() {
            @Override
            public void onSuccess(List<CauDienKhuyet> data) {
                listDienKhuyet.clear();
                listDienKhuyet.addAll(data);
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
