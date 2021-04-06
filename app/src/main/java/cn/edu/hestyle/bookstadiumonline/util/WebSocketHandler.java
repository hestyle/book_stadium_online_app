package cn.edu.hestyle.bookstadiumonline.util;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import cn.edu.hestyle.bookstadiumonline.entity.ChatMessage;
import cn.edu.hestyle.bookstadiumonline.entity.WebSocketMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketHandler extends WebSocketListener {
    private static final String TAG = "WebSocketHandler";

    private String wsUrl;

    private WebSocket webSocket;

    private ConnectStatus status;

    private OkHttpClient client = new OkHttpClient.Builder().build();

    private WebSocketHandler(String wsUrl) {
        this.wsUrl = wsUrl;
        this.connect();
    }

    private static WebSocketHandler INST;

    public static WebSocketHandler getInstance(String url) {
        if (INST == null) {
            synchronized (WebSocketHandler.class) {
                INST = new WebSocketHandler(url);
            }
        } else if (!INST.wsUrl.equals(url)) {
            // 更换了url
            synchronized (WebSocketHandler.class) {
                INST.close();
                INST = new WebSocketHandler(url);
            }
        }
        return INST;
    }

    public ConnectStatus getStatus() {
        return status;
    }

    public void connect() {
        //构造request对象
        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        webSocket = client.newWebSocket(request, this);
        status = ConnectStatus.Connecting;
    }

    public void reConnect() {
        if (webSocket != null) {
            webSocket = client.newWebSocket(webSocket.request(), this);
        }
    }

    public void send(String text) {
        if (webSocket != null) {
            Log.i("WebSocket", "send： " + text);
            webSocket.send(text);
        }
    }

    public void cancel() {
        if (webSocket != null) {
            webSocket.cancel();
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
        webSocket = null;
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        super.onOpen(webSocket, response);
        Log.i("WebSocket", "onOpen");
        this.status = ConnectStatus.Open;
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        super.onMessage(webSocket, text);
        Log.i("WebSocket", "onMessage: " + text);
        // 转json
        try {
            Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
            WebSocketMessage webSocketMessage = gson.fromJson(text, WebSocketMessage.class);
            if (WebSocketMessage.WEBSOCKET_MESSAGE_TYPE_CHAT_MESSAGE.equals(webSocketMessage.getMessageType())) {
                // 收到了ChatMessage消息
                ChatMessage chatMessage = gson.fromJson(webSocketMessage.getContent(), ChatMessage.class);
                if (chatMessage != null) {
                    // 发送广播
                    BroadcastUtil.sendReceivedChatMessageBroadcast(chatMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosing(webSocket, code, reason);
        this.status = ConnectStatus.Closing;
        Log.i("WebSocket", "onClosing");
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosed(webSocket, code, reason);
        Log.i("WebSocket", "onClosed");
        this.status = ConnectStatus.Closed;
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        Log.i("WebSocket", "onFailure: " + t.toString());
        t.printStackTrace();
        this.status = ConnectStatus.Canceled;
    }
}

