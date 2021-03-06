package cn.edu.hestyle.bookstadiumonline.ui.moment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
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
import java.text.SimpleDateFormat;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.Report;
import cn.edu.hestyle.bookstadiumonline.entity.UserSportMoment;
import cn.edu.hestyle.bookstadiumonline.entity.UserSportMomentComment;
import cn.edu.hestyle.bookstadiumonline.ui.message.ChattingActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class UserSportMomentDetailActivity extends BaseActivity {
    private UserSportMoment userSportMoment;
    /** ?????????????????????????????? */
    private ConstraintLayout userInfoConstraintLayout;
    private ImageView userAvatarImageView;
    private TextView usernameTextView;
    private TextView chatActionTextView;
    /** ????????????content???date */
    private TextView contentTextView;
    private ConstraintLayout imageConstraintLayout;
    private ImageView oneImageView;
    private ImageView twoImageView;
    private ImageView threeImageView;
    private TextView sentTimeTextView;
    /** ??????title */
    private TextView commentTitleTextView;
    private TextView likeTitleTextView;
    /** ????????????????????? */
    /** ??????????????? */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<UserSportMomentComment> userSportMomentCommentList;
    private SmartRefreshLayout userSportMomentSmartRefreshLayout;
    private UserSportMomentCommentRecycleAdapter userSportMomentCommentRecycleAdapter;
    private RecyclerView userSportMomentCommentRecyclerView;
    /** ??????footer?????????????????? */
    private TextView writeCommentTextView;
    private TextView likeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sport_moment_detail);

        Intent intent = getIntent();
        userSportMoment = (UserSportMoment) intent.getSerializableExtra("UserSportMoment");

        userInfoConstraintLayout = findViewById(R.id.userInfoConstraintLayout);
        userAvatarImageView = findViewById(R.id.userAvatarImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        chatActionTextView = findViewById(R.id.chatActionTextView);
        contentTextView = findViewById(R.id.contentTextView);
        imageConstraintLayout = findViewById(R.id.imageConstraintLayout);
        oneImageView = findViewById(R.id.oneImageView);
        twoImageView = findViewById(R.id.twoImageView);
        threeImageView = findViewById(R.id.threeImageView);
        sentTimeTextView = findViewById(R.id.sentTimeTextView);
        commentTitleTextView = findViewById(R.id.commentTitleTextView);
        likeTitleTextView = findViewById(R.id.likeTitleTextView);
        writeCommentTextView = findViewById(R.id.writeCommentTextView);
        likeTextView = findViewById(R.id.likeTextView);
        userSportMomentSmartRefreshLayout = findViewById(R.id.userSportMomentSmartRefreshLayout);
        userSportMomentCommentRecyclerView = findViewById(R.id.userSportMomentCommentRecyclerView);

        this.nextPageIndex = 1;
        this.userSportMomentCommentList = null;

        userSportMomentSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                // ??????UserSportMoment
                UserSportMomentDetailActivity.this.getUserSportMomentFromServer();
                // ??????UserSportMomentComment
                UserSportMomentDetailActivity.this.nextPageIndex = 1;
                UserSportMomentDetailActivity.this.getNextPageUserSportMomentCommentFromServer();
            }
        });
        userSportMomentSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // ???????????????
                UserSportMomentDetailActivity.this.getNextPageUserSportMomentCommentFromServer();
            }
        });
        // ??????userSportMomentCommentRecyclerView????????????
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        userSportMomentCommentRecyclerView.setLayoutManager(linearLayoutManager);
        userSportMomentCommentRecycleAdapter = new UserSportMomentCommentRecycleAdapter(this, userSportMomentCommentList);
        userSportMomentCommentRecyclerView.setAdapter(userSportMomentCommentRecycleAdapter);
        // ???????????????
        userSportMomentCommentRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // ??????
        writeCommentTextView.setOnClickListener(v -> {
            // ??????????????????
            UserSportMomentDetailActivity.this.showCommentPopueWindow(SportMomentCommentTypeEnum.SPORT_MOMENT_COMMENT, null);
        });

        init();
        navigationBarInit("??????????????????");
    }

    /**
     * init
     */
    private void init() {
        if (userSportMoment != null) {
            userInfoConstraintLayout.setOnClickListener(v -> {
                Toast.makeText(UserSportMomentDetailActivity.this, "????????????????????? userId = " + userSportMoment.getUserId(), Toast.LENGTH_SHORT).show();
            });
            // ????????????
            if (userSportMoment.getUserAvatarPath() != null && userSportMoment.getUserAvatarPath().length() != 0) {
                Glide.with(UserSportMomentDetailActivity.this)
                        .load(ServerSettingActivity.getServerHostUrl() + userSportMoment.getUserAvatarPath())
                        .into(userAvatarImageView);
            }
            usernameTextView.setText(String.format("%s", userSportMoment.getUsername()));
            chatActionTextView.setOnClickListener(v -> {
                Toast.makeText(UserSportMomentDetailActivity.this, "????????????????????? userId = " + userSportMoment.getUserId(), Toast.LENGTH_SHORT).show();
            });
            // ????????????
            contentTextView.setText(String.format("%s", userSportMoment.getContent()));
            if (userSportMoment.getImagePaths() != null && userSportMoment.getImagePaths().length() != 0) {
                String[] imagePaths = userSportMoment.getImagePaths().split(",");
                Glide.with(UserSportMomentDetailActivity.this)
                        .load(ServerSettingActivity.getServerHostUrl() + imagePaths[0])
                        .into(oneImageView);
                if (imagePaths.length > 1) {
                    Glide.with(UserSportMomentDetailActivity.this)
                            .load(ServerSettingActivity.getServerHostUrl() + imagePaths[1])
                            .into(twoImageView);
                    twoImageView.setVisibility(View.VISIBLE);
                } else {
                    twoImageView.setVisibility(View.INVISIBLE);
                }
                if (imagePaths.length > 2) {
                    Glide.with(UserSportMomentDetailActivity.this)
                            .load(ServerSettingActivity.getServerHostUrl() + imagePaths[2])
                            .into(threeImageView);
                    threeImageView.setVisibility(View.VISIBLE);
                } else {
                    threeImageView.setVisibility(View.INVISIBLE);
                }
                imageConstraintLayout.setVisibility(View.VISIBLE);
            } else {
                imageConstraintLayout.setVisibility(View.GONE);
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
            sentTimeTextView.setText(String.format("%s", simpleDateFormat.format(userSportMoment.getSentTime())));
            // ????????????
            commentTitleTextView.setText(String.format("?????? %d", userSportMoment.getCommentCount()));
            // ????????????
            likeTitleTextView.setText(String.format("?????? %d", userSportMoment.getLikeCount()));
            // ???????????????????????????
            likeTextView.setOnClickListener(v -> {
                UserSportMomentDetailActivity.this.sportMomentLikeAction();
            });
            // ??????????????????????????????????????????????????????
            if (LoginUserInfoUtil.getLoginUser() != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("sportMomentId", "" + userSportMoment.getSportMomentId())
                        .build();
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/hasLiked.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            Toast.makeText(UserSportMomentDetailActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // ???json
                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                        Type type = new TypeToken<ResponseResult<Boolean>>() {
                        }.getType();
                        final ResponseResult<Boolean> responseResult = gson.fromJson(responseString, type);
                        Log.w("ResponseResult", "" + responseResult);
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            Boolean flag = responseResult.getData();
                            if (flag) {
                                // ???????????????????????????
                                UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                                    Drawable drawable = UserSportMomentDetailActivity.this.getResources().getDrawable(R.drawable.ic_liked);
                                    UserSportMomentDetailActivity.this.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                    UserSportMomentDetailActivity.this.likeTextView.setText(" ????????????");
                                    // ??????????????????(?????????????????????)
                                    UserSportMomentDetailActivity.this.likeTextView.setOnClickListener(v -> {
                                        UserSportMomentDetailActivity.this.sportMomentDislikeAction();
                                    });
                                });
                                return;
                            }
                        }
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            Drawable drawable = UserSportMomentDetailActivity.this.getResources().getDrawable(R.drawable.ic_like);
                            UserSportMomentDetailActivity.this.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                            UserSportMomentDetailActivity.this.likeTextView.setText(" ??????");
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.userSportMomentCommentList == null) {
            // ?????????????????????
            this.nextPageIndex = 1;
            this.userSportMomentCommentList = null;
            getNextPageUserSportMomentCommentFromServer();
        }
    }

    /**
     * sportMoment??????
     */
    private void sportMomentLikeAction() {
        if (userSportMoment != null && userSportMoment.getSportMomentId() != null) {
            FormBody formBody = new FormBody.Builder()
                    .add("sportMomentId", "" + userSportMoment.getSportMomentId())
                    .build();
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/like.do", null, formBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                        Toast.makeText(UserSportMomentDetailActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // ???json
                    Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                    Type type = new TypeToken<ResponseResult<Void>>() {
                    }.getType();
                    final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                    if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            Toast.makeText(UserSportMomentDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // ???????????????????????????????????????
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            UserSportMomentDetailActivity.this.userSportMoment.setLikeCount(UserSportMomentDetailActivity.this.userSportMoment.getLikeCount() + 1);
                            Drawable drawable = UserSportMomentDetailActivity.this.getResources().getDrawable(R.drawable.ic_liked);
                            UserSportMomentDetailActivity.this.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                            UserSportMomentDetailActivity.this.likeTextView.setText(" ????????????");
                            UserSportMomentDetailActivity.this.likeTitleTextView.setText(String.format("?????? %d", UserSportMomentDetailActivity.this.userSportMoment.getLikeCount()));
                            // ???????????????????????????????????????????????????
                            UserSportMomentDetailActivity.this.likeTextView.setOnClickListener(v -> {
                                UserSportMomentDetailActivity.this.sportMomentDislikeAction();
                            });
                        });
                    }
                }
            });
        } else {
            Toast.makeText(this, "???????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * sportMoment????????????
     */
    private void sportMomentDislikeAction() {
        if (userSportMoment != null && userSportMoment.getSportMomentId() != null) {
            FormBody formBody = new FormBody.Builder()
                    .add("sportMomentId", "" + userSportMoment.getSportMomentId())
                    .build();
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/dislike.do", null, formBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                        Toast.makeText(UserSportMomentDetailActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // ???json
                    Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                    Type type = new TypeToken<ResponseResult<Void>>() {
                    }.getType();
                    final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                    if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            Toast.makeText(UserSportMomentDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // ????????????????????????????????????????????????
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            UserSportMomentDetailActivity.this.userSportMoment.setLikeCount(UserSportMomentDetailActivity.this.userSportMoment.getLikeCount() - 1);
                            Drawable drawable = UserSportMomentDetailActivity.this.getResources().getDrawable(R.drawable.ic_like);
                            UserSportMomentDetailActivity.this.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                            UserSportMomentDetailActivity.this.likeTextView.setText(" ??????");
                            UserSportMomentDetailActivity.this.likeTitleTextView.setText(String.format("?????? %d", UserSportMomentDetailActivity.this.userSportMoment.getLikeCount()));
                            // ?????????????????????????????????????????????
                            UserSportMomentDetailActivity.this.likeTextView.setOnClickListener(v -> {
                                UserSportMomentDetailActivity.this.sportMomentLikeAction();
                            });
                        });
                    }
                }
            });
        } else {
            Toast.makeText(UserSportMomentDetailActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ??????UserSportMoment
     */
    private void getUserSportMomentFromServer() {
        if (userSportMoment != null && userSportMoment.getSportMomentId() != null) {
            // ??????????????????stadiumCategory
            FormBody formBody = new FormBody.Builder()
                    .add("sportMomentId", "" + userSportMoment.getSportMomentId())
                    .build();
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/findBySportMomentId.do", null, formBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    UserSportMomentDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(UserSportMomentDetailActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // ???json
                    Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                    Type type =  new TypeToken<ResponseResult<UserSportMoment>>(){}.getType();
                    final ResponseResult<UserSportMoment> responseResult = gson.fromJson(responseString, type);
                    if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                        UserSportMomentDetailActivity.this.runOnUiThread(()->{
                            Toast.makeText(UserSportMomentDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    UserSportMoment userSportMoment = responseResult.getData();
                    Log.i("UserSportMoment", userSportMoment + "");
                    // ??????ui
                    UserSportMomentDetailActivity.this.runOnUiThread(()->{
                        UserSportMomentDetailActivity.this.userSportMoment = userSportMoment;
                        UserSportMomentDetailActivity.this.init();
                    });
                }
            });
        }
    }

    /**
     * ???????????????UserSportMomentComment
     */
    private void getNextPageUserSportMomentCommentFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
            userSportMomentSmartRefreshLayout.finishLoadmore();
            userSportMomentSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // ??????????????????stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("sportMomentId", "" + userSportMoment.getSportMomentId())
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMomentComment/findBySportMomentIdAndPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                UserSportMomentDetailActivity.this.runOnUiThread(()->{
                    Toast.makeText(UserSportMomentDetailActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // ???json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<UserSportMomentComment>>>(){}.getType();
                final ResponseResult<List<UserSportMomentComment>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    UserSportMomentDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(UserSportMomentDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<UserSportMomentComment> userSportMomentCommentList = responseResult.getData();
                Log.i("UserSportMomentComment", userSportMomentCommentList + "");
                // ??????????????????????????????
                if (UserSportMomentDetailActivity.this.nextPageIndex == 1) {
                    UserSportMomentDetailActivity.this.userSportMomentCommentList = userSportMomentCommentList;
                } else {
                    UserSportMomentDetailActivity.this.userSportMomentCommentList.addAll(userSportMomentCommentList);
                }
                // ????????????????????????
                boolean hasNextPage = true;
                if (userSportMomentCommentList == null || userSportMomentCommentList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                UserSportMomentDetailActivity.this.runOnUiThread(()->{
                    if (UserSportMomentDetailActivity.this.nextPageIndex == 1) {
                        // ????????????????????????????????????
                        UserSportMomentDetailActivity.this.userSportMomentSmartRefreshLayout.finishRefresh();
                        UserSportMomentDetailActivity.this.userSportMomentSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        UserSportMomentDetailActivity.this.userSportMomentSmartRefreshLayout.finishLoadmore();
                    }
                    // ?????????????????????????????????nextPageIndex
                    if (finalHasNextPage) {
                        UserSportMomentDetailActivity.this.nextPageIndex += 1;
                    } else {
                        UserSportMomentDetailActivity.this.nextPageIndex = 0;
                    }
                    UserSportMomentDetailActivity.this.userSportMomentSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    UserSportMomentDetailActivity.this.userSportMomentCommentRecycleAdapter.updateData(UserSportMomentDetailActivity.this.userSportMomentCommentList);
                });
            }
        });
    }

    /**
     * ??????/?????? ??????
     * @param sportMomentCommentTypeEnum    ?????? or ??????
     * @param parentSportMomentCommentId    ????????????????????????????????????id
     */
    private void showCommentPopueWindow(SportMomentCommentTypeEnum sportMomentCommentTypeEnum, Integer parentSportMomentCommentId) {
        View popView = View.inflate(this, R.layout.popue_window_sport_moment_comment,null);
        TextView commentContentTextText = popView.findViewById(R.id.commentContentTextText);
        TextView cancelActionTextView = popView.findViewById(R.id.cancelActionTextView);
        TextView commentActionTextView = popView.findViewById(R.id.commentActionTextView);
        // ??????????????????
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels / 3;

        final PopupWindow popupWindow = new PopupWindow(popView, weight, height);
        popupWindow.setAnimationStyle(R.style.MaterialAlertDialog_MaterialComponents_Title_Panel);
        popupWindow.setFocusable(true);
        if (sportMomentCommentTypeEnum == SportMomentCommentTypeEnum.SPORT_MOMENT_COMMENT) {
            commentContentTextText.setHint("???????????????????????????~");
            commentActionTextView.setText("????????????");
        } else if (sportMomentCommentTypeEnum == SportMomentCommentTypeEnum.SPORT_MOMENT_REPLY) {
            commentContentTextText.setHint("???????????????????????????~");
            commentActionTextView.setText("????????????");
        }
        // ????????????popueWindow??????
        popupWindow.setOutsideTouchable(true);
        // ????????????????????????
        cancelActionTextView.setOnClickListener(v -> popupWindow.dismiss());
        // ????????????
        commentActionTextView.setOnClickListener(v -> {
            String commentContext = commentContentTextText.getText().toString();
            if (commentContext.length() == 0) {
                Toast.makeText(UserSportMomentDetailActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                return;
            }
            UserSportMomentComment userSportMomentComment = new UserSportMomentComment();
            userSportMomentComment.setSportMomentId(userSportMoment.getSportMomentId());
            userSportMomentComment.setContent(commentContext);
            userSportMomentComment.setParentId(parentSportMomentCommentId);
            // ???json
            Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
            String userSportMomentCommentData = gson.toJson(userSportMomentComment);
            FormBody formBody = new FormBody.Builder()
                    .add("userSportMomentCommentData", userSportMomentCommentData)
                    .build();
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMomentComment/add.do", null, formBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    UserSportMomentDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(UserSportMomentDetailActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // ???json
                    Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                    Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                    final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                    UserSportMomentDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(UserSportMomentDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            // ??????????????????????????????
                            popupWindow.dismiss();
                        }
                    });
                }
            });
        });
        // popupWindow???????????????????????????
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 1.0f;
            getWindow().setAttributes(lp);
        });
        // popupWindow???????????????????????????
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,0);
    }

    /**
     * ??????navigationBar
     */
    private void navigationBarInit(String title) {
        // ??????title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText(String.format("%s", title));
        // ????????????
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
    }

    /**
     * SportMomentComment??????
     */
    enum SportMomentCommentTypeEnum {
        /** ??????SportMoment */
        SPORT_MOMENT_COMMENT,
        /** ??????SportMomentComment */
        SPORT_MOMENT_REPLY;
    }

    class UserSportMomentCommentRecycleAdapter extends RecyclerView.Adapter<UserSportMomentCommentRecycleAdapter.UserSportMomentCommentViewHolder> {
        private Activity activityContext;
        private View inflater;
        private List<UserSportMomentComment> userSportMomentCommentList;

        public UserSportMomentCommentRecycleAdapter(Activity activityContext, List<UserSportMomentComment> userSportMomentCommentList) {
            this.activityContext = activityContext;
            this.userSportMomentCommentList = userSportMomentCommentList;
        }

        /**
         * ??????data
         *
         * @param userSportMomentCommentList userSportMomentCommentList
         */
        public void updateData(List<UserSportMomentComment> userSportMomentCommentList) {
            this.userSportMomentCommentList = userSportMomentCommentList;
            this.notifyDataSetChanged();
        }


        @Override
        public UserSportMomentCommentRecycleAdapter.UserSportMomentCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //??????ViewHolder???????????????????????????
            inflater = LayoutInflater.from(activityContext).inflate(R.layout.item_user_sport_moment_comment_recyclerview, parent, false);
            return new UserSportMomentCommentRecycleAdapter.UserSportMomentCommentViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(UserSportMomentCommentRecycleAdapter.UserSportMomentCommentViewHolder holder, int position) {
            // ????????????????????????
            UserSportMomentComment userSportMomentComment = userSportMomentCommentList.get(position);
            holder.userInfoConstraintLayout.setOnClickListener(v -> {
                Toast.makeText(activityContext, "????????????????????? userId = " + userSportMomentComment.getUserId(), Toast.LENGTH_SHORT).show();
            });
            // ????????????
            if (userSportMomentComment.getCommentUserAvatarPath() != null && userSportMomentComment.getCommentUserAvatarPath().length() != 0) {
                Glide.with(inflater.getContext())
                        .load(ServerSettingActivity.getServerHostUrl() + userSportMomentComment.getCommentUserAvatarPath())
                        .into(holder.userAvatarImageView);
            }
            holder.usernameTextView.setText(String.format("%s", userSportMomentComment.getCommentUsername()));
            holder.chatActionTextView.setOnClickListener(v -> {
                if (LoginUserInfoUtil.getLoginUser() == null) {
                    Toast.makeText(activityContext, "?????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                // ?????????????????????
                Intent intent = new Intent(activityContext, ChattingActivity.class);
                intent.putExtra("otherUserId", userSportMomentComment.getUserId());
                activityContext.startActivity(intent);
            });
            // ?????????SportMomentComment???????????????????????????????????????"??????"
            if (userSportMomentComment.getUserId().equals(LoginUserInfoUtil.getLoginUser().getId())) {
                holder.chatActionTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.chatActionTextView.setVisibility(View.VISIBLE);
            }
            // ????????????
            if (userSportMomentComment.getParentId() != null) {
                holder.parentContentTextView.setText(String.format("?????????%s", userSportMomentComment.getParentContent()));
                holder.parentContentTextView.setVisibility(View.VISIBLE);
            } else {
                holder.parentContentTextView.setVisibility(View.GONE);
            }
            holder.contentTextView.setText(String.format("%s", userSportMomentComment.getContent()));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
            holder.sentTimeTextView.setText(String.format("%s", simpleDateFormat.format(userSportMomentComment.getCommentedTime())));

            // ??????
            holder.reportTextView.setOnClickListener(v -> {
                if (LoginUserInfoUtil.getLoginUser() == null) {
                    Toast.makeText(activityContext, "?????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                // ?????????????????????
                Intent intent = new Intent(activityContext, ReportActivity.class);
                intent.putExtra("reportContentType", Report.REPORT_CONTENT_TYPE_SPORT_MOMENT_COMMENT);
                intent.putExtra("reportContentId", userSportMomentComment.getId());
                activityContext.startActivity(intent);
            });
            // ??????
            holder.setLikeCount(userSportMomentComment.getLikeCount());
            holder.likeTextView.setOnClickListener(v -> {
                sportMomentCommentLikeAction(holder, position);
            });
            // ???????????????????????????????????????
            if (LoginUserInfoUtil.getLoginUser() != null && userSportMomentComment.getId() != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("sportMomentCommentId", "" + userSportMomentComment.getId())
                        .build();
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMomentComment/hasLiked.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        activityContext.runOnUiThread(() -> {
                            Toast.makeText(activityContext, "?????????????????????", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // ???json
                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                        Type type = new TypeToken<ResponseResult<Boolean>>() {
                        }.getType();
                        final ResponseResult<Boolean> responseResult = gson.fromJson(responseString, type);
                        Log.w("ResponseResult", "" + responseResult);
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            Boolean flag = responseResult.getData();
                            if (flag) {
                                // ???????????????????????????
                                activityContext.runOnUiThread(() -> {
                                    Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_liked);
                                    holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                    // ??????????????????(?????????????????????)
                                    holder.likeTextView.setOnClickListener(v -> {
                                        sportMomentCommentDislikeAction(holder, position);
                                    });
                                });
                                return;
                            }
                        }
                        activityContext.runOnUiThread(() -> {
                            // ??????????????????????????????
                            Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_like);
                            holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                            // ???????????????????????? ??????action
                            holder.likeTextView.setOnClickListener(v -> {
                                sportMomentCommentLikeAction(holder, position);
                            });
                        });
                    }
                });
            }
            // ??????
            holder.commentTextView.setText("??????");
            holder.commentTextView.setOnClickListener(v -> {
                // ??????????????????
                UserSportMomentDetailActivity.this.showCommentPopueWindow(SportMomentCommentTypeEnum.SPORT_MOMENT_REPLY, userSportMomentComment.getId());
            });
        }


        /**
         * ??????
         *
         * @param holder   holder
         * @param position position
         */
        private void sportMomentCommentLikeAction(UserSportMomentCommentRecycleAdapter.UserSportMomentCommentViewHolder holder, int position) {
            UserSportMomentComment userSportMomentComment = userSportMomentCommentList.get(position);
            Integer sportMomentCommentId = userSportMomentComment.getId();
            if (sportMomentCommentId != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("sportMomentCommentId", "" + sportMomentCommentId)
                        .build();
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMomentComment/like.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        activityContext.runOnUiThread(() -> {
                            Toast.makeText(activityContext, "?????????????????????", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // ???json
                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                        Type type = new TypeToken<ResponseResult<Void>>() {
                        }.getType();
                        final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                        if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            activityContext.runOnUiThread(() -> {
                                Toast.makeText(activityContext, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // ???????????????????????????????????????
                            activityContext.runOnUiThread(() -> {
                                userSportMomentComment.setLikeCount(userSportMomentComment.getLikeCount() + 1);
                                Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_liked);
                                holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                // ??????????????????
                                holder.setLikeCount(userSportMomentComment.getLikeCount());
                                // ???????????????????????????????????????????????????
                                holder.likeTextView.setOnClickListener(v -> {
                                    sportMomentCommentDislikeAction(holder, position);
                                });
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(activityContext, "???????????????????????????", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * ????????????
         *
         * @param holder   holder
         * @param position position
         */
        private void sportMomentCommentDislikeAction(UserSportMomentCommentRecycleAdapter.UserSportMomentCommentViewHolder holder, int position) {
            UserSportMomentComment userSportMomentComment = userSportMomentCommentList.get(position);
            Integer sportMomentCommentId = userSportMomentComment.getId();
            if (sportMomentCommentId != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("sportMomentCommentId", "" + sportMomentCommentId)
                        .build();
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMomentComment/dislike.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        activityContext.runOnUiThread(() -> {
                            Toast.makeText(activityContext, "?????????????????????", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // ???json
                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                        Type type = new TypeToken<ResponseResult<Void>>() {
                        }.getType();
                        final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                        if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            activityContext.runOnUiThread(() -> {
                                Toast.makeText(activityContext, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // ????????????????????????????????????????????????
                            activityContext.runOnUiThread(() -> {
                                userSportMomentComment.setLikeCount(userSportMomentComment.getLikeCount() - 1);
                                Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_like);
                                holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                // ??????????????????
                                holder.setLikeCount(userSportMomentComment.getLikeCount());
                                // ?????????????????????????????????????????????
                                holder.likeTextView.setOnClickListener(v -> {
                                    sportMomentCommentLikeAction(holder, position);
                                });
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(activityContext, "???????????????????????????", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public int getItemCount() {
            // ??????Item?????????
            if (userSportMomentCommentList != null) {
                return userSportMomentCommentList.size();
            } else {
                return 0;
            }
        }

        // ????????????????????????
        class UserSportMomentCommentViewHolder extends RecyclerView.ViewHolder {
            public ConstraintLayout userInfoConstraintLayout;
            public ImageView userAvatarImageView;
            public TextView usernameTextView;
            public TextView chatActionTextView;
            public TextView parentContentTextView;
            public TextView contentTextView;
            public TextView sentTimeTextView;
            public TextView reportTextView;
            public TextView likeTextView;
            public TextView commentTextView;

            public UserSportMomentCommentViewHolder(View itemView) {
                super(itemView);
                userInfoConstraintLayout = itemView.findViewById(R.id.userInfoConstraintLayout);
                userAvatarImageView = itemView.findViewById(R.id.userAvatarImageView);
                usernameTextView = itemView.findViewById(R.id.usernameTextView);
                chatActionTextView = itemView.findViewById(R.id.chatActionTextView);
                parentContentTextView = itemView.findViewById(R.id.parentContentTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                sentTimeTextView = itemView.findViewById(R.id.sentTimeTextView);
                reportTextView = itemView.findViewById(R.id.reportTextView);
                likeTextView = itemView.findViewById(R.id.likeTextView);
                commentTextView = itemView.findViewById(R.id.commentTextView);
            }

            /**
             * ??????????????????
             *
             * @param likeCount ????????????
             */
            public void setLikeCount(Integer likeCount) {
                if (likeCount == null || likeCount == 0) {
                    likeTextView.setText("??????");
                } else {
                    likeTextView.setText(String.format(" %d", likeCount));
                }
            }
        }
    }
}