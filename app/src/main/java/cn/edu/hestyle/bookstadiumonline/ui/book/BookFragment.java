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
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class BookFragment extends Fragment {
    private View rootView;
    private Banner banner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_book, container, false);
        TextView titleTextView = this.rootView.findViewById(R.id.titleTextView);
        titleTextView.setText("搜索体育场馆");

        this.banner = this.rootView.findViewById(R.id.banner);

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
                Log.i("Banner", path.toString());
                Glide.with(context).load(path.toString()).into(imageView);
            }
        });
        // 设置点击事件
        this.banner.setOnBannerListener(position -> {
            Toast.makeText(BookFragment.this.getContext(), "您点击了轮播第 " + position + " 张图片！", Toast.LENGTH_SHORT).show();
        });
        this.banner.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 获取服务器banner
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/banner/findAll.do", null, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(BookFragment.this.getContext(), "网络访问失败！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<BannerItem>>>(){}.getType();
                final ResponseResult<List<BannerItem>> responseResult = gson.fromJson(responseString, type);
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

    @Override
    public void onStop() {
        super.onStop();

    }
}