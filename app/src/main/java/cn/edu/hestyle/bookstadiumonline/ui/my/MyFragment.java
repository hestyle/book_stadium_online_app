package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import cn.edu.hestyle.bookstadiumonline.LoginActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.User;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.SettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;

public class MyFragment extends Fragment {
    private View rootView;
    private ConstraintLayout noLoginConstraintLayout;
    private ConstraintLayout loginConstraintLayout;
    private ImageView avatarImageView;
    private TextView usernameTextView;
    private TextView scoreTextView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_my, container, false);
        // 设置navigationBar
        this.navigationBarInit();
        // 设置"去登录"点击事件
        TextView titleTextView = this.rootView.findViewById(R.id.gotoLoginTextView);
        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        this.noLoginConstraintLayout = this.rootView.findViewById(R.id.noLoginConstraintLayout);
        this.loginConstraintLayout = this.rootView.findViewById(R.id.loginConstraintLayout);
        this.avatarImageView = this.rootView.findViewById(R.id.avatarImageView);
        this.usernameTextView = this.rootView.findViewById(R.id.usernameTextView);
        this.scoreTextView = this.rootView.findViewById(R.id.scoreTextView);

        // 查看我的预约action
        ConstraintLayout myStadiumBookItemConstraintLayout = this.rootView.findViewById(R.id.myStadiumBookItemConstraintLayout);
        myStadiumBookItemConstraintLayout.setOnClickListener(v -> {
            User loginUser = LoginUserInfoUtil.getLoginUser();
            // 判断是否登录
            if (loginUser == null || loginUser.getId() == null) {
                Toast.makeText(MyFragment.this.getContext(), "请先进行登录！", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MyFragment.this.getContext(), MyStadiumBookItemListActivity.class);
                intent.putExtra("userId", loginUser.getId());
                startActivity(intent);
            }
        });

        return this.rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        User loginUser = LoginUserInfoUtil.getLoginUser();
        if (loginUser != null) {
            this.noLoginConstraintLayout.setVisibility(View.INVISIBLE);
            this.loginConstraintLayout.setVisibility(View.VISIBLE);
            String avatarPath = loginUser.getAvatarPath();
            if (avatarPath != null && avatarPath.length() != 0) {
                // 加载网络图片
                Glide.with(MyFragment.this.getActivity())
                        .load(ServerSettingActivity.getServerHostUrl() + loginUser.getAvatarPath())
                        .into(this.avatarImageView);
            }
            this.usernameTextView.setText(loginUser.getUsername());
            this.scoreTextView.setText(String.format("%d", loginUser.getCreditScore()));
        } else {
            this.loginConstraintLayout.setVisibility(View.INVISIBLE);
            this.noLoginConstraintLayout.setVisibility(View.VISIBLE);
            // 设置默认图片
            this.avatarImageView.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit() {
        ConstraintLayout commonTitleConstraintLayout = this.rootView.findViewById(R.id.my_fragment_navigation_bar);
        // 设置title
        TextView titleTextView = this.rootView.findViewById(R.id.titleTextView);
        titleTextView.setText("我的");
        // 设置left setting
        ImageButton leftSettingImageButton = new ImageButton(getActivity());
        leftSettingImageButton.setBackgroundColor(Color.TRANSPARENT);
        leftSettingImageButton.setImageResource(R.drawable.ic_setting_24dp);
        ConstraintLayout.LayoutParams leftSettingLayoutParams = new ConstraintLayout.LayoutParams(56, 56);
        leftSettingLayoutParams.leftMargin = 15;
        leftSettingLayoutParams.startToStart = R.id.my_fragment_navigation_bar;
        leftSettingLayoutParams.topToTop = R.id.my_fragment_navigation_bar;
        leftSettingLayoutParams.bottomToBottom = R.id.my_fragment_navigation_bar;
        leftSettingImageButton.setLayoutParams(leftSettingLayoutParams);
        leftSettingImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
            }
        });
        // 设置right announcement
        ImageButton rightAnnouncementImageButton = new ImageButton(getActivity());
        rightAnnouncementImageButton.setBackgroundColor(Color.TRANSPARENT);
        rightAnnouncementImageButton.setImageResource(R.drawable.ic_announcement_24dp);
        ConstraintLayout.LayoutParams rightAnnouncementLayoutParams = new ConstraintLayout.LayoutParams(56, 56);
        rightAnnouncementLayoutParams.rightMargin = 15;
        rightAnnouncementLayoutParams.endToEnd = R.id.my_fragment_navigation_bar;
        rightAnnouncementLayoutParams.topToTop = R.id.my_fragment_navigation_bar;
        rightAnnouncementLayoutParams.bottomToBottom = R.id.my_fragment_navigation_bar;
        rightAnnouncementImageButton.setLayoutParams(rightAnnouncementLayoutParams);
        // 添加到common_title
        commonTitleConstraintLayout.addView(leftSettingImageButton);
        commonTitleConstraintLayout.addView(rightAnnouncementImageButton);
    }
}