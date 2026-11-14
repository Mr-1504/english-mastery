package com.example.practicetoeicapp.dienkhuyet;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.taikhoan.User;
import com.example.practicetoeicapp.ui.UiFeedback;

import java.util.ArrayList;
import java.util.List;

public class FillBlanksActivity extends AppCompatActivity {

    final String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;
    TextView txtscoreDK, txtquestcountDK, txtquestionDK, txttimeDK, txtGoiy;
    EditText edtAnswerDK;
    Button btnconfirm, btnQuit, btnback;
    LinearProgressIndicator progressDK;
    private ArrayList<CauDienKhuyet> cauDienKhuyets;
    int questioncurrent = 0;
    int questiontrue = 0;
    String answer;
    int score = 0;
    int idbo;
    QuestionRepository repository;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_blanks);
        DB = DatabaseAccess.getInstance(getApplicationContext());
        repository = QuestionRepository.getInstance();

        Anhxa();
        LayUser();

        Intent intent = getIntent();
        idbo = intent.getIntExtra("BoDK", 0);
        txttimeDK.setText(" ");

        cauDienKhuyets = new ArrayList<>();
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnQuit.performClick();
            }
        });
        loadQuestions();
    }

    private void loadQuestions() {
        repository.loadDienKhuyet(idbo, new QuestionRepository.DataCallback<List<CauDienKhuyet>>() {
            @Override
            public void onSuccess(List<CauDienKhuyet> data) {
                cauDienKhuyets.clear();
                cauDienKhuyets.addAll(data);
                if (cauDienKhuyets.isEmpty()) {
                    UiFeedback.showTransient(FillBlanksActivity.this,
                            getString(R.string.question_set_empty), false);
                    finish();
                    startActivity(new Intent(FillBlanksActivity.this, DienKhuyetActivity.class));
                } else {
                    startQuiz();
                }
            }

            @Override
            public void onError(Exception e) {
                UiFeedback.showError(FillBlanksActivity.this,
                        getString(R.string.admin_list_error));
            }
        });
    }

    private void startQuiz() {
        shownextquestion(questioncurrent, cauDienKhuyets);

        CountDownTimer countDownTimer = new CountDownTimer(3000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                questioncurrent++;
                edtAnswerDK.setTextColor(ContextCompat.getColor(FillBlanksActivity.this, R.color.app_on_surface));
                btnconfirm.setEnabled(true);
                edtAnswerDK.setEnabled(true);
                edtAnswerDK.setText("");
                answer = "";
                shownextquestion(questioncurrent, cauDienKhuyets);
            }
        };

        btnconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
                showanswer();
                countDownTimer.start();
            }
        });
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(FillBlanksActivity.this, DienKhuyetActivity.class));
            }
        });
    }

    public void LayUser() {
        database = Database.initDatabase(FillBlanksActivity.this, DATABASE_NAME);
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

    public void shownextquestion(int pos, ArrayList<CauDienKhuyet> cauDienKhuyets) {
        int total = cauDienKhuyets.size();
        int current = Math.min(questioncurrent + 1, total);
        txtquestcountDK.setText(getString(R.string.quiz_progress, current, total));
        progressDK.setProgressCompat(total == 0 ? 0 : current * 100 / total, true);
        edtAnswerDK.setBackgroundResource(R.drawable.bg_input);

        if (pos == cauDienKhuyets.size()) {
            DB.capnhatdiem(DB.iduser, user.getPoint(), score);
            Intent intent = new Intent(FillBlanksActivity.this, FinishDKActivity.class);
            intent.putExtra("scoreDK", score);
            intent.putExtra("questiontrueDK", questiontrue);
            intent.putExtra("qcountDK", pos);
            startActivity(intent);
            finish();
        } else {
            answer = cauDienKhuyets.get(pos).getDapan();
            txtGoiy.setText(cauDienKhuyets.get(pos).getGoiy());
            txtquestionDK.setText(cauDienKhuyets.get(pos).getNoidung());
        }
    }

    public void showanswer() {
        edtAnswerDK.setText(answer);
        edtAnswerDK.setTextColor(ContextCompat.getColor(this, R.color.app_success));
        edtAnswerDK.clearFocus();
    }

    public void checkAnswer() {
        btnconfirm.setEnabled(false);
        edtAnswerDK.setEnabled(false);
        if (answer.equals(edtAnswerDK.getText().toString())) {
            UiFeedback.showAnswer(this, true);
            edtAnswerDK.setTextColor(ContextCompat.getColor(this, R.color.app_success));
            questiontrue++;
            score += 5;
            txtscoreDK.setText(getString(R.string.quiz_score, score));
            edtAnswerDK.clearFocus();
        } else {
            UiFeedback.showAnswer(this, false);
            edtAnswerDK.setTextColor(ContextCompat.getColor(this, R.color.app_error));
            edtAnswerDK.startAnimation(shakeError());
            edtAnswerDK.clearFocus();
        }
    }

    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(7));
        return shake;
    }

    public void Anhxa() {
        txtscoreDK = findViewById(R.id.txtscoreDK);
        txtquestcountDK = findViewById(R.id.txtquestcountDK);
        txtquestionDK = findViewById(R.id.txtquestionDK);
        txttimeDK = findViewById(R.id.txttimeDK);
        edtAnswerDK = findViewById(R.id.AnswerDK);
        btnconfirm = findViewById(R.id.btnconfirmDK);
        txtGoiy = findViewById(R.id.textviewGoiy);
        btnQuit = findViewById(R.id.btnQuitDK);
        btnback = findViewById(R.id.btnBackDK);
        progressDK = findViewById(R.id.progressDK);
    }
}
