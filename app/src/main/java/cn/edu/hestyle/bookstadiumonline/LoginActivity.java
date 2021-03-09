package cn.edu.hestyle.bookstadiumonline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import cn.edu.hestyle.bookstadiumonline.entity.User;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 设置navigationBar
        this.navigationBarInit();
        // 设置注册跳转
        this.registerActionInit();
        this.usernameEditText = findViewById(R.id.usernameEditText);
        this.passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            // 检查表单
            FormBody formBody = LoginActivity.this.fromCheck();
            if (formBody == null) {
                return;
            }
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/user/login.do", null, formBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录失败，发生网络错误！", Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // 转json
                    Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                    Type type =  new TypeToken<ResponseResult<User>>(){}.getType();
                    final ResponseResult<User> responseResult = gson.fromJson(responseString, type);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        if (responseResult.getCode() == 200) {
                            LoginActivity.this.finish();
                        }
                    });
                }
            });
        });
    }

    /**
     * 检查登录表单
     * @return      登录表单
     */
    private FormBody fromCheck() {
        String username = this.usernameEditText.getText().toString();
        if (username.length() == 0) {
            Toast.makeText(LoginActivity.this, "请输入登录账号的用户名！", Toast.LENGTH_SHORT).show();
            return null;
        }
        String password = this.passwordEditText.getText().toString();
        if (password.length() == 0) {
            Toast.makeText(LoginActivity.this, "请输入登录账号的密码！", Toast.LENGTH_SHORT).show();
            return null;
        }
        return new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
    }

    /**
     * 设置注册跳转button
     */
    private void registerActionInit() {
        TextView registerTextView = this.findViewById(R.id.registerTextView);
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit() {
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText("登录");
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