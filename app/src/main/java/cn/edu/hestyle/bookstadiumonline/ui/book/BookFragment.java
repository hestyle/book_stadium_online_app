package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
import cn.edu.hestyle.bookstadiumonline.entity.StadiumCategory;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class BookFragment extends Fragment {
    private View rootView;
    private Banner banner;
    private List<BannerItem> bannerItemList;
    private StadiumCategoryGridView stadiumCategoryGridView;
    private List<StadiumCategory> stadiumCategoryList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_book, container, false);
        TextView titleTextView = this.rootView.findViewById(R.id.titleTextView);
        titleTextView.setText("搜索体育场馆");

        this.bannerItemList = null;
        this.stadiumCategoryList = null;

        this.banner = this.rootView.findViewById(R.id.banner);
        this.stadiumCategoryGridView = this.rootView.findViewById(R.id.stadiumCategoryGridView);

        return this.rootView;
    }

    private void bannerInit(List<String> imageList) {
        // 设置圆形指示器
        this.banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        // 设置指示器位置
        this.banner.setIndicatorGravity(BannerConfig.CENTER);
        // 自动轮播
        this.banner.isAutoPlay(true);
        // 设置轮播间隔
        this.banner.setDelayTime(5000);
        // 设置图片资源
        this.banner.setImages(imageList);
        this.banner.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                Log.i("BannerItem", path.toString());
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
            Toast.makeText(BookFragment.this.getContext(), "点击了第" + position + "个StadiumCategory", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.bannerItemList == null) {
            // 从服务器获取banner
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
                    // 提取出image path
                    List<String> imageList = new ArrayList<>();
                    if (responseResult.getData() != null) {
                        for (BannerItem bannerItem : responseResult.getData()) {
                            imageList.add(ServerSettingActivity.getServerHostUrl() + bannerItem.getImagePath());
                        }
                    }
                    Log.i("Banner", imageList.toString());
                    BookFragment.this.getActivity().runOnUiThread(()->{
                        BookFragment.this.bannerInit(imageList);
                    });
                }
            });
        }
        if (this.stadiumCategoryList == null) {
            // 从服务器获取stadiumCategory
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
        } else {
            this.stadiumCategoryGridViewInit();
        }
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