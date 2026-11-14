package com.example.practicetoeicapp.admin.dienkhuyet;

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

public class AdminBoDienKhuyetActivity extends AppCompatActivity {
    ImageView imgBack;
    ArrayList<BoHocTap> listBoDienKhuyet;
    BoHocTapAdapter adapter;
    ListView lvBoDienKhuyet;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bodienkhuyet);
        lvBoDienKhuyet = findViewById(R.id.listviewAdminBDK);
        imgBack = findViewById(R.id.imgBackAdminBDK);
        repository = QuestionRepository.getInstance();
        listBoDienKhuyet = new ArrayList<>();
        adapter = new BoHocTapAdapter(AdminBoDienKhuyetActivity.this,
                R.layout.row_bodienkhuyet, listBoDienKhuyet);
        lvBoDienKhuyet.setAdapter(adapter);
        loadBoDienKhuyet();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminBoDienKhuyetActivity.this, AdminActivity.class));
            }
        });
        lvBoDienKhuyet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AdminBoDienKhuyetActivity.this, AdminDienKhuyetActivity.class);
                intent.putExtra("idBoDienKhuyet", listBoDienKhuyet.get(position).getIdBo());
                startActivity(intent);
            }
        });
    }

    private void loadBoDienKhuyet() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                listBoDienKhuyet.clear();
                listBoDienKhuyet.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminBoDienKhuyetActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
