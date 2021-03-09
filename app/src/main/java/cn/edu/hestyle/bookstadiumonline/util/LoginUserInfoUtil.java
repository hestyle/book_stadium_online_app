package cn.edu.hestyle.bookstadiumonline.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.edu.hestyle.bookstadiumonline.entity.User;

public class LoginUserInfoUtil {
    private static final String LOGIN_USER_INFO_NAME = "LOGIN_ACCOUNT_INFO_NAME";
    private static final String LOGIN_USER = "LOGIN_USER";
    private static User loginUser;

    static {
        LoginUserInfoUtil.loginUser = null;
    }

    /**
     * 从SharedPreferences读取loginUser信息
     * @param context   context
     */
    public static void init(Context context) {
        // 从SharedPreferences读取loginUser信息
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginUserInfoUtil.LOGIN_USER_INFO_NAME, Context.MODE_PRIVATE);
        String loginUserJsonString = sharedPreferences.getString(LoginUserInfoUtil.LOGIN_USER, null);
        if (loginUserJsonString == null || loginUserJsonString.length() == 0) {
            LoginUserInfoUtil.loginUser = null;
        } else {
            // json转user
            Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
            try {
                LoginUserInfoUtil.loginUser = gson.fromJson(loginUserJsonString, User.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新loginUser
     * @param loginUser loginUser
     */
    public static void update(User loginUser) {
        LoginUserInfoUtil.loginUser = loginUser;
    }

    public static User getLoginUser() {
        return LoginUserInfoUtil.loginUser;
    }

    public static String getToken() {
        if (LoginUserInfoUtil.loginUser != null) {
            return LoginUserInfoUtil.loginUser.getToken();
        } else {
            return null;
        }
    }
}
