package com.example.practicetoeicapp.admin.tuvung;

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

public class AdminBoTuVungActivity extends AppCompatActivity {
    ImageView imgBack;
    ArrayList<BoHocTap> listBoTuVung;
    BoHocTapAdapter adapter;
    ListView lvBoTuVung;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_botuvung);
        lvBoTuVung = findViewById(R.id.listviewAdminBTV);
        imgBack = findViewById(R.id.imgBackAdminBTV);
        repository = QuestionRepository.getInstance();
        listBoTuVung = new ArrayList<>();
        adapter = new BoHocTapAdapter(AdminBoTuVungActivity.this,
                R.layout.row_botuvung, listBoTuVung);
        lvBoTuVung.setAdapter(adapter);
        loadBoTuVung();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminBoTuVungActivity.this, AdminActivity.class));
            }
        });
        lvBoTuVung.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminBoTuVungActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
