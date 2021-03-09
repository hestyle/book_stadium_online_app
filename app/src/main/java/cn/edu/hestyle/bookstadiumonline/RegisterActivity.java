package cn.edu.hestyle.bookstadiumonline;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class RegisterActivity extends BaseActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText rePasswordEditText;
    private RadioButton manRadioButton;
    private EditText phoneNumberEditText;
    private EditText addressEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // 设置navigationBar
        this.navigationBarInit();
        this.usernameEditText = findViewById(R.id.usernameEditText);
        this.passwordEditText = findViewById(R.id.passwordEditText);
        this.rePasswordEditText = findViewById(R.id.rePasswordEditText);
        this.manRadioButton = findViewById(R.id.manRadioButton);
        this.phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        this.addressEditText = findViewById(R.id.addressEditText);
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            // 检查、生成表单
            FormBody formBody = RegisterActivity.this.formCheck();
            if (formBody == null) {
                return;
            }
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/user/register.do", null, formBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "注册失败，发生网络错误！", Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // 转json
                    Gson gson = new Gson();
                    Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                    final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    /**
     * 检查注册表单是否完成
     * @return      表单
     */
    private FormBody formCheck() {
        String username = this.usernameEditText.getText().toString();
        if (username.length() == 0) {
            Toast.makeText(RegisterActivity.this, "请输入注册账号的用户名！", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (username.length() > 20) {
            Toast.makeText(RegisterActivity.this, "用户名过长，超过了20字符！", Toast.LENGTH_SHORT).show();
            return null;
        }
        String password = this.passwordEditText.getText().toString();
        if (password.length() == 0) {
            Toast.makeText(RegisterActivity.this, "请输入注册账号的密码！", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (password.length() > 20) {
            Toast.makeText(RegisterActivity.this, "密码过长，超过了20字符！", Toast.LENGTH_SHORT).show();
            return null;
        }
        String rePassword = this.rePasswordEditText.getText().toString();
        if (!password.equals(rePassword)) {
            Toast.makeText(RegisterActivity.this, "两次输入的密码不一致！", Toast.LENGTH_SHORT).show();
            return null;
        }
        String phoneNumber = this.phoneNumberEditText.getText().toString();
        Pattern pattern = Pattern.compile("^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$");
        Matcher matcher = pattern.matcher(phoneNumber);
        if (!matcher.matches()) {
            Toast.makeText(RegisterActivity.this, "电话号码非法，请输入正确的电话号码！", Toast.LENGTH_SHORT).show();
            return null;
        }
        String address = this.addressEditText.getText().toString();
        if (address.length() == 0) {
            Toast.makeText(RegisterActivity.this, "请输入您的住址！", Toast.LENGTH_SHORT).show();
            return null;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("gender", manRadioButton.isChecked() ? "男" : "女");
        jsonObject.addProperty("phoneNumber", phoneNumber);
        jsonObject.addProperty("address", address);
        return new FormBody.Builder()
                .add("userData", jsonObject.toString())
                .build();
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit() {
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText("注册");
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