package com.example.practicetoeicapp.tracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.MainActivity;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.bohoctap.BoHocTapAdapter;
import com.example.practicetoeicapp.data.QuestionRepository;

import java.util.ArrayList;
import java.util.List;

public class TracNghiemActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<BoHocTap> boHocTapArrayList;
    BoHocTapAdapter boHocTapAdapter;
    ImageView imgback;
    ProgressBar progressBar;
    TextView emptyState;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trac_nghiem);

        listView = findViewById(R.id.lvtracnghiem);
        imgback = findViewById(R.id.imgVBackTN);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        boHocTapArrayList = new ArrayList<>();
        boHocTapAdapter = new BoHocTapAdapter(TracNghiemActivity.this,
                R.layout.row_botracnghiem, boHocTapArrayList);
        listView.setAdapter(boHocTapAdapter);
        repository = QuestionRepository.getInstance();
        loadBoHocTap();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent quiz = new Intent(TracNghiemActivity.this, QuizActivity.class);
                quiz.putExtra("Bo", boHocTapArrayList.get(position).getIdBo());
                startActivity(quiz);
            }
        });

        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TracNghiemActivity.this, MainActivity.class);
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
                boHocTapArrayList.clear();
                boHocTapArrayList.addAll(data);
                boHocTapAdapter.notifyDataSetChanged();
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
