package com.example.practicetoeicapp.admin.tracnghiem;

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

public class AdminBoTracNghiemActivity extends AppCompatActivity {
    ImageView imgBack;
    ArrayList<BoHocTap> listBoTracNghiem;
    BoHocTapAdapter adapter;
    ListView lvBoTracNghiem;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_botracnghiem);
        lvBoTracNghiem = findViewById(R.id.listviewAdminBTN);
        imgBack = findViewById(R.id.imgBackAdminBTN);
        repository = QuestionRepository.getInstance();
        listBoTracNghiem = new ArrayList<>();
        adapter = new BoHocTapAdapter(AdminBoTracNghiemActivity.this,
                R.layout.row_botracnghiem, listBoTracNghiem);
        lvBoTracNghiem.setAdapter(adapter);
        loadBoTracNghiem();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminBoTracNghiemActivity.this, AdminActivity.class));
            }
        });
        lvBoTracNghiem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AdminBoTracNghiemActivity.this, AdminTracNghiemActivity.class);
                intent.putExtra("idBoTracNghiem", listBoTracNghiem.get(position).getIdBo());
                startActivity(intent);
            }
        });
    }

    private void loadBoTracNghiem() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                listBoTracNghiem.clear();
                listBoTracNghiem.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminBoTracNghiemActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
