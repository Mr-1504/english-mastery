package com.example.practicetoeicapp.tracnghiem;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.MainActivity;
import com.example.practicetoeicapp.R;

public class FinishQuizActivity extends AppCompatActivity {

    TextView txtcongrats,txtfinalqtrue,txtfinaltext, txtfinalScore;
    Button btnReturn;
    ImageView imgtrophy;
    final  String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    int score;
    int questiontrue;
    int qcount;
    Animation smalltobig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_hoctap);

        smalltobig = AnimationUtils.loadAnimation(this, R.anim.smalltobig);
        Intent intent=getIntent();
        score=intent.getIntExtra("score",0);
        questiontrue = intent.getIntExtra("questiontrue",0);
        qcount = intent.getIntExtra("qcount",0);

        Anhxa();


        if(questiontrue==4){
            txtfinalqtrue.setText(getString(R.string.finish_score_format, questiontrue, qcount));
            txtcongrats.setText(R.string.finish_result_label);
            txtfinaltext.setText(R.string.finish_almost);
            txtfinalScore.setText(String.valueOf(score));
        }
        if(questiontrue<4){
            txtfinalqtrue.setText(getString(R.string.finish_score_format, questiontrue, qcount));
            txtcongrats.setText(R.string.finish_result_label);
            txtfinaltext.setText(R.string.finish_good_luck);
            txtfinalScore.setText(String.valueOf(score));
        }
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinishQuizActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

    }
    public void Anhxa(){
        txtfinalScore=findViewById(R.id.tvPoints);
        txtcongrats=findViewById(R.id.txtcongrats);
        txtfinalqtrue=findViewById(R.id.txtquestiontrue);
        txtfinaltext=findViewById(R.id.txtfinaltext);
        btnReturn=findViewById(R.id.btnReturn);
        imgtrophy=findViewById(R.id.imgtrophy);
        imgtrophy.startAnimation(smalltobig);
    }
}