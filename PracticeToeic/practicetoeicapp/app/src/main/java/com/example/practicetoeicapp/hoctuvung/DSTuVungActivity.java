package com.example.practicetoeicapp.hoctuvung;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.ui.UiFeedback;

import java.util.ArrayList;
import java.util.List;

public class DSTuVungActivity extends AppCompatActivity {

    ImageView imgback;
    GridView dstuvungs;
    Button Ontap;
    ArrayList<TuVung> DStuvung;
    DSTuVungAdapter adapter;
    QuestionRepository repository;

    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ds_tuvung);

        Intent intent = getIntent();
        idbo = intent.getIntExtra("idbo", 0);

        dstuvungs = findViewById(R.id.lgvTuVung);
        Ontap = findViewById(R.id.btnOnTap);
        imgback = findViewById(R.id.imgVBackDSTV);
        DStuvung = new ArrayList<>();
        adapter = new DSTuVungAdapter(DSTuVungActivity.this,
                R.layout.row_dstuvung, DStuvung);
        dstuvungs.setAdapter(adapter);
        repository = QuestionRepository.getInstance();
        loadTuVung();

        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DSTuVungActivity.this, HocTuVungActivity.class));
            }
        });

        Ontap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ontap = new Intent(DSTuVungActivity.this, WordMattersActivity.class);
                ontap.putExtra("idbo", idbo);
                startActivity(ontap);
            }
        });
    }

    private void loadTuVung() {
        repository.loadTuVung(idbo, new QuestionRepository.DataCallback<List<TuVung>>() {
            @Override
            public void onSuccess(List<TuVung> data) {
                DStuvung.clear();
                DStuvung.addAll(data);
                adapter.notifyDataSetChanged();
                if (DStuvung.isEmpty()) {
                    UiFeedback.showTransient(DSTuVungActivity.this,
                            getString(R.string.question_set_empty), false);
                }
            }

            @Override
            public void onError(Exception e) {
                UiFeedback.showError(DSTuVungActivity.this,
                        getString(R.string.admin_list_error));
            }
        });
    }
}
