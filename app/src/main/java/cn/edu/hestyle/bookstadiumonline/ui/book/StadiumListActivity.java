package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
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
import cn.edu.hestyle.bookstadiumonline.entity.Stadium;
import cn.edu.hestyle.bookstadiumonline.ui.book.adapter.StadiumRecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class StadiumListActivity extends BaseActivity {
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<Stadium> stadiumList;
    private SmartRefreshLayout stadiumSmartRefreshLayout;
    private RecyclerView stadiumRecyclerView;
    private StadiumRecycleAdapter stadiumRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium_list);

        this.navigationBarInit("体育场馆");

        this.nextPageIndex = 1;
        this.stadiumList = null;

        stadiumSmartRefreshLayout = findViewById(R.id.stadiumSmartRefreshLayout);
        stadiumSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                StadiumListActivity.this.nextPageIndex = 1;
                StadiumListActivity.this.getNextPageStadiumFromServer();
            }
        });
        stadiumSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                StadiumListActivity.this.getNextPageStadiumFromServer();
            }
        });
        stadiumRecyclerView = findViewById(R.id.stadiumRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        stadiumRecyclerView.setLayoutManager(linearLayoutManager);
        stadiumRecycleAdapter = new StadiumRecycleAdapter(this, stadiumList);
        stadiumRecyclerView.setAdapter(stadiumRecycleAdapter);
        // 添加分割线
        stadiumRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.stadiumList == null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.stadiumList = null;
            getNextPageStadiumFromServer();
        }
    }

    /**
     * 获取下一页Stadium
     */
    private void getNextPageStadiumFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            stadiumSmartRefreshLayout.finishLoadmore();
            stadiumSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadium/findByPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                StadiumListActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumListActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<Stadium>>>(){}.getType();
                final ResponseResult<List<Stadium>> responseResult = gson.fromJson(responseString, type);
                List<Stadium> stadiumList = responseResult.getData();
                Log.i("Stadium", stadiumList.toString());
                // 访问第一页，或者追加
                if (StadiumListActivity.this.nextPageIndex == 1) {
                    StadiumListActivity.this.stadiumList = stadiumList;
                } else {
                    StadiumListActivity.this.stadiumList.addAll(stadiumList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (stadiumList == null || stadiumList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                StadiumListActivity.this.runOnUiThread(()->{
                    if (StadiumListActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        StadiumListActivity.this.stadiumSmartRefreshLayout.finishRefresh();
                        StadiumListActivity.this.stadiumSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        StadiumListActivity.this.stadiumSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        StadiumListActivity.this.nextPageIndex += 1;
                    } else {
                        StadiumListActivity.this.nextPageIndex = 0;
                    }
                    StadiumListActivity.this.stadiumSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    StadiumListActivity.this.stadiumRecycleAdapter.updateData(StadiumListActivity.this.stadiumList);
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

        ConstraintLayout stadiumListConstraintLayout = findViewById(R.id.stadium_list_navigation_bar);
        // 设置right search
        ImageButton rightSearchImageButton = new ImageButton(this);
        rightSearchImageButton.setBackgroundColor(Color.TRANSPARENT);
        rightSearchImageButton.setImageResource(R.drawable.ic_search_white);
        ConstraintLayout.LayoutParams rightSearchLayoutParams = new ConstraintLayout.LayoutParams(56, 56);
        rightSearchLayoutParams.rightMargin = 15;
        rightSearchLayoutParams.endToEnd = R.id.stadium_list_navigation_bar;
        rightSearchLayoutParams.topToTop = R.id.stadium_list_navigation_bar;
        rightSearchLayoutParams.bottomToBottom = R.id.stadium_list_navigation_bar;
        rightSearchImageButton.setLayoutParams(rightSearchLayoutParams);
        rightSearchImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(StadiumListActivity.this, StadiumSearchActivity.class);
            startActivity(intent);
        });
        stadiumListConstraintLayout.addView(rightSearchImageButton);

    }
}