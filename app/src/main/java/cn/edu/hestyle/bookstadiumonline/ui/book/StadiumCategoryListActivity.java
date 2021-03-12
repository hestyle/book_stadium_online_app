package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
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
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.StadiumCategory;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class StadiumCategoryListActivity extends BaseActivity {
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<StadiumCategory> stadiumCategoryList;
    private SmartRefreshLayout stadiumCategorySmartRefreshLayout;
    private RecyclerView stadiumCategoryRecyclerView;
    private RecycleAdapter recycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium_category_list);
        // 设置navigationBar
        this.navigationBarInit("体育场馆分类");

        this.nextPageIndex = 1;
        this.stadiumCategoryList = null;

        stadiumCategorySmartRefreshLayout = findViewById(R.id.stadiumCategorySmartRefreshLayout);
        stadiumCategorySmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                // 重新访问第一页
                StadiumCategoryListActivity.this.nextPageIndex = 1;
                StadiumCategoryListActivity.this.getNextPageFromServer();
            }
        });
        stadiumCategorySmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                StadiumCategoryListActivity.this.getNextPageFromServer();
            }
        });
        stadiumCategoryRecyclerView = findViewById(R.id.stadiumCategoryRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        stadiumCategoryRecyclerView.setLayoutManager(linearLayoutManager);
        recycleAdapter = new RecycleAdapter(this);
        stadiumCategoryRecyclerView.setAdapter(recycleAdapter);
        // 添加分割线
        stadiumCategoryRecyclerView.addItemDecoration(new DividerItemDecoration(StadiumCategoryListActivity.this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (stadiumCategoryList == null) {
            this.nextPageIndex = 1;
            this.stadiumCategoryList = null;
            getNextPageFromServer();
        }
    }

    private void getNextPageFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            stadiumCategorySmartRefreshLayout.finishLoadmore();
            stadiumCategorySmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder().add("pageIndex", "" + nextPageIndex).add("pageSize", "" + PER_PAGE_COUNT).build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadiumCategory/findByPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                StadiumCategoryListActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumCategoryListActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<StadiumCategory>>>(){}.getType();
                final ResponseResult<List<StadiumCategory>> responseResult = gson.fromJson(responseString, type);
                List<StadiumCategory> stadiumCategoryList = responseResult.getData();
                Log.i("StadiumCategory", stadiumCategoryList.toString());
                // 访问第一页，或者追加
                if (StadiumCategoryListActivity.this.nextPageIndex == 1) {
                    StadiumCategoryListActivity.this.stadiumCategoryList = stadiumCategoryList;
                } else {
                    StadiumCategoryListActivity.this.stadiumCategoryList.addAll(stadiumCategoryList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (stadiumCategoryList == null || stadiumCategoryList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                StadiumCategoryListActivity.this.runOnUiThread(()->{
                    if (StadiumCategoryListActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        StadiumCategoryListActivity.this.stadiumCategorySmartRefreshLayout.finishRefresh();
                        StadiumCategoryListActivity.this.stadiumCategorySmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        StadiumCategoryListActivity.this.stadiumCategorySmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        StadiumCategoryListActivity.this.nextPageIndex += 1;
                    } else {
                        StadiumCategoryListActivity.this.nextPageIndex = 0;
                    }
                    StadiumCategoryListActivity.this.stadiumCategorySmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    StadiumCategoryListActivity.this.recycleAdapter.notifyDataSetChanged();
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

    private class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.MyViewHolder>{
        private Context context;
        private View inflater;

        public RecycleAdapter(Context context){
            this.context = context;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //创建ViewHolder，返回每一项的布局
            inflater = LayoutInflater.from(context).inflate(R.layout.item_stadium_category_recyclerview, parent, false);
            return new MyViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            // 将数据和控件绑定
            StadiumCategory stadiumCategory = stadiumCategoryList.get(position);
            Log.i("StadiumCategory", stadiumCategory.toString());
            // 加载网络图片
            Glide.with(inflater.getContext())
                    .load(ServerSettingActivity.getServerHostUrl() + stadiumCategory.getImagePath())
                    .into(holder.stadiumCategoryImageView);
            holder.stadiumCategoryTitleTextView.setText(stadiumCategory.getTitle() + "");
            holder.stadiumCategoryDescriptionTextView.setText(stadiumCategory.getDescription() + "");
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(context, "点击了" + holder.stadiumCategoryTitleTextView.getText(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            // 返回Item总条数
            if (stadiumCategoryList != null) {
                return stadiumCategoryList.size();
            } else {
                return 0;
            }
        }

        //内部类，绑定控件
        class MyViewHolder extends RecyclerView.ViewHolder{
            public ImageView stadiumCategoryImageView;
            public TextView stadiumCategoryTitleTextView;
            public TextView stadiumCategoryDescriptionTextView;

            public MyViewHolder(View itemView) {
                super(itemView);
                stadiumCategoryImageView = itemView.findViewById(R.id.stadiumCategoryImageView);
                stadiumCategoryTitleTextView = itemView.findViewById(R.id.stadiumCategoryTitleTextView);
                stadiumCategoryDescriptionTextView = itemView.findViewById(R.id.stadiumCategoryDescriptionTextView);
            }
        }
    }
}