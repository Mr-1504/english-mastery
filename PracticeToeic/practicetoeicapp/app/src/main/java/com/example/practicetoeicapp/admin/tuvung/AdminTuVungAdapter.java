package com.example.practicetoeicapp.admin.tuvung;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.practicetoeicapp.hoctuvung.TuVung;

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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_tuvung, null);
        TextView txtTenTuVung = view.findViewById(R.id.tvTenTuVung);
        ImageView imgEdit = view.findViewById(R.id.imgEditTV);
        ImageView imgDelete = view.findViewById(R.id.imgDeleteTV);
        TuVung tv = list.get(position);
        txtTenTuVung.setText(tv.getDapan());
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditTuVungActivity.class);
                intent.putExtra("ID_TV", tv.getIdtu());
                context.startActivity(intent);
            }
        });
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn chắc chắn muốn xóa từ vựng này?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                repository.deleteTuVung(tv.getIdtu(),
                                        new QuestionRepository.DataCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void data) {
                                                Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                                reload(tv.getIdbo());
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Không", null)
                        .create()
                        .show();
            }
        });
        return view;
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
                Toast.makeText(context, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
