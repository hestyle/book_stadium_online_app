package cn.edu.hestyle.bookstadiumonline.ui.my.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.util.NetworkHelp;

public class ServerSettingActivity extends BaseActivity {
    /** SharedPreferences本地存储 ServerSetting name*/
    private static final String SERVER_SETTING_NAME = "SERVER_SETTING_NAME";
    private static final String SERVER_IP_ADDRESS = "SERVER_IP_ADDRESS";
    private static final String SERVER_PORT = "SERVER_PORT";

    private EditText serverIpAddressEditText;
    private EditText serverPortEditText;

    private static String serverIpAddress;
    private static String serverPort;

    public static String getServerIpAddress() {
        return serverIpAddress;
    }

    public static String getServerPort() {
        return serverPort;
    }

    public static String getServerBaseUrl() {
        return "http://" + serverIpAddress + ":" + serverPort + "/book_stadium_online";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_setting);
        // 设置navigationBar
        this.navigationBarInit();

        this.serverIpAddressEditText = findViewById(R.id.serverIpAddressEditText);
        this.serverPortEditText = findViewById(R.id.serverPortEditText);
        Button saveButton = findViewById(R.id.saveButton);
        // 设置保存按钮点击事件
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverIpAddressString = serverIpAddressEditText.getText().toString();
                String serverPortString = serverPortEditText.getText().toString();
                // 检查ip、port的合法性
                if (!NetworkHelp.isValidIpv4Address(serverIpAddressString)) {
                    Toast.makeText(ServerSettingActivity.this, "服务器ip地址非法！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!NetworkHelp.isValidPort(serverPortString)) {
                    Toast.makeText(ServerSettingActivity.this, "服务器端口非法！", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 更新SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences(SERVER_SETTING_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SERVER_IP_ADDRESS, serverIpAddressString);
                editor.putString(SERVER_PORT, serverPortString);
                if (editor.commit()) {
                    ServerSettingActivity.serverIpAddress = serverIpAddressString;
                    ServerSettingActivity.serverPort = serverPortString;
                    Toast.makeText(ServerSettingActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ServerSettingActivity.this, "保存失败！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 读取SharedPreferences
        this.serverIpAddressAndPortRead();
    }

    /**
     * 读取SharedPreferences
     */
    private void serverIpAddressAndPortRead() {
        // 读取SharedPreferences，显示serverIpAddress、serverPort
        SharedPreferences sharedPreferences = getSharedPreferences(SERVER_SETTING_NAME, Context.MODE_PRIVATE);
        String serverIpAddress = sharedPreferences.getString(SERVER_IP_ADDRESS, null);
        if (serverIpAddress != null && serverIpAddress.length() != 0) {
            ServerSettingActivity.serverIpAddress = serverIpAddress;
            serverIpAddressEditText.setText(serverIpAddress);
        }
        String serverPort = sharedPreferences.getString(SERVER_PORT, null);
        if (serverPort != null && serverPort.length() != 0) {
            ServerSettingActivity.serverPort = serverPort;
            serverPortEditText.setText(serverPort);
        }
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit() {
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText("服务器相关设置");
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ServerSettingActivity.isSavedServerSetting(ServerSettingActivity.this)) {
                    Toast.makeText(ServerSettingActivity.this, "必须设置服务器ip地址与端口，才能进行使用！", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                }
            }
        });
    }

    /**
     * 判断是否保存了服务器相关设置
     * @return  判断结果
     */
    public static boolean isSavedServerSetting(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SERVER_SETTING_NAME, Context.MODE_PRIVATE);
        String serverIpAddress = sharedPreferences.getString(SERVER_IP_ADDRESS, null);
        if (serverIpAddress == null || serverIpAddress.length() == 0) {
            return false;
        }
        ServerSettingActivity.serverIpAddress = serverIpAddress;
        String serverPort = sharedPreferences.getString(SERVER_PORT, null);
        if (serverPort == null || serverPort.length() == 0) {
            return false;
        }
        ServerSettingActivity.serverPort = serverPort;
        return true;
    }
}