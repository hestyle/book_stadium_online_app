package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import cn.edu.hestyle.bookstadiumonline.entity.Stadium;
import cn.edu.hestyle.bookstadiumonline.adapter.StadiumRecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class StadiumSearchActivity extends BaseActivity {
    private TextView tipsTextView;
    private EditText searchEditText;
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
        setContentView(R.layout.activity_stadium_serach);

        // 设置navigationBar
        this.navigationBarInit();

        this.nextPageIndex = 1;
        this.stadiumList = null;

        this.tipsTextView = findViewById(R.id.tipsTextView);
        this.searchEditText = findViewById(R.id.searchEditText);

        stadiumSmartRefreshLayout = findViewById(R.id.stadiumSmartRefreshLayout);
        stadiumSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                String name = StadiumSearchActivity.this.searchEditText.getText().toString();
                StadiumSearchActivity.this.nextPageIndex = 1;
                StadiumSearchActivity.this.getStadiumFromServer(name, StadiumSearchActivity.this.nextPageIndex);
            }
        });
        stadiumSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                String name = StadiumSearchActivity.this.searchEditText.getText().toString();
                StadiumSearchActivity.this.getStadiumFromServer(name, StadiumSearchActivity.this.nextPageIndex);
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

        TextView searchTextView = findViewById(R.id.searchTextView);
        searchTextView.setOnClickListener(v -> {
            String name = StadiumSearchActivity.this.searchEditText.getText().toString();
            if (name.length() == 0) {
                Toast.makeText(StadiumSearchActivity.this, "请输入需要搜索的体育场馆名称！", Toast.LENGTH_SHORT).show();
            } else {
                StadiumSearchActivity.this.nextPageIndex = 1;
                StadiumSearchActivity.this.getStadiumFromServer(name, StadiumSearchActivity.this.nextPageIndex);
            }
        });

        tipsTextView.setVisibility(View.GONE);
        stadiumSmartRefreshLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 获取下一页Stadium
     */
    private void getStadiumFromServer(String name, Integer pageIndex) {
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("name", "" + name)
                .add("pageIndex", "" + pageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadium/findByName.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                StadiumSearchActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumSearchActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<Stadium>>>(){}.getType();
                final ResponseResult<List<Stadium>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    StadiumSearchActivity.this.runOnUiThread(()->{
                        Toast.makeText(StadiumSearchActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<Stadium> stadiumList = responseResult.getData();
                Log.i("Stadium", stadiumList.toString());
                // 访问第一页，或者追加
                if (StadiumSearchActivity.this.nextPageIndex == 1) {
                    StadiumSearchActivity.this.stadiumList = stadiumList;
                } else {
                    StadiumSearchActivity.this.stadiumList.addAll(stadiumList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (stadiumList == null || stadiumList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                StadiumSearchActivity.this.runOnUiThread(()->{
                    if (StadiumSearchActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        StadiumSearchActivity.this.stadiumSmartRefreshLayout.finishRefresh();
                        StadiumSearchActivity.this.stadiumSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        StadiumSearchActivity.this.stadiumSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        StadiumSearchActivity.this.nextPageIndex += 1;
                    } else {
                        StadiumSearchActivity.this.nextPageIndex = 0;
                    }
                    StadiumSearchActivity.this.stadiumSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    StadiumSearchActivity.this.stadiumRecycleAdapter.updateData(StadiumSearchActivity.this.stadiumList);
                    if (StadiumSearchActivity.this.stadiumList == null || StadiumSearchActivity.this.stadiumList.size() == 0) {
                        StadiumSearchActivity.this.stadiumSmartRefreshLayout.setVisibility(View.GONE);
                        StadiumSearchActivity.this.tipsTextView.setText("未搜索到内容！");
                        StadiumSearchActivity.this.tipsTextView.setVisibility(View.VISIBLE);
                    } else {
                        StadiumSearchActivity.this.stadiumSmartRefreshLayout.setVisibility(View.VISIBLE);
                        StadiumSearchActivity.this.tipsTextView.setVisibility(View.GONE);
                    }
                });
            }
        });
    }


    /**
     * 设置navigationBar
     */
    private void navigationBarInit() {
        // 设置title
        EditText searchEditText = this.findViewById(R.id.searchEditText);
        searchEditText.setText("");
        searchEditText.setHint("请输入体育场馆名称");
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
    }
}