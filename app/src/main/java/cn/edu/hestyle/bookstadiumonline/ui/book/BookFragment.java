package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
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
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.loader.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.BannerItem;
import cn.edu.hestyle.bookstadiumonline.entity.Stadium;
import cn.edu.hestyle.bookstadiumonline.entity.StadiumCategory;
import cn.edu.hestyle.bookstadiumonline.ui.book.view.StadiumCategoryGridView;
import cn.edu.hestyle.bookstadiumonline.ui.book.adapter.StadiumRecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class BookFragment extends Fragment {
    private View rootView;
    /** 顶部轮播 */
    private Banner banner;
    /** 中部场馆分类 */
    private List<BannerItem> bannerItemList;
    private StadiumCategoryGridView stadiumCategoryGridView;
    private List<StadiumCategory> stadiumCategoryList;
    /** 尾部场馆推荐 */
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<Stadium> stadiumList;
    private SmartRefreshLayout stadiumSmartRefreshLayout;
    private RecyclerView stadiumRecyclerView;
    private StadiumRecycleAdapter stadiumRecycleAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_book, container, false);
        TextView titleTextView = this.rootView.findViewById(R.id.titleTextView);
        titleTextView.setText("搜索体育场馆");

        this.bannerItemList = null;
        this.stadiumCategoryList = null;

        this.banner = this.rootView.findViewById(R.id.banner);
        this.stadiumCategoryGridView = this.rootView.findViewById(R.id.stadiumCategoryGridView);

        // 所有场馆分类action
        TextView gotoAllStadiumCategoryTextView = this.rootView.findViewById(R.id.gotoAllStadiumCategoryTextView);
        gotoAllStadiumCategoryTextView.setOnClickListener(v -> {
            Intent intent = new Intent(BookFragment.this.getActivity(), StadiumCategoryListActivity.class);
            startActivity(intent);
        });

        this.nextPageIndex = 1;
        this.stadiumList = null;

        stadiumSmartRefreshLayout = this.rootView.findViewById(R.id.stadiumSmartRefreshLayout);
        stadiumSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                // 重新获取banner
                BookFragment.this.getBannerFromServer();
                // 重新获取StadiumCategory
                BookFragment.this.getStadiumCategoryFromServer();
                // 重新访问第一页
                BookFragment.this.nextPageIndex = 1;
                BookFragment.this.getNextPageStadiumFromServer();
            }
        });
        stadiumSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                BookFragment.this.getNextPageStadiumFromServer();
            }
        });
        stadiumRecyclerView = this.rootView.findViewById(R.id.stadiumRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        stadiumRecyclerView.setLayoutManager(linearLayoutManager);
        stadiumRecycleAdapter = new StadiumRecycleAdapter(this.getContext(), stadiumList);
        stadiumRecyclerView.setAdapter(stadiumRecycleAdapter);
        // 添加分割线
        stadiumRecyclerView.addItemDecoration(new DividerItemDecoration(BookFragment.this.getContext(), DividerItemDecoration.VERTICAL));

        // 所有推荐场馆action
        TextView gotoAllStadiumTextView = this.rootView.findViewById(R.id.gotoAllStadiumTextView);
        gotoAllStadiumTextView.setOnClickListener(v -> {
            Intent intent = new Intent(BookFragment.this.getActivity(), StadiumListActivity.class);
            startActivity(intent);
        });

        // 场馆搜索action
        ConstraintLayout searchConstraintLayout = this.rootView.findViewById(R.id.fragment_book_navigation_bar);
        searchConstraintLayout.setOnClickListener(v -> {
            Intent intent = new Intent(BookFragment.this.getActivity(), StadiumSearchActivity.class);
            startActivity(intent);
        });
        return this.rootView;
    }

    private void bannerInit() {
        // 设置圆形指示器
        this.banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        // 设置指示器位置
        this.banner.setIndicatorGravity(BannerConfig.CENTER);
        // 自动轮播
        this.banner.isAutoPlay(true);
        // 设置轮播间隔
        this.banner.setDelayTime(5000);
        // 设置图片资源
        List<String> imageList = new ArrayList<>();
        if (this.bannerItemList != null) {
            for (BannerItem bannerItem : this.bannerItemList) {
                imageList.add(ServerSettingActivity.getServerHostUrl() + bannerItem.getImagePath());
            }
        }
        this.banner.setImages(imageList);
        // 设置图片加载器
        this.banner.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                Glide.with(context).load(path.toString()).into(imageView);
            }
        });
        // 设置点击事件
        this.banner.setOnBannerListener(position -> {
            // 进入BannerItemDetailActivity
            BookFragment.this.getActivity().runOnUiThread(()->{
                Intent intent = new Intent(BookFragment.this.getActivity(), BannerItemDetailActivity.class);
                intent.putExtra("BannerItem", bannerItemList.get(position));
                startActivity(intent);
            });
        });
        this.banner.start();
    }

    private void stadiumCategoryGridViewInit() {
        this.stadiumCategoryGridView.setAdapter(new StadiumCategoryAdapter(stadiumCategoryList, BookFragment.this.getContext()));
        this.stadiumCategoryGridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(BookFragment.this.getActivity(), StadiumCategoryDetailActivity.class);
            intent.putExtra("StadiumCategory", stadiumCategoryList.get(position));
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.bannerItemList == null) {
            this.getBannerFromServer();
        } else {
            this.bannerInit();
        }
        if (this.stadiumCategoryList == null) {
            this.getStadiumCategoryFromServer();
        } else {
            this.stadiumCategoryGridViewInit();
        }
        if (this.stadiumList == null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.stadiumList = null;
            getNextPageStadiumFromServer();
        }
    }

    /**
     * 从服务器获取banner
     */
    private void getBannerFromServer() {
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/banner/findAll.do", null, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                BookFragment.this.getActivity().runOnUiThread(()->{
                    Toast.makeText(BookFragment.this.getContext(), "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<BannerItem>>>(){}.getType();
                final ResponseResult<List<BannerItem>> responseResult = gson.fromJson(responseString, type);
                BookFragment.this.bannerItemList = responseResult.getData();
                Log.i("Banner", BookFragment.this.bannerItemList.toString());
                BookFragment.this.getActivity().runOnUiThread(()->{
                    BookFragment.this.bannerInit();
                });
            }
        });
    }

    /**
     * 从服务器获取stadiumCategory
     */
    private void getStadiumCategoryFromServer() {
        FormBody formBody = new FormBody.Builder().add("pageIndex", "1").add("pageSize", "10").build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadiumCategory/findByPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                BookFragment.this.getActivity().runOnUiThread(()->{
                    Toast.makeText(BookFragment.this.getContext(), "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<StadiumCategory>>>(){}.getType();
                final ResponseResult<List<StadiumCategory>> responseResult = gson.fromJson(responseString, type);
                BookFragment.this.stadiumCategoryList = responseResult.getData();
                Log.i("StadiumCategory", BookFragment.this.stadiumCategoryList.toString());
                BookFragment.this.getActivity().runOnUiThread(()->{
                    BookFragment.this.stadiumCategoryGridViewInit();
                });
            }
        });
    }

    /**
     * 获取下一页Stadium
     */
    private void getNextPageStadiumFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this.getContext(), "暂无更多内容！", Toast.LENGTH_SHORT).show();
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
                BookFragment.this.getActivity().runOnUiThread(()->{
                    Toast.makeText(BookFragment.this.getContext(), "网络访问失败！", Toast.LENGTH_SHORT).show();
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
                if (BookFragment.this.nextPageIndex == 1) {
                    BookFragment.this.stadiumList = stadiumList;
                } else {
                    BookFragment.this.stadiumList.addAll(stadiumList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (stadiumList == null || stadiumList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                BookFragment.this.getActivity().runOnUiThread(()->{
                    if (BookFragment.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        BookFragment.this.stadiumSmartRefreshLayout.finishRefresh();
                        BookFragment.this.stadiumSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        BookFragment.this.stadiumSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        BookFragment.this.nextPageIndex += 1;
                    } else {
                        BookFragment.this.nextPageIndex = 0;
                    }
                    BookFragment.this.stadiumSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    BookFragment.this.stadiumRecycleAdapter.updateData(BookFragment.this.stadiumList);
                });
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private static class StadiumCategoryViewHolder {
        public ImageView stadiumCategoryImageView;
        public TextView stadiumCategoryTitleTextView;
    }

    private static class StadiumCategoryAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<StadiumCategory> stadiumCategoryList;

        public StadiumCategoryAdapter(List<StadiumCategory> stadiumCategoryList, Context context) {
            super();
            this.stadiumCategoryList = stadiumCategoryList;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if (this.stadiumCategoryList != null) {
                return this.stadiumCategoryList.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return this.stadiumCategoryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            StadiumCategoryViewHolder stadiumCategoryViewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_stadium_category_gridview, null);
                stadiumCategoryViewHolder = new StadiumCategoryViewHolder();
                stadiumCategoryViewHolder.stadiumCategoryImageView = convertView.findViewById(R.id.stadiumCategoryImageView);
                stadiumCategoryViewHolder.stadiumCategoryTitleTextView = convertView.findViewById(R.id.stadiumCategoryTitleTextView);
                convertView.setTag(stadiumCategoryViewHolder);
            } else {
                stadiumCategoryViewHolder = (StadiumCategoryViewHolder) convertView.getTag();
            }
            StadiumCategory stadiumCategory = this.stadiumCategoryList.get(position);
            // 加载网络图片
            Glide.with(inflater.getContext())
                    .load(ServerSettingActivity.getServerHostUrl() + stadiumCategory.getImagePath())
                    .into(stadiumCategoryViewHolder.stadiumCategoryImageView);
            stadiumCategoryViewHolder.stadiumCategoryTitleTextView.setText(stadiumCategory.getTitle());
            return convertView;
        }
    }
}