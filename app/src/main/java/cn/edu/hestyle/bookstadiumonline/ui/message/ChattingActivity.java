package cn.edu.hestyle.bookstadiumonline.ui.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.Chat;
import cn.edu.hestyle.bookstadiumonline.entity.ChatMessage;
import cn.edu.hestyle.bookstadiumonline.entity.ChatVO;
import cn.edu.hestyle.bookstadiumonline.entity.Complaint;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.BroadcastUtil;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import cn.edu.hestyle.bookstadiumonline.util.SoftKeyBoardListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class ChattingActivity extends BaseActivity {
    private ChatVO chatVO;
    /** 对话左边（对方） */
    private String userAvatarPathLeft;
    private String usernameLeft;
    /** 对话右边（自己） */
    private String userAvatarPathRight;
    private String usernameRight;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Boolean hasBeforePageChatMessage;
    private List<ChatMessage> chatMessageList;
    private SmartRefreshLayout chatMessageSmartRefreshLayout;
    private RecyclerView chatMessageRecyclerView;
    private ChatMessageRecycleAdapter chatMessageRecycleAdapter;

    private View bottomView;
    private EditText chatMessageEditText;
    private Button sendButton;
    private ChatMessageBroadcastReceiver chatMessageBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        navigationBarInit("聊天");

        Intent intent = getIntent();
        chatVO = (ChatVO) intent.getSerializableExtra("ChatVO");
        int otherUserId = intent.getIntExtra("otherUserId", -1);
        int stadiumManagerId = intent.getIntExtra("stadiumManagerId", -1);

        if (chatVO != null) {
            init();
        } else if (otherUserId != -1) {
            // 获取chat
            getChatWithUserFromServer(otherUserId);
        } else if (stadiumManagerId != -1) {
            // 获取chat
            getChatWithStadiumManagerFromServer(stadiumManagerId);
        }

        this.hasBeforePageChatMessage = true;
        this.chatMessageList = new ArrayList<>();

        chatMessageSmartRefreshLayout = findViewById(R.id.chatMessageSmartRefreshLayout);
        chatMessageSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                // 访问前一页
                ChattingActivity.this.getBeforePageChatMessageFromServer();
            }
        });
        chatMessageSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 往上拉，加载最新
                ChattingActivity.this.getAfterPageChatMessageFromServer();
            }
        });
        chatMessageRecyclerView = findViewById(R.id.chatMessageRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatMessageRecyclerView.setLayoutManager(linearLayoutManager);
        chatMessageRecycleAdapter = new ChatMessageRecycleAdapter(this, chatMessageList);
        chatMessageRecyclerView.setAdapter(chatMessageRecycleAdapter);

        bottomView = findViewById(R.id.bottomView);
        // 监听键盘弹出、隐藏事件，进而修改bottomView的高度，达到吸附键盘顶部的效果
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ChattingActivity.this.bottomView.getLayoutParams();
                layoutParams.height = height;
                ChattingActivity.this.bottomView.setLayoutParams(layoutParams);
            }

            @Override
            public void keyBoardHide(int height) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) ChattingActivity.this.bottomView.getLayoutParams();
                layoutParams.height = 0;
                ChattingActivity.this.bottomView.setLayoutParams(layoutParams);
            }
        });
        chatMessageEditText = findViewById(R.id.chatMessageEditText);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> ChattingActivity.this.sendChatMessage());
        // 注册广播
        chatMessageBroadcastReceiver = new ChatMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastUtil.RECEIVED_CHAT_MESSAGE);
        registerReceiver(chatMessageBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (LoginUserInfoUtil.getLoginUser() == null) {
            Toast.makeText(this, "请先进行登录！", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // activity销毁时，注销广播
        unregisterReceiver(chatMessageBroadcastReceiver);
    }

    /**
     * 初始化chatVO
     */
    private void init() {
        Integer userId = LoginUserInfoUtil.getLoginUser().getId();
        if (chatVO != null) {
            if ((chatVO.getChatType().equals(Chat.CHAT_TYPE_USER_TO_USER) || chatVO.getChatType().equals(Chat.CHAT_TYPE_USER_TO_MANAGER)) && chatVO.getFromAccountId().equals(userId)) {
                // 当前聊天是自己主动发起的
                userAvatarPathLeft = chatVO.getToAccountAvatarPath();
                usernameLeft = chatVO.getToAccountUsername();
                userAvatarPathRight = chatVO.getFromAccountAvatarPath();
                usernameRight = chatVO.getFromAccountUsername();
            } else {
                // 当前聊天是对方发起的
                userAvatarPathLeft = chatVO.getFromAccountAvatarPath();
                usernameLeft = chatVO.getFromAccountUsername();
                userAvatarPathRight = chatVO.getToAccountAvatarPath();
                usernameRight = chatVO.getToAccountUsername();
            }
            navigationBarInit(usernameLeft);
            // 获取ChatMessage
            this.hasBeforePageChatMessage = true;
            this.chatMessageList = null;
            getBeforePageChatMessageFromServer();
        } else {
            Toast.makeText(this, "程序内部出现错误！聊天内容获取失败！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从服务器获取chat
     * @param otherUserId   对方userId
     */
    private void getChatWithUserFromServer(Integer otherUserId) {
        FormBody formBody = new FormBody.Builder().add("otherUserId", otherUserId + "").build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/chat/userGetChatWithUser.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ChattingActivity.this.runOnUiThread(()->{
                    Toast.makeText(ChattingActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<ChatVO>>(){}.getType();
                final ResponseResult<ChatVO> responseResult = gson.fromJson(responseString, type);
                ChattingActivity.this.runOnUiThread(()->{
                    if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                        ChattingActivity.this.chatMessageEditText.setText("");
                        // chat获取成功
                        ChattingActivity.this.chatVO = responseResult.getData();
                        ChattingActivity.this.init();
                    } else {
                        Toast.makeText(ChattingActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 从服务器获取chat
     * @param stadiumManagerId   stadiumManagerId
     */
    private void getChatWithStadiumManagerFromServer(Integer stadiumManagerId) {
        FormBody formBody = new FormBody.Builder().add("stadiumManagerId", stadiumManagerId + "").build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/chat/userGetChatWithStadiumManager.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ChattingActivity.this.runOnUiThread(()->{
                    Toast.makeText(ChattingActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<ChatVO>>(){}.getType();
                final ResponseResult<ChatVO> responseResult = gson.fromJson(responseString, type);
                ChattingActivity.this.runOnUiThread(()->{
                    if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                        ChattingActivity.this.chatMessageEditText.setText("");
                        // chat获取成功
                        ChattingActivity.this.chatVO = responseResult.getData();
                        ChattingActivity.this.init();
                    } else {
                        Toast.makeText(ChattingActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 发送ChatMessage
     */
    private void sendChatMessage() {
        // 生成表单
        FormBody formBody = checkForm();
        if (formBody == null) {
            return;
        }
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/chatMessage/userSend.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ChattingActivity.this.runOnUiThread(()->{
                    Toast.makeText(ChattingActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<ChatMessage>>(){}.getType();
                final ResponseResult<ChatMessage> responseResult = gson.fromJson(responseString, type);
                ChattingActivity.this.runOnUiThread(()->{
                    if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                        ChattingActivity.this.chatMessageEditText.setText("");
                        // 消息发送成功
                        ChatMessage chatMessage = responseResult.getData();
                        if (ChattingActivity.this.chatMessageList != null) {
                            ChattingActivity.this.chatMessageList.add(chatMessage);
                            ChattingActivity.this.chatMessageRecycleAdapter.notifyItemInserted(ChattingActivity.this.chatMessageList.size() - 1);
                        } else {
                            ChattingActivity.this.chatMessageList = new ArrayList<>();
                            ChattingActivity.this.chatMessageList.add(chatMessage);
                            ChattingActivity.this.chatMessageRecycleAdapter.updateData(ChattingActivity.this.chatMessageList);
                        }
                        ChattingActivity.this.chatMessageRecyclerView.scrollToPosition(ChattingActivity.this.chatMessageRecycleAdapter.getItemCount() - 1);
                    } else {
                        Toast.makeText(ChattingActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 检查ChatMessage表单
     * @return      FormBody
     */
    private FormBody checkForm() {
        String content = this.chatMessageEditText.getText().toString();
        if (chatVO == null) {
            Toast.makeText(this, "聊天内容获取失败，无法发送消息！", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (content.length() == 0) {
            Toast.makeText(this, "请输入发送的消息内容！", Toast.LENGTH_SHORT).show();
            return null;
        } else if (content.length() > ChatMessage.CHAT_MESSAGE_CONTENT_MAX_LENGTH) {
            Toast.makeText(this, "消息过长，超过了" + ChatMessage.CHAT_MESSAGE_CONTENT_MAX_LENGTH + "个字符！", Toast.LENGTH_SHORT).show();
            return null;
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatId(chatVO.getId());
        chatMessage.setContent(content);
        // 转json
        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
        String chatMessageData = gson.toJson(chatMessage);
        return new FormBody.Builder()
                .add("chatMessageData", chatMessageData)
                .build();
    }

    /**
     * 获取上一页ChatMessage
     */
    private void getBeforePageChatMessageFromServer() {
        if (chatVO == null) {
            return;
        }
        if (!this.hasBeforePageChatMessage) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            chatMessageSmartRefreshLayout.finishRefresh();
            chatMessageSmartRefreshLayout.setEnableRefresh(false);
        }
        FormBody formBody = null;
        if (this.chatMessageList != null && this.chatMessageList.size() != 0) {
            formBody = new FormBody.Builder()
                    .add("chatId", "" + chatVO.getId())
                    .add("chatMessageId", "" + chatMessageList.get(0).getId())
                    .add("pageSize", "" + PER_PAGE_COUNT)
                    .build();
        } else {
            formBody = new FormBody.Builder()
                    .add("chatId", "" + chatVO.getId())
                    .add("pageSize", "" + PER_PAGE_COUNT)
                    .build();
        }
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/chatMessage/userFindBeforePage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ChattingActivity.this.runOnUiThread(()->{
                    Toast.makeText(ChattingActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<ChatMessage>>>(){}.getType();
                final ResponseResult<List<ChatMessage>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    // 获取数据失败
                    ChattingActivity.this.runOnUiThread(()->{
                        Toast.makeText(ChattingActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<ChatMessage> chatMessageList = responseResult.getData();
                if (chatMessageList != null) {
                    // 按照sentTime升序
                    Collections.sort(chatMessageList, (o1, o2) -> o1.getSentTime().compareTo(o2.getSentTime()));
                }
                Log.i("ChatMessage", chatMessageList + "");
                // 判断是否有更早的聊天记录
                if (chatMessageList == null || chatMessageList.size() < ChattingActivity.PER_PAGE_COUNT) {
                    // 没有更之前的聊天记录
                    ChattingActivity.this.hasBeforePageChatMessage = false;
                } else {
                    ChattingActivity.this.hasBeforePageChatMessage = true;
                }
                // 更新聊天记录
                if (chatMessageList != null && chatMessageList.size() != 0) {
                    chatMessageList.addAll(ChattingActivity.this.chatMessageList);
                    ChattingActivity.this.chatMessageList = chatMessageList;
                }
                ChattingActivity.this.runOnUiThread(()->{
                    ChattingActivity.this.chatMessageSmartRefreshLayout.finishRefresh();
                    ChattingActivity.this.chatMessageSmartRefreshLayout.setEnableRefresh(ChattingActivity.this.hasBeforePageChatMessage);
                    // update
                    ChattingActivity.this.chatMessageRecycleAdapter.updateData(ChattingActivity.this.chatMessageList);
                });
            }
        });
    }

    /**
     * 获取下一页ChatMessage
     */
    private void getAfterPageChatMessageFromServer() {
        if (chatVO == null) {
            return;
        }
        FormBody formBody = null;
        if (this.chatMessageList != null && this.chatMessageList.size() != 0) {
            formBody = new FormBody.Builder()
                    .add("chatId", "" + chatVO.getId())
                    .add("chatMessageId", "" + chatMessageList.get(chatMessageList.size() - 1).getId())
                    .add("pageSize", "" + PER_PAGE_COUNT)
                    .build();
        } else {
            formBody = new FormBody.Builder()
                    .add("chatId", "" + chatVO.getId())
                    .add("pageSize", "" + PER_PAGE_COUNT)
                    .build();
        }
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/chatMessage/userFindAfterPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ChattingActivity.this.runOnUiThread(()->{
                    Toast.makeText(ChattingActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<ChatMessage>>>(){}.getType();
                final ResponseResult<List<ChatMessage>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    // 获取数据失败
                    ChattingActivity.this.runOnUiThread(()->{
                        Toast.makeText(ChattingActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<ChatMessage> chatMessageList = responseResult.getData();
                if (chatMessageList != null) {
                    // 按照sentTime升序
                    Collections.sort(chatMessageList, (o1, o2) -> o1.getSentTime().compareTo(o2.getSentTime()));
                }
                Log.i("ChatMessage", chatMessageList + "");
                // 更新聊天记录
                boolean hasMoreChatMessage = false;
                if (chatMessageList != null && chatMessageList.size() != 0) {
                    ChattingActivity.this.chatMessageList.addAll(chatMessageList);
                    // 判断是否有更多的未读消息
                    if (chatMessageList.size() == ChattingActivity.PER_PAGE_COUNT) {
                        hasMoreChatMessage = true;
                    }
                }
                boolean finalHasMoreChatMessage = hasMoreChatMessage;
                ChattingActivity.this.runOnUiThread(()->{
                    // update
                    ChattingActivity.this.chatMessageRecycleAdapter.updateData(ChattingActivity.this.chatMessageList);
                    if (ChattingActivity.this.chatMessageRecycleAdapter.getItemCount() > 0) {
                        // 滚动到底部
                        ChattingActivity.this.chatMessageRecyclerView.scrollToPosition(ChattingActivity.this.chatMessageRecycleAdapter.getItemCount() - 1);
                    }
                    if (!finalHasMoreChatMessage) {
                        ChattingActivity.this.chatMessageSmartRefreshLayout.finishLoadmore();
                    } else {
                        // 递归调用
                        ChattingActivity.this.getAfterPageChatMessageFromServer();
                    }
                });
            }
        });
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit(String title) {
        ConstraintLayout commonTitleConstraintLayout = findViewById(R.id.chatting_navigation_bar);
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText(String.format("%s", title));
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());

        TextView textView = new TextView(this);
        textView.setText("投诉");
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        ConstraintLayout.LayoutParams rightAnnouncementLayoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        rightAnnouncementLayoutParams.rightMargin = 15;
        rightAnnouncementLayoutParams.endToEnd = R.id.chatting_navigation_bar;
        rightAnnouncementLayoutParams.topToTop = R.id.chatting_navigation_bar;
        rightAnnouncementLayoutParams.bottomToBottom = R.id.chatting_navigation_bar;
        textView.setLayoutParams(rightAnnouncementLayoutParams);
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(ChattingActivity.this, ComplaintActivity.class);
            if (chatVO != null) {
                // 判断投诉类型、投诉用户的id
                if (chatVO.getChatType().equals(Chat.CHAT_TYPE_USER_TO_MANAGER)) {
                    intent.putExtra("respondentAccountType", Complaint.COMPLAIN_ACCOUNT_TYPE_STADIUM_MANAGER);
                    intent.putExtra("respondentAccountId", chatVO.getToAccountId());
                } else if (chatVO.getChatType().equals(Chat.CHAT_TYPE_MANAGER_TO_USER)) {
                    intent.putExtra("respondentAccountType", Complaint.COMPLAIN_ACCOUNT_TYPE_STADIUM_MANAGER);
                    intent.putExtra("respondentAccountId", chatVO.getFromAccountId());
                } else if (chatVO.getChatType().equals(Chat.CHAT_TYPE_USER_TO_USER)) {
                    intent.putExtra("respondentAccountType", Complaint.COMPLAIN_ACCOUNT_TYPE_USER);
                    if (chatVO.getFromAccountId().equals(LoginUserInfoUtil.getLoginUser().getId())) {
                        intent.putExtra("respondentAccountId", chatVO.getToAccountId());
                    } else {
                        intent.putExtra("respondentAccountId", chatVO.getFromAccountId());
                    }
                }
                startActivity(intent);
            } else {
                Toast.makeText(this, "程序内部发生数据传递错误！", Toast.LENGTH_SHORT).show();
            }
        });
        // 添加到common_title
        commonTitleConstraintLayout.addView(textView);
    }

    class ChatMessageRecycleAdapter extends RecyclerView.Adapter<ChatMessageRecycleAdapter.ChatMessageViewHolder> {
        private Activity activityContext;
        private View inflater;
        private List<ChatMessage> chatMessageList;

        public ChatMessageRecycleAdapter(Activity activityContext, List<ChatMessage> chatMessageList){
            this.activityContext = activityContext;
            this.chatMessageList = chatMessageList;
        }

        /**
         * 更新data
         * @param chatMessageList        chatMessageList
         */
        public void updateData(List<ChatMessage> chatMessageList) {
            this.chatMessageList = chatMessageList;
            this.notifyDataSetChanged();
        }


        @Override
        public ChatMessageRecycleAdapter.ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //创建ViewHolder，返回每一项的布局
            inflater = LayoutInflater.from(activityContext).inflate(R.layout.item_chat_message_recyclerview, parent, false);
            return new ChatMessageRecycleAdapter.ChatMessageViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(ChatMessageRecycleAdapter.ChatMessageViewHolder holder, int position) {
            // 将数据和控件绑定
            ChatMessage chatMessage = chatMessageList.get(position);
            holder.leftConstraintLayout.setVisibility(View.GONE);
            holder.rightConstraintLayout.setVisibility(View.GONE);
            if (chatVO == null) {
                return;
            }
            Integer userId = LoginUserInfoUtil.getLoginUser().getId();
            if (chatMessage.getFromAccountId().equals(userId) && (chatMessage.getMessageType().equals(ChatMessage.MESSAGE_TYPE_USER_TO_USER) || chatMessage.getMessageType().equals(ChatMessage.MESSAGE_TYPE_USER_TO_MANAGER))) {
                // 我发给对方的消息
                holder.leftConstraintLayout.setVisibility(View.GONE);
                holder.rightConstraintLayout.setVisibility(View.VISIBLE);
                holder.contentRightTextView.setText(String.format("%s", chatMessage.getContent()));
                // 自己头像
                if (userAvatarPathRight != null && userAvatarPathRight.length() != 0) {
                    Glide.with(inflater.getContext())
                            .load(ServerSettingActivity.getServerHostUrl() + userAvatarPathRight)
                            .into(holder.userAvatarRightImageView);
                }

            } else {
                // 对方发给我的消息
                holder.leftConstraintLayout.setVisibility(View.VISIBLE);
                holder.rightConstraintLayout.setVisibility(View.GONE);
                holder.contentLeftTextView.setText(String.format("%s", chatMessage.getContent()));
                // 对方头像
                if (userAvatarPathLeft != null && userAvatarPathLeft.length() != 0) {
                    Glide.with(inflater.getContext())
                            .load(ServerSettingActivity.getServerHostUrl() + userAvatarPathLeft)
                            .into(holder.userAvatarLeftImageView);
                }
            }
            holder.setSendTime(chatMessage.getSentTime());
            // 如果两条消息的间隔不超过5分钟，则不显示时间
            if (position != 0) {
                ChatMessage beforeChatMessage = chatMessageList.get(position - 1);
                long between = chatMessage.getSentTime().getTime() - beforeChatMessage.getSentTime().getTime();
                if (between < 60 * 5 * 1000) {
                    holder.sendTimeTextView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            // 返回Item总条数
            if (chatMessageList != null) {
                return chatMessageList.size();
            } else {
                return 0;
            }
        }

        //内部类，绑定控件
        class ChatMessageViewHolder extends RecyclerView.ViewHolder {
            public TextView sendTimeTextView;
            public ConstraintLayout leftConstraintLayout;
            public ImageView userAvatarLeftImageView;
            public TextView contentLeftTextView;
            public ConstraintLayout rightConstraintLayout;
            public ImageView userAvatarRightImageView;
            public TextView contentRightTextView;

            public ChatMessageViewHolder(View itemView) {
                super(itemView);
                sendTimeTextView = itemView.findViewById(R.id.sendTimeTextView);
                leftConstraintLayout = itemView.findViewById(R.id.leftConstraintLayout);
                userAvatarLeftImageView = itemView.findViewById(R.id.userAvatarLeftImageView);
                contentLeftTextView = itemView.findViewById(R.id.contentLeftTextView);
                rightConstraintLayout = itemView.findViewById(R.id.rightConstraintLayout);
                userAvatarRightImageView = itemView.findViewById(R.id.userAvatarRightImageView);
                contentRightTextView = itemView.findViewById(R.id.contentRightTextView);
            }

            /**
             * 设置时间
             * @param date     最后一条消息时间
             */
            public void setSendTime(Date date) {
                if (date != null) {
                    Date now = new Date();
                    SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");
                    if (dayDateFormat.format(date).equals(dayDateFormat.format(now))) {
                        // 当天的消息，只显示时间
                        SimpleDateFormat timeDateFormat = new SimpleDateFormat("HH:mm:ss");
                        sendTimeTextView.setText(String.format("%s", timeDateFormat.format(date)));
                    } else if (yearDateFormat.format(date).equals(yearDateFormat.format(now))) {
                        // 同一年的消息，显示月份、日期 时间
                        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
                        sendTimeTextView.setText(String.format("%s", monthDateFormat.format(date)));
                    } else {
                        sendTimeTextView.setText(String.format("%s", dayDateFormat.format(date)));
                    }
                    sendTimeTextView.setVisibility(View.VISIBLE);
                } else {
                    sendTimeTextView.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * ChatMessageBroadcast Receiver
     */
    class ChatMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 读取Broadcast中的ChatMessage
            ChatMessage chatMessage = (ChatMessage) intent.getSerializableExtra(ChatMessage.BROAD_CAST_KEY);
            if (chatMessage != null && chatVO != null && chatMessage.getChatId().equals(chatVO.getId())) {
                // ChatMessage属于当前聊天窗口
                if (ChattingActivity.this.chatMessageList != null) {
                    ChattingActivity.this.chatMessageList.add(chatMessage);
                    ChattingActivity.this.chatMessageRecycleAdapter.notifyItemInserted(ChattingActivity.this.chatMessageList.size() - 1);
                } else {
                    ChattingActivity.this.chatMessageList = new ArrayList<>();
                    ChattingActivity.this.chatMessageList.add(chatMessage);
                    ChattingActivity.this.chatMessageRecycleAdapter.updateData(ChattingActivity.this.chatMessageList);
                }
                ChattingActivity.this.chatMessageRecyclerView.scrollToPosition(ChattingActivity.this.chatMessageRecycleAdapter.getItemCount() - 1);
            }
        }
    }
}