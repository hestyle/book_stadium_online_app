package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.StadiumComment;
import cn.edu.hestyle.bookstadiumonline.entity.User;
import cn.edu.hestyle.bookstadiumonline.entity.UserStadiumBookItem;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class MyStadiumCommentDetailActivity extends BaseActivity {
    private UserStadiumBookItem userStadiumBookItem;
    public ImageView oneStarImageView;
    public ImageView twoStarImageView;
    public ImageView threeStarImageView;
    public ImageView fourStarImageView;
    public ImageView fiveStarImageView;
    public TextView commentedTimeTextView;
    public TextView commentContentTextView;
    public TextView managerReplyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stadium_comment_detail);

        this.navigationBarInit("我的评论");
        this.oneStarImageView = findViewById(R.id.oneStarImageView);
        this.twoStarImageView = findViewById(R.id.twoStarImageView);
        this.threeStarImageView = findViewById(R.id.threeStarImageView);
        this.fourStarImageView = findViewById(R.id.fourStarImageView);
        this.fiveStarImageView = findViewById(R.id.fiveStarImageView);
        this.commentedTimeTextView = findViewById(R.id.commentedTimeTextView);
        this.commentContentTextView = findViewById(R.id.commentContentTextView);
        this.managerReplyTextView = findViewById(R.id.managerReplyTextView);

        Intent intent = getIntent();
        userStadiumBookItem = (UserStadiumBookItem) intent.getSerializableExtra("UserStadiumBookItem");
        if (userStadiumBookItem != null) {
            // 场馆image
            if (userStadiumBookItem.getStadiumImagePaths() != null && userStadiumBookItem.getStadiumImagePaths().length() != 0) {
                ImageView stadiumImageView = findViewById(R.id.stadiumImageView);
                String[] imagePaths = userStadiumBookItem.getStadiumImagePaths().split(",");
                Glide.with(this)
                        .load(ServerSettingActivity.getServerHostUrl() + imagePaths[0])
                        .into(stadiumImageView);
            }
            // 用户名、地址
            TextView stadiumNameTextView = findViewById(R.id.stadiumNameTextView);
            stadiumNameTextView.setText(String.format("%s", userStadiumBookItem.getStadiumName()));
            TextView stadiumAddressTextView = findViewById(R.id.stadiumAddressTextView);
            stadiumAddressTextView.setText(String.format("%s", userStadiumBookItem.getStadiumAddress()));
            // 获取评论
            if (userStadiumBookItem.getStadiumCommentId() != null) {
                getStadiumCommentById(userStadiumBookItem.getStadiumCommentId());
            }
        }
        // 登录账号信息
        User loginUser = LoginUserInfoUtil.getLoginUser();
        if (loginUser != null) {
            // 头像
            if (loginUser.getAvatarPath() != null) {
                ImageView userAvatarImageView = findViewById(R.id.userAvatarImageView);
                Glide.with(this)
                        .load(ServerSettingActivity.getServerHostUrl() + loginUser.getAvatarPath())
                        .into(userAvatarImageView);
            }
            // 用户名
            TextView usernameTextView = findViewById(R.id.usernameTextView);
            usernameTextView.setText(String.format("%s", loginUser.getUsername()));
        }
    }

    /**
     * 通过stadiumCommentId获取stadiumComment
     * @param stadiumCommentId          stadiumCommentId
     */
    private void getStadiumCommentById(Integer stadiumCommentId) {
        FormBody formBody = new FormBody.Builder()
                .add("stadiumCommentId", "" + stadiumCommentId)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadiumComment/findByStadiumCommentId.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                MyStadiumCommentDetailActivity.this.runOnUiThread(()->{
                    Toast.makeText(MyStadiumCommentDetailActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<StadiumComment>>(){}.getType();
                final ResponseResult<StadiumComment> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    // 获取数据失败
                    MyStadiumCommentDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(MyStadiumCommentDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                StadiumComment stadiumComment = responseResult.getData();
                Log.i("StadiumComment", "" + stadiumComment);
                if (stadiumComment != null) {
                    MyStadiumCommentDetailActivity.this.runOnUiThread(()->{
                        MyStadiumCommentDetailActivity.this.setStarCount(stadiumComment.getStarCount());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
                        MyStadiumCommentDetailActivity.this.commentedTimeTextView.setText(simpleDateFormat.format(stadiumComment.getCommentedTime()));
                        // 评论内容
                        MyStadiumCommentDetailActivity.this.commentContentTextView.setText(String.format("%s", stadiumComment.getContent()));
                        // 官方回复
                        String managerReply = stadiumComment.getManagerReply();
                        if (managerReply != null && managerReply.length() != 0) {
                            MyStadiumCommentDetailActivity.this.managerReplyTextView.setText(String.format("官方回复：%s", managerReply));
                            MyStadiumCommentDetailActivity.this.managerReplyTextView.setVisibility(View.VISIBLE);
                        } else {
                            MyStadiumCommentDetailActivity.this.managerReplyTextView.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    /**
     * 评论星级设置
     * @param starCount     starCount
     */
    private void setStarCount(Integer starCount) {
        if (starCount > 4) {
            fiveStarImageView.setImageResource(R.drawable.ic_star_fill);
        } else {
            fiveStarImageView.setImageResource(R.drawable.ic_star_empty);
        }
        if (starCount > 3) {
            fourStarImageView.setImageResource(R.drawable.ic_star_fill);
        } else {
            fourStarImageView.setImageResource(R.drawable.ic_star_empty);
        }
        if (starCount > 2) {
            threeStarImageView.setImageResource(R.drawable.ic_star_fill);
        } else {
            threeStarImageView.setImageResource(R.drawable.ic_star_empty);
        }
        if (starCount > 1) {
            twoStarImageView.setImageResource(R.drawable.ic_star_fill);
        } else {
            twoStarImageView.setImageResource(R.drawable.ic_star_empty);
        }
        if (starCount > 0) {
            oneStarImageView.setImageResource(R.drawable.ic_star_fill);
        } else {
            oneStarImageView.setImageResource(R.drawable.ic_star_empty);
        }
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
}