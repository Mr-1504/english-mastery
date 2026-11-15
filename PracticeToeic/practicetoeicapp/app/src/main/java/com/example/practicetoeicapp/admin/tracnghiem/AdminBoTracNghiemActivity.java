package com.example.practicetoeicapp.admin.tracnghiem;

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

public class AdminBoTracNghiemActivity extends AppCompatActivity {
    ArrayList<BoHocTap> listBoTracNghiem;
    BoHocTapAdapter adapter;
    AdminListController listController;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_botracnghiem);
        listController = new AdminListController(this,
                R.string.admin_botracnghiem_title,
                R.string.admin_select_subtitle,
                false,
                R.string.admin_list_empty);
        repository = QuestionRepository.getInstance();
        listBoTracNghiem = new ArrayList<>();
        adapter = new BoHocTapAdapter(AdminBoTracNghiemActivity.this,
                R.layout.row_botracnghiem, listBoTracNghiem);
        listController.listView.setAdapter(adapter);

        listController.bindBack(v -> finish());
        listController.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AdminBoTracNghiemActivity.this, AdminTracNghiemActivity.class);
                intent.putExtra("idBoTracNghiem", listBoTracNghiem.get(position).getIdBo());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBoTracNghiem();
    }

    private void loadBoTracNghiem() {
        listController.showLoading();
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                listBoTracNghiem.clear();
                listBoTracNghiem.addAll(data);
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
