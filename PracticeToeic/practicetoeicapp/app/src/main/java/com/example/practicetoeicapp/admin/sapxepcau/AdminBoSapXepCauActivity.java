package com.example.practicetoeicapp.admin.sapxepcau;

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

public class AdminBoSapXepCauActivity extends AppCompatActivity {
    ImageView imgBack;
    ArrayList<BoHocTap> listBoSapXepCau;
    BoHocTapAdapter adapter;
    ListView lvBoSapXepCau;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bosapxepcau);
        lvBoSapXepCau = findViewById(R.id.listviewAdminBSXC);
        imgBack = findViewById(R.id.imgBackAdminBSXC);
        repository = QuestionRepository.getInstance();
        listBoSapXepCau = new ArrayList<>();
        adapter = new BoHocTapAdapter(AdminBoSapXepCauActivity.this,
                R.layout.row_bosapxepcau, listBoSapXepCau);
        lvBoSapXepCau.setAdapter(adapter);
        loadBoSapXepCau();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminBoSapXepCauActivity.this, AdminActivity.class));
            }
        });
        lvBoSapXepCau.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AdminBoSapXepCauActivity.this, AdminSapXepCauActivity.class);
                intent.putExtra("idBoSapXepCau", listBoSapXepCau.get(position).getIdBo());
                startActivity(intent);
            }
        });
    }

    private void loadBoSapXepCau() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                listBoSapXepCau.clear();
                listBoSapXepCau.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminBoSapXepCauActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
