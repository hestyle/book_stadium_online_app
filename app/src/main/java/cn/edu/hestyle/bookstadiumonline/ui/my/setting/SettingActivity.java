package cn.edu.hestyle.bookstadiumonline.ui.my.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SettingActivity extends BaseActivity {
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // 设置navigationBar
        this.navigationBarInit();
        // 设置goto ServerSettingActivity
        TextView serverSettingTextView = findViewById(R.id.serverSettingTextView);
        serverSettingTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, ServerSettingActivity.class);
            startActivity(intent);
        });
        // 设置logout
        this.logoutButton = findViewById(R.id.logoutButton);
        this.logoutButton.setOnClickListener(v -> {
            // 弹窗提示
            AlertDialog alertDialog = new AlertDialog.Builder(SettingActivity.this)
                    .setTitle("提示信息")
                    .setMessage("您确定要注销登录吗？")
                    .setNegativeButton("取消", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton("退出", (dialog, which) -> {
                        dialog.dismiss();
                        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/user/logout.do", null, null, new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                runOnUiThread(() -> {
                                    Toast.makeText(SettingActivity.this, "网络错误，登录注销失败！", Toast.LENGTH_LONG).show();
                                });
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                String responseString = response.body().string();
                                Gson gson = new Gson();
                                Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                                final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                                runOnUiThread(() -> {
                                    Toast.makeText(SettingActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                                if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                                    // 注销成功，清除账号信息，隐藏退出登录按钮
                                    LoginUserInfoUtil.update(null);
                                    runOnUiThread(() -> {
                                        SettingActivity.this.logoutButton.setVisibility(View.INVISIBLE);
                                    });
                                }
                            }
                        });
                    })
                    .create();
            alertDialog.show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 如果已经登录，则显示退出登录按钮
        if (LoginUserInfoUtil.getLoginUser() == null) {
            this.logoutButton.setVisibility(View.INVISIBLE);
        } else {
            this.logoutButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit() {
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText("设置");
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}