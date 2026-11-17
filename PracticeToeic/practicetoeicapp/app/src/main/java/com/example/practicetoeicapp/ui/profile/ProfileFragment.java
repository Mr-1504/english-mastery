package com.example.practicetoeicapp.ui.profile;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.practicetoeicapp.BuildConfig;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.admin.AdminActivity;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.taikhoan.LoginActivity;
import com.example.practicetoeicapp.taikhoan.RankingActivity;
import com.example.practicetoeicapp.taikhoan.ThongTinTaikhoanActivity;
import com.example.practicetoeicapp.theme.ThemePrefs;
import com.example.practicetoeicapp.ui.UiFeedback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String DATABASE_NAME = "HocNgonNgu.db";
    private static final String FEEDBACK_EMAIL = "21102003tai@gmail.com";
    private DatabaseAccess DB;
    private SQLiteDatabase database;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfilePoint;
    private TextView tvProfileRole;
    private TextView tvRole;
    private TextView tvThemeValue;
    private TextView profileAvatar;
    private ImageView imgProfileAvatar;
    private TextView tvAppVersion;
    private TextView tvBuildTime;
    private View rowAdmin;
    private View dividerAdmin;
    private int role;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        DB = DatabaseAccess.getInstance(requireContext().getApplicationContext());

        tvProfileName = root.findViewById(R.id.tvProfileName);
        tvProfileEmail = root.findViewById(R.id.tvProfileEmail);
        tvProfilePoint = root.findViewById(R.id.tvProfilePoint);
        tvProfileRole = root.findViewById(R.id.tvProfileRole);
        tvRole = root.findViewById(R.id.tvRole);
        tvThemeValue = root.findViewById(R.id.tvThemeValue);
        profileAvatar = root.findViewById(R.id.profileAvatar);
        imgProfileAvatar = root.findViewById(R.id.imgProfileAvatar);
        tvAppVersion = root.findViewById(R.id.tvAppVersion);
        tvBuildTime = root.findViewById(R.id.tvBuildTime);
        rowAdmin = root.findViewById(R.id.rowAdmin);
        dividerAdmin = root.findViewById(R.id.dividerAdmin);

        root.findViewById(R.id.rowAccount).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ThongTinTaikhoanActivity.class)));
        root.findViewById(R.id.rowRanking).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RankingActivity.class)));
        root.findViewById(R.id.rowTheme).setOnClickListener(v ->
                ThemePrefs.showThemeDialog(requireContext()));
        root.findViewById(R.id.rowFeedback).setOnClickListener(v -> sendFeedback());
        rowAdmin.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AdminActivity.class)));
        root.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());

        bindBuildInfo();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateThemeSummary();
        loadUser();
    }

    private void updateThemeSummary() {
        if (tvThemeValue == null) {
            return;
        }
        String label = ThemePrefs.getModeLabel(requireContext(), ThemePrefs.getMode(requireContext()));
        tvThemeValue.setText(getString(R.string.theme_current, label));
    }

    private void bindBuildInfo() {
        if (tvAppVersion != null) {
            tvAppVersion.setText(getString(R.string.profile_version,
                    BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        }
        if (tvBuildTime != null) {
            tvBuildTime.setText(getString(R.string.profile_build_time, BuildConfig.BUILD_TIME));
        }
    }

    private void loadUser() {
        if (DB == null || DB.iduser == null || DB.iduser.isEmpty()) {
            return;
        }
        database = Database.initDatabase(requireActivity(), DATABASE_NAME);
        Cursor cursor = database.rawQuery(
                "SELECT HoTen, Point, Email, Role FROM User WHERE ID_User = ?",
                new String[]{DB.iduser});
        String name = "Bạn";
        int point = 0;
        String email = "";
        role = 1;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
            point = cursor.getInt(1);
            email = cursor.getString(2);
            role = cursor.getInt(3);
        }
        cursor.close();

        String displayName = name == null || name.trim().isEmpty() ? "Bạn" : name.trim();
        String displayEmail = email == null || email.trim().isEmpty() ? "Chưa cập nhật email" : email.trim();
        tvProfileName.setText(displayName);
        tvProfileEmail.setText(displayEmail);
        tvProfilePoint.setText(String.valueOf(point));
        String initial = displayName.substring(0, 1).toUpperCase(Locale.getDefault());
        profileAvatar.setText(initial);
        loadAvatar(initial);

        String roleLabel = role == 0
                ? getString(R.string.profile_admin)
                : getString(R.string.profile_member);
        tvRole.setText(roleLabel);
        tvProfileRole.setText(roleLabel);

        boolean isAdmin = role == 0;
        rowAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        dividerAdmin.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void loadAvatar(String initial) {
        if (DB == null || DB.iduser == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User")
                .child(DB.iduser).child("avatarUrl");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!isAdded() || profileAvatar == null || imgProfileAvatar == null) return;
                String url = snapshot.getValue(String.class);
                if (url != null && !url.trim().isEmpty()) {
                    profileAvatar.setVisibility(View.GONE);
                    imgProfileAvatar.setVisibility(View.VISIBLE);
                    Glide.with(ProfileFragment.this).load(url).circleCrop().into(imgProfileAvatar);
                } else {
                    imgProfileAvatar.setVisibility(View.GONE);
                    profileAvatar.setVisibility(View.VISIBLE);
                    profileAvatar.setText(initial);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_dialog_title)
                .setMessage(R.string.logout_dialog_message)
                .setNegativeButton(R.string.logout_cancel, null)
                .setPositiveButton(R.string.profile_logout, (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.email_chooser)));
        } catch (Exception exception) {
            UiFeedback.showTransient(requireActivity(), exception.getMessage(), false);
        }
    }
}
