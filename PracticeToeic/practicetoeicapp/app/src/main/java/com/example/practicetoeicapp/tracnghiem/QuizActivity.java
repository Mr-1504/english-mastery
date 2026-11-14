package com.example.practicetoeicapp.tracnghiem;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.taikhoan.User;
import com.example.practicetoeicapp.ui.UiFeedback;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    final String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;
    TextView txtscore, txtquestcount, txtquestion, txttime;
    RadioGroup rdgchoices;
    RadioButton btnop1, btnop2, btnop3, btnop4;
    Button btnconfirm;
    Button btnquit;
    Button btnback;
    LinearProgressIndicator progressTN;
    private ArrayList<CauTracNghiem> cauTracNghiems;
    int questioncurrent = 0;
    int questiontrue = 0;
    int answer = 0;
    int score = 0;
    int idbo;
    QuestionRepository repository;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        DB = DatabaseAccess.getInstance(getApplicationContext());
        repository = QuestionRepository.getInstance();
        Anhxa();

        LayUser();
        Intent intent = getIntent();
        idbo = intent.getIntExtra("Bo", 0);
        txttime.setText(" ");

        cauTracNghiems = new ArrayList<>();
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnquit.performClick();
            }
        });
        loadQuestions();
    }

    public void Anhxa() {
        txtscore = findViewById(R.id.txtscoreTN);
        txtquestcount = findViewById(R.id.txtquestcountTN);
        txtquestion = findViewById(R.id.txtquestionTN);
        txttime = findViewById(R.id.txttimeTN);
        rdgchoices = findViewById(R.id.radiochoices);
        btnop1 = findViewById(R.id.op1);
        btnop2 = findViewById(R.id.op2);
        btnop3 = findViewById(R.id.op3);
        btnop4 = findViewById(R.id.op4);
        btnconfirm = findViewById(R.id.btnconfirmTN);
        btnquit = findViewById(R.id.btnQuitTN);
        btnback = findViewById(R.id.btnBackTN);
        progressTN = findViewById(R.id.progressTN);
    }

    private void loadQuestions() {
        repository.loadTracNghiem(idbo, new QuestionRepository.DataCallback<List<CauTracNghiem>>() {
            @Override
            public void onSuccess(List<CauTracNghiem> data) {
                cauTracNghiems.clear();
                cauTracNghiems.addAll(data);
                if (cauTracNghiems.isEmpty()) {
                    UiFeedback.showTransient(QuizActivity.this,
                            getString(R.string.question_set_empty), false);
                    finish();
                    startActivity(new Intent(QuizActivity.this, TracNghiemActivity.class));
                } else {
                    startQuiz();
                }
            }

            @Override
            public void onError(Exception e) {
                UiFeedback.showError(QuizActivity.this,
                        getString(R.string.admin_list_error));
            }
        });
    }

    private void startQuiz() {
        shownextquestion(questioncurrent, cauTracNghiems);

        CountDownTimer countDownTimer = new CountDownTimer(3000, 300) {
            @Override
            public void onTick(long millisUntilFinished) {
                showanswer();
            }

            @Override
            public void onFinish() {
                btnconfirm.setEnabled(true);
                shownextquestion(questioncurrent, cauTracNghiems);
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

        btnquit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(QuizActivity.this, TracNghiemActivity.class));
            }
        });
    }

    public void LayUser() {
        database = Database.initDatabase(QuizActivity.this, DATABASE_NAME);
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

    public void shownextquestion(int pos, ArrayList<CauTracNghiem> cauTracNghiems) {
        int total = cauTracNghiems.size();
        int current = Math.min(questioncurrent + 1, total);
        txtquestcount.setText(getString(R.string.quiz_progress, current, total));
        progressTN.setProgressCompat(total == 0 ? 0 : current * 100 / total, true);
        rdgchoices.clearCheck();
        resetOptions();
        setOptionsEnabled(true);
        if (pos == cauTracNghiems.size()) {
            DB.capnhatdiem(DB.iduser, user.getPoint(), score);
            Intent intent = new Intent(QuizActivity.this, FinishQuizActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("questiontrue", questiontrue);
            intent.putExtra("qcount", pos);
            startActivity(intent);
            finish();
        } else {
            answer = Integer.valueOf(cauTracNghiems.get(pos).getDapanTrue());
            txtquestion.setText(cauTracNghiems.get(pos).getNoidung());
            btnop1.setText(cauTracNghiems.get(pos).getDapanA());
            btnop2.setText(cauTracNghiems.get(pos).getDapanB());
            btnop3.setText(cauTracNghiems.get(pos).getDapanC());
            btnop4.setText(cauTracNghiems.get(pos).getDapanD());
        }
    }

    public void checkans() {
        btnconfirm.setEnabled(false);
        setOptionsEnabled(false);
        boolean correct = (btnop1.isChecked() && answer == 1)
                || (btnop2.isChecked() && answer == 2)
                || (btnop3.isChecked() && answer == 3)
                || (btnop4.isChecked() && answer == 4);
        if (correct) {
            questiontrue++;
            score += 5;
        }
        UiFeedback.showAnswer(this, correct);
        txtscore.setText(getString(R.string.quiz_score, score));
    }

    public void showanswer() {
        resetOptions();
        RadioButton[] options = {btnop1, btnop2, btnop3, btnop4};
        for (int i = 0; i < options.length; i++) {
            if (i + 1 == answer) {
                options[i].setBackgroundResource(R.drawable.bg_option_correct);
            } else if (options[i].isChecked()) {
                options[i].setBackgroundResource(R.drawable.bg_option_wrong);
            }
        }
    }

    private void resetOptions() {
        btnop1.setBackgroundResource(R.drawable.bg_option_selector);
        btnop2.setBackgroundResource(R.drawable.bg_option_selector);
        btnop3.setBackgroundResource(R.drawable.bg_option_selector);
        btnop4.setBackgroundResource(R.drawable.bg_option_selector);
    }

    private void setOptionsEnabled(boolean enabled) {
        btnop1.setEnabled(enabled);
        btnop2.setEnabled(enabled);
        btnop3.setEnabled(enabled);
        btnop4.setEnabled(enabled);
    }
}
