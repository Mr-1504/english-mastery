package com.example.practicetoeicapp.hoctuvung;

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

public class HocTuVungActivity extends AppCompatActivity {

    ImageView imgback;
    ProgressBar progressBar;
    TextView emptyState;
    ArrayList<BoHocTap> boTuVungs;
    BoHocTapAdapter adapter;
    ListView botuvungs;
    QuestionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoctuvung);
        botuvungs = findViewById(R.id.listviewHTV);
        imgback = findViewById(R.id.imgVBackHTV);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        boTuVungs = new ArrayList<>();
        adapter = new BoHocTapAdapter(HocTuVungActivity.this,
                R.layout.row_botuvung, boTuVungs);
        botuvungs.setAdapter(adapter);
        repository = QuestionRepository.getInstance();
        loadBoHocTap();

        botuvungs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent dstv = new Intent(HocTuVungActivity.this, DSTuVungActivity.class);
                dstv.putExtra("idbo", boTuVungs.get(position).getIdBo());
                startActivity(dstv);
            }
        });

        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HocTuVungActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadBoHocTap() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                boTuVungs.clear();
                boTuVungs.addAll(data);
                adapter.notifyDataSetChanged();
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
