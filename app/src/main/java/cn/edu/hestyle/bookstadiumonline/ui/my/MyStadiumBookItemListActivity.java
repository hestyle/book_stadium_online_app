package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.content.Intent;
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
import cn.edu.hestyle.bookstadiumonline.adapter.UserStadiumBookItemRecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.entity.UserStadiumBookItem;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class MyStadiumBookItemListActivity extends BaseActivity {
    private Integer userId;
    private TextView tipsTextView;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<UserStadiumBookItem> userStadiumBookItemList;
    private SmartRefreshLayout userStadiumBookItemSmartRefreshLayout;
    private RecyclerView userStadiumBookItemRecyclerView;
    private UserStadiumBookItemRecycleAdapter userStadiumBookItemRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stadium_book_item_list);

        Intent intent = getIntent();
        this.userId = intent.getIntExtra("userId", 0);
        this.navigationBarInit("我的预约");

        this.nextPageIndex = 1;
        this.userStadiumBookItemList = null;
        this.tipsTextView = findViewById(R.id.tipsTextView);

        userStadiumBookItemSmartRefreshLayout = findViewById(R.id.userStadiumBookItemSmartRefreshLayout);
        userStadiumBookItemSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                MyStadiumBookItemListActivity.this.nextPageIndex = 1;
                MyStadiumBookItemListActivity.this.getNextPageUserStadiumBookItemFromServer();
            }
        });
        userStadiumBookItemSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                MyStadiumBookItemListActivity.this.getNextPageUserStadiumBookItemFromServer();
            }
        });
        userStadiumBookItemRecyclerView = findViewById(R.id.userStadiumBookItemRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        userStadiumBookItemRecyclerView.setLayoutManager(linearLayoutManager);
        userStadiumBookItemRecycleAdapter = new UserStadiumBookItemRecycleAdapter(this, userStadiumBookItemList);
        userStadiumBookItemRecyclerView.setAdapter(userStadiumBookItemRecycleAdapter);
        // 添加分割线
        userStadiumBookItemRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.userId != null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.userStadiumBookItemList = null;
            getNextPageUserStadiumBookItemFromServer();
        } else {
            this.tipsTextView.setText("暂无评论数据！");
            this.tipsTextView.setVisibility(View.VISIBLE);
            this.userStadiumBookItemSmartRefreshLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 获取下一页UserStadiumComment
     */
    private void getNextPageUserStadiumBookItemFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            userStadiumBookItemSmartRefreshLayout.finishLoadmore();
            userStadiumBookItemSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("userId", "" + userId)
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userStadiumBookItem/findByUserIdAndPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                MyStadiumBookItemListActivity.this.runOnUiThread(()->{
                    Toast.makeText(MyStadiumBookItemListActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<UserStadiumBookItem>>>(){}.getType();
                final ResponseResult<List<UserStadiumBookItem>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    // 获取数据失败
                    MyStadiumBookItemListActivity.this.runOnUiThread(()->{
                        Toast.makeText(MyStadiumBookItemListActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<UserStadiumBookItem> userStadiumBookItemList = responseResult.getData();
                Log.i("UserStadiumBookItem", userStadiumBookItemList + "");
                // 访问第一页，或者追加
                if (MyStadiumBookItemListActivity.this.nextPageIndex == 1) {
                    MyStadiumBookItemListActivity.this.userStadiumBookItemList = userStadiumBookItemList;
                } else {
                    MyStadiumBookItemListActivity.this.userStadiumBookItemList.addAll(userStadiumBookItemList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (userStadiumBookItemList == null || userStadiumBookItemList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                MyStadiumBookItemListActivity.this.runOnUiThread(()->{
                    if (MyStadiumBookItemListActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        MyStadiumBookItemListActivity.this.userStadiumBookItemSmartRefreshLayout.finishRefresh();
                        MyStadiumBookItemListActivity.this.userStadiumBookItemSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        MyStadiumBookItemListActivity.this.userStadiumBookItemSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        MyStadiumBookItemListActivity.this.nextPageIndex += 1;
                    } else {
                        MyStadiumBookItemListActivity.this.nextPageIndex = 0;
                    }
                    MyStadiumBookItemListActivity.this.userStadiumBookItemSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    MyStadiumBookItemListActivity.this.userStadiumBookItemRecycleAdapter.updateData(MyStadiumBookItemListActivity.this.userStadiumBookItemList);
                    if (MyStadiumBookItemListActivity.this.userStadiumBookItemList == null || MyStadiumBookItemListActivity.this.userStadiumBookItemList.size() == 0) {
                        MyStadiumBookItemListActivity.this.tipsTextView.setText("暂无评论数据！");
                        MyStadiumBookItemListActivity.this.tipsTextView.setVisibility(View.VISIBLE);
                        MyStadiumBookItemListActivity.this.userStadiumBookItemSmartRefreshLayout.setVisibility(View.GONE);
                    } else {
                        MyStadiumBookItemListActivity.this.tipsTextView.setVisibility(View.GONE);
                        MyStadiumBookItemListActivity.this.userStadiumBookItemSmartRefreshLayout.setVisibility(View.VISIBLE);
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