package com.example.practicetoeicapp.taikhoan;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.MainActivity;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminActivity;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.ui.UiFeedback;


public class ThongTinTaikhoanActivity extends AppCompatActivity {

    final  String DATABASE_NAME = "HocNgonNgu.db";
    DatabaseAccess DB;
    SQLiteDatabase database;
    ImageView imghome;
    EditText tvHoten,tvEmail,tvSdt;
    TextView tvtaikhoan, tvTen,tvPoint;
    Button btnCapNhat;
    String iduser;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_taikhoan);
        DB = DatabaseAccess.getInstance(getApplicationContext());
        AnhXa();
        iduser = DB.iduser;
        LayUser();

        btnCapNhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CapNhatThongTin();
            }
        });

        imghome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user == null) {
                    UiFeedback.showError(ThongTinTaikhoanActivity.this,
                            getString(R.string.account_update_error));
                    finish();
                    return;
                }
                if(user.getRole() == 0) {
                    Intent intent = new Intent(ThongTinTaikhoanActivity.this, AdminActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent intent
                            = new Intent(ThongTinTaikhoanActivity.this,
                            MainActivity.class);
                    intent.putExtras(getIntent());
                    startActivity(intent);
                    finish();
                }

            }
        });

    }
    private void AnhXa()
    {
        tvHoten = findViewById(R.id.textIntEdtHoten);
        tvEmail = findViewById(R.id.textIntEdtEmail);
        tvSdt = findViewById(R.id.textIntEdtSdt);
        tvtaikhoan = findViewById(R.id.tVusername);
        tvTen = findViewById(R.id.textViewTen);
        tvPoint = findViewById(R.id.textviewPoint);
        btnCapNhat = findViewById(R.id.buttonCapNhat);
        imghome = findViewById(R.id.imgHOME);

        tvEmail.setEnabled(false);

    }
    private void CapNhatThongTin()
    {
        String hoten = tvHoten.getText().toString().trim();
        String sdt = tvSdt.getText().toString().trim();
        if(hoten.isEmpty() || sdt.isEmpty())
        {
            UiFeedback.showTransient(this, getString(R.string.account_update_error), false);
        }
        else{
            Boolean checkupdate = DB.capnhatthongtin(DB.iduser,hoten,sdt);
            if(checkupdate == true)
            {
                UiFeedback.showTransient(this, getString(R.string.account_update_success), true);
            }
            else{
                UiFeedback.showTransient(this, getString(R.string.account_update_error), false);
            }
        }

    }
    private void TruyenThongTin(){
        //Truyền thông tin
        tvHoten.setText(user.getHoTen());
        tvTen.setText(user.getHoTen());
        tvtaikhoan.setText(user.getEmail());
        tvPoint.setText(String.valueOf(user.getPoint()));
        tvEmail.setText(user.getEmail());
        tvSdt.setText(user.getSDT());

    }

    public void LayUser()
    {
        database = Database.initDatabase(ThongTinTaikhoanActivity.this, DATABASE_NAME);
        Cursor cursor = database.rawQuery("SELECT * FROM User WHERE ID_User = ?",new String[]{String.valueOf(DB.iduser)});
        try {
            if (cursor.moveToNext()) {
                String Iduser = cursor.getString(0);
                String HoTen = cursor.getString(1);
                int Point = cursor.getInt(2);
                String Email = cursor.getString(3);
                String SDT = cursor.getString(4);
                int Role = cursor.getInt(5);
                user = new User(Iduser, HoTen, Point, Email, SDT, Role);

                TruyenThongTin();
            }
        } finally {
            cursor.close();
        }

    }



}