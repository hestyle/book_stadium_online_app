package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.adapter.SystemNoticeRecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.entity.Notice;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class SystemNoticeListActivity extends BaseActivity {
    private TextView tipsTextView;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<Notice> noticeList;
    private SmartRefreshLayout noticeSmartRefreshLayout;
    private RecyclerView noticeRecyclerView;
    private SystemNoticeRecycleAdapter systemNoticeRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_notice_list);

        this.navigationBarInit("系统通知");

        this.nextPageIndex = 1;
        this.noticeList = null;
        this.tipsTextView = findViewById(R.id.tipsTextView);

        noticeSmartRefreshLayout = findViewById(R.id.noticeSmartRefreshLayout);
        noticeSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                SystemNoticeListActivity.this.nextPageIndex = 1;
                SystemNoticeListActivity.this.getNextPageNoticeFromServer();
            }
        });
        noticeSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                SystemNoticeListActivity.this.getNextPageNoticeFromServer();
            }
        });
        noticeRecyclerView = findViewById(R.id.noticeRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        noticeRecyclerView.setLayoutManager(linearLayoutManager);
        systemNoticeRecycleAdapter = new SystemNoticeRecycleAdapter(this, noticeList);
        noticeRecyclerView.setAdapter(systemNoticeRecycleAdapter);
        // 添加分割线
        noticeRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.noticeList == null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.noticeList = null;
            getNextPageNoticeFromServer();
        }
    }

    /**
     * 获取下一页eNotice
     */
    private void getNextPageNoticeFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            noticeSmartRefreshLayout.finishLoadmore();
            noticeSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/notice/findUserNoticeByPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                SystemNoticeListActivity.this.runOnUiThread(()->{
                    Toast.makeText(SystemNoticeListActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<Notice>>>(){}.getType();
                final ResponseResult<List<Notice>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    // 获取数据失败
                    SystemNoticeListActivity.this.runOnUiThread(()->{
                        Toast.makeText(SystemNoticeListActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<Notice> noticeList = responseResult.getData();
                Log.i("Notice", noticeList + "");
                // 访问第一页，或者追加
                if (SystemNoticeListActivity.this.nextPageIndex == 1) {
                    SystemNoticeListActivity.this.noticeList = noticeList;
                } else {
                    SystemNoticeListActivity.this.noticeList.addAll(noticeList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (noticeList == null || noticeList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                SystemNoticeListActivity.this.runOnUiThread(()->{
                    if (SystemNoticeListActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        SystemNoticeListActivity.this.noticeSmartRefreshLayout.finishRefresh();
                        SystemNoticeListActivity.this.noticeSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        SystemNoticeListActivity.this.noticeSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        SystemNoticeListActivity.this.nextPageIndex += 1;
                    } else {
                        SystemNoticeListActivity.this.nextPageIndex = 0;
                    }
                    SystemNoticeListActivity.this.noticeSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    SystemNoticeListActivity.this.systemNoticeRecycleAdapter.updateData(SystemNoticeListActivity.this.noticeList);
                    if (SystemNoticeListActivity.this.noticeList == null || SystemNoticeListActivity.this.noticeList.size() == 0) {
                        SystemNoticeListActivity.this.tipsTextView.setText("暂无评论数据！");
                        SystemNoticeListActivity.this.tipsTextView.setVisibility(View.VISIBLE);
                        SystemNoticeListActivity.this.noticeSmartRefreshLayout.setVisibility(View.GONE);
                    } else {
                        SystemNoticeListActivity.this.tipsTextView.setVisibility(View.GONE);
                        SystemNoticeListActivity.this.noticeSmartRefreshLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit(String title) {
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
    }
}