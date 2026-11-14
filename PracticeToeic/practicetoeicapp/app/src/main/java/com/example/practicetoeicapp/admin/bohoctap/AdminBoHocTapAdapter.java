package com.example.practicetoeicapp.admin.bohoctap;

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
import com.example.practicetoeicapp.bohoctap.BoHocTap;
import com.example.practicetoeicapp.data.QuestionRepository;

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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_bohoctap, null);
        TextView txtTenBo = view.findViewById(R.id.tvTenBo);
        ImageView imgEdit = view.findViewById(R.id.imgEditBHT);
        ImageView imgDelete = view.findViewById(R.id.imgDeleteBHT);
        BoHocTap bht = list.get(position);
        txtTenBo.setText(bht.getTenBo());
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditBoHocTapActivity.class);
                intent.putExtra("ID_BHT", bht.getIdBo());
                context.startActivity(intent);
            }
        });
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn chắc chắn muốn xóa bộ học tập này?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                repository.deleteBoHocTap(bht.getIdBo(),
                                        new QuestionRepository.DataCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void data) {
                                                Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                                reload();
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
                Toast.makeText(context, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
