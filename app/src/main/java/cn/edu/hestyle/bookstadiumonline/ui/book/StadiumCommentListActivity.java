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
import cn.edu.hestyle.bookstadiumonline.entity.StadiumComment;
import cn.edu.hestyle.bookstadiumonline.ui.book.adapter.StadiumCommentRecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class StadiumCommentListActivity extends BaseActivity {
    private Integer stadiumId;
    private TextView tipsTextView;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<StadiumComment> stadiumCommentList;
    private SmartRefreshLayout stadiumCommentSmartRefreshLayout;
    private RecyclerView stadiumCommentRecyclerView;
    private StadiumCommentRecycleAdapter stadiumCommentRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium_comment_list);

        Intent intent = getIntent();
        this.stadiumId = intent.getIntExtra("stadiumId", 0);

        this.navigationBarInit("评论");

        this.nextPageIndex = 1;
        this.stadiumCommentList = null;
        this.tipsTextView = findViewById(R.id.tipsTextView);

        stadiumCommentSmartRefreshLayout = findViewById(R.id.stadiumCommentSmartRefreshLayout);
        stadiumCommentSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                StadiumCommentListActivity.this.nextPageIndex = 1;
                StadiumCommentListActivity.this.getNextPageStadiumCommentFromServer();
            }
        });
        stadiumCommentSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                StadiumCommentListActivity.this.getNextPageStadiumCommentFromServer();
            }
        });
        stadiumCommentRecyclerView = findViewById(R.id.stadiumCommentRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        stadiumCommentRecyclerView.setLayoutManager(linearLayoutManager);
        stadiumCommentRecycleAdapter = new StadiumCommentRecycleAdapter(this, stadiumCommentList);
        stadiumCommentRecyclerView.setAdapter(stadiumCommentRecycleAdapter);
        // 添加分割线
        stadiumCommentRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.stadiumId != null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.stadiumCommentList = null;
            getNextPageStadiumCommentFromServer();
        } else {
            this.tipsTextView.setText("暂无评论数据！");
            this.tipsTextView.setVisibility(View.VISIBLE);
            this.stadiumCommentSmartRefreshLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 获取下一页StadiumComment
     */
    private void getNextPageStadiumCommentFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            stadiumCommentSmartRefreshLayout.finishLoadmore();
            stadiumCommentSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("stadiumId", "" + this.stadiumId)
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadiumComment/findByStadiumIdAndPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                StadiumCommentListActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumCommentListActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<StadiumComment>>>(){}.getType();
                final ResponseResult<List<StadiumComment>> responseResult = gson.fromJson(responseString, type);
                List<StadiumComment> stadiumCommentList = responseResult.getData();
                Log.i("Stadium", stadiumCommentList + "");
                // 访问第一页，或者追加
                if (StadiumCommentListActivity.this.nextPageIndex == 1) {
                    StadiumCommentListActivity.this.stadiumCommentList = stadiumCommentList;
                } else {
                    StadiumCommentListActivity.this.stadiumCommentList.addAll(stadiumCommentList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (stadiumCommentList == null || stadiumCommentList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                StadiumCommentListActivity.this.runOnUiThread(()->{
                    if (StadiumCommentListActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        StadiumCommentListActivity.this.stadiumCommentSmartRefreshLayout.finishRefresh();
                        StadiumCommentListActivity.this.stadiumCommentSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        StadiumCommentListActivity.this.stadiumCommentSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        StadiumCommentListActivity.this.nextPageIndex += 1;
                    } else {
                        StadiumCommentListActivity.this.nextPageIndex = 0;
                    }
                    StadiumCommentListActivity.this.stadiumCommentSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    StadiumCommentListActivity.this.stadiumCommentRecycleAdapter.updateData(StadiumCommentListActivity.this.stadiumCommentList);
                    if (StadiumCommentListActivity.this.stadiumCommentList == null || StadiumCommentListActivity.this.stadiumCommentList.size() == 0) {
                        StadiumCommentListActivity.this.tipsTextView.setText("暂无评论数据！");
                        StadiumCommentListActivity.this.tipsTextView.setVisibility(View.VISIBLE);
                        StadiumCommentListActivity.this.stadiumCommentSmartRefreshLayout.setVisibility(View.GONE);
                    } else {
                        StadiumCommentListActivity.this.tipsTextView.setVisibility(View.GONE);
                        StadiumCommentListActivity.this.stadiumCommentSmartRefreshLayout.setVisibility(View.VISIBLE);
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