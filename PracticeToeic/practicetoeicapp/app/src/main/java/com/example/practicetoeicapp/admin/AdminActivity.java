package com.example.practicetoeicapp.admin;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practicetoeicapp.MainActivity;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.bohoctap.AdminBoHocTapActivity;
import com.example.practicetoeicapp.admin.dienkhuyet.AdminBoDienKhuyetActivity;
import com.example.practicetoeicapp.admin.luyennghe.AdminBoLuyenNgheActivity;
import com.example.practicetoeicapp.admin.sapxepcau.AdminBoSapXepCauActivity;
import com.example.practicetoeicapp.admin.tracnghiem.AdminBoTracNghiemActivity;
import com.example.practicetoeicapp.admin.tuvung.AdminBoTuVungActivity;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.taikhoan.LoginActivity;
import com.example.practicetoeicapp.taikhoan.ThongTinTaikhoanActivity;
import com.example.practicetoeicapp.taikhoan.User;
import com.example.practicetoeicapp.theme.ThemePrefs;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private static final String DATABASE_NAME = "HocNgonNgu.db";

    private DatabaseAccess databaseAccess;
    private boolean doubleBack;
    private User user;
    private TextView tvAdminGreeting;
    private TextView tvAdminAvatar;
    private TextView tvAdminThemeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        tvAdminGreeting = findViewById(R.id.tvAdminGreeting);
        tvAdminAvatar = findViewById(R.id.tvAdminAvatar);
        tvAdminThemeValue = findViewById(R.id.tvAdminThemeValue);

        bindHeader();
        bindContentCards();
        bindQuickActions();
        setupBackHandling();

        if (!loadUser() || user.getRole() != 0) {
            showRoleDenied();
            return;
        }
        showGreeting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tvAdminThemeValue != null) {
            tvAdminThemeValue.setText(getString(R.string.theme_current,
                    ThemePrefs.getModeLabel(this, ThemePrefs.getMode(this))));
        }
    }

    private void bindHeader() {
        findViewById(R.id.btnThemeAdmin).setOnClickListener(v -> ThemePrefs.showThemeDialog(this));
        findViewById(R.id.btnLogoutAdmin).setOnClickListener(v -> confirmLogout());
    }

    private void bindContentCards() {
        findViewById(R.id.adminCardBoHocTap).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBoHocTapActivity.class)));
        findViewById(R.id.adminCardBoTuVung).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBoTuVungActivity.class)));
        findViewById(R.id.adminCardBoTracNghiem).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBoTracNghiemActivity.class)));
        findViewById(R.id.adminCardBoSapXepCau).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBoSapXepCauActivity.class)));
        findViewById(R.id.adminCardBoLuyenNghe).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBoLuyenNgheActivity.class)));
        findViewById(R.id.adminCardBoDienKhuyet).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBoDienKhuyetActivity.class)));
    }

    private void bindQuickActions() {
        findViewById(R.id.rowAdminTheme).setOnClickListener(v -> ThemePrefs.showThemeDialog(this));
        findViewById(R.id.rowAdminLearning).setOnClickListener(v -> openLearningMode());
        findViewById(R.id.rowAdminAccount).setOnClickListener(v ->
                startActivity(new Intent(this, ThongTinTaikhoanActivity.class)));
    }

    private void setupBackHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBack) {
                    finish();
                    return;
                }
                doubleBack = true;
                Toast.makeText(getApplicationContext(), R.string.admin_press_back_again,
                        Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBack = false, 2000);
            }
        });
    }

    private void showGreeting() {
        String name = user.getHoTen();
        if (name == null || name.trim().isEmpty()) {
            name = getString(R.string.admin_role_chip);
        }
        tvAdminGreeting.setText(getString(R.string.admin_greeting, name));
        tvAdminAvatar.setText(name.substring(0, 1).toUpperCase(Locale.getDefault()));
    }

    private void showRoleDenied() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.admin_role_denied_title)
                .setMessage(R.string.admin_role_denied)
                .setCancelable(false)
                .setPositiveButton(R.string.admin_back_to_learning, (dialog, which) ->
                        openLearningMode())
                .show();
    }

    private void openLearningMode() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_dialog_title)
                .setMessage(R.string.logout_dialog_message)
                .setNegativeButton(R.string.logout_cancel, null)
                .setPositiveButton(R.string.admin_logout, (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean loadUser() {
        SQLiteDatabase database = Database.initDatabase(this, DATABASE_NAME);
        try (Cursor cursor = database.rawQuery(
                "SELECT * FROM User WHERE ID_User = ?",
                new String[]{String.valueOf(databaseAccess.iduser)})) {
            if (!cursor.moveToFirst()) {
                return false;
            }
            user = new User(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5));
            return true;
        }
    }
}
