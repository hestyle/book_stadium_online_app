package cn.edu.hestyle.bookstadiumonline.util;

import android.content.Intent;

import cn.edu.hestyle.bookstadiumonline.entity.ChatMessage;

public class BroadcastUtil {
    /** intent action */
    public static final String RECEIVED_CHAT_MESSAGE = "edu.cn.hestyle.bookstadiumonline.received.chat.message";

    /**
     * 收到chatMessage的广播
     * @param chatMessage   chatMessage
     */
    public static void sendReceivedChatMessageBroadcast(ChatMessage chatMessage) {
        Intent intent = new Intent();
        intent.setAction(RECEIVED_CHAT_MESSAGE);
        intent.putExtra(ChatMessage.BROAD_CAST_KEY, chatMessage);
        ApplicationContextUtil.getApplicationContext().sendBroadcast(intent);
    }
}
