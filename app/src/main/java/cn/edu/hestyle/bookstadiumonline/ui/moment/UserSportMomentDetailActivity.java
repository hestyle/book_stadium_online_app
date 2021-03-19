package cn.edu.hestyle.bookstadiumonline.ui.moment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import cn.edu.hestyle.bookstadiumonline.entity.UserSportMoment;
import cn.edu.hestyle.bookstadiumonline.entity.UserSportMomentComment;
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
    /** 发表次动态的用户信息 */
    private ConstraintLayout userInfoConstraintLayout;
    private ImageView userAvatarImageView;
    private TextView usernameTextView;
    private TextView chatActionTextView;
    /** 运动动态content、date */
    private TextView contentTextView;
    private ConstraintLayout imageConstraintLayout;
    private ImageView oneImageView;
    private ImageView twoImageView;
    private ImageView threeImageView;
    private TextView sentTimeTextView;
    /** 中间title */
    private TextView commentTitleTextView;
    private TextView likeTitleTextView;
    /** 运动动态的评论 */
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<UserSportMomentComment> userSportMomentCommentList;
    private SmartRefreshLayout userSportMomentSmartRefreshLayout;
    private UserSportMomentCommentRecycleAdapter userSportMomentCommentRecycleAdapter;
    private RecyclerView userSportMomentCommentRecyclerView;
    /** 底部footer，评论、点赞 */
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
                // 更新UserSportMoment
                UserSportMomentDetailActivity.this.getUserSportMomentFromServer();
                // 更新UserSportMomentComment
                UserSportMomentDetailActivity.this.nextPageIndex = 1;
                UserSportMomentDetailActivity.this.getNextPageUserSportMomentCommentFromServer();
            }
        });
        userSportMomentSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                UserSportMomentDetailActivity.this.getNextPageUserSportMomentCommentFromServer();
            }
        });
        // 禁止userSportMomentCommentRecyclerView内部滑动
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        userSportMomentCommentRecyclerView.setLayoutManager(linearLayoutManager);
        userSportMomentCommentRecycleAdapter = new UserSportMomentCommentRecycleAdapter(this, userSportMomentCommentList);
        userSportMomentCommentRecyclerView.setAdapter(userSportMomentCommentRecycleAdapter);
        // 添加分割线
        userSportMomentCommentRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        init();
        navigationBarInit("运动动态详情");
    }

    /**
     * init
     */
    private void init() {
        if (userSportMoment != null) {
            userInfoConstraintLayout.setOnClickListener(v -> {
                Toast.makeText(UserSportMomentDetailActivity.this, "点击了查看用户 userId = " + userSportMoment.getUserId(), Toast.LENGTH_SHORT).show();
            });
            // 用户信息
            if (userSportMoment.getUserAvatarPath() != null && userSportMoment.getUserAvatarPath().length() != 0) {
                Glide.with(UserSportMomentDetailActivity.this)
                        .load(ServerSettingActivity.getServerHostUrl() + userSportMoment.getUserAvatarPath())
                        .into(userAvatarImageView);
            }
            usernameTextView.setText(String.format("%s", userSportMoment.getUsername()));
            chatActionTextView.setOnClickListener(v -> {
                Toast.makeText(UserSportMomentDetailActivity.this, "点击了私信用户 userId = " + userSportMoment.getUserId(), Toast.LENGTH_SHORT).show();
            });
            // 动态信息
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
            // 评论数量
            commentTitleTextView.setText(String.format("评论 %d", userSportMoment.getCommentCount()));
            // 点赞数量
            likeTitleTextView.setText(String.format("点赞 %d", userSportMoment.getLikeCount()));
            // 动态点赞、取消点赞
            likeTextView.setOnClickListener(v -> {
                UserSportMomentDetailActivity.this.sportMomentLikeAction();
            });
            // 判断当前登录账号是否点赞了该运动动态
            if (LoginUserInfoUtil.getLoginUser() != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("sportMomentId", "" + userSportMoment.getSportMomentId())
                        .build();
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/hasLiked.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            Toast.makeText(UserSportMomentDetailActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // 转json
                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                        Type type = new TypeToken<ResponseResult<Boolean>>() {
                        }.getType();
                        final ResponseResult<Boolean> responseResult = gson.fromJson(responseString, type);
                        Log.w("ResponseResult", "" + responseResult);
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            Boolean flag = responseResult.getData();
                            if (flag) {
                                // 当前登录账号已点赞
                                UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                                    Drawable drawable = UserSportMomentDetailActivity.this.getResources().getDrawable(R.drawable.ic_liked);
                                    UserSportMomentDetailActivity.this.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                    UserSportMomentDetailActivity.this.likeTextView.setText(" 取消点赞");
                                    // 修改点击事件(修改为取消点赞)
                                    UserSportMomentDetailActivity.this.likeTextView.setOnClickListener(v -> {
                                        // sportMomentDislikeAction(holder, position);
                                    });
                                });
                            }
                            return;
                        }
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            Drawable drawable = UserSportMomentDetailActivity.this.getResources().getDrawable(R.drawable.ic_like);
                            UserSportMomentDetailActivity.this.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                            UserSportMomentDetailActivity.this.likeTextView.setText(" 点赞");
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
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.userSportMomentCommentList = null;
            getNextPageUserSportMomentCommentFromServer();
        }
    }

    /**
     * sportMoment点赞
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
                        Toast.makeText(UserSportMomentDetailActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // 转json
                    Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                    Type type = new TypeToken<ResponseResult<Void>>() {
                    }.getType();
                    final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                    if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            Toast.makeText(UserSportMomentDetailActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // 点赞成功，修改已点赞的图标
                        UserSportMomentDetailActivity.this.runOnUiThread(() -> {
                            UserSportMomentDetailActivity.this.userSportMoment.setLikeCount(UserSportMomentDetailActivity.this.userSportMoment.getLikeCount() + 1);
                            Drawable drawable = UserSportMomentDetailActivity.this.getResources().getDrawable(R.drawable.ic_liked);
                            UserSportMomentDetailActivity.this.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                            UserSportMomentDetailActivity.this.likeTextView.setText(" 取消点赞");
//                            // 点赞取消后，修改点击事件为取消点赞
//                            holder.likeTextView.setOnClickListener(v -> {
//                                sportMomentDislikeAction(holder, position);
//                            });
                        });
                    }
                }
            });
        } else {
            Toast.makeText(this, "程序发生未知错误！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取UserSportMoment
     */
    private void getUserSportMomentFromServer() {
        if (userSportMoment != null && userSportMoment.getSportMomentId() != null) {
            // 从服务器获取stadiumCategory
            FormBody formBody = new FormBody.Builder()
                    .add("sportMomentId", "" + userSportMoment.getSportMomentId())
                    .build();
            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/findBySportMomentId.do", null, formBody, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    UserSportMomentDetailActivity.this.runOnUiThread(()->{
                        Toast.makeText(UserSportMomentDetailActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    // 转json
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
                    // 更新ui
                    UserSportMomentDetailActivity.this.runOnUiThread(()->{
                        UserSportMomentDetailActivity.this.userSportMoment = userSportMoment;
                        UserSportMomentDetailActivity.this.init();
                    });
                }
            });
        }
    }

    /**
     * 获取下一页UserSportMomentComment
     */
    private void getNextPageUserSportMomentCommentFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            userSportMomentSmartRefreshLayout.finishLoadmore();
            userSportMomentSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("sportMomentId", "" + userSportMoment.getSportMomentId())
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMomentComment/findBySportMomentIdAndPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                UserSportMomentDetailActivity.this.runOnUiThread(()->{
                    Toast.makeText(UserSportMomentDetailActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
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
                // 访问第一页，或者追加
                if (UserSportMomentDetailActivity.this.nextPageIndex == 1) {
                    UserSportMomentDetailActivity.this.userSportMomentCommentList = userSportMomentCommentList;
                } else {
                    UserSportMomentDetailActivity.this.userSportMomentCommentList.addAll(userSportMomentCommentList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (userSportMomentCommentList == null || userSportMomentCommentList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                UserSportMomentDetailActivity.this.runOnUiThread(()->{
                    if (UserSportMomentDetailActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        UserSportMomentDetailActivity.this.userSportMomentSmartRefreshLayout.finishRefresh();
                        UserSportMomentDetailActivity.this.userSportMomentSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        UserSportMomentDetailActivity.this.userSportMomentSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
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
     * 设置navigationBar
     */
    private void navigationBarInit(String title) {
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText(String.format("%s", title));
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
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
         * 更新data
         *
         * @param userSportMomentCommentList userSportMomentCommentList
         */
        public void updateData(List<UserSportMomentComment> userSportMomentCommentList) {
            this.userSportMomentCommentList = userSportMomentCommentList;
            this.notifyDataSetChanged();
        }


        @Override
        public UserSportMomentCommentRecycleAdapter.UserSportMomentCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //创建ViewHolder，返回每一项的布局
            inflater = LayoutInflater.from(activityContext).inflate(R.layout.item_user_sport_moment_comment_recyclerview, parent, false);
            return new UserSportMomentCommentRecycleAdapter.UserSportMomentCommentViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(UserSportMomentCommentRecycleAdapter.UserSportMomentCommentViewHolder holder, int position) {
            // 将数据和控件绑定
            UserSportMomentComment userSportMomentComment = userSportMomentCommentList.get(position);
            holder.userInfoConstraintLayout.setOnClickListener(v -> {
                Toast.makeText(activityContext, "点击了查看用户 userId = " + userSportMomentComment.getUserId(), Toast.LENGTH_SHORT).show();
            });
            // 用户信息
            if (userSportMomentComment.getCommentUserAvatarPath() != null && userSportMomentComment.getCommentUserAvatarPath().length() != 0) {
                Glide.with(inflater.getContext())
                        .load(ServerSettingActivity.getServerHostUrl() + userSportMomentComment.getCommentUserAvatarPath())
                        .into(holder.userAvatarImageView);
            }
            holder.usernameTextView.setText(String.format("%s", userSportMomentComment.getCommentUsername()));
            holder.chatActionTextView.setOnClickListener(v -> {
                Toast.makeText(activityContext, "点击了私信用户 userId = " + userSportMomentComment.getUserId(), Toast.LENGTH_SHORT).show();
            });
            // 动态信息
            if (userSportMomentComment.getParentId() != null) {
                holder.parentContentTextView.setText(String.format("回复：%s", userSportMomentComment.getParentContent()));
                holder.parentContentTextView.setVisibility(View.VISIBLE);
            } else {
                holder.parentContentTextView.setVisibility(View.GONE);
            }
            holder.contentTextView.setText(String.format("%s", userSportMomentComment.getContent()));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
            holder.sentTimeTextView.setText(String.format("%s", simpleDateFormat.format(userSportMomentComment.getCommentedTime())));

            // 举报
            holder.reportTextView.setOnClickListener(v -> {
                Toast.makeText(activityContext, "点击了举报 sportMomentCommentId = " + userSportMomentComment.getId(), Toast.LENGTH_SHORT).show();
            });
            // 点赞
            holder.setLikeCount(userSportMomentComment.getLikeCount());
            holder.likeTextView.setOnClickListener(v -> {
                Toast.makeText(activityContext, "点击了点赞 sportMomentCommentId = " + userSportMomentComment.getId(), Toast.LENGTH_SHORT).show();
                //sportMomentCommentLikeAction(holder, position);
            });
//            // 判断当前登录账号是否点过赞
//            if (LoginUserInfoUtil.getLoginUser() != null) {
//                FormBody formBody = new FormBody.Builder()
//                        .add("sportMomentId", "" + userSportMoment.getSportMomentId())
//                        .build();
//                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/hasLiked.do", null, formBody, new Callback() {
//                    @Override
//                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        activityContext.runOnUiThread(() -> {
//                            Toast.makeText(activityContext, "网络访问失败！", Toast.LENGTH_SHORT).show();
//                        });
//                    }
//
//                    @Override
//                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                        String responseString = response.body().string();
//                        // 转json
//                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
//                        Type type = new TypeToken<ResponseResult<Boolean>>() {
//                        }.getType();
//                        final ResponseResult<Boolean> responseResult = gson.fromJson(responseString, type);
//                        Log.w("ResponseResult", "" + responseResult);
//                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
//                            Boolean flag = responseResult.getData();
//                            if (flag) {
//                                // 当前登录账号已点赞
//                                activityContext.runOnUiThread(() -> {
//                                    Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_liked);
//                                    holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
//                                    // 修改点击事件(修改为取消点赞)
//                                    holder.likeTextView.setOnClickListener(v -> {
//                                        sportMomentDislikeAction(holder, position);
//                                    });
//                                });
//                            }
//                            return;
//                        }
//                        activityContext.runOnUiThread(() -> {
//                            Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_like);
//                            holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
//                        });
//                    }
//                });
//            }
            // 回复
            holder.commentTextView.setText("回复");
            holder.commentTextView.setOnClickListener(v -> {
                Toast.makeText(activityContext, "点击了回复 sportMomentCommentId = " + userSportMomentComment.getId(), Toast.LENGTH_SHORT).show();
            });
        }


//        /**
//         * 点赞
//         *
//         * @param holder   holder
//         * @param position position
//         */
//        private void sportMomentLikeAction(cn.edu.hestyle.bookstadiumonline.adapter.UserSportMomentRecycleAdapter.UserSportMomentItemViewHolder holder, int position) {
//            UserSportMoment userSportMoment = userSportMomentList.get(position);
//            Integer sportMomentId = userSportMoment.getSportMomentId();
//            if (sportMomentId != null) {
//                FormBody formBody = new FormBody.Builder()
//                        .add("sportMomentId", "" + sportMomentId)
//                        .build();
//                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/like.do", null, formBody, new Callback() {
//                    @Override
//                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        activityContext.runOnUiThread(() -> {
//                            Toast.makeText(activityContext, "网络访问失败！", Toast.LENGTH_SHORT).show();
//                        });
//                    }
//
//                    @Override
//                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                        String responseString = response.body().string();
//                        // 转json
//                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
//                        Type type = new TypeToken<ResponseResult<Void>>() {
//                        }.getType();
//                        final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
//                        if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
//                            activityContext.runOnUiThread(() -> {
//                                Toast.makeText(activityContext, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
//                            });
//                            return;
//                        } else {
//                            // 点赞成功，修改已点赞的图标
//                            activityContext.runOnUiThread(() -> {
//                                userSportMoment.setLikeCount(userSportMoment.getLikeCount() + 1);
//                                Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_liked);
//                                holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
//                                // 更新点赞数量
//                                holder.setLikeCount(userSportMoment.getLikeCount());
//                                // 点赞取消后，修改点击事件为取消点赞
//                                holder.likeTextView.setOnClickListener(v -> {
//                                    sportMomentDislikeAction(holder, position);
//                                });
//                            });
//                        }
//                    }
//                });
//            } else {
//                Toast.makeText(activityContext, "程序发生未知错误！", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        /**
//         * 取消点赞
//         *
//         * @param holder   holder
//         * @param position position
//         */
//        private void sportMomentDislikeAction(cn.edu.hestyle.bookstadiumonline.adapter.UserSportMomentRecycleAdapter.UserSportMomentItemViewHolder holder, int position) {
//            UserSportMoment userSportMoment = userSportMomentList.get(position);
//            Integer sportMomentId = userSportMoment.getSportMomentId();
//            if (sportMomentId != null) {
//                FormBody formBody = new FormBody.Builder()
//                        .add("sportMomentId", "" + sportMomentId)
//                        .build();
//                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/dislike.do", null, formBody, new Callback() {
//                    @Override
//                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        activityContext.runOnUiThread(() -> {
//                            Toast.makeText(activityContext, "网络访问失败！", Toast.LENGTH_SHORT).show();
//                        });
//                    }
//
//                    @Override
//                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                        String responseString = response.body().string();
//                        // 转json
//                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
//                        Type type = new TypeToken<ResponseResult<Void>>() {
//                        }.getType();
//                        final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
//                        if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
//                            activityContext.runOnUiThread(() -> {
//                                Toast.makeText(activityContext, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
//                            });
//                            return;
//                        } else {
//                            // 点赞取消成功，修改为未点赞的图标
//                            activityContext.runOnUiThread(() -> {
//                                userSportMoment.setLikeCount(userSportMoment.getLikeCount() - 1);
//                                Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_like);
//                                holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
//                                // 更新点赞数量
//                                holder.setLikeCount(userSportMoment.getLikeCount());
//                                // 点赞取消后，修改点击事件为点赞
//                                holder.likeTextView.setOnClickListener(v -> {
//                                    sportMomentLikeAction(holder, position);
//                                });
//                            });
//                        }
//                    }
//                });
//            } else {
//                Toast.makeText(activityContext, "程序发生未知错误！", Toast.LENGTH_SHORT).show();
//            }
//        }

        @Override
        public int getItemCount() {
            // 返回Item总条数
            if (userSportMomentCommentList != null) {
                return userSportMomentCommentList.size();
            } else {
                return 0;
            }
        }

        // 内部类，绑定控件
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
             * 设置点赞数量
             *
             * @param likeCount 点赞数量
             */
            public void setLikeCount(Integer likeCount) {
                if (likeCount == null || likeCount == 0) {
                    likeTextView.setText("点赞");
                } else {
                    likeTextView.setText(String.format(" %d", likeCount));
                }
            }
        }
    }
}