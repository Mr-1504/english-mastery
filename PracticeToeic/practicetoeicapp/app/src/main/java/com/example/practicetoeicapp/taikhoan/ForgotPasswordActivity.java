package com.example.practicetoeicapp.taikhoan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.ui.UiFeedback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText EmailF;
    private Button btnResetP;
    private TextView tvDangNhap;

    FirebaseAuth mAuth;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        EmailF = (EditText) findViewById(R.id.etEmailForgot);
        btnResetP = (Button) findViewById(R.id.btnResetPass);
        tvDangNhap = findViewById(R.id.textView_login);

        mAuth = FirebaseAuth.getInstance();

        btnResetP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        tvDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void resetPassword(){
        String email = EmailF.getText().toString().trim();

        if(email.isEmpty()){
            EmailF.setError("Hãy nhập Email của bạn!");
            EmailF.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            EmailF.setError("Hãy nhập đúng Email!");
            EmailF.requestFocus();
            return;
        }

        loadingDialog = UiFeedback.showLoading(this, R.string.feedback_loading);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                UiFeedback.dismiss(loadingDialog);
                if(task.isSuccessful()){
                    UiFeedback.showTransient(ForgotPasswordActivity.this,
                            getString(R.string.auth_reset_success), true);
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    UiFeedback.showError(ForgotPasswordActivity.this,
                            getString(R.string.auth_reset_failed));
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UiFeedback.dismiss(loadingDialog);
    }
}