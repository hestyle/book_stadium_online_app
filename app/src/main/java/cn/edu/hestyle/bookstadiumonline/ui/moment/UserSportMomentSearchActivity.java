package cn.edu.hestyle.bookstadiumonline.ui.moment;

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
import cn.edu.hestyle.bookstadiumonline.adapter.UserSportMomentRecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.entity.UserSportMoment;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class UserSportMomentSearchActivity extends BaseActivity {
    private TextView tipsTextView;
    private EditText searchEditText;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<UserSportMoment> userSportMomentList;
    private SmartRefreshLayout userSportMomentSmartRefreshLayout;
    private RecyclerView userSportMomentRecyclerView;
    private UserSportMomentRecycleAdapter userSportMomentRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sport_moment_search);

        // 设置navigationBar
        this.navigationBarInit();

        this.nextPageIndex = 1;
        this.userSportMomentList = null;

        this.tipsTextView = findViewById(R.id.tipsTextView);
        this.searchEditText = findViewById(R.id.searchEditText);

        userSportMomentSmartRefreshLayout = findViewById(R.id.userSportMomentSmartRefreshLayout);
        userSportMomentSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                String name = UserSportMomentSearchActivity.this.searchEditText.getText().toString();
                UserSportMomentSearchActivity.this.nextPageIndex = 1;
                UserSportMomentSearchActivity.this.getNextPageUserSportMomentFromServer(name, UserSportMomentSearchActivity.this.nextPageIndex);
            }
        });
        userSportMomentSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                String contentKey = UserSportMomentSearchActivity.this.searchEditText.getText().toString();
                UserSportMomentSearchActivity.this.getNextPageUserSportMomentFromServer(contentKey, UserSportMomentSearchActivity.this.nextPageIndex);
            }
        });
        userSportMomentRecyclerView = findViewById(R.id.userSportMomentRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        userSportMomentRecyclerView.setLayoutManager(linearLayoutManager);
        userSportMomentRecycleAdapter = new UserSportMomentRecycleAdapter(this, userSportMomentList);
        userSportMomentRecyclerView.setAdapter(userSportMomentRecycleAdapter);
        // 添加分割线
        userSportMomentRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        TextView searchTextView = findViewById(R.id.searchTextView);
        searchTextView.setOnClickListener(v -> {
            String name = UserSportMomentSearchActivity.this.searchEditText.getText().toString();
            if (name.length() == 0) {
                Toast.makeText(UserSportMomentSearchActivity.this, "请输入搜索内容关键字！", Toast.LENGTH_SHORT).show();
            } else {
                UserSportMomentSearchActivity.this.nextPageIndex = 1;
                UserSportMomentSearchActivity.this.getNextPageUserSportMomentFromServer(name, UserSportMomentSearchActivity.this.nextPageIndex);
            }
        });

        tipsTextView.setVisibility(View.GONE);
        userSportMomentSmartRefreshLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 获取下一页UserSportMoment
     */
    private void getNextPageUserSportMomentFromServer(String contentKey, Integer pageIndex) {
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("contentKey", "" + contentKey)
                .add("pageIndex", "" + pageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/findByContentKeyPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                UserSportMomentSearchActivity.this.runOnUiThread(()->{
                    Toast.makeText(UserSportMomentSearchActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<UserSportMoment>>>(){}.getType();
                final ResponseResult<List<UserSportMoment>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    UserSportMomentSearchActivity.this.runOnUiThread(()->{
                        Toast.makeText(UserSportMomentSearchActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<UserSportMoment> userSportMomentList = responseResult.getData();
                Log.i("UserSportMoment", userSportMomentList + "");
                // 访问第一页，或者追加
                if (UserSportMomentSearchActivity.this.nextPageIndex == 1) {
                    UserSportMomentSearchActivity.this.userSportMomentList = userSportMomentList;
                } else {
                    UserSportMomentSearchActivity.this.userSportMomentList.addAll(userSportMomentList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (userSportMomentList == null || userSportMomentList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                UserSportMomentSearchActivity.this.runOnUiThread(()->{
                    if (UserSportMomentSearchActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        UserSportMomentSearchActivity.this.userSportMomentSmartRefreshLayout.finishRefresh();
                        UserSportMomentSearchActivity.this.userSportMomentSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        UserSportMomentSearchActivity.this.userSportMomentSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        UserSportMomentSearchActivity.this.nextPageIndex += 1;
                    } else {
                        UserSportMomentSearchActivity.this.nextPageIndex = 0;
                    }
                    UserSportMomentSearchActivity.this.userSportMomentSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    UserSportMomentSearchActivity.this.userSportMomentRecycleAdapter.updateData(UserSportMomentSearchActivity.this.userSportMomentList);
                    if (UserSportMomentSearchActivity.this.userSportMomentList == null || UserSportMomentSearchActivity.this.userSportMomentList.size() == 0) {
                        UserSportMomentSearchActivity.this.userSportMomentSmartRefreshLayout.setVisibility(View.GONE);
                        UserSportMomentSearchActivity.this.tipsTextView.setText("未搜索到内容！");
                        UserSportMomentSearchActivity.this.tipsTextView.setVisibility(View.VISIBLE);
                    } else {
                        UserSportMomentSearchActivity.this.userSportMomentSmartRefreshLayout.setVisibility(View.VISIBLE);
                        UserSportMomentSearchActivity.this.tipsTextView.setVisibility(View.GONE);
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
        searchEditText.setHint("请输入动态内容的关键字~");
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
    }
}