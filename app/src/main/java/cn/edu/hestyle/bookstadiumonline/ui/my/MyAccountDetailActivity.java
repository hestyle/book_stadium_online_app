package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import okhttp3.FormBody;
import okhttp3.Response;

public class MyAccountDetailActivity extends BaseActivity {
    private static final Integer RESULT_CAMERA_IMAGE = 1;
    private static final Integer RESULT_LOAD_IMAGE = 2;
    /** 性别：男 */
    private static final String USER_GENDER_MAN = "男";
    /** 性别：女 */
    private static final String USER_GENDER_WOMAN = "女";
    /** 性别：保密 */
    private static final String USER_GENDER_SECRECY = "保密";
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

    private String uploadingFilePath;

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
            // 修改密码弹窗
            MyAccountDetailActivity.this.showModifyPasswordPopueWindow();
        });
        userAvatarConstraintLayout.setOnClickListener(v -> {
            // 修改头像弹窗
            MyAccountDetailActivity.this.showUploadImagePopueWindow();
        });
        genderConstraintLayout.setOnClickListener(v -> {
            // 修改性别弹窗
            MyAccountDetailActivity.this.showModifyGenderPopueWindow();
        });
        addressConstraintLayout.setOnClickListener(v -> {
            Toast.makeText(MyAccountDetailActivity.this, "点击了修改地址！", Toast.LENGTH_SHORT).show();
        });
        phoneNumberConstraintLayout.setOnClickListener(v -> {
            // 修改电话号码
            MyAccountDetailActivity.this.showModifyPhoneNumberPopueWindow();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK ) {
            if (requestCode == RESULT_LOAD_IMAGE && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                uploadingFilePath = cursor.getString(columnIndex);
                cursor.close();
                Log.i("UserSportMoment", "已选中图片 imagePath = " + uploadingFilePath);
                // 上传图片
                File imageFile = new File(uploadingFilePath);
                uploadImageToServer(imageFile);
            } else if (requestCode == RESULT_CAMERA_IMAGE){
                Log.i("UserSportMoment", "已选中(拍照)图片 imagePath = " + uploadingFilePath);
                // 上传图片
                File imageFile = new File(uploadingFilePath);
                uploadImageToServer(imageFile);
            }
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
     * 修改密码
     */
    private void showModifyPasswordPopueWindow() {
        View popView = View.inflate(this, R.layout.popue_window_user_modify_password,null);
        EditText passwordEditText = popView.findViewById(R.id.passwordEditText);
        EditText newPasswordEditText = popView.findViewById(R.id.newPasswordEditText);
        EditText reNewPasswordEditText = popView.findViewById(R.id.reNewPasswordEditText);
        TextView cancelActionTextView = popView.findViewById(R.id.cancelActionTextView);
        TextView saveActionTextView = popView.findViewById(R.id.saveActionTextView);
        // 获取屏幕宽高
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        final PopupWindow popupWindow = new PopupWindow(popView, width, height);
        popupWindow.setAnimationStyle(R.style.MaterialAlertDialog_MaterialComponents_Title_Panel);
        popupWindow.setFocusable(true);
        // 点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);
        // 取消（关闭窗口）
        cancelActionTextView.setOnClickListener(v -> popupWindow.dismiss());
        // 保存修改
        saveActionTextView.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString();
            if (password.length() == 0) {
                Toast.makeText(MyAccountDetailActivity.this, "请输入原密码！", Toast.LENGTH_SHORT).show();
                return;
            }
            String newPassword = newPasswordEditText.getText().toString();
            if (newPassword.length() == 0) {
                Toast.makeText(MyAccountDetailActivity.this, "请输入新密码！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(reNewPasswordEditText.getText().toString())) {
                Toast.makeText(MyAccountDetailActivity.this, "两次输入的密码不一致！", Toast.LENGTH_SHORT).show();
                return;
            }
            FormBody formBody = new FormBody.Builder()
                    .add("password", password)
                    .add("newPassword", newPassword)
                    .build();
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/user/modifyPassword.do", null, formBody, new Callback() {
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
                    Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                    final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                    MyAccountDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(MyAccountDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            // 保存成功，则隐藏弹窗
                            popupWindow.dismiss();
                        }
                    });
                }
            });
        });
        // popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 1.0f;
            getWindow().setAttributes(lp);
        });
        // popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.CENTER,0,0);
    }

    /**
     * 选择上传图片弹窗
     */
    private void showUploadImagePopueWindow() {
        View popView = View.inflate(this, R.layout.popue_window_upload_image,null);
        TextView bt_album = popView.findViewById(R.id.btn_pop_album);
        TextView bt_camera = popView.findViewById(R.id.btn_pop_camera);
        TextView bt_cancel = popView.findViewById(R.id.btn_pop_cancel);
        // 获取屏幕宽高
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels / 3;

        final PopupWindow popupWindow = new PopupWindow(popView, weight, height);
        popupWindow.setAnimationStyle(R.style.MaterialAlertDialog_MaterialComponents_Title_Panel);
        popupWindow.setFocusable(true);
        // 点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);
        bt_camera.setOnClickListener(v -> {
            takeCamera(RESULT_CAMERA_IMAGE);
            popupWindow.dismiss();
        });
        bt_album.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
            popupWindow.dismiss();
        });
        bt_cancel.setOnClickListener(v -> popupWindow.dismiss());
        // popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 1.0f;
            getWindow().setAttributes(lp);
        });
        // popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,0);
    }

    /**
     * 将图片上传至服务器
     * @param imageFile     待上传图片文件
     */
    private void uploadImageToServer(File imageFile) {
        OkHttpUtil.uploadFile(ServerSettingActivity.getServerBaseUrl() + "/user/uploadAvatar.do", imageFile, OkHttpUtil.MEDIA_TYPE_JPG, new Callback() {
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
                Type type =  new TypeToken<ResponseResult<String>>(){}.getType();
                final ResponseResult<String> responseResult = gson.fromJson(responseString, type);
                MyAccountDetailActivity.this.runOnUiThread(()->{
                    Toast.makeText(MyAccountDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                });
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    return;
                }
                String imagePath = responseResult.getData();
                Log.i("User", "文件上传成功！imagePath = " + responseResult.getData() + "");
                MyAccountDetailActivity.this.runOnUiThread(()->{
                    // 更新本地缓存
                    MyAccountDetailActivity.this.loginUser.setAvatarPath(imagePath);
                    LoginUserInfoUtil.update(MyAccountDetailActivity.this.loginUser);
                    Glide.with(MyAccountDetailActivity.this)
                            .load(ServerSettingActivity.getServerHostUrl() + imagePath)
                            .into(MyAccountDetailActivity.this.userAvatarImageView);
                });
            }
        });
    }

    /**
     * 调用相机，拍照
     * @param num
     */
    private void takeCamera(int num) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, num);//跳转界面传回拍照所得数据
            } else {
                Toast.makeText(getApplicationContext(), "发生未知错误！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "相机不可用！", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = null;
        try {
            image = File.createTempFile(
                    generateFileName(),  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        uploadingFilePath = image.getAbsolutePath();
        return image;
    }

    public static String generateFileName() {
        String timeStamp = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT).format(new Date());
        return "JPEG_" + timeStamp + "_";
    }

    /**
     * 修改性别
     */
    private void showModifyGenderPopueWindow() {
        View popView = View.inflate(this, R.layout.popue_window_user_modify_gender,null);
        TextView manTextView = popView.findViewById(R.id.manTextView);
        TextView womanTextView = popView.findViewById(R.id.womanTextView);
        TextView secrecyTextView = popView.findViewById(R.id.secrecyTextView);
        TextView cancelActionTextView = popView.findViewById(R.id.cancelActionTextView);
        // 获取屏幕宽高
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels / 3;

        final PopupWindow popupWindow = new PopupWindow(popView, width, height);
        popupWindow.setAnimationStyle(R.style.MaterialAlertDialog_MaterialComponents_Title_Panel);
        popupWindow.setFocusable(true);
        // 点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);
        // 取消（关闭窗口）
        cancelActionTextView.setOnClickListener(v -> popupWindow.dismiss());
        // 男
        manTextView.setOnClickListener(v -> {
            MyAccountDetailActivity.this.modifyGender(USER_GENDER_MAN, popupWindow);
        });
        // 女
        womanTextView.setOnClickListener(v -> {
            MyAccountDetailActivity.this.modifyGender(USER_GENDER_WOMAN, popupWindow);
        });
        // 保密
        secrecyTextView.setOnClickListener(v -> {
            MyAccountDetailActivity.this.modifyGender(USER_GENDER_SECRECY, popupWindow);
        });
        // popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 1.0f;
            getWindow().setAttributes(lp);
        });
        // popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,20);
    }

    /**
     * 修改电话号码
     */
    private void showModifyPhoneNumberPopueWindow() {
        View popView = View.inflate(this, R.layout.popue_window_user_modify_phone_number,null);
        EditText phoneNumberEditText = popView.findViewById(R.id.phoneNumberEditText);
        TextView cancelActionTextView = popView.findViewById(R.id.cancelActionTextView);
        TextView saveActionTextView = popView.findViewById(R.id.saveActionTextView);

        phoneNumberEditText.setText(String.format("%s", loginUser.getPhoneNumber()));

        // 获取屏幕宽高
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        final PopupWindow popupWindow = new PopupWindow(popView, width, height);
        popupWindow.setAnimationStyle(R.style.MaterialAlertDialog_MaterialComponents_Title_Panel);
        popupWindow.setFocusable(true);
        // 点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);
        // 取消（关闭窗口）
        cancelActionTextView.setOnClickListener(v -> popupWindow.dismiss());
        // 保存修改
        saveActionTextView.setOnClickListener(v -> {
            String phoneNumber = phoneNumberEditText.getText().toString();
            if (phoneNumber.length() == 0) {
                Toast.makeText(MyAccountDetailActivity.this, "请输入电话号码！", Toast.LENGTH_SHORT).show();
                return;
            }
            FormBody formBody = new FormBody.Builder()
                    .add("phoneNumber", phoneNumber)
                    .build();
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/user/modifyPhoneNumber.do", null, formBody, new Callback() {
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
                    Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                    final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                    MyAccountDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(MyAccountDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            // 保存成功，则隐藏弹窗
                            popupWindow.dismiss();
                            // 更新本地缓存
                            MyAccountDetailActivity.this.loginUser.setPhoneNumber(phoneNumber);
                            MyAccountDetailActivity.this.phoneNumberTextView.setText(phoneNumber);
                            LoginUserInfoUtil.update(MyAccountDetailActivity.this.loginUser);
                        }
                    });
                }
            });
        });
        // popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 1.0f;
            getWindow().setAttributes(lp);
        });
        // popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.CENTER,0,0);
    }

    /**
     * 修改性别
     * @param gender            性别
     * @param popupWindow       修改性别的弹窗
     */
    private void modifyGender(String gender, PopupWindow popupWindow) {
        FormBody formBody = new FormBody.Builder()
                .add("gender", gender)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/user/modifyGender.do", null, formBody, new Callback() {
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
                Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                MyAccountDetailActivity.this.runOnUiThread(()->{
                    Toast.makeText(MyAccountDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                        // 修改成功，则隐藏弹窗
                        popupWindow.dismiss();
                        // 更新本地缓存
                        MyAccountDetailActivity.this.loginUser.setGender(gender);
                        MyAccountDetailActivity.this.genderTextView.setText(gender);
                        LoginUserInfoUtil.update(MyAccountDetailActivity.this.loginUser);
                    }
                });
            }
        });
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