package com.example.practicetoeicapp.admin.tuvung;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.ui.UiFeedback;
import com.example.practicetoeicapp.hoctuvung.TuVung;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminTuVungAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<TuVung> list;
    private final QuestionRepository repository;

    public AdminTuVungAdapter(Context context, ArrayList<TuVung> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_tuvung, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TuVung tuVung = list.get(position);
        holder.title.setText(tuVung.getDapan());
        holder.edit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTuVungActivity.class);
            intent.putExtra("ID_TV", tuVung.getIdtu());
            context.startActivity(intent);
        });
        holder.delete.setOnClickListener(v -> confirmDelete(tuVung));
        return convertView;
    }

    private void confirmDelete(TuVung tuVung) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.admin_delete_title)
                .setMessage(R.string.admin_delete_message)
                .setNegativeButton(R.string.admin_cancel, null)
                .setPositiveButton(R.string.admin_delete_confirm, (dialog, which) ->
                        repository.deleteTuVung(tuVung.getIdtu(),
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        UiFeedback.showTransient((Activity) context,
                                                context.getString(R.string.admin_delete_success), true);
                                        reload(tuVung.getIdbo());
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        UiFeedback.showTransient((Activity) context,
                                                context.getString(R.string.admin_delete_error), false);
                                    }
                                }))
                .show();
    }

    private void reload(int idBo) {
        repository.loadTuVung(idBo, new QuestionRepository.DataCallback<List<TuVung>>() {
            @Override
            public void onSuccess(List<TuVung> data) {
                list.clear();
                list.addAll(data);
                notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                UiFeedback.showTransient((Activity) context,
                        context.getString(R.string.admin_list_error), false);
            }
        });
    }

    private static class ViewHolder {
        final TextView title;
        final ImageView edit;
        final ImageView delete;

        ViewHolder(View view) {
            title = view.findViewById(R.id.tvTenTuVung);
            edit = view.findViewById(R.id.imgEditTV);
            delete = view.findViewById(R.id.imgDeleteTV);
        }
    }
}
