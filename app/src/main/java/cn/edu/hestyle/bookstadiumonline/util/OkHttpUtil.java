package cn.edu.hestyle.bookstadiumonline.util;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtil {
    private static OkHttpClient httpClient = new OkHttpClient();

    private static String cookieString = "";

    /**
     * post 请求
     * @param url           url
     * @param headersMap    附加请求头请求头map
     * @param formBody      请求体
     * @param callback      回调
     */
    public static void post(String url, HashMap<String, String> headersMap, FormBody formBody, Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (headersMap != null && headersMap.size() != 0) {
            // 拼接附加请求头
            for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        // 后台服务器的url
        if (url.startsWith(ServerSettingActivity.getServerBaseUrl())) {
            // 添加cookie
            if (cookieString.length() != 0) {
                requestBuilder.addHeader("Cookie", "JSESSIONID=" + OkHttpUtil.cookieString);
            }
            // 添加token
            String token = LoginUserInfoUtil.getToken();
            if (token != null) {
                requestBuilder.addHeader("token", token);
            }
        }


        Request request = requestBuilder.post(formBody).build();
        Call call = OkHttpUtil.httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                callback.onResponse(call, response);
                // 保存cookie
                OkHttpUtil.updateCookie(response);
            }
        });
    }

    private static void updateCookie(Response response) {
        // 后台服务器的url
        if (response.request().url().toString().startsWith(ServerSettingActivity.getServerBaseUrl())) {
            // 截取cookie中的JSESSIONID
            String cookieString = response.headers("Set-Cookie").toString();
            cookieString = cookieString.substring(cookieString.indexOf("JSESSIONID=") + 11);
            cookieString = cookieString.substring(0, cookieString.indexOf(";"));
            OkHttpUtil.cookieString = cookieString;
            Log.i("Cookie", "Cookie 更新！Cookie = " + cookieString);
        }

    }
}
