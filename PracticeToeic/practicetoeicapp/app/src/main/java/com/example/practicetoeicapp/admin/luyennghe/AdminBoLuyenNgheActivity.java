package com.example.practicetoeicapp.admin.luyennghe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminListController;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.bohoctap.BoHocTapAdapter;
import com.example.practicetoeicapp.data.QuestionRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminBoLuyenNgheActivity extends AppCompatActivity {
    ArrayList<BoHocTap> listBoLuyenNghe;
    BoHocTapAdapter adapter;
    AdminListController listController;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_boluyennghe);
        listController = new AdminListController(this,
                R.string.admin_boluyennghe_title,
                R.string.admin_select_subtitle,
                false,
                R.string.admin_list_empty);
        repository = QuestionRepository.getInstance();
        listBoLuyenNghe = new ArrayList<>();
        adapter = new BoHocTapAdapter(AdminBoLuyenNgheActivity.this,
                R.layout.row_boluyennghe, listBoLuyenNghe);
        listController.listView.setAdapter(adapter);
        loadBoLuyenNghe();

        listController.bindBack(v -> finish());
        listController.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AdminBoLuyenNgheActivity.this, AdminLuyenNgheActivity.class);
                intent.putExtra("idBoLuyenNghe", listBoLuyenNghe.get(position).getIdBo());
                startActivity(intent);
            }
        });
    }

    private void loadBoLuyenNghe() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                listBoLuyenNghe.clear();
                listBoLuyenNghe.addAll(data);
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
