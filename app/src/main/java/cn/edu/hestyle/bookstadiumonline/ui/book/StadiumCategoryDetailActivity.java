package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.content.Context;
import android.content.Intent;
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
import cn.edu.hestyle.bookstadiumonline.entity.Stadium;
import cn.edu.hestyle.bookstadiumonline.entity.StadiumCategory;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class StadiumCategoryDetailActivity extends BaseActivity {
    private StadiumCategory stadiumCategory;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<Stadium> stadiumList;
    private SmartRefreshLayout stadiumSmartRefreshLayout;
    private RecyclerView stadiumRecyclerView;
    private StadiumCategoryDetailActivity.RecycleAdapter recycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium_category_detail);

        ImageView stadiumCategoryImageView = findViewById(R.id.stadiumCategoryImageView);
        TextView stadiumCategoryTitleTextView = findViewById(R.id.stadiumCategoryTitleTextView);
        TextView stadiumCategoryDescriptionTextView = findViewById(R.id.stadiumCategoryDescriptionTextView);

        Intent intent = getIntent();
        this.stadiumCategory = (StadiumCategory) intent.getSerializableExtra("StadiumCategory");
        // 设置navigationBar
        if (this.stadiumCategory != null) {
            // title
            this.navigationBarInit(this.stadiumCategory.getTitle());
            stadiumCategoryTitleTextView.setText(String.format("%s", this.stadiumCategory.getTitle()));
            stadiumCategoryDescriptionTextView.setText(String.format("%s", this.stadiumCategory.getDescription()));
            // image
            if (this.stadiumCategory.getImagePath() != null) {
                Glide.with(StadiumCategoryDetailActivity.this)
                        .load(ServerSettingActivity.getServerHostUrl() + this.stadiumCategory.getImagePath())
                        .into(stadiumCategoryImageView);
            }
        }

        stadiumSmartRefreshLayout = findViewById(R.id.stadiumSmartRefreshLayout);
        stadiumSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                // 重新访问第一页
                StadiumCategoryDetailActivity.this.nextPageIndex = 1;
                StadiumCategoryDetailActivity.this.getNextPageFromServer();
            }
        });
        stadiumSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                StadiumCategoryDetailActivity.this.getNextPageFromServer();
            }
        });
        stadiumRecyclerView = findViewById(R.id.stadiumRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        stadiumRecyclerView.setLayoutManager(linearLayoutManager);
        recycleAdapter = new StadiumCategoryDetailActivity.RecycleAdapter(this);
        stadiumRecyclerView.setAdapter(recycleAdapter);
        // 添加分割线
        stadiumRecyclerView.addItemDecoration(new DividerItemDecoration(StadiumCategoryDetailActivity.this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (stadiumList == null) {
            this.nextPageIndex = 1;
            this.stadiumList = null;
            getNextPageFromServer();
        }
    }

    private void getNextPageFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            stadiumSmartRefreshLayout.finishLoadmore();
            stadiumSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("stadiumCategoryId", "" + stadiumCategory.getId())
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadium/findByStadiumCategoryId.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                StadiumCategoryDetailActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumCategoryDetailActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
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
                if (StadiumCategoryDetailActivity.this.nextPageIndex == 1) {
                    StadiumCategoryDetailActivity.this.stadiumList = stadiumList;
                } else {
                    StadiumCategoryDetailActivity.this.stadiumList.addAll(stadiumList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (stadiumList == null || stadiumList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                StadiumCategoryDetailActivity.this.runOnUiThread(()->{
                    if (StadiumCategoryDetailActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        StadiumCategoryDetailActivity.this.stadiumSmartRefreshLayout.finishRefresh();
                        StadiumCategoryDetailActivity.this.stadiumSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        StadiumCategoryDetailActivity.this.stadiumSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        StadiumCategoryDetailActivity.this.nextPageIndex += 1;
                    } else {
                        StadiumCategoryDetailActivity.this.nextPageIndex = 0;
                    }
                    StadiumCategoryDetailActivity.this.stadiumSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    StadiumCategoryDetailActivity.this.recycleAdapter.notifyDataSetChanged();
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
        titleTextView.setText(String.format("分类 - %s", title));
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
    }

    private class RecycleAdapter extends RecyclerView.Adapter<StadiumCategoryDetailActivity.RecycleAdapter.MyViewHolder>{
        private Context context;
        private View inflater;

        public RecycleAdapter(Context context){
            this.context = context;
        }

        @Override
        public StadiumCategoryDetailActivity.RecycleAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //创建ViewHolder，返回每一项的布局
            inflater = LayoutInflater.from(context).inflate(R.layout.item_stadium_recyclerview, parent, false);
            return new StadiumCategoryDetailActivity.RecycleAdapter.MyViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(StadiumCategoryDetailActivity.RecycleAdapter.MyViewHolder holder, int position) {
            // 将数据和控件绑定
            Stadium stadium = stadiumList.get(position);
            Log.i("Stadium", stadium.toString());
            // 加载网络图片(只加载第一张)
            if (stadium.getImagePaths() != null && stadium.getImagePaths().length() != 0) {
                String[] imagePaths = stadium.getImagePaths().split(",");
                Glide.with(inflater.getContext())
                        .load(ServerSettingActivity.getServerHostUrl() + imagePaths[0])
                        .into(holder.stadiumImageView);
            }
            holder.stadiumTitleTextView.setText(String.format("%s", stadium.getName()));
            holder.stadiumDescriptionTextView.setText(String.format("%s", stadium.getDescription()));
            holder.stadiumAddressTextView.setText(String.format("地址：%s", stadium.getAddress()));
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(context, "点击了体育场馆[ " + stadium.getName() + " ]", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            // 返回Item总条数
            if (stadiumList != null) {
                return stadiumList.size();
            } else {
                return 0;
            }
        }

        //内部类，绑定控件
        class MyViewHolder extends RecyclerView.ViewHolder{
            public ImageView stadiumImageView;
            public TextView stadiumTitleTextView;
            public TextView stadiumDescriptionTextView;
            public TextView stadiumAddressTextView;

            public MyViewHolder(View itemView) {
                super(itemView);
                stadiumImageView = itemView.findViewById(R.id.stadiumImageView);
                stadiumTitleTextView = itemView.findViewById(R.id.stadiumTitleTextView);
                stadiumDescriptionTextView = itemView.findViewById(R.id.stadiumDescriptionTextView);
                stadiumAddressTextView = itemView.findViewById(R.id.stadiumAddressTextView);
            }
        }
    }
}