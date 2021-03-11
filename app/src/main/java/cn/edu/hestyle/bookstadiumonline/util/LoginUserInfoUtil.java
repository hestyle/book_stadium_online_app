package cn.edu.hestyle.bookstadiumonline.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.edu.hestyle.bookstadiumonline.entity.User;

public class LoginUserInfoUtil {
    private static final String LOGIN_USER_INFO_NAME = "LOGIN_ACCOUNT_INFO_NAME";
    private static final String LOGIN_USER_JSON_STRING = "LOGIN_USER_JSON_STRING";
    private static User loginUser;
    private static Context context;

    static {
        LoginUserInfoUtil.loginUser = null;
    }

    /**
     * 从SharedPreferences读取loginUser信息
     * @param context   context
     */
    public static void init(Context context) {
        LoginUserInfoUtil.context = context;
        // 从SharedPreferences读取loginUser信息
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginUserInfoUtil.LOGIN_USER_INFO_NAME, Context.MODE_PRIVATE);
        String loginUserJsonString = sharedPreferences.getString(LoginUserInfoUtil.LOGIN_USER_JSON_STRING, null);
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
        // 如果传入loginUser == null，说明是退出登录
        if (loginUser != null) {
            LoginUserInfoUtil.writeToLocalStore(loginUser);
        } else {
            LoginUserInfoUtil.removeFromLocalStore();
        }
    }

    /**
     * 将loginUser写入SharedPreferences
     * @param loginUser loginUser
     */
    private static void writeToLocalStore(User loginUser) {
        // 转成json string
        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
        String loginUserJsonString = gson.toJson(loginUser);
        // 写入SharedPreferences
        SharedPreferences sharedPreferences = LoginUserInfoUtil.context.getSharedPreferences(LOGIN_USER_INFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LOGIN_USER_JSON_STRING, loginUserJsonString);
        if (editor.commit()) {
            Log.i("LoginUser", "登录账号信息保存成功！loginUser = " + loginUser);
        } else {
            Log.w("LoginUser", "登录账号信息保存失败！loginUser = " + loginUser);
        }
    }

    /**
     * 将SharedPreferences中的loginUser清除
     */
    private static void removeFromLocalStore() {
        SharedPreferences sharedPreferences = LoginUserInfoUtil.context.getSharedPreferences(LOGIN_USER_INFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(LOGIN_USER_JSON_STRING);
        if (editor.commit()) {
            Log.i("LoginUser", "已成功清除登录账号信息！");
        } else {
            Log.w("LoginUser", "清除登录账号信息失败！");
        }
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
