package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

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

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.LoginActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.User;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyAccountDetailActivity extends BaseActivity {
    private User loginUser;
    private SmartRefreshLayout myAccountDetailSmartRefreshLayout;
    private TextView usernameTextView;
    private ConstraintLayout passwordConstraintLayout;
    private ConstraintLayout userAvatarConstraintLayout;
    private ImageView userAvatarImageView;
    private ConstraintLayout genderConstraintLayout;
    private TextView genderTextView;
    private ConstraintLayout addressConstraintLayout;
    private TextView addressTextView;
    private ConstraintLayout phoneNumberConstraintLayout;
    private TextView phoneNumberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_detail);

        myAccountDetailSmartRefreshLayout = findViewById(R.id.myAccountDetailSmartRefreshLayout);
        usernameTextView = findViewById(R.id.usernameTextView);
        passwordConstraintLayout = findViewById(R.id.passwordConstraintLayout);
        userAvatarConstraintLayout = findViewById(R.id.userAvatarConstraintLayout);
        userAvatarImageView = findViewById(R.id.userAvatarImageView);
        genderConstraintLayout = findViewById(R.id.genderConstraintLayout);
        genderTextView = findViewById(R.id.genderTextView);
        addressConstraintLayout = findViewById(R.id.addressConstraintLayout);
        addressTextView = findViewById(R.id.addressTextView);
        phoneNumberConstraintLayout = findViewById(R.id.phoneNumberConstraintLayout);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);

        myAccountDetailSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                MyAccountDetailActivity.this.getInfo();
            }
        });
        passwordConstraintLayout.setOnClickListener(v -> {
            Toast.makeText(MyAccountDetailActivity.this, "点击了修改密码！", Toast.LENGTH_SHORT).show();
        });
        userAvatarConstraintLayout.setOnClickListener(v -> {
            Toast.makeText(MyAccountDetailActivity.this, "点击了修改头像！", Toast.LENGTH_SHORT).show();
        });
        genderConstraintLayout.setOnClickListener(v -> {
            Toast.makeText(MyAccountDetailActivity.this, "点击了修改性别！", Toast.LENGTH_SHORT).show();
        });
        addressConstraintLayout.setOnClickListener(v -> {
            Toast.makeText(MyAccountDetailActivity.this, "点击了修改地址！", Toast.LENGTH_SHORT).show();
        });
        phoneNumberConstraintLayout.setOnClickListener(v -> {
            Toast.makeText(MyAccountDetailActivity.this, "点击了修改电话号码！", Toast.LENGTH_SHORT).show();
        });

        loginUser = LoginUserInfoUtil.getLoginUser();
        navigationBarInit("我的账号信息");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (loginUser != null) {
            init();
        } else {
            // 跳转到登录页面
            Toast.makeText(this, "请先进行登录！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 初始化
     */
    private void init() {
        if (loginUser != null) {
            // 填写信息
            if (loginUser.getUsername() != null) {
                usernameTextView.setText(String.format("%s", loginUser.getUsername()));
            } else {
                usernameTextView.setText("");
            }
            if (loginUser.getAvatarPath() != null && loginUser.getAvatarPath().length() != 0) {
                Glide.with(this)
                        .load(ServerSettingActivity.getServerHostUrl() + loginUser.getAvatarPath())
                        .into(userAvatarImageView);
            } else {
                userAvatarImageView.setImageResource(R.drawable.ic_default_avatar);
            }
            if (loginUser.getGender() != null) {
                genderTextView.setText(String.format("%s", loginUser.getGender()));
            } else {
                genderTextView.setText("未知");
            }
            if (loginUser.getAddress() != null) {
                addressTextView.setText(String.format("%s", loginUser.getAddress()));
            } else {
                addressTextView.setText("未填写");
            }
            if (loginUser.getPhoneNumber() != null) {
                phoneNumberTextView.setText(String.format("%s", loginUser.getPhoneNumber()));
            } else {
                phoneNumberTextView.setText("未填写");
            }
        }
    }

    /**
     * 从服务器获取当前登录账号的信息
     */
    private void getInfo() {
        User user = LoginUserInfoUtil.getLoginUser();
        if (user != null) {
            // 从服务器获取登录账号信息
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/user/getInfo.do", null, null, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    MyAccountDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(MyAccountDetailActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
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
                        MyAccountDetailActivity.this.runOnUiThread(()->{
                            Toast.makeText(MyAccountDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                            MyAccountDetailActivity.this.myAccountDetailSmartRefreshLayout.finishRefresh();
                            if (responseResult.getCode().equals(ResponseResult.TOKEN_VERIFICATION_FAILED_Code)) {
                                // token未通过验证(退出登录)
                                LoginUserInfoUtil.update(null);
                                MyAccountDetailActivity.this.finish();
                            }
                        });
                        return;
                    }
                    User loginUser = responseResult.getData();
                    Log.i("MyFragment", "loginUser = " + loginUser);
                    MyAccountDetailActivity.this.runOnUiThread(()->{
                        LoginUserInfoUtil.update(loginUser);
                        MyAccountDetailActivity.this.init();
                        MyAccountDetailActivity.this.myAccountDetailSmartRefreshLayout.finishRefresh();
                    });
                }
            });
        }
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit(String title) {
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
    }
}