package com.example.practicetoeicapp.luyennghe;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.taikhoan.User;

import java.util.ArrayList;
import java.util.List;

public class ListeningActivity extends AppCompatActivity {
    final String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;
    TextView txtscore, txtquestcount;
    RadioGroup rdgchoices;
    RadioButton btnop1, btnop2, btnop3, btnop4;
    Button btnconfirm;
    Button btnquit;
    ImageView imHA;
    private ArrayList<CauLuyenNghe> cauLuyenNghes;
    private MediaPlayer mediaPlayer;
    private ImageButton ImgBT;
    private QuestionRepository repository;

    User user;

    String URL = "";
    int questioncurrent = 0;
    int questiontrue = 0;
    int answer = 0;
    int score = 0;
    int idbo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quiz_listening);
        DB = DatabaseAccess.getInstance(getApplicationContext());
        repository = QuestionRepository.getInstance();
        Anhxa();
        LayUser();
        Intent intent = getIntent();
        idbo = intent.getIntExtra("Bo", 0);
        cauLuyenNghes = new ArrayList<>();
        loadQuestions();
    }

    private void loadQuestions() {
        repository.loadLuyenNghe(idbo, new QuestionRepository.DataCallback<List<CauLuyenNghe>>() {
            @Override
            public void onSuccess(List<CauLuyenNghe> data) {
                cauLuyenNghes.clear();
                cauLuyenNghes.addAll(data);
                if (cauLuyenNghes.isEmpty()) {
                    Toast.makeText(ListeningActivity.this,
                            "Nội dung sẽ cập nhật cập nhật trong thời gian sớm nhất! Mong mọi người thông càm!!",
                            Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(ListeningActivity.this, LuyenNgheActivity.class));
                } else {
                    startQuiz();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ListeningActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startQuiz() {
        shownextquestion(questioncurrent, cauLuyenNghes);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        CountDownTimer countDownTimer = new CountDownTimer(3000, 300) {
            @Override
            public void onTick(long millisUntilFinished) {
                showanswer();
            }

            @Override
            public void onFinish() {
                btnconfirm.setEnabled(true);
                shownextquestion(questioncurrent, cauLuyenNghes);
            }
        };
        btnconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkans();
                questioncurrent++;
                countDownTimer.start();
            }
        });

        this.ImgBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayerUtils.playURLMedia(ListeningActivity.this, mediaPlayer, URL);
            }
        });

        btnquit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                doStop();
                startActivity(new Intent(ListeningActivity.this, LuyenNgheActivity.class));
            }
        });
    }

    public void Anhxa() {
        txtscore = findViewById(R.id.txtscoreLN);
        txtquestcount = findViewById(R.id.txtquestcountLN);
        rdgchoices = findViewById(R.id.radiochoices);
        btnop1 = findViewById(R.id.op1);
        btnop2 = findViewById(R.id.op2);
        btnop3 = findViewById(R.id.op3);
        btnop4 = findViewById(R.id.op4);
        btnconfirm = findViewById(R.id.btnconfirmLN);
        btnquit = findViewById(R.id.btnQuitLN);
        imHA = findViewById(R.id.imgHinh);
        ImgBT = findViewById(R.id.ImgBT);
    }

    public void LayUser() {
        database = Database.initDatabase(ListeningActivity.this, DATABASE_NAME);
        Cursor cursor = database.rawQuery("SELECT * FROM User WHERE ID_User = ?",
                new String[]{String.valueOf(DB.iduser)});
        cursor.moveToNext();
        String Iduser = cursor.getString(0);
        String HoTen = cursor.getString(1);
        int Point = cursor.getInt(2);
        String Email = cursor.getString(3);
        String SDT = cursor.getString(4);
        int Role = cursor.getInt(5);
        user = new User(Iduser, HoTen, Point, Email, SDT, Role);
    }

    public void shownextquestion(int pos, ArrayList<CauLuyenNghe> cauLuyenNghes) {
        if (pos > 0) doStop();
        txtquestcount.setText("Question: " + (questioncurrent + 1) + "/" + cauLuyenNghes.size() + "");
        rdgchoices.clearCheck();
        btnop1.setBackground(this.getResources().getDrawable(R.drawable.bgbtn));
        btnop2.setBackground(this.getResources().getDrawable(R.drawable.bgbtn));
        btnop3.setBackground(this.getResources().getDrawable(R.drawable.bgbtn));
        btnop4.setBackground(this.getResources().getDrawable(R.drawable.bgbtn));
        if (pos == cauLuyenNghes.size()) {
            DB.capnhatdiem(DB.iduser, user.getPoint(), score);
            Intent intent = new Intent(ListeningActivity.this, FinishQuizLSActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("questiontrue", questiontrue);
            intent.putExtra("qcount", pos);
            startActivity(intent);
        } else {
            Glide.with(this)
                    .load(cauLuyenNghes.get(pos).getHinhAnhUrl())
                    .into(imHA);

            URL = cauLuyenNghes.get(pos).getAudio();

            answer = Integer.valueOf(cauLuyenNghes.get(pos).getDapanTrue());
            btnop1.setText(cauLuyenNghes.get(pos).getDapanA());
            btnop2.setText(cauLuyenNghes.get(pos).getDapanB());
            btnop3.setText(cauLuyenNghes.get(pos).getDapanC());
            btnop4.setText(cauLuyenNghes.get(pos).getDapanD());
        }
    }

    public void checkans() {
        btnconfirm.setEnabled(false);
        if (btnop1.isChecked()) {
            if (1 == answer) {
                score += 5;
                questiontrue++;
            }
        }
        if (btnop2.isChecked()) {
            if (2 == answer) {
                score += 5;
                questiontrue++;
            }
        }
        if (btnop3.isChecked()) {
            if (3 == answer) {
                score += 5;
                questiontrue++;
            }
        }
        if (btnop4.isChecked()) {
            if (4 == answer) {
                score += 5;
                questiontrue++;
            }
        }

        txtscore.setText("Score: " + score + "");
    }

    public void showanswer() {
        if (1 == answer) {
            btnop1.setBackground(this.getResources().getDrawable(R.drawable.button_2));
            btnop2.setBackground(this.getResources().getDrawable(R.drawable.button_1));
            btnop3.setBackground(this.getResources().getDrawable(R.drawable.button_1));
            btnop4.setBackground(this.getResources().getDrawable(R.drawable.button_1));
        }
        if (2 == answer) {
            btnop1.setBackground(this.getResources().getDrawable(R.drawable.button_1));
            btnop2.setBackground(this.getResources().getDrawable(R.drawable.button_2));
            btnop3.setBackground(this.getResources().getDrawable(R.drawable.button_1));
            btnop4.setBackground(this.getResources().getDrawable(R.drawable.button_1));
        }
        if (3 == answer) {
            btnop1.setBackground(this.getResources().getDrawable(R.drawable.button_1));
            btnop2.setBackground(this.getResources().getDrawable(R.drawable.button_1));
            btnop3.setBackground(this.getResources().getDrawable(R.drawable.button_2));
            btnop4.setBackground(this.getResources().getDrawable(R.drawable.button_1));
        }
        if (4 == answer) {
            btnop1.setBackground(this.getResources().getDrawable(R.drawable.button_1));
            btnop2.setBackground(this.getResources().getDrawable(R.drawable.button_1));
            btnop3.setBackground(this.getResources().getDrawable(R.drawable.button_1));
            btnop4.setBackground(this.getResources().getDrawable(R.drawable.button_2));
        }
    }

    private void doStop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
    }
}
