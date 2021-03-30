package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import cn.edu.hestyle.bookstadiumonline.LoginActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.User;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.SettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyFragment extends Fragment {
    private View rootView;
    private ConstraintLayout noLoginConstraintLayout;
    private ConstraintLayout loginConstraintLayout;
    private ImageView avatarImageView;
    private TextView usernameTextView;
    private TextView scoreTextView;
    private SmartRefreshLayout myFragmentSmartRefreshLayout;


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
        this.loginConstraintLayout.setOnClickListener(v -> {
            User loginUser = LoginUserInfoUtil.getLoginUser();
            if (loginUser != null) {
                // 跳转到账号详情页面
                Intent intent = new Intent(MyFragment.this.getContext(), MyAccountDetailActivity.class);
                startActivity(intent);
            } else {
                // 跳转到登录页面
                Toast.makeText(MyFragment.this.getContext(), "请先进行登录！", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MyFragment.this.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        this.avatarImageView = this.rootView.findViewById(R.id.avatarImageView);
        this.usernameTextView = this.rootView.findViewById(R.id.usernameTextView);
        this.scoreTextView = this.rootView.findViewById(R.id.scoreTextView);
        this.myFragmentSmartRefreshLayout = this.rootView.findViewById(R.id.myFragmentSmartRefreshLayout);
        myFragmentSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                // 刷新(登录账号信息)
                MyFragment.this.getInfo();
            }
        });

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

        // 查看我的动态action
        ConstraintLayout myMomentConstraintLayout = this.rootView.findViewById(R.id.myMomentConstraintLayout);
        myMomentConstraintLayout.setOnClickListener(v -> {
            User loginUser = LoginUserInfoUtil.getLoginUser();
            // 判断是否登录
            if (loginUser == null || loginUser.getId() == null) {
                Toast.makeText(MyFragment.this.getContext(), "请先进行登录！", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MyFragment.this.getContext(), MySportMomentActivity.class);
                startActivity(intent);
            }
        });

        // 查看运动常识action
        ConstraintLayout sportKnowledgeConstraintLayout = this.rootView.findViewById(R.id.sportKnowledgeConstraintLayout);
        sportKnowledgeConstraintLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MyFragment.this.getContext(), SportKnowledgeListActivity.class);
            startActivity(intent);
        });

        return this.rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    /**
     * 页面初始化
     */
    private void init() {
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

    private void getInfo() {
        User user = LoginUserInfoUtil.getLoginUser();
        if (user != null) {
            // 从服务器获取登录账号信息
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/user/getInfo.do", null, null, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    MyFragment.this.getActivity().runOnUiThread(()->{
                        Toast.makeText(MyFragment.this.getContext(), "网络访问失败！", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // 转json
                    Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                    Type type =  new TypeToken<ResponseResult<User>>(){}.getType();
                    final ResponseResult<User> responseResult = gson.fromJson(responseString, type);
                    if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                        // 获取数据失败
                        MyFragment.this.getActivity().runOnUiThread(()->{
                            if (responseResult.getCode().equals(ResponseResult.TOKEN_VERIFICATION_FAILED_Code)) {
                                // token未通过验证(退出登录)
                                LoginUserInfoUtil.update(null);
                                MyFragment.this.init();
                            }
                            Toast.makeText(MyFragment.this.getContext(), responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                            myFragmentSmartRefreshLayout.finishRefresh();
                        });
                        return;
                    }
                    User loginUser = responseResult.getData();
                    Log.i("MyFragment", "loginUser = " + loginUser);
                    MyFragment.this.getActivity().runOnUiThread(()->{
                        LoginUserInfoUtil.update(loginUser);
                        MyFragment.this.init();
                        myFragmentSmartRefreshLayout.finishRefresh();
                    });
                }
            });
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
        leftSettingImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingActivity.class);
            startActivity(intent);
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
        rightAnnouncementImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SystemNoticeListActivity.class);
            startActivity(intent);
        });
        // 添加到common_title
        commonTitleConstraintLayout.addView(leftSettingImageButton);
        commonTitleConstraintLayout.addView(rightAnnouncementImageButton);
    }
}