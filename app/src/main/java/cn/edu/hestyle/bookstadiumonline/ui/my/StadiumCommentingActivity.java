package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.StadiumComment;
import cn.edu.hestyle.bookstadiumonline.entity.UserStadiumBookItem;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class StadiumCommentingActivity extends BaseActivity {
    /** 评论的最大长度 */
    private static final Integer STADIUM_COMMENT_CONTENT_MAX_LENGTH = 200;

    private UserStadiumBookItem userStadiumBookItem;
    public ImageView oneStarImageView;
    public ImageView twoStarImageView;
    public ImageView threeStarImageView;
    public ImageView fourStarImageView;
    public ImageView fiveStarImageView;
    public EditText stadiumCommentContentEditText;
    public Button saveStadiumCommentButton;

    private Integer starCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium_commenting);

        this.starCount = 0;

        this.navigationBarInit("评价本次场馆预约");
        this.oneStarImageView = findViewById(R.id.oneStarImageView);
        this.twoStarImageView = findViewById(R.id.twoStarImageView);
        this.threeStarImageView = findViewById(R.id.threeStarImageView);
        this.fourStarImageView = findViewById(R.id.fourStarImageView);
        this.fiveStarImageView = findViewById(R.id.fiveStarImageView);
        this.stadiumCommentContentEditText = findViewById(R.id.stadiumCommentContentEditText);
        this.saveStadiumCommentButton = findViewById(R.id.saveStadiumCommentButton);


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
        }

        this.oneStarImageView.setOnClickListener(v -> {
            StadiumCommentingActivity.this.starCount = 1;
            StadiumCommentingActivity.this.setStarCount(1);
        });
        this.twoStarImageView.setOnClickListener(v -> {
            StadiumCommentingActivity.this.starCount = 2;
            StadiumCommentingActivity.this.setStarCount(2);
        });
        this.threeStarImageView.setOnClickListener(v -> {
            StadiumCommentingActivity.this.starCount = 3;
            StadiumCommentingActivity.this.setStarCount(3);
        });
        this.fourStarImageView.setOnClickListener(v -> {
            StadiumCommentingActivity.this.starCount = 4;
            StadiumCommentingActivity.this.setStarCount(4);
        });
        this.fiveStarImageView.setOnClickListener(v -> {
            StadiumCommentingActivity.this.starCount = 5;
            StadiumCommentingActivity.this.setStarCount(5);
        });
        this.saveStadiumCommentButton.setOnClickListener(v -> {
            FormBody formBody = StadiumCommentingActivity.this.checkForm();
            if (formBody != null) {
                // 提交评论
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/stadiumComment/userComment.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        StadiumCommentingActivity.this.runOnUiThread(()->{
                            Toast.makeText(StadiumCommentingActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // 转json
                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                        Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                        final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                        StadiumCommentingActivity.this.runOnUiThread(()->{
                            Toast.makeText(StadiumCommentingActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            // 保存成功，则返回
                            StadiumCommentingActivity.this.finish();
                        }
                    }
                });
            }
        });

    }

    /**
     * 检查表单
     * @return              FormBody
     */
    private FormBody checkForm() {
        StadiumComment stadiumComment = new StadiumComment();
        stadiumComment.setStarCount(this.starCount);
        String stadiumCommentContent = this.stadiumCommentContentEditText.getText().toString();
        if (stadiumCommentContent.length() == 0) {
            Toast.makeText(this, "请输入评论内容！", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (stadiumCommentContent.length() > STADIUM_COMMENT_CONTENT_MAX_LENGTH) {
            Toast.makeText(this, "评论内容超过了 " + STADIUM_COMMENT_CONTENT_MAX_LENGTH + " 个字符，请重新编辑！", Toast.LENGTH_SHORT).show();
            return null;
        }
        stadiumComment.setContent(stadiumCommentContent);
        // 设置需要评论的Stadium
        stadiumComment.setStadiumId(userStadiumBookItem.getStadiumId());
        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
        String stadiumCommentData = gson.toJson(stadiumComment);
        FormBody formBody = new FormBody.Builder()
                .add("stadiumBookItemId", "" + userStadiumBookItem.getStadiumBookItemId())
                .add("stadiumCommentData", stadiumCommentData)
                .build();
        return formBody;
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