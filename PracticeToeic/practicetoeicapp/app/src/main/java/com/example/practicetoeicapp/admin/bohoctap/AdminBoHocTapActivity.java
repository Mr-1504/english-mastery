package com.example.practicetoeicapp.admin.bohoctap;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminListController;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.data.QuestionRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminBoHocTapActivity extends AppCompatActivity {
    ArrayList<BoHocTap> listBHT;
    AdminBoHocTapAdapter adapter;
    AdminListController listController;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bohoctap);
        listController = new AdminListController(this,
                R.string.admin_bohoctap_title,
                R.string.admin_list_subtitle,
                true,
                R.string.admin_list_empty_add);
        repository = QuestionRepository.getInstance();
        listBHT = new ArrayList<>();
        adapter = new AdminBoHocTapAdapter(AdminBoHocTapActivity.this, listBHT);
        listController.listView.setAdapter(adapter);

        listController.bindBack(v -> finish());
        listController.bindAdd(v ->
                startActivity(new Intent(AdminBoHocTapActivity.this, AddBoHocTapActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBoHocTap();
    }

    private void loadBoHocTap() {
        listController.showLoading();
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                listBHT.clear();
                listBHT.addAll(data);
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
