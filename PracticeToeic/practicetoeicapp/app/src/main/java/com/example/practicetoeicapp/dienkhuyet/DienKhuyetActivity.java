package com.example.practicetoeicapp.dienkhuyet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.MainActivity;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.bohoctap.BoHocTapAdapter;
import com.example.practicetoeicapp.data.QuestionRepository;

import java.util.ArrayList;
import java.util.List;

public class DienKhuyetActivity extends AppCompatActivity {

    ListView listView;
    ImageView imgback;
    ArrayList<BoHocTap> boHocTapArrayList;
    BoHocTapAdapter boHocTapAdapter;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dien_khuyet);

        listView = findViewById(R.id.lvdienkhuyet);
        imgback = findViewById(R.id.imgVBackDK);
        boHocTapArrayList = new ArrayList<>();
        boHocTapAdapter = new BoHocTapAdapter(DienKhuyetActivity.this,
                R.layout.row_bodienkhuyet, boHocTapArrayList);
        listView.setAdapter(boHocTapAdapter);
        repository = QuestionRepository.getInstance();
        loadBoHocTap();

        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DienKhuyetActivity.this, MainActivity.class);
                intent.putExtras(getIntent());
                startActivity(intent);
                finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent quiz = new Intent(DienKhuyetActivity.this, FillBlanksActivity.class);
                quiz.putExtra("BoDK", boHocTapArrayList.get(position).getIdBo());
                startActivity(quiz);
            }
        });
    }

    private void loadBoHocTap() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                boHocTapArrayList.clear();
                boHocTapArrayList.addAll(data);
                boHocTapAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(DienKhuyetActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
