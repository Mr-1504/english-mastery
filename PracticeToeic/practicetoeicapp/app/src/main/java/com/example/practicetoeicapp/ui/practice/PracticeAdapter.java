package com.example.practicetoeicapp.ui.practice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicetoeicapp.R;

import java.util.List;

public class PracticeAdapter extends RecyclerView.Adapter<PracticeAdapter.PracticeViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PracticeItem item);
    }

    private final List<PracticeItem> items;
    private final OnItemClickListener listener;

    public PracticeAdapter(List<PracticeItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PracticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_practice, parent, false);
        return new PracticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PracticeViewHolder holder, int position) {
        PracticeItem item = items.get(position);
        holder.icon.setImageResource(item.getIconRes());
        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PracticeViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;
        final TextView subtitle;

        PracticeViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.practiceIcon);
            title = itemView.findViewById(R.id.practiceTitle);
            subtitle = itemView.findViewById(R.id.practiceSubtitle);
        }
    }
}
