package com.example.practicetoeicapp.admin.bohoctap;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminBoHocTapAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<BoHocTap> list;
    private final QuestionRepository repository;

    public AdminBoHocTapAdapter(Context context, ArrayList<BoHocTap> list) {
        this.context = context;
        this.list = list;
        this.repository = QuestionRepository.getInstance();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_bohoctap, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BoHocTap boHocTap = list.get(position);
        holder.title.setText(boHocTap.getTenBo());
        holder.edit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditBoHocTapActivity.class);
            intent.putExtra("ID_BHT", boHocTap.getIdBo());
            context.startActivity(intent);
        });
        holder.delete.setOnClickListener(v -> confirmDelete(boHocTap));
        return convertView;
    }

    private void confirmDelete(BoHocTap boHocTap) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.admin_delete_title)
                .setMessage(R.string.admin_delete_message)
                .setNegativeButton(R.string.admin_cancel, null)
                .setPositiveButton(R.string.admin_delete_confirm, (dialog, which) ->
                        repository.deleteBoHocTap(boHocTap.getIdBo(),
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        Toast.makeText(context, R.string.admin_delete_success,
                                                Toast.LENGTH_SHORT).show();
                                        reload();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(context, R.string.admin_delete_error,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }))
                .show();
    }

    private void reload() {
        repository.loadBoHocTap(new QuestionRepository.DataCallback<List<BoHocTap>>() {
            @Override
            public void onSuccess(List<BoHocTap> data) {
                list.clear();
                list.addAll(data);
                notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(context, R.string.admin_list_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class ViewHolder {
        final TextView title;
        final ImageView edit;
        final ImageView delete;

        ViewHolder(View view) {
            title = view.findViewById(R.id.tvTenBo);
            edit = view.findViewById(R.id.imgEditBHT);
            delete = view.findViewById(R.id.imgDeleteBHT);
        }
    }
}
