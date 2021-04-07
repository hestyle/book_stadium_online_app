package cn.edu.hestyle.bookstadiumonline;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import cn.edu.hestyle.bookstadiumonline.entity.Announcement;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.ApplicationContextUtil;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import cn.edu.hestyle.bookstadiumonline.util.WebSocketHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends BaseActivity {
    /** 首页公告弹窗，一次app生命周期只弹出一次 */
    private static boolean hasShowedAnnouncement = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_main);
        // 保存ApplicationContext
        ApplicationContextUtil.setApplicationContext(this.getApplicationContext());
        // 判断是否设置了LocalServerSetting
        if (!ServerSettingActivity.isSavedServerSetting(getApplicationContext())) {
            Toast.makeText(MainActivity.this, "请先设置服务器ip地址与端口，否则无法访问！", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, ServerSettingActivity.class);
            startActivity(intent);
        }
        // LoginUserInfoUtil init
        LoginUserInfoUtil.init(this.getApplicationContext());
        // 去除标题栏
        this.getSupportActionBar().hide();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_book, R.id.navigation_moment, R.id.navigation_message, R.id.navigation_my)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String token = LoginUserInfoUtil.getToken();
        if (token != null && token.length() != 0) {
            // 连接websocket
            WebSocketHandler.getInstance(ServerSettingActivity.getServerBaseUrl() + "/webSocket/" + token);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String token = LoginUserInfoUtil.getToken();
        if (token != null && token.length() != 0) {
            // 关闭websocket
            WebSocketHandler.getInstance(ServerSettingActivity.getServerBaseUrl() + "/webSocket/" + token).close();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !MainActivity.hasShowedAnnouncement) {
            // 检查是否弹出过公告
            getAnnouncementFromServer();
            MainActivity.hasShowedAnnouncement = true;
        }
    }

    /**
     * 从服务器获取announcement
     */
    private void getAnnouncementFromServer() {
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/announcement/find.do", null, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                MainActivity.this.runOnUiThread(()->{
                    Toast.makeText(MainActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<Announcement>>(){}.getType();
                final ResponseResult<Announcement> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    MainActivity.this.runOnUiThread(()->{
                        Toast.makeText(MainActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                Announcement announcement = responseResult.getData();
                Log.i("Announcement", announcement + "");
                MainActivity.this.runOnUiThread(()->{
                    if (announcement != null && announcement.getId() != null) {
                        MainActivity.this.showAnnouncementPopueWindow(announcement);
                    }
                });
            }
        });
    }

    /**
     * 首页公告弹窗
     */
    private void showAnnouncementPopueWindow(Announcement announcement) {
        View popView = View.inflate(this, R.layout.popue_window_system_announcement,null);
        TextView titleTextView = popView.findViewById(R.id.titleTextView);
        TextView contentTextView = popView.findViewById(R.id.contentTextView);
        TextView closeActionTextView = popView.findViewById(R.id.closeActionTextView);
        // 获取屏幕宽高
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        final PopupWindow popupWindow = new PopupWindow(popView, width, height);
        popupWindow.setAnimationStyle(R.style.MaterialAlertDialog_MaterialComponents_Title_Panel);
        popupWindow.setFocusable(true);
        // 点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);
        if (announcement != null) {
            titleTextView.setText(String.format("%s", announcement.getTitle()));
            contentTextView.setText(String.format("%s", announcement.getContent()));
        }
        // 取消（关闭窗口）
        closeActionTextView.setOnClickListener(v -> popupWindow.dismiss());
        // popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = this.getWindow().getAttributes();
            lp.alpha = 1.0f;
            this.getWindow().setAttributes(lp);
        });
        // popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.alpha = 0.5f;
        this.getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.CENTER,0,0);
    }

}