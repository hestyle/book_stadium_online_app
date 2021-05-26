package cn.edu.hestyle.bookstadiumonline.util;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {
    public static final String MEDIA_TYPE_JSON = "application/json";
    public static final String MEDIA_TYPE_XML = "application/xml";
    public static final String MEDIA_TYPE_PNG = "image/png";
    public static final String MEDIA_TYPE_JPG = "image/jpeg";
    public static final String MEDIA_TYPE_GIF = "image/gif";

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
        if (ServerSettingActivity.getServerIpAddress() == null) {
            // 检查是否设置了服务器地址
            Log.w("OkHttpUtil", "还未设置服务器地址！");
            return;
        }
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
        // 检查是否是空formBody
        if (formBody == null) {
            formBody = new FormBody.Builder().build();
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

    /**
     * 上传文件
     * @param url           url
     * @param file          file
     * @param fileType      fileType
     * @param callback      callback
     */
    public static void uploadFile(String url, File file, String fileType, Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
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
        // 构建fileRequestBody
        RequestBody fileRequestBody = null;
        try {
            fileRequestBody = RequestBody.create(MediaType.parse(fileType), file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fileRequestBody == null) {
            Log.w("OkHttpUtil", "url = " + url + "请求体构造失败！");
            return;
        }
        // 构建MultipartBody
        MultipartBody.Builder multiRequestBuilder = new MultipartBody.Builder();
        multiRequestBuilder.setType(MultipartBody.FORM);
        multiRequestBuilder.addFormDataPart("file", file.getName(), fileRequestBody);
        // 构造request
        RequestBody requestBody = multiRequestBuilder.build();
        Request request = requestBuilder.post(requestBody).build();
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
            if (cookieString.contains("JSESSIONID=")) {
                cookieString = cookieString.substring(cookieString.indexOf("JSESSIONID=") + 11);
                cookieString = cookieString.substring(0, cookieString.indexOf(";"));
                OkHttpUtil.cookieString = cookieString;
                Log.i("Cookie", "Cookie 更新！Cookie = " + cookieString);
            }
        }

    }
}
