package com.example.practicetoeicapp.admin.luyennghe;

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
import com.example.practicetoeicapp.data.QuestionRepository;
import com.example.practicetoeicapp.luyennghe.CauLuyenNghe;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminLuyenNgheAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<CauLuyenNghe> list;
    private final QuestionRepository repository;

    public AdminLuyenNgheAdapter(Context context, ArrayList<CauLuyenNghe> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_luyennghe, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CauLuyenNghe cauLuyenNghe = list.get(position);
        holder.title.setText(context.getString(R.string.admin_listen_item, cauLuyenNghe.getIdbai()));
        holder.edit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditLuyenNgheActivity.class);
            intent.putExtra("ID_LN", cauLuyenNghe.getIdbai());
            context.startActivity(intent);
        });
        holder.delete.setOnClickListener(v -> confirmDelete(cauLuyenNghe));
        return convertView;
    }

    private void confirmDelete(CauLuyenNghe cauLuyenNghe) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.admin_delete_title)
                .setMessage(R.string.admin_delete_message)
                .setNegativeButton(R.string.admin_cancel, null)
                .setPositiveButton(R.string.admin_delete_confirm, (dialog, which) ->
                        repository.deleteLuyenNghe(cauLuyenNghe.getIdbai(),
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        Toast.makeText(context, R.string.admin_delete_success,
                                                Toast.LENGTH_SHORT).show();
                                        reload(cauLuyenNghe.getIdbo());
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(context, R.string.admin_delete_error,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }))
                .show();
    }

    private void reload(int idBo) {
        repository.loadLuyenNghe(idBo, new QuestionRepository.DataCallback<List<CauLuyenNghe>>() {
            @Override
            public void onSuccess(List<CauLuyenNghe> data) {
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
            title = view.findViewById(R.id.tvTenLuyenNghe);
            edit = view.findViewById(R.id.imgEditLN);
            delete = view.findViewById(R.id.imgDeleteLN);
        }
    }
}
