package cn.edu.hestyle.bookstadiumonline.ui.message;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.adapter.ChatVORecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.entity.ChatVO;
import cn.edu.hestyle.bookstadiumonline.ui.my.SystemNoticeListActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class MessageFragment extends Fragment {
    private View rootView;

    private TextView tipsTextView;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<ChatVO> chatVOList;
    private SmartRefreshLayout chatVOSmartRefreshLayout;
    private RecyclerView chatVORecyclerView;
    private ChatVORecycleAdapter chatVORecycleAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_message, container, false);

        this.navigationBarInit("消息");

        this.nextPageIndex = 1;
        this.chatVOList = null;
        this.tipsTextView = rootView.findViewById(R.id.tipsTextView);

        chatVOSmartRefreshLayout = rootView.findViewById(R.id.chatVOSmartRefreshLayout);
        chatVOSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                MessageFragment.this.nextPageIndex = 1;
                MessageFragment.this.getNextPageChatVOFromServer();
            }
        });
        chatVOSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                MessageFragment.this.getNextPageChatVOFromServer();
            }
        });
        chatVORecyclerView = rootView.findViewById(R.id.chatVORecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatVORecyclerView.setLayoutManager(linearLayoutManager);
        chatVORecycleAdapter = new ChatVORecycleAdapter(this.getActivity(), chatVOList);
        chatVORecyclerView.setAdapter(chatVORecycleAdapter);
        // 添加分割线
        chatVORecyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (LoginUserInfoUtil.getLoginUser() != null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.chatVOList = null;
            getNextPageChatVOFromServer();
        } else {
            this.tipsTextView.setText("请先登录！");
            this.tipsTextView.setVisibility(View.VISIBLE);
            this.chatVOSmartRefreshLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 获取下一页ChatVO
     */
    private void getNextPageChatVOFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this.getContext(), "暂无更多内容！", Toast.LENGTH_SHORT).show();
            chatVOSmartRefreshLayout.finishLoadmore();
            chatVOSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/chat/userFindByPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                MessageFragment.this.getActivity().runOnUiThread(()->{
                    Toast.makeText(MessageFragment.this.getActivity(), "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<ChatVO>>>(){}.getType();
                final ResponseResult<List<ChatVO>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    // 获取数据失败
                    MessageFragment.this.getActivity().runOnUiThread(()->{
                        Toast.makeText(MessageFragment.this.getActivity(), responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<ChatVO> chatVOList = responseResult.getData();
                Log.i("ChatVO", chatVOList + "");
                // 访问第一页，或者追加
                if (MessageFragment.this.nextPageIndex == 1) {
                    MessageFragment.this.chatVOList = chatVOList;
                } else {
                    MessageFragment.this.chatVOList.addAll(chatVOList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (chatVOList == null || chatVOList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                MessageFragment.this.getActivity().runOnUiThread(()->{
                    if (MessageFragment.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        MessageFragment.this.chatVOSmartRefreshLayout.finishRefresh();
                        MessageFragment.this.chatVOSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        MessageFragment.this.chatVOSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        MessageFragment.this.nextPageIndex += 1;
                    } else {
                        MessageFragment.this.nextPageIndex = 0;
                    }
                    MessageFragment.this.chatVOSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    MessageFragment.this.chatVORecycleAdapter.updateData(MessageFragment.this.chatVOList);
                    if (MessageFragment.this.chatVOList == null || MessageFragment.this.chatVOList.size() == 0) {
                        MessageFragment.this.tipsTextView.setText("暂无消息数据！");
                        MessageFragment.this.tipsTextView.setVisibility(View.VISIBLE);
                        MessageFragment.this.chatVOSmartRefreshLayout.setVisibility(View.GONE);
                    } else {
                        MessageFragment.this.tipsTextView.setVisibility(View.GONE);
                        MessageFragment.this.chatVOSmartRefreshLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit(String title) {
        ConstraintLayout commonTitleConstraintLayout = this.rootView.findViewById(R.id.fragment_message_navigation_bar);
        // 设置title
        TextView titleTextView = rootView.findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        // 设置right announcement
        ImageButton rightAnnouncementImageButton = new ImageButton(getActivity());
        rightAnnouncementImageButton.setBackgroundColor(Color.TRANSPARENT);
        rightAnnouncementImageButton.setImageResource(R.drawable.ic_announcement_24dp);
        ConstraintLayout.LayoutParams rightAnnouncementLayoutParams = new ConstraintLayout.LayoutParams(56, 56);
        rightAnnouncementLayoutParams.rightMargin = 15;
        rightAnnouncementLayoutParams.endToEnd = R.id.fragment_message_navigation_bar;
        rightAnnouncementLayoutParams.topToTop = R.id.fragment_message_navigation_bar;
        rightAnnouncementLayoutParams.bottomToBottom = R.id.fragment_message_navigation_bar;
        rightAnnouncementImageButton.setLayoutParams(rightAnnouncementLayoutParams);
        rightAnnouncementImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SystemNoticeListActivity.class);
            startActivity(intent);
        });
        // 添加到common_title
        commonTitleConstraintLayout.addView(rightAnnouncementImageButton);
    }
}