package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.text.SimpleDateFormat;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.SportKnowledge;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class SportKnowledgeListActivity extends BaseActivity {
    private TextView tipsTextView;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<SportKnowledge> sportKnowledgeList;
    private SmartRefreshLayout sportKnowledgeSmartRefreshLayout;
    private RecyclerView sportKnowledgeRecyclerView;
    private SportKnowledgeRecycleAdapter sportKnowledgeRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_knowledge_list);

        this.navigationBarInit("运动常识");

        this.nextPageIndex = 1;
        this.sportKnowledgeList = null;
        this.tipsTextView = findViewById(R.id.tipsTextView);

        sportKnowledgeSmartRefreshLayout = findViewById(R.id.sportKnowledgeSmartRefreshLayout);
        sportKnowledgeSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                SportKnowledgeListActivity.this.nextPageIndex = 1;
                SportKnowledgeListActivity.this.getNextPageSportKnowledgeFromServer();
            }
        });
        sportKnowledgeSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                SportKnowledgeListActivity.this.getNextPageSportKnowledgeFromServer();
            }
        });
        sportKnowledgeRecyclerView = findViewById(R.id.sportKnowledgeRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        sportKnowledgeRecyclerView.setLayoutManager(linearLayoutManager);
        sportKnowledgeRecycleAdapter = new SportKnowledgeRecycleAdapter(this, sportKnowledgeList);
        sportKnowledgeRecyclerView.setAdapter(sportKnowledgeRecycleAdapter);
        // 添加分割线
        sportKnowledgeRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.sportKnowledgeList == null || this.sportKnowledgeList.size() == 0) {
            // 获取第一页sportKnowledge
            this.nextPageIndex = 1;
            this.sportKnowledgeList = null;
            getNextPageSportKnowledgeFromServer();
        } else {
            this.sportKnowledgeRecycleAdapter.updateData(sportKnowledgeList);
        }
    }

    /**
     * 获取下一页sportKnowledge
     */
    private void getNextPageSportKnowledgeFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            sportKnowledgeSmartRefreshLayout.finishLoadmore();
            sportKnowledgeSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取sportKnowledge
        FormBody formBody = new FormBody.Builder()
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/sportKnowledge/findByPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                SportKnowledgeListActivity.this.runOnUiThread(()->{
                    Toast.makeText(SportKnowledgeListActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<SportKnowledge>>>(){}.getType();
                final ResponseResult<List<SportKnowledge>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    // 获取数据失败
                    SportKnowledgeListActivity.this.runOnUiThread(()->{
                        Toast.makeText(SportKnowledgeListActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<SportKnowledge> sportKnowledgeList = responseResult.getData();
                Log.i("SportKnowledge", sportKnowledgeList + "");
                // 访问第一页，或者追加
                if (SportKnowledgeListActivity.this.nextPageIndex == 1) {
                    SportKnowledgeListActivity.this.sportKnowledgeList = sportKnowledgeList;
                } else {
                    SportKnowledgeListActivity.this.sportKnowledgeList.addAll(sportKnowledgeList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (sportKnowledgeList == null || sportKnowledgeList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                SportKnowledgeListActivity.this.runOnUiThread(()->{
                    if (SportKnowledgeListActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        SportKnowledgeListActivity.this.sportKnowledgeSmartRefreshLayout.finishRefresh();
                        SportKnowledgeListActivity.this.sportKnowledgeSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        SportKnowledgeListActivity.this.sportKnowledgeSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        SportKnowledgeListActivity.this.nextPageIndex += 1;
                    } else {
                        SportKnowledgeListActivity.this.nextPageIndex = 0;
                    }
                    SportKnowledgeListActivity.this.sportKnowledgeSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    SportKnowledgeListActivity.this.sportKnowledgeRecycleAdapter.updateData(SportKnowledgeListActivity.this.sportKnowledgeList);
                    if (SportKnowledgeListActivity.this.sportKnowledgeList == null || SportKnowledgeListActivity.this.sportKnowledgeList.size() == 0) {
                        SportKnowledgeListActivity.this.tipsTextView.setText("暂无运动常识数据！");
                        SportKnowledgeListActivity.this.tipsTextView.setVisibility(View.VISIBLE);
                        SportKnowledgeListActivity.this.sportKnowledgeSmartRefreshLayout.setVisibility(View.GONE);
                    } else {
                        SportKnowledgeListActivity.this.tipsTextView.setVisibility(View.GONE);
                        SportKnowledgeListActivity.this.sportKnowledgeSmartRefreshLayout.setVisibility(View.VISIBLE);
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

    class SportKnowledgeRecycleAdapter extends RecyclerView.Adapter<SportKnowledgeRecycleAdapter.SportKnowledgeTitleViewHolder> {
        private Activity activityContext;
        private View inflater;
        private List<SportKnowledge> sportKnowledgeList;

        public SportKnowledgeRecycleAdapter(Activity activityContext, List<SportKnowledge> sportKnowledgeList){
            this.activityContext = activityContext;
            this.sportKnowledgeList = sportKnowledgeList;
        }

        /**
         * 更新data
         * @param sportKnowledgeList    sportKnowledgeList
         */
        public void updateData(List<SportKnowledge> sportKnowledgeList) {
            this.sportKnowledgeList = sportKnowledgeList;
            this.notifyDataSetChanged();
        }


        @Override
        public SportKnowledgeRecycleAdapter.SportKnowledgeTitleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //创建ViewHolder，返回每一项的布局
            inflater = LayoutInflater.from(activityContext).inflate(R.layout.item_sport_knowledge_title_recyclerview, parent, false);
            return new SportKnowledgeRecycleAdapter.SportKnowledgeTitleViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(SportKnowledgeRecycleAdapter.SportKnowledgeTitleViewHolder holder, int position) {
            // 将数据和控件绑定
            SportKnowledge sportKnowledge = sportKnowledgeList.get(position);
            Log.i("SportKnowledge", sportKnowledge + "");
            holder.titleTextView.setText(String.format("%s", sportKnowledge.getTitle()));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
            if (sportKnowledge.getModifiedTime() != null) {
                holder.createdTimeTextView.setText(String.format("%s", simpleDateFormat.format(sportKnowledge.getModifiedTime())));
            } else {
                holder.createdTimeTextView.setText(String.format("%s", simpleDateFormat.format(sportKnowledge.getCreatedTime())));
            }
            // 点击事件
            holder.sportKnowledgeConstraintLayout.setOnClickListener(v -> {
                // 进入运动常识详情页面
                Intent intent = new Intent(activityContext, SportKnowledgeDetailActivity.class);
                intent.putExtra("SportKnowledge", sportKnowledge);
                activityContext.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            // 返回Item总条数
            if (sportKnowledgeList != null) {
                return sportKnowledgeList.size();
            } else {
                return 0;
            }
        }

        // 内部类，绑定控件
        class SportKnowledgeTitleViewHolder extends RecyclerView.ViewHolder {
            public ConstraintLayout sportKnowledgeConstraintLayout;
            public TextView titleTextView;
            public TextView createdTimeTextView;

            public SportKnowledgeTitleViewHolder(View itemView) {
                super(itemView);
                sportKnowledgeConstraintLayout = itemView.findViewById(R.id.sportKnowledgeConstraintLayout);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                createdTimeTextView = itemView.findViewById(R.id.createdTimeTextView);
            }
        }
    }
}