package com.example.practicetoeicapp.ui.practice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.dienkhuyet.DienKhuyetActivity;
import com.example.practicetoeicapp.hoctuvung.HocTuVungActivity;
import com.example.practicetoeicapp.luyennghe.LuyenNgheActivity;
import com.example.practicetoeicapp.sapxepcau.SapXepCauActivity;
import com.example.practicetoeicapp.taikhoan.RankingActivity;
import com.example.practicetoeicapp.tracnghiem.TracNghiemActivity;

import java.util.Arrays;
import java.util.List;

public class PracticeFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_practice, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.practiceGrid);
        int spanCount = getResources().getInteger(R.integer.practice_grid_span);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));

        List<PracticeItem> items = Arrays.asList(
                new PracticeItem(R.drawable.ic_vocab,
                        getString(R.string.practice_vocab),
                        getString(R.string.practice_vocab_desc),
                        HocTuVungActivity.class),
                new PracticeItem(R.drawable.ic_quiz,
                        getString(R.string.practice_quiz),
                        getString(R.string.practice_quiz_desc),
                        TracNghiemActivity.class),
                new PracticeItem(R.drawable.ic_fill,
                        getString(R.string.practice_fill),
                        getString(R.string.practice_fill_desc),
                        DienKhuyetActivity.class),
                new PracticeItem(R.drawable.ic_order,
                        getString(R.string.practice_order),
                        getString(R.string.practice_order_desc),
                        SapXepCauActivity.class),
                new PracticeItem(R.drawable.ic_listen,
                        getString(R.string.practice_listen),
                        getString(R.string.practice_listen_desc),
                        LuyenNgheActivity.class),
                new PracticeItem(R.drawable.ic_rank,
                        getString(R.string.practice_rank),
                        getString(R.string.practice_rank_desc),
                        RankingActivity.class)
        );

        PracticeAdapter adapter = new PracticeAdapter(items, item -> {
            if (item.getTarget() != null) {
                startActivity(new Intent(requireContext(), item.getTarget()));
            }
        });
        recyclerView.setAdapter(adapter);
        return root;
    }
}
