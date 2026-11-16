package com.example.practicetoeicapp.ui.home;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.database.Database;
import com.example.practicetoeicapp.database.DatabaseAccess;
import com.example.practicetoeicapp.hoctuvung.HocTuVungActivity;
import com.example.practicetoeicapp.taikhoan.RankingActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String DATABASE_NAME = "HocNgonNgu.db";
    private DatabaseAccess DB;
    private SQLiteDatabase database;
    private TextView tvGreeting;
    private TextView tvAvatar;
    private ImageView imgAvatar;
    private TextView tvPoint;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        DB = DatabaseAccess.getInstance(requireContext().getApplicationContext());

        tvGreeting = root.findViewById(R.id.tvGreeting);
        tvAvatar = root.findViewById(R.id.tvAvatar);
        imgAvatar = root.findViewById(R.id.imgAvatar);
        tvPoint = root.findViewById(R.id.tvPoint);

        root.findViewById(R.id.btnStartPractice).setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.nav_practice));
        root.findViewById(R.id.cardVocabulary).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), HocTuVungActivity.class)));
        root.findViewById(R.id.cardRank).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), RankingActivity.class)));
        root.findViewById(R.id.cardAccount).setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.nav_profile));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindUser();
    }

    private void bindUser() {
        if (DB == null || DB.iduser == null || DB.iduser.isEmpty()) {
            return;
        }
        database = Database.initDatabase(requireActivity(), DATABASE_NAME);
        Cursor cursor = database.rawQuery("SELECT HoTen, Point FROM User WHERE ID_User = ?",
                new String[]{DB.iduser});
        String name = "bạn";
        int point = 0;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
            point = cursor.getInt(1);
        }
        cursor.close();

        String displayName = name == null ? "bạn" : name.trim();
        if (displayName.isEmpty()) {
            displayName = "bạn";
        }
        String firstName = displayName.split("\\s+")[0];
        tvGreeting.setText(getString(R.string.learn_greeting, firstName));
        tvPoint.setText(String.valueOf(point));
        String initial = firstName.substring(0, 1).toUpperCase(Locale.getDefault());
        tvAvatar.setText(initial);
        loadAvatar(initial);
    }

    private void loadAvatar(String initial) {
        if (DB == null || DB.iduser == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User")
                .child(DB.iduser).child("avatarUrl");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!isAdded() || imgAvatar == null || tvAvatar == null) return;
                String url = snapshot.getValue(String.class);
                if (url != null && !url.trim().isEmpty()) {
                    tvAvatar.setVisibility(View.GONE);
                    imgAvatar.setVisibility(View.VISIBLE);
                    Glide.with(HomeFragment.this).load(url).circleCrop().into(imgAvatar);
                } else {
                    imgAvatar.setVisibility(View.GONE);
                    tvAvatar.setVisibility(View.VISIBLE);
                    tvAvatar.setText(initial);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}
