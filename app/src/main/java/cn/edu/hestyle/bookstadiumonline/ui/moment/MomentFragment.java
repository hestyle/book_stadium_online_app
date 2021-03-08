package cn.edu.hestyle.bookstadiumonline.ui.moment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.edu.hestyle.bookstadiumonline.R;

public class MomentFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_moment, container, false);
        TextView textView = root.findViewById(R.id.text_moment);
        textView.setText("moment");
        return root;
    }
}