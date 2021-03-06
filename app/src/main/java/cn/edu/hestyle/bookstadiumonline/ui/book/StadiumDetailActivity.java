package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.loader.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.Report;
import cn.edu.hestyle.bookstadiumonline.entity.Stadium;
import cn.edu.hestyle.bookstadiumonline.ui.message.ChattingActivity;
import cn.edu.hestyle.bookstadiumonline.ui.moment.ReportActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class StadiumDetailActivity extends BaseActivity {
    private Integer stadiumId;
    private Stadium stadium;
    private Banner stadiumBanner;
    private TextView titleTextView;
    private TextView stadiumDetailTextView;
    private SmartRefreshLayout stadiumSmartRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium_detail);
        Intent intent = getIntent();
        this.stadiumId = intent.getIntExtra("stadiumId", 0);
        this.stadium = (Stadium) intent.getSerializableExtra("Stadium");
        // 设置navigationBar
        if (this.stadium != null) {
            if (stadium.getId() != null) {
                this.stadiumId = stadium.getId();
            }
            this.navigationBarInit(this.stadium.getName());
        } else {
            this.navigationBarInit("体育场馆");
        }
        this.stadiumBanner = findViewById(R.id.stadiumBanner);
        this.titleTextView = findViewById(R.id.titleTextView);
        this.stadiumDetailTextView = findViewById(R.id.stadiumDetailTextView);

        this.stadiumSmartRefreshLayout = findViewById(R.id.stadiumSmartRefreshLayout);
        stadiumSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                // 刷新
                if (StadiumDetailActivity.this.stadium != null && StadiumDetailActivity.this.stadium.getId() != null) {
                    StadiumDetailActivity.this.getStadiumByIdFromServer(StadiumDetailActivity.this.stadium.getId());
                }
            }
        });

        // 查看所有评论
        TextView gotoStadiumAllCommentTextView = findViewById(R.id.gotoStadiumAllCommentTextView);
        gotoStadiumAllCommentTextView.setOnClickListener(v -> {
            if (stadiumId != null) {
                Intent stadiumCommentIntent = new Intent(StadiumDetailActivity.this, StadiumCommentListActivity.class);
                stadiumCommentIntent.putExtra("stadiumId", stadiumId);
                startActivity(stadiumCommentIntent);
            }
        });
        // 选择预约时段
        TextView selectStadiumBookTextView = findViewById(R.id.selectStadiumBookTextView);
        selectStadiumBookTextView.setOnClickListener(v -> {
            StadiumDetailActivity.this.selectStadiumBookAction();
        });

        // 咨询场馆管理员
        TextView stadiumManagerTextView = findViewById(R.id.stadiumManagerTextView);
        stadiumManagerTextView.setOnClickListener(v -> {
            if (LoginUserInfoUtil.getLoginUser() == null) {
                Toast.makeText(StadiumDetailActivity.this, "请先进行登录！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (StadiumDetailActivity.this.stadium == null) {
                Toast.makeText(StadiumDetailActivity.this, "程序内部发生错误！", Toast.LENGTH_SHORT).show();
                return;
            }
            // 跳转到聊天页面
            Intent chattingActivityIntent = new Intent(StadiumDetailActivity.this, ChattingActivity.class);
            chattingActivityIntent.putExtra("stadiumManagerId", StadiumDetailActivity.this.stadium.getStadiumManagerId());
            StadiumDetailActivity.this.startActivity(chattingActivityIntent);
        });

        // 立即预约action
        TextView bookActionTextView = findViewById(R.id.bookActionTextView);
        bookActionTextView.setOnClickListener(v -> {
            StadiumDetailActivity.this.selectStadiumBookAction();
        });

        this.init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 对于只传入了stadiumId参数，此时需要访问服务器
        if (stadiumId != null && stadium == null) {
            getStadiumByIdFromServer(stadiumId);
        }
    }

    /**
     * 选择体育场馆
     */
    private void selectStadiumBookAction() {
        if (stadiumId != null) {
            Intent stadiumBookIntent = new Intent(StadiumDetailActivity.this, StadiumBookListActivity.class);
            stadiumBookIntent.putExtra("stadiumId", stadium.getId());
            stadiumBookIntent.putExtra("stadiumManagerId", stadium.getStadiumManagerId());
            startActivity(stadiumBookIntent);
        }
    }

    /**
     * 填充数据
     */
    private void init() {
        if (stadium != null) {
            this.titleTextView.setText(String.format("%s", stadium.getName()));
            this.stadiumDetailTextView.setText(String.format("场馆名称：%s\n场馆描述：%s\n场馆地址：%s\n", stadium.getName(), stadium.getDescription(), stadium.getAddress()));
            // init banner
            if (stadium.getImagePaths() != null && stadium.getImagePaths().length() != 0) {
                // 设置圆形指示器
                this.stadiumBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
                // 设置指示器位置
                this.stadiumBanner.setIndicatorGravity(BannerConfig.CENTER);
                // 自动轮播
                this.stadiumBanner.isAutoPlay(true);
                // 设置轮播间隔
                this.stadiumBanner.setDelayTime(5000);
                // 设置图片资源
                List<String> imageList = new ArrayList<>();
                String[] imagePaths = stadium.getImagePaths().split(",");
                if (imagePaths.length != 0) {
                    for (String imagePath : imagePaths) {
                        imageList.add(ServerSettingActivity.getServerHostUrl() + imagePath);
                    }
                    this.stadiumBanner.setImages(imageList);
                    // 设置图片加载器
                    this.stadiumBanner.setImageLoader(new ImageLoader() {
                        @Override
                        public void displayImage(Context context, Object path, ImageView imageView) {
                            Glide.with(context).load(path.toString()).into(imageView);
                        }
                    });
                    this.stadiumBanner.start();
                }
            }
        }
    }

    /**
     * 通过stadiumId获取stadium
     * @param stadiumId     stadiumId
     */
    private void getStadiumByIdFromServer(Integer stadiumId) {
        FormBody formBody = new FormBody.Builder().add("stadiumId", "" + stadiumId).build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadium/findById.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                StadiumDetailActivity.this.runOnUiThread(()->{
                    Toast.makeText(StadiumDetailActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<Stadium>>(){}.getType();
                final ResponseResult<Stadium> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    StadiumDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(StadiumDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                StadiumDetailActivity.this.stadium = responseResult.getData();
                StadiumDetailActivity.this.stadiumId = StadiumDetailActivity.this.stadium.getId();
                Log.i("Stadium", responseResult.getData() + "");
                StadiumDetailActivity.this.runOnUiThread(()->{
                    StadiumDetailActivity.this.init();
                    StadiumDetailActivity.this.stadiumSmartRefreshLayout.finishRefresh();
                });
            }
        });
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit(String title) {
        ConstraintLayout commonTitleConstraintLayout = findViewById(R.id.stadium_detail_navigation_bar);
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());

        TextView textView = new TextView(this);
        textView.setText("举报");
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        ConstraintLayout.LayoutParams rightAnnouncementLayoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        rightAnnouncementLayoutParams.rightMargin = 15;
        rightAnnouncementLayoutParams.endToEnd = R.id.stadium_detail_navigation_bar;
        rightAnnouncementLayoutParams.topToTop = R.id.stadium_detail_navigation_bar;
        rightAnnouncementLayoutParams.bottomToBottom = R.id.stadium_detail_navigation_bar;
        textView.setLayoutParams(rightAnnouncementLayoutParams);
        textView.setOnClickListener(v -> {
            if (LoginUserInfoUtil.getLoginUser() == null) {
                Toast.makeText(StadiumDetailActivity.this, "请先进行登录！", Toast.LENGTH_SHORT).show();
                return;
            }
            // 跳转到举报页面
            Intent intent = new Intent(StadiumDetailActivity.this, ReportActivity.class);
            intent.putExtra("reportContentType", Report.REPORT_CONTENT_TYPE_STADIUM);
            intent.putExtra("reportContentId", stadium.getId());
            StadiumDetailActivity.this.startActivity(intent);
        });
        // 添加到common_title
        commonTitleConstraintLayout.addView(textView);
    }
}