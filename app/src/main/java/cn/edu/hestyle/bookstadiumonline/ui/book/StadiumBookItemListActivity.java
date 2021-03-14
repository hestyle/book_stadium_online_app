package cn.edu.hestyle.bookstadiumonline.ui.book;

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
import cn.edu.hestyle.bookstadiumonline.entity.StadiumBookItem;
import cn.edu.hestyle.bookstadiumonline.ui.book.adapter.StadiumBookItemRecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class StadiumBookItemListActivity extends BaseActivity {
    private Integer stadiumBookId;
    private TextView tipsTextView;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<StadiumBookItem> stadiumBookItemList;
    private SmartRefreshLayout stadiumBookItemSmartRefreshLayout;
    private RecyclerView stadiumBookItemRecyclerView;
    private StadiumBookItemRecycleAdapter stadiumBookItemRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium_book_item_list);

        Intent intent = getIntent();
        this.stadiumBookId = intent.getIntExtra("stadiumBookId", 0);

        this.navigationBarInit("已预约用户");

        this.nextPageIndex = 1;
        this.stadiumBookItemList = null;
        this.tipsTextView = findViewById(R.id.tipsTextView);

        stadiumBookItemSmartRefreshLayout = findViewById(R.id.stadiumBookItemSmartRefreshLayout);
        stadiumBookItemSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                StadiumBookItemListActivity.this.nextPageIndex = 1;
                StadiumBookItemListActivity.this.getNextPageStadiumBookItemFromServer();
            }
        });
        stadiumBookItemSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                StadiumBookItemListActivity.this.getNextPageStadiumBookItemFromServer();
            }
        });
        stadiumBookItemRecyclerView = findViewById(R.id.stadiumBookItemRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        stadiumBookItemRecyclerView.setLayoutManager(linearLayoutManager);
        stadiumBookItemRecycleAdapter = new StadiumBookItemRecycleAdapter(this, stadiumBookItemList);
        stadiumBookItemRecyclerView.setAdapter(stadiumBookItemRecycleAdapter);
        // 添加分割线
        stadiumBookItemRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.stadiumBookId != null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.stadiumBookItemList = null;
            getNextPageStadiumBookItemFromServer();
        } else {
            this.tipsTextView.setText("暂无评论数据！");
            this.tipsTextView.setVisibility(View.VISIBLE);
            this.stadiumBookItemSmartRefreshLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 获取下一页StadiumComment
     */
    private void getNextPageStadiumBookItemFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            stadiumBookItemSmartRefreshLayout.finishLoadmore();
            stadiumBookItemSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("stadiumBookId", "" + this.stadiumBookId)
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadiumBookItem/findByStadiumBookIdAndPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                StadiumBookItemListActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumBookItemListActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<StadiumBookItem>>>(){}.getType();
                final ResponseResult<List<StadiumBookItem>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    // 获取数据失败
                    StadiumBookItemListActivity.this.runOnUiThread(()->{
                        Toast.makeText(StadiumBookItemListActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<StadiumBookItem> stadiumBookItemList = responseResult.getData();
                Log.i("StadiumBookItem", stadiumBookItemList + "");
                // 访问第一页，或者追加
                if (StadiumBookItemListActivity.this.nextPageIndex == 1) {
                    StadiumBookItemListActivity.this.stadiumBookItemList = stadiumBookItemList;
                } else {
                    StadiumBookItemListActivity.this.stadiumBookItemList.addAll(stadiumBookItemList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (stadiumBookItemList == null || stadiumBookItemList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                StadiumBookItemListActivity.this.runOnUiThread(()->{
                    if (StadiumBookItemListActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        StadiumBookItemListActivity.this.stadiumBookItemSmartRefreshLayout.finishRefresh();
                        StadiumBookItemListActivity.this.stadiumBookItemSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        StadiumBookItemListActivity.this.stadiumBookItemSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        StadiumBookItemListActivity.this.nextPageIndex += 1;
                    } else {
                        StadiumBookItemListActivity.this.nextPageIndex = 0;
                    }
                    StadiumBookItemListActivity.this.stadiumBookItemSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    StadiumBookItemListActivity.this.stadiumBookItemRecycleAdapter.updateData(StadiumBookItemListActivity.this.stadiumBookItemList);
                    if (StadiumBookItemListActivity.this.stadiumBookItemList == null || StadiumBookItemListActivity.this.stadiumBookItemList.size() == 0) {
                        StadiumBookItemListActivity.this.tipsTextView.setText("暂无评论数据！");
                        StadiumBookItemListActivity.this.tipsTextView.setVisibility(View.VISIBLE);
                        StadiumBookItemListActivity.this.stadiumBookItemSmartRefreshLayout.setVisibility(View.GONE);
                    } else {
                        StadiumBookItemListActivity.this.tipsTextView.setVisibility(View.GONE);
                        StadiumBookItemListActivity.this.stadiumBookItemSmartRefreshLayout.setVisibility(View.VISIBLE);
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