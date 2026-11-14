package com.example.practicetoeicapp.sapxepcau;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.taikhoan.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArrangeSentencesActivity extends AppCompatActivity {

    final String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;

    private int presCounter = 0;
    private String[] keys = {"part1", "part2", "part3", "part4"};
    private String textAnswer = "ENGLISH";
    private int maxPresCounter = 4;
    Button btnquit, btnback;
    LinearProgressIndicator progressSXC;
    TextView textScreen, textTitle;
    TextView tvQuestionCount, tvScore;
    ArrayList<CauSapXep> cauSapXeps;
    Animation smallbigforth;
    int idbo;
    private int score = 0;
    int dem;
    int cau = 1;
    QuestionRepository repository;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrange_sentences);
        DB = DatabaseAccess.getInstance(getApplicationContext());
        repository = QuestionRepository.getInstance();
        AnhXa();

        Intent intent = getIntent();
        idbo = intent.getIntExtra("idbo", 0);
        cauSapXeps = new ArrayList<>();
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnquit.performClick();
            }
        });
        LayUser();
        loadQuestions();
    }

    private void loadQuestions() {
        repository.loadSapXepCau(idbo, new QuestionRepository.DataCallback<List<CauSapXep>>() {
            @Override
            public void onSuccess(List<CauSapXep> data) {
                cauSapXeps.clear();
                cauSapXeps.addAll(data);
                if (cauSapXeps.isEmpty()) {
                    Toast.makeText(ArrangeSentencesActivity.this,
                            "Nội dung sẽ cập nhật cập nhật trong thời gian sớm nhất! Mong mọi người thông càm!!",
                            Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(ArrangeSentencesActivity.this, SapXepCauActivity.class));
                } else {
                    startGame();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ArrangeSentencesActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startGame() {
        textAnswer = cauSapXeps.get(0).getDapan();

        smallbigforth = AnimationUtils.loadAnimation(this, R.anim.smallbigforth);

        keys[0] = cauSapXeps.get(0).getPart1();
        keys[1] = cauSapXeps.get(0).getPart2();
        keys[2] = cauSapXeps.get(0).getPart3();
        keys[3] = cauSapXeps.get(0).getPart4();

        updateHeader();

        keys = shuffleArray(keys);

        dem = 0;
        while (dem < keys.length) {
            if (dem < 1) {
                addView(((LinearLayout) findViewById(R.id.layoutPart1)), keys[dem], ((EditText) findViewById(R.id.editDapAn)));
            } else if (dem < 2) {
                addView(((LinearLayout) findViewById(R.id.layoutPart2)), keys[dem], ((EditText) findViewById(R.id.editDapAn)));
            } else if (dem < 3) {
                addView(((LinearLayout) findViewById(R.id.layoutPart3)), keys[dem], ((EditText) findViewById(R.id.editDapAn)));
            } else {
                addView(((LinearLayout) findViewById(R.id.layoutPart4)), keys[dem], ((EditText) findViewById(R.id.editDapAn)));
            }
            dem++;
        }

        btnquit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(ArrangeSentencesActivity.this, SapXepCauActivity.class));
            }
        });
    }

    private void AnhXa() {
        textScreen = findViewById(R.id.textSXC);
        textTitle = findViewById(R.id.textOption);
        tvQuestionCount = findViewById(R.id.tvQuestionSXC);
        tvScore = findViewById(R.id.tvScoreSXC);
        btnquit = findViewById(R.id.btnQuitSXC);
        btnback = findViewById(R.id.btnBackSXC);
        progressSXC = findViewById(R.id.progressSXC);
    }

    public void LayUser() {
        database = Database.initDatabase(ArrangeSentencesActivity.this, DATABASE_NAME);
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

    private String[] shuffleArray(String[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            String a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        return ar;
    }

    private void addView(LinearLayout viewParent, final String text, final EditText editText) {
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        linearLayoutParams.setMargins(dp(4), dp(4), dp(4), dp(4));

        final TextView textView = new TextView(this);

        textView.setLayoutParams(linearLayoutParams);
        textView.setBackgroundResource(R.drawable.bg_chip);
        textView.setTextColor(ContextCompat.getColor(this, R.color.app_on_primary_container));
        textView.setGravity(Gravity.CENTER);
        textView.setText(text);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setMinHeight(dp(48));
        textView.setPadding(dp(14), dp(10), dp(14), dp(10));
        textView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (presCounter < maxPresCounter) {
                    if (presCounter == 0)
                        editText.setText("");

                    int chuoi = 0;

                    if (editText.getText().toString().equals("") || chuoi == 3) {
                        editText.setText(editText.getText().toString() + text);
                        chuoi++;
                    } else {
                        editText.setText(editText.getText().toString() + " " + text);
                        chuoi++;
                    }

                    textView.startAnimation(smallbigforth);
                    textView.animate().alpha(0).setDuration(300);
                    presCounter++;
                    textView.setClickable(false);

                    if (presCounter == maxPresCounter)
                        doValidate();
                }
            }
        });
        viewParent.addView(textView);
    }

    private void updateHeader() {
        int total = cauSapXeps.size();
        int current = Math.min(cau, total);
        tvQuestionCount.setText(getString(R.string.quiz_progress, current, total));
        tvScore.setText(getString(R.string.quiz_score, score));
        progressSXC.setProgressCompat(total == 0 ? 0 : current * 100 / total, true);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()));
    }

    private void doValidate() {
        presCounter = 0;

        EditText editText = findViewById(R.id.editDapAn);
        LinearLayout Part1 = findViewById(R.id.layoutPart1);
        LinearLayout Part2 = findViewById(R.id.layoutPart2);
        LinearLayout Part3 = findViewById(R.id.layoutPart3);
        LinearLayout Part4 = findViewById(R.id.layoutPart4);

        if (editText.getText().toString().equals(textAnswer)) {
            if (cau == cauSapXeps.size()) {
                score += 5;
                DB.capnhatdiem(DB.iduser, user.getPoint(), score);
                Toast.makeText(ArrangeSentencesActivity.this, "Hoàn Thành Xuất Sắc!!~(^.^)~", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ArrangeSentencesActivity.this, FinishSXCActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("questiontrue", cau);
                intent.putExtra("qcount", cauSapXeps.size());
                startActivity(intent);
                finish();
                return;
            } else {
                Toast.makeText(ArrangeSentencesActivity.this, "Chính xác!!(^.^)", Toast.LENGTH_SHORT).show();
                textAnswer = cauSapXeps.get(cau).getDapan();
                keys[0] = cauSapXeps.get(cau).getPart1();
                keys[1] = cauSapXeps.get(cau).getPart2();
                keys[2] = cauSapXeps.get(cau).getPart3();
                keys[3] = cauSapXeps.get(cau).getPart4();
                editText.setText("");
                score += 5;
                cau++;
            }
        } else {
            Toast.makeText(ArrangeSentencesActivity.this, "Sai rồi!!(T.T)", Toast.LENGTH_SHORT).show();
            editText.setText("");
        }

        updateHeader();

        keys = shuffleArray(keys);
        Part1.removeAllViews();
        Part2.removeAllViews();
        Part3.removeAllViews();
        Part4.removeAllViews();

        dem = 0;
        while (dem < keys.length) {
            if (dem < 1) {
                addView(Part1, keys[dem], editText);
            } else if (dem < 2) {
                addView(Part2, keys[dem], editText);
            } else if (dem < 3) {
                addView(Part3, keys[dem], editText);
            } else {
                addView(Part4, keys[dem], editText);
            }
            dem++;
        }
    }
}
