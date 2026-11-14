package com.example.practicetoeicapp.luyennghe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.MainActivity;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.bohoctap.BoHocTapAdapter;
import com.example.practicetoeicapp.data.QuestionRepository;

import java.util.ArrayList;
import java.util.List;

public class LuyenNgheActivity extends AppCompatActivity {
    ListView listView;
    ImageView imgback;
    ProgressBar progressBar;
    TextView emptyState;
    ArrayList<BoHocTap> boCauHoiArrayList;
    BoHocTapAdapter boCauHoiAdapter;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luyen_nghe);
        listView = findViewById(R.id.lvluyennghe);
        imgback = findViewById(R.id.imgVBackLN);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        boCauHoiArrayList = new ArrayList<>();
        boCauHoiAdapter = new BoHocTapAdapter(LuyenNgheActivity.this,
                R.layout.row_boluyennghe, boCauHoiArrayList);
        listView.setAdapter(boCauHoiAdapter);
        repository = QuestionRepository.getInstance();
        loadBoHocTap();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent quiz = new Intent(LuyenNgheActivity.this, ListeningActivity.class);
                quiz.putExtra("Bo", boCauHoiArrayList.get(position).getIdBo());
                startActivity(quiz);
            }
        });

        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LuyenNgheActivity.this, MainActivity.class);
                intent.putExtras(getIntent());
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadBoHocTap() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                boCauHoiArrayList.clear();
                boCauHoiArrayList.addAll(data);
                boCauHoiAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                emptyState.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                emptyState.setText(R.string.question_set_error);
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
