package com.example.practicetoeicapp.admin.luyennghe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminActivity;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.bohoctap.BoHocTapAdapter;
import com.example.practicetoeicapp.data.QuestionRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminBoLuyenNgheActivity extends AppCompatActivity {
    ImageView imgBack;
    ArrayList<BoHocTap> listBoLuyenNghe;
    BoHocTapAdapter adapter;
    ListView lvBoLuyenNghe;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_boluyennghe);
        lvBoLuyenNghe = findViewById(R.id.listviewAdminBLN);
        imgBack = findViewById(R.id.imgBackAdminBLN);
        repository = QuestionRepository.getInstance();
        listBoLuyenNghe = new ArrayList<>();
        adapter = new BoHocTapAdapter(AdminBoLuyenNgheActivity.this,
                R.layout.row_boluyennghe, listBoLuyenNghe);
        lvBoLuyenNghe.setAdapter(adapter);
        loadBoLuyenNghe();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminBoLuyenNgheActivity.this, AdminActivity.class));
            }
        });
        lvBoLuyenNghe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminBoLuyenNgheActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
