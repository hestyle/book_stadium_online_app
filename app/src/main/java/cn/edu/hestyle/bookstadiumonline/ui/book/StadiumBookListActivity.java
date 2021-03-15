package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.StadiumBook;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class StadiumBookListActivity extends BaseActivity {
    private Integer stadiumId;
    private Integer stadiumManagerId;
    private TextView tipsTextView;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<StadiumBook> stadiumBookList;
    private SmartRefreshLayout stadiumBookSmartRefreshLayout;
    private RecyclerView stadiumBookRecyclerView;
    private StadiumBookRecycleAdapter stadiumBookRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium_book_list);

        Intent intent = getIntent();
        this.stadiumId = intent.getIntExtra("stadiumId", 0);
        this.stadiumManagerId = intent.getIntExtra("stadiumManagerId", 0);

        this.navigationBarInit("场馆预约");

        this.nextPageIndex = 1;
        this.stadiumBookList = null;
        this.tipsTextView = findViewById(R.id.tipsTextView);

        stadiumBookSmartRefreshLayout = findViewById(R.id.stadiumBookSmartRefreshLayout);
        stadiumBookSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                StadiumBookListActivity.this.nextPageIndex = 1;
                StadiumBookListActivity.this.getNextPageStadiumBookFromServer();
            }
        });
        stadiumBookSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                StadiumBookListActivity.this.getNextPageStadiumBookFromServer();
            }
        });
        stadiumBookRecyclerView = findViewById(R.id.stadiumBookRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        stadiumBookRecyclerView.setLayoutManager(linearLayoutManager);
        stadiumBookRecycleAdapter = new StadiumBookRecycleAdapter(this, stadiumBookList);
        stadiumBookRecyclerView.setAdapter(stadiumBookRecycleAdapter);
        // 添加分割线
        stadiumBookRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.stadiumId != null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.stadiumBookList = null;
            getNextPageStadiumBookFromServer();
        } else {
            this.tipsTextView.setText("暂无预约数据！");
            this.tipsTextView.setVisibility(View.VISIBLE);
            this.stadiumBookSmartRefreshLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 获取下一页StadiumBook
     */
    private void getNextPageStadiumBookFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            stadiumBookSmartRefreshLayout.finishLoadmore();
            stadiumBookSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("stadiumId", "" + this.stadiumId)
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadiumBook/userFindByStadiumIdAndPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                StadiumBookListActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumBookListActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<StadiumBook>>>(){}.getType();
                final ResponseResult<List<StadiumBook>> responseResult = gson.fromJson(responseString, type);
                List<StadiumBook> stadiumBookList = responseResult.getData();
                Log.i("StadiumBook", stadiumBookList + "");
                // 访问第一页，或者追加
                if (StadiumBookListActivity.this.nextPageIndex == 1) {
                    StadiumBookListActivity.this.stadiumBookList = stadiumBookList;
                } else {
                    StadiumBookListActivity.this.stadiumBookList.addAll(stadiumBookList);
                }
                // 按起始时间升序排列
                if (StadiumBookListActivity.this.stadiumBookList != null && StadiumBookListActivity.this.stadiumBookList.size() != 0) {
                    Collections.sort(StadiumBookListActivity.this.stadiumBookList, (o1, o2) -> o1.getStartTime().compareTo(o2.getStartTime()));
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (stadiumBookList == null || stadiumBookList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                StadiumBookListActivity.this.runOnUiThread(()->{
                    if (StadiumBookListActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        StadiumBookListActivity.this.stadiumBookSmartRefreshLayout.finishRefresh();
                        StadiumBookListActivity.this.stadiumBookSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        StadiumBookListActivity.this.stadiumBookSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        StadiumBookListActivity.this.nextPageIndex += 1;
                    } else {
                        StadiumBookListActivity.this.nextPageIndex = 0;
                    }
                    StadiumBookListActivity.this.stadiumBookSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    StadiumBookListActivity.this.stadiumBookRecycleAdapter.updateData(StadiumBookListActivity.this.stadiumBookList);
                    if (StadiumBookListActivity.this.stadiumBookList == null || StadiumBookListActivity.this.stadiumBookList.size() == 0) {
                        StadiumBookListActivity.this.tipsTextView.setText("暂无预约数据！");
                        StadiumBookListActivity.this.tipsTextView.setVisibility(View.VISIBLE);
                        StadiumBookListActivity.this.stadiumBookSmartRefreshLayout.setVisibility(View.GONE);
                    } else {
                        StadiumBookListActivity.this.tipsTextView.setVisibility(View.GONE);
                        StadiumBookListActivity.this.stadiumBookSmartRefreshLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    /**
     * 预约
     * @param stadiumBookId     stadiumBookId
     */
    private void bookStadiumBook(Integer stadiumBookId) {
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("stadiumBookId", "" + stadiumBookId)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadiumBookItem/add.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                StadiumBookListActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumBookListActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    StadiumBookListActivity.this.runOnUiThread(()->{
                        Toast.makeText(StadiumBookListActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                StadiumBookListActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumBookListActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit(String title) {
        ConstraintLayout commonTitleConstraintLayout = findViewById(R.id.stadium_book_list_navigation_bar);
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
        // rightStadiumManager按钮
        ImageButton rightStadiumManagerImageButton = new ImageButton(this);
        rightStadiumManagerImageButton.setBackgroundColor(Color.TRANSPARENT);
        rightStadiumManagerImageButton.setImageResource(R.drawable.ic_stadium_manager_white);
        ConstraintLayout.LayoutParams rightStadiumManagerLayoutParams = new ConstraintLayout.LayoutParams(56, 56);
        rightStadiumManagerLayoutParams.rightMargin = 15;
        rightStadiumManagerLayoutParams.endToEnd = R.id.stadium_book_list_navigation_bar;
        rightStadiumManagerLayoutParams.topToTop = R.id.stadium_book_list_navigation_bar;
        rightStadiumManagerLayoutParams.bottomToBottom = R.id.stadium_book_list_navigation_bar;
        rightStadiumManagerImageButton.setLayoutParams(rightStadiumManagerLayoutParams);
        rightStadiumManagerImageButton.setOnClickListener(v -> {
            Toast.makeText(StadiumBookListActivity.this, "点击了场馆管理员 " + StadiumBookListActivity.this.stadiumManagerId, Toast.LENGTH_SHORT).show();
        });
        commonTitleConstraintLayout.addView(rightStadiumManagerImageButton);
    }

    class StadiumBookRecycleAdapter extends RecyclerView.Adapter<StadiumBookRecycleAdapter.StadiumBookViewHolder> {
        private Context context;
        private View inflater;
        private List<StadiumBook> stadiumBookList;

        public StadiumBookRecycleAdapter(Context context, List<StadiumBook> stadiumBookList){
            this.context = context;
            this.stadiumBookList = stadiumBookList;
        }

        /**
         * 更新data
         * @param stadiumBookList    stadiumBookList
         */
        public void updateData(List<StadiumBook> stadiumBookList) {
            this.stadiumBookList = stadiumBookList;
            this.notifyDataSetChanged();
        }


        @Override
        public StadiumBookRecycleAdapter.StadiumBookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //创建ViewHolder，返回每一项的布局
            inflater = LayoutInflater.from(context).inflate(R.layout.item_stadium_book_recyclerview, parent, false);
            return new StadiumBookViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(StadiumBookRecycleAdapter.StadiumBookViewHolder holder, int position) {
            // 将数据和控件绑定
            StadiumBook stadiumBook = stadiumBookList.get(position);
            Log.i("stadiumBook", stadiumBook + "");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
            holder.startTimeTextView.setText(String.format("%s", simpleDateFormat.format(stadiumBook.getStartTime())));
            holder.endTimeTextView.setText(String.format("%s", simpleDateFormat.format(stadiumBook.getEndTime())));
            holder.maxBookCountTextView.setText(String.format("%d", stadiumBook.getMaxBookCount()));
            holder.nowBookCountTextView.setText(String.format("%d", stadiumBook.getNowBookCount()));
            if (stadiumBook.getMaxBookCount() <= stadiumBook.getNowBookCount()) {
                holder.bookActionTextView.setBackgroundColor(Color.GRAY);
            } else {
                holder.bookActionTextView.setBackgroundColor(Color.RED);
            }
            // 预约
            holder.bookActionTextView.setOnClickListener(v -> {
                if (stadiumBook.getMaxBookCount() > stadiumBook.getNowBookCount()) {
                    StadiumBookListActivity.this.bookStadiumBook(stadiumBook.getId());
                } else {
                    Toast.makeText(context, "该预约场次已无预约名额！", Toast.LENGTH_SHORT).show();
                }
            });
            // 查询已预约用户
            holder.checkHadBookedUserTextView.setOnClickListener(v -> {
                if (stadiumBook.getId() != null) {
                    Intent stadiumBookItemIntent = new Intent(context, StadiumBookItemListActivity.class);
                    stadiumBookItemIntent.putExtra("stadiumBookId", stadiumBook.getId());
                    context.startActivity(stadiumBookItemIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            // 返回Item总条数
            if (stadiumBookList != null) {
                return stadiumBookList.size();
            } else {
                return 0;
            }
        }

        //内部类，绑定控件
        class StadiumBookViewHolder extends RecyclerView.ViewHolder {
            public TextView startTimeTextView;
            public TextView endTimeTextView;
            public TextView maxBookCountTextView;
            public TextView nowBookCountTextView;
            public TextView bookActionTextView;
            public TextView checkHadBookedUserTextView;

            public StadiumBookViewHolder(View itemView) {
                super(itemView);
                startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
                endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
                maxBookCountTextView = itemView.findViewById(R.id.maxBookCountTextView);
                nowBookCountTextView = itemView.findViewById(R.id.nowBookCountTextView);
                bookActionTextView = itemView.findViewById(R.id.bookActionTextView);
                checkHadBookedUserTextView = itemView.findViewById(R.id.checkHadBookedUserTextView);
            }
        }
    }

}