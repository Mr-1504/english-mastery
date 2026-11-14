package com.example.practicetoeicapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.practicetoeicapp.admin.AdminActivity;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.taikhoan.LoginActivity;
import com.example.practicetoeicapp.taikhoan.User;
import com.example.practicetoeicapp.ui.UiFeedback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String DATABASE_NAME = "HocNgonNgu.db";
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private SQLiteDatabase database;
    private DatabaseAccess DB;
    private User user;
    private boolean doubleBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DB = DatabaseAccess.getInstance(getApplicationContext());
        resolveCurrentUser();
        if (DB.iduser == null || DB.iduser.isEmpty()) {
            goToLogin();
            return;
        }
        LayUser();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            return;
        }
        navController = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_practice, R.id.nav_profile)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);
        bottomNav.setOnItemReselectedListener(item -> {
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (navController.getCurrentDestination() != null
                        && navController.getCurrentDestination().getId()
                        != navController.getGraph().getStartDestination()) {
                    navController.navigateUp();
                    return;
                }
                if (doubleBack) {
                    moveTaskToBack(true);
                    return;
                }
                doubleBack = true;
                Toast.makeText(getApplicationContext(), "Nhấn lần nữa để thoát", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> doubleBack = false, 2000);
            }
        });
    }

    private void resolveCurrentUser() {
        if (DB.iduser != null && !DB.iduser.isEmpty()) {
            return;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DB.iduser = firebaseUser.getUid();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void LayUser() {
        database = Database.initDatabase(this, DATABASE_NAME);
        Cursor cursor = database.rawQuery("SELECT * FROM User WHERE ID_User = ?",
                new String[]{DB.iduser});
        if (cursor.moveToFirst()) {
            String idUser = cursor.getString(0);
            String hoTen = cursor.getString(1);
            int point = cursor.getInt(2);
            String email = cursor.getString(3);
            String sdt = cursor.getString(4);
            int role = cursor.getInt(5);
            user = new User(idUser, hoTen, point, email, sdt, role);
        } else {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String email = firebaseUser != null && firebaseUser.getEmail() != null
                    ? firebaseUser.getEmail() : "";
            user = new User(DB.iduser, "Bạn", 0, email, "", 1);
        }
        cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem adminItem = menu.findItem(R.id.action_admin);
        if (adminItem != null) {
            adminItem.setVisible(user != null && user.getRole() == 0);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_admin) {
            if (user != null && user.getRole() == 0) {
                startActivity(new Intent(this, AdminActivity.class));
            } else {
                UiFeedback.showError(this, getString(R.string.admin_role_denied));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && NavigationUI.navigateUp(navController, appBarConfiguration))
                || super.onSupportNavigateUp();
    }
}
