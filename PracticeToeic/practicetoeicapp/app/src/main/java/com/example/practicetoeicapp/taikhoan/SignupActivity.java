
package com.example.practicetoeicapp.taikhoan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.ui.UiFeedback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignupActivity extends AppCompatActivity {

    TextView tvDangNhap;
    EditText edtHoTen,edtEmail,edtSdt,edtMatKhau,edtXacNhan;
    Button btnSignUp;
    FirebaseAuth mAuth;

    FirebaseDatabase rootNode;
    DatabaseReference reference;
    DatabaseAccess DB;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        AnhXa();
        mAuth = FirebaseAuth.getInstance();

        DB =  DatabaseAccess.getInstance(getApplicationContext());
        tvDangNhap = (TextView) findViewById(R.id.textView_login);
        tvDangNhap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hoten = edtHoTen.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String sdt = edtSdt.getText().toString().trim();
                String matkhau = edtMatKhau.getText().toString().trim();
                String xacnhanmatkhau = edtXacNhan.getText().toString().trim();

                if(hoten.equals("")||email.equals("")||sdt.equals("")||matkhau.equals(""))
                {
                    UiFeedback.showTransient(SignupActivity.this,
                            getString(R.string.auth_signup_required), false);
                }
                else{
                    if(matkhau.equals(xacnhanmatkhau)){

                        Boolean kiemtrataikhoan = DB.checktaikhoan(email);
                        if(kiemtrataikhoan == false)
                        {
                            loadingDialog = UiFeedback.showLoading(SignupActivity.this,
                                    R.string.feedback_loading);
                            mAuth.createUserWithEmailAndPassword(email, matkhau).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task)
                                        {
                                            if (task.isSuccessful()) {
                                                DB.open();
                                                Boolean insert = DB.insertData(mAuth.getCurrentUser().getUid(),hoten,email,sdt,0, 1);
                                                DB.close();

                                                // if the user created intent to login activity
                                                rootNode= FirebaseDatabase.getInstance();
                                                reference= rootNode.getReference("User");
                                                User newuser = new User(mAuth.getCurrentUser().getUid(), hoten,0,email,sdt, 1);
                                                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newuser);

                                                UiFeedback.dismiss(loadingDialog);
                                                UiFeedback.showTransient(SignupActivity.this,
                                                        getString(R.string.auth_signup_success), true);
                                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                            else {
                                                UiFeedback.dismiss(loadingDialog);
                                                UiFeedback.showError(SignupActivity.this,
                                                        getString(R.string.auth_signup_failed));
                                            }
                                        }
                                    });
                        }
                        else{
                            UiFeedback.showTransient(SignupActivity.this,
                                    getString(R.string.auth_email_exists), false);
                        }


                    }
                    else{
                        UiFeedback.showTransient(SignupActivity.this,
                                getString(R.string.auth_password_mismatch), false);
                        edtMatKhau.setText("");
                        edtXacNhan.setText("");
                    }
                }
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        UiFeedback.dismiss(loadingDialog);
    }

    private void AnhXa()
    {
        tvDangNhap = (TextView) findViewById(R.id.textView_login);
        edtHoTen = (EditText) findViewById(R.id.editTextEmailNav);
        edtEmail = (EditText) findViewById(R.id.editTextEmail);
        edtSdt = (EditText) findViewById(R.id.editTextSdt);
        edtMatKhau= (EditText) findViewById(R.id.editTextMatKhau);
        edtXacNhan = (EditText) findViewById(R.id.editTextXacNhan);
        btnSignUp = (Button) findViewById(R.id.buttonSignUp);
    }
}