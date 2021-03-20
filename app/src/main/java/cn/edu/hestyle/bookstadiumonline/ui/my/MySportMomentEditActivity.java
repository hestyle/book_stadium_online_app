package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import cn.edu.hestyle.bookstadiumonline.entity.UserSportMoment;
import cn.edu.hestyle.bookstadiumonline.ui.moment.UserSportMomentAddActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class MySportMomentEditActivity extends UserSportMomentAddActivity {
    private UserSportMoment userSportMoment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.navigationBarInit("编辑动态");

        Intent intent = getIntent();
        userSportMoment = (UserSportMoment) intent.getSerializableExtra("UserSportMoment");

        super.saveButton.setOnClickListener(v -> {
            FormBody formBody = MySportMomentEditActivity.this.checkForm();
            if (formBody != null) {
                // 提交评论
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/modify.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        MySportMomentEditActivity.this.runOnUiThread(()->{
                            Toast.makeText(MySportMomentEditActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // 转json
                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                        Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                        final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                        MySportMomentEditActivity.this.runOnUiThread(()->{
                            Toast.makeText(MySportMomentEditActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            // 保存成功，则返回
                            MySportMomentEditActivity.this.finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userSportMoment != null) {
            // 显示userSportMoment信息
            super.contentEditText.setText(String.format("%s", userSportMoment.getContent()));
            String imagePathString = userSportMoment.getImagePaths();
            if (imagePathString != null && imagePathString.length() != 0) {
                String[] imagePaths = imagePathString.split(",");
                for (String imagePath : imagePaths) {
                    super.uploadImage(imagePath);
                }
            }
        } else {
            Toast.makeText(this, "程序内部发生错误！", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 检查表单
     * @return      FormBody
     */
    @Override
    protected FormBody checkForm() {
        // 检查content
        String content = contentEditText.getText().toString();
        if (content.length() == 0) {
            Toast.makeText(this, "请输入运动动态的文字内容！", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (content.length() > SPORT_MOMENT_CONTENT_MAX_LENGTH) {
            Toast.makeText(this, "运动动态的文字超过了 " + SPORT_MOMENT_CONTENT_MAX_LENGTH + " 个字符！", Toast.LENGTH_SHORT).show();
            return null;
        }
        // 检查imagePaths
        StringBuilder imagePaths = new StringBuilder();
        for (String imagePath : userSportMomentImagePathList) {
            if (imagePaths.length() != 0) {
                imagePaths.append(",");
            }
            imagePaths.append(imagePath);
        }
        UserSportMoment modifyUserSportMoment = new UserSportMoment();
        modifyUserSportMoment.setUserId(this.userSportMoment.getUserId());
        modifyUserSportMoment.setSportMomentId(this.userSportMoment.getSportMomentId());
        modifyUserSportMoment.setContent(content);
        modifyUserSportMoment.setImagePaths(imagePaths.toString());
        // 转json
        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
        String userSportMomentData = gson.toJson(modifyUserSportMoment);
        return new FormBody.Builder()
                .add("userSportMomentData", userSportMomentData)
                .build();
    }
}