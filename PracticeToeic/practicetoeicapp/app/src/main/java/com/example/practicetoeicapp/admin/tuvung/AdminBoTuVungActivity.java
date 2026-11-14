package com.example.practicetoeicapp.admin.tuvung;

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

public class AdminBoTuVungActivity extends AppCompatActivity {
    ArrayList<BoHocTap> listBoTuVung;
    BoHocTapAdapter adapter;
    AdminListController listController;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_botuvung);
        listController = new AdminListController(this,
                R.string.admin_botuvung_title,
                R.string.admin_select_subtitle,
                false,
                R.string.admin_list_empty);
        repository = QuestionRepository.getInstance();
        listBoTuVung = new ArrayList<>();
        adapter = new BoHocTapAdapter(AdminBoTuVungActivity.this,
                R.layout.row_botuvung, listBoTuVung);
        listController.listView.setAdapter(adapter);
        loadBoTuVung();

        listController.bindBack(v -> finish());
        listController.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AdminBoTuVungActivity.this, AdminTuVungActivity.class);
                intent.putExtra("idBoTuVung", listBoTuVung.get(position).getIdBo());
                startActivity(intent);
            }
        });
    }

    private void loadBoTuVung() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                listBoTuVung.clear();
                listBoTuVung.addAll(data);
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
