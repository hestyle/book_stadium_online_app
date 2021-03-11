package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.edu.hestyle.bookstadiumonline.R;

public class BookFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_book, container, false);
        TextView titleTextView = root.findViewById(R.id.titleTextView);
        titleTextView.setText("搜索体育场馆");
        return root;
    }
}