package com.example.practicetoeicapp.admin.sapxepcau;

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
import com.example.practicetoeicapp.sapxepcau.CauSapXep;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminSapXepCauAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<CauSapXep> list;
    private final QuestionRepository repository;

    public AdminSapXepCauAdapter(Context context, ArrayList<CauSapXep> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_sapxepcau, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CauSapXep cauSapXep = list.get(position);
        holder.title.setText(cauSapXep.getDapan());
        holder.edit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditSapXepCauActivity.class);
            intent.putExtra("ID_SXC", cauSapXep.getIdcau());
            context.startActivity(intent);
        });
        holder.delete.setOnClickListener(v -> confirmDelete(cauSapXep));
        return convertView;
    }

    private void confirmDelete(CauSapXep cauSapXep) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.admin_delete_title)
                .setMessage(R.string.admin_delete_message)
                .setNegativeButton(R.string.admin_cancel, null)
                .setPositiveButton(R.string.admin_delete_confirm, (dialog, which) ->
                        repository.deleteSapXepCau(cauSapXep.getIdcau(),
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        UiFeedback.showTransient((Activity) context,
                                                context.getString(R.string.admin_delete_success), true);
                                        reload(cauSapXep.getIdbo());
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
        repository.loadSapXepCau(idBo, new QuestionRepository.DataCallback<List<CauSapXep>>() {
            @Override
            public void onSuccess(List<CauSapXep> data) {
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
            title = view.findViewById(R.id.tvTenSapXepCau);
            edit = view.findViewById(R.id.imgEditSXC);
            delete = view.findViewById(R.id.imgDeleteSXC);
        }
    }
}
