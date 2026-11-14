package com.example.practicetoeicapp.hoctuvung;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.bumptech.glide.Glide;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.luyennghe.MediaPlayerUtils;
import com.example.practicetoeicapp.taikhoan.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordMattersActivity extends AppCompatActivity {

    final String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;

    private int presCounter = 0;
    private String[] keys = {"a", "b", "c", "d", "e", "f", "g",
            "v", "w", "x", "y", "z"};

    private MediaPlayer mediaPlayer;
    String URL = "";
    String UL = "hello";

    private String textAnswer = "ENGLISH";
    private int maxPresCounter = 0;
    private TextView textScreen, textQuestion, textTitle;
    private TextView tvWordCount, tvScore;
    private ImageButton ListenTV;
    private ImageView imgview;
    private Button btnquit, btnback;
    private LinearProgressIndicator progressHTV;
    private ArrayList<TuVung> DStuvung;
    private Animation smallbigforth;
    private int idbo;
    private int score = 0;
    private int dem;
    private int tu = 1;
    private QuestionRepository repository;
    User user;
    Animation smalltobig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_matters);
        smalltobig = AnimationUtils.loadAnimation(WordMattersActivity.this, R.anim.smalltobig);

        DB = DatabaseAccess.getInstance(getApplicationContext());
        repository = QuestionRepository.getInstance();
        AnhXa();
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnquit.performClick();
            }
        });
        LayUser();

        Intent intent = getIntent();
        idbo = intent.getIntExtra("idbo", 0);
        DStuvung = new ArrayList<>();
        loadTuVung();
    }

    private void loadTuVung() {
        repository.loadTuVung(idbo, new QuestionRepository.DataCallback<List<TuVung>>() {
            @Override
            public void onSuccess(List<TuVung> data) {
                DStuvung.clear();
                DStuvung.addAll(data);
                if (DStuvung.isEmpty()) {
                    Toast.makeText(WordMattersActivity.this,
                            "Nội dung sẽ cập nhật cập nhật trong thời gian sớm nhất! Mong mọi người thông càm!!",
                            Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(WordMattersActivity.this, HocTuVungActivity.class));
                } else {
                    startWordGame();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(WordMattersActivity.this,
                        "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startWordGame() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

        showWord(0);
        textQuestion.setText("(" + DStuvung.get(0).getLoaitu() + ") - " + "(" + DStuvung.get(0).getDichnghia() + ")");
        textAnswer = DStuvung.get(0).getDapan();
        updateHeader();
        URL = DStuvung.get(0).getAudio();

        smallbigforth = AnimationUtils.loadAnimation(this, R.anim.smallbigforth);

        keys = new String[textAnswer.length()];
        for (int i = 0; i < textAnswer.length(); i++) {
            keys[i] = String.valueOf(textAnswer.charAt(i));
        }

        keys = shuffleArray(keys);

        dem = 0;
        while (dem < keys.length) {
            if (dem < 4) {
                addView(((LinearLayout) findViewById(R.id.layoutParent1)), keys[dem], ((EditText) findViewById(R.id.editText)));
            } else if (dem < 8) {
                addView(((LinearLayout) findViewById(R.id.layoutParent2)), keys[dem], ((EditText) findViewById(R.id.editText)));
            } else {
                addView(((LinearLayout) findViewById(R.id.layoutParent3)), keys[dem], ((EditText) findViewById(R.id.editText)));
            }
            dem++;
        }

        ListenTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStop();
                if (UL.equals(URL)) {
                    MediaPlayerUtils.playURLMedia(WordMattersActivity.this, mediaPlayer, UL);
                } else {
                    UL = URL;
                    MediaPlayerUtils.playURLMedia(WordMattersActivity.this, mediaPlayer, URL);
                }
            }
        });

        btnquit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(WordMattersActivity.this, HocTuVungActivity.class));
            }
        });

        maxPresCounter = textAnswer.length();
    }

    private void showWord(int position) {
        Glide.with(this)
                .load(DStuvung.get(position).getAnhUrl())
                .into(imgview);
        imgview.startAnimation(smalltobig);
    }

    private void AnhXa() {
        textQuestion = findViewById(R.id.textQuestion);
        imgview = findViewById(R.id.imgview);
        textScreen = findViewById(R.id.textScreen);
        textTitle = findViewById(R.id.textTitle);
        tvWordCount = findViewById(R.id.tvWord);
        tvScore = findViewById(R.id.tvScore);
        ListenTV = findViewById(R.id.ListenTuVung);
        btnquit = findViewById(R.id.btnQuitHTV);
        btnback = findViewById(R.id.btnBackHTV);
        progressHTV = findViewById(R.id.progressHTV);
    }

    public void LayUser() {
        database = Database.initDatabase(WordMattersActivity.this, DATABASE_NAME);
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
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView.setMinWidth(dp(48));
        textView.setMinHeight(dp(48));
        textView.setPadding(dp(12), dp(10), dp(12), dp(10));

        textView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (presCounter < maxPresCounter) {
                    if (presCounter == 0)
                        editText.setText("");

                    editText.setText(editText.getText().toString() + text);
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
        int total = DStuvung.size();
        tvWordCount.setText(getString(R.string.vocab_progress, Math.min(tu, total), total));
        tvScore.setText(getString(R.string.quiz_score, score));
        progressHTV.setProgressCompat(total == 0 ? 0 : Math.min(tu, total) * 100 / total, true);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()));
    }

    private void doValidate() {
        presCounter = 0;

        EditText editText = findViewById(R.id.editText);
        LinearLayout linearLayout1 = findViewById(R.id.layoutParent1);
        LinearLayout linearLayout2 = findViewById(R.id.layoutParent2);
        LinearLayout linearLayout3 = findViewById(R.id.layoutParent3);

        if (editText.getText().toString().equals(textAnswer)) {
            if (tu == DStuvung.size()) {
                score += 5;
                DB.capnhatdiem(DB.iduser, user.getPoint(), score);
                Toast.makeText(WordMattersActivity.this, "Hoàn Thành Xuất Sắc!!~(^.^)~", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(WordMattersActivity.this, FinishHTVActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("questiontrue", tu);
                intent.putExtra("qcount", DStuvung.size());
                startActivity(intent);
                finish();
                return;
            } else {
                if (tu > 0) doStop();
                Toast.makeText(WordMattersActivity.this, "Chính xác!!(^.^)", Toast.LENGTH_SHORT).show();
                showWord(tu);
                textQuestion.setText("(" + DStuvung.get(tu).getLoaitu() + ") - " + "(" + DStuvung.get(tu).getDichnghia() + ")");
                textAnswer = DStuvung.get(tu).getDapan();
                URL = DStuvung.get(tu).getAudio();
                for (int i = 0; i < textAnswer.length(); i++) {
                    keys[i] = String.valueOf(textAnswer.charAt(i));
                }
                maxPresCounter = textAnswer.length();
                editText.setText("");
                score += 5;
                tu++;
                updateHeader();
            }
        } else {
            Toast.makeText(WordMattersActivity.this, "Sai rồi!!(T.T)", Toast.LENGTH_SHORT).show();
            editText.setText("");
        }

        keys = shuffleArray(keys);
        linearLayout1.removeAllViews();
        linearLayout2.removeAllViews();
        linearLayout3.removeAllViews();

        dem = 0;
        while (dem < keys.length) {
            if (dem < 4) {
                addView(linearLayout1, keys[dem], editText);
            } else if (dem < 8) {
                addView(linearLayout2, keys[dem], editText);
            } else {
                addView(linearLayout3, keys[dem], editText);
            }
            dem++;
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
