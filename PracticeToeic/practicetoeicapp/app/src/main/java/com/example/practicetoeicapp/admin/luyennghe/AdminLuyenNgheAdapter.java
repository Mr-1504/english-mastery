package com.example.practicetoeicapp.admin.luyennghe;

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
import com.example.practicetoeicapp.luyennghe.CauLuyenNghe;

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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_luyennghe, null);
        TextView txtTenLuyenNghe = view.findViewById(R.id.tvTenLuyenNghe);
        ImageView imgEdit = view.findViewById(R.id.imgEditLN);
        ImageView imgDelete = view.findViewById(R.id.imgDeleteLN);
        CauLuyenNghe ln = list.get(position);
        txtTenLuyenNghe.setText("Bài " + ln.getIdbai());
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditLuyenNgheActivity.class);
                intent.putExtra("ID_LN", ln.getIdbai());
                context.startActivity(intent);
            }
        });
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn chắc chắn muốn xóa bài nghe này?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                repository.deleteLuyenNghe(ln.getIdbai(),
                                        new QuestionRepository.DataCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void data) {
                                                Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                                reload(ln.getIdbo());
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
        repository.loadLuyenNghe(idBo, new QuestionRepository.DataCallback<List<CauLuyenNghe>>() {
            @Override
            public void onSuccess(List<CauLuyenNghe> data) {
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
