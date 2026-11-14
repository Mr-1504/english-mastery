package com.example.practicetoeicapp.admin;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.practicetoeicapp.R;

import androidx.appcompat.app.AppCompatActivity;

public class AdminListController {

    public final ListView listView;
    private final AppCompatActivity activity;
    private final ImageButton addButton;
    private final ProgressBar progressBar;
    private final TextView emptyState;
    private final int emptyMessageRes;

    public AdminListController(AppCompatActivity activity, int titleRes, int subtitleRes,
                               boolean showAdd, int emptyMessageRes) {
        this.activity = activity;
        this.emptyMessageRes = emptyMessageRes;
        ((TextView) activity.findViewById(R.id.tvAdminTitle)).setText(titleRes);
        ((TextView) activity.findViewById(R.id.tvAdminSubtitle)).setText(subtitleRes);
        listView = activity.findViewById(R.id.listviewAdmin);
        addButton = activity.findViewById(R.id.imgAddAdminList);
        progressBar = activity.findViewById(R.id.progressBar);
        emptyState = activity.findViewById(R.id.emptyState);
        addButton.setVisibility(showAdd ? View.VISIBLE : View.GONE);
        emptyState.setText(emptyMessageRes);
    }

    public void bindBack(View.OnClickListener listener) {
        activity.findViewById(R.id.imgBackAdminList).setOnClickListener(listener);
    }

    public void bindAdd(View.OnClickListener listener) {
        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener(listener);
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
    }

    public void showContent(boolean empty) {
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setText(emptyMessageRes);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    public void showError() {
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        emptyState.setText(R.string.admin_list_error);
        emptyState.setVisibility(View.VISIBLE);
    }
}
