package com.example.practicetoeicapp.admin.dienkhuyet;

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
import com.example.practicetoeicapp.dienkhuyet.CauDienKhuyet;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminDienKhuyetAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<CauDienKhuyet> list;
    private final QuestionRepository repository;

    public AdminDienKhuyetAdapter(Context context, ArrayList<CauDienKhuyet> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.row_dienkhuyet, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CauDienKhuyet cauDienKhuyet = list.get(position);
        holder.title.setText(cauDienKhuyet.getNoidung());
        holder.edit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditDienKhuyetActivity.class);
            intent.putExtra("ID_DK", cauDienKhuyet.getIdcau());
            context.startActivity(intent);
        });
        holder.delete.setOnClickListener(v -> confirmDelete(cauDienKhuyet));
        return convertView;
    }

    private void confirmDelete(CauDienKhuyet cauDienKhuyet) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.admin_delete_title)
                .setMessage(R.string.admin_delete_message)
                .setNegativeButton(R.string.admin_cancel, null)
                .setPositiveButton(R.string.admin_delete_confirm, (dialog, which) ->
                        repository.deleteDienKhuyet(cauDienKhuyet.getIdcau(),
                                new QuestionRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        Toast.makeText(context, R.string.admin_delete_success,
                                                Toast.LENGTH_SHORT).show();
                                        reload(cauDienKhuyet.getIdbo());
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
        repository.loadDienKhuyet(idBo, new QuestionRepository.DataCallback<List<CauDienKhuyet>>() {
            @Override
            public void onSuccess(List<CauDienKhuyet> data) {
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
            title = view.findViewById(R.id.tvTenDienKhuyet);
            edit = view.findViewById(R.id.imgEditDK);
            delete = view.findViewById(R.id.imgDeleteDK);
        }
    }
}
