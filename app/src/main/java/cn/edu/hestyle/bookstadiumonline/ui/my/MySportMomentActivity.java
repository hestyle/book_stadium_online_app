package cn.edu.hestyle.bookstadiumonline.ui.my;

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

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import cn.edu.hestyle.bookstadiumonline.ui.moment.UserSportMomentDetailActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class MySportMomentActivity extends BaseActivity {
    private TextView tipsTextView;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<UserSportMoment> userSportMomentList;
    private SmartRefreshLayout mySportMomentSmartRefreshLayout;
    private RecyclerView mySportMomentItemRecyclerView;
    private MySportMomentRecycleAdapter mySportMomentRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sport_moment);

        this.navigationBarInit("我的动态");

        this.nextPageIndex = 1;
        this.userSportMomentList = null;
        this.tipsTextView = findViewById(R.id.tipsTextView);

        mySportMomentSmartRefreshLayout = findViewById(R.id.mySportMomentSmartRefreshLayout);
        mySportMomentSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                MySportMomentActivity.this.nextPageIndex = 1;
                MySportMomentActivity.this.getNextPageMySportMomentFromServer();
            }
        });
        mySportMomentSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                MySportMomentActivity.this.getNextPageMySportMomentFromServer();
            }
        });
        mySportMomentItemRecyclerView = findViewById(R.id.mySportMomentItemRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mySportMomentItemRecyclerView.setLayoutManager(linearLayoutManager);
        mySportMomentRecycleAdapter = new MySportMomentRecycleAdapter(this, userSportMomentList);
        mySportMomentItemRecyclerView.setAdapter(mySportMomentRecycleAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.userSportMomentList == null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.userSportMomentList = null;
            getNextPageMySportMomentFromServer();
        }
    }

    /**
     * 获取下一页UserSportMoment
     */
    private void getNextPageMySportMomentFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this, "暂无更多内容！", Toast.LENGTH_SHORT).show();
            mySportMomentSmartRefreshLayout.finishLoadmore();
            mySportMomentSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/findMySportMomentByPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                MySportMomentActivity.this.runOnUiThread(()->{
                    Toast.makeText(MySportMomentActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<UserSportMoment>>>(){}.getType();
                final ResponseResult<List<UserSportMoment>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    MySportMomentActivity.this.runOnUiThread(()->{
                        Toast.makeText(MySportMomentActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<UserSportMoment> userSportMomentList = responseResult.getData();
                Log.i("UserSportMoment", userSportMomentList + "");
                // 访问第一页，或者追加
                if (MySportMomentActivity.this.nextPageIndex == 1) {
                    MySportMomentActivity.this.userSportMomentList = userSportMomentList;
                } else {
                    MySportMomentActivity.this.userSportMomentList.addAll(userSportMomentList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (userSportMomentList == null || userSportMomentList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                MySportMomentActivity.this.runOnUiThread(()->{
                    if (MySportMomentActivity.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        MySportMomentActivity.this.mySportMomentSmartRefreshLayout.finishRefresh();
                        MySportMomentActivity.this.mySportMomentSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        MySportMomentActivity.this.mySportMomentSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        MySportMomentActivity.this.nextPageIndex += 1;
                    } else {
                        MySportMomentActivity.this.nextPageIndex = 0;
                    }
                    MySportMomentActivity.this.mySportMomentSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    MySportMomentActivity.this.mySportMomentRecycleAdapter.updateData(MySportMomentActivity.this.userSportMomentList);
                    if (MySportMomentActivity.this.userSportMomentList == null || MySportMomentActivity.this.userSportMomentList.size() == 0) {
                        MySportMomentActivity.this.tipsTextView.setText("暂无动态~");
                        MySportMomentActivity.this.tipsTextView.setVisibility(View.VISIBLE);
                        MySportMomentActivity.this.mySportMomentSmartRefreshLayout.setVisibility(View.GONE);
                    } else {
                        MySportMomentActivity.this.tipsTextView.setVisibility(View.GONE);
                        MySportMomentActivity.this.mySportMomentSmartRefreshLayout.setVisibility(View.VISIBLE);
                    }
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

    class MySportMomentRecycleAdapter extends RecyclerView.Adapter<MySportMomentRecycleAdapter.MySportMomentViewHolder> {
        private Activity activityContext;
        private View inflater;
        private List<UserSportMoment> userSportMomentList;

        public MySportMomentRecycleAdapter(Activity activityContext, List<UserSportMoment> userSportMomentList) {
            this.activityContext = activityContext;
            this.userSportMomentList = userSportMomentList;
        }

        /**
         * 更新data
         *
         * @param userSportMomentList   userSportMomentList
         */
        public void updateData(List<UserSportMoment> userSportMomentList) {
            this.userSportMomentList = userSportMomentList;
            this.notifyDataSetChanged();
        }

        @Override
        public MySportMomentRecycleAdapter.MySportMomentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // 创建ViewHolder，返回每一项的布局
            inflater = LayoutInflater.from(activityContext).inflate(R.layout.item_my_sport_moment_recyclerview, parent, false);
            return new MySportMomentRecycleAdapter.MySportMomentViewHolder(inflater);
        }

        @Override
        public void onBindViewHolder(MySportMomentRecycleAdapter.MySportMomentViewHolder holder, int position) {
            // 将数据和控件绑定
            UserSportMoment userSportMoment = userSportMomentList.get(position);
            // 动态信息
            holder.userSportMomentInfoConstraintLayout.setOnClickListener(v -> {
                // 跳转到详情页面
                if (LoginUserInfoUtil.getLoginUser() != null) {
                    Intent intent = new Intent(activityContext, UserSportMomentDetailActivity.class);
                    intent.putExtra("UserSportMoment", userSportMoment);
                    activityContext.startActivity(intent);
                } else {
                    Toast.makeText(activityContext, "请先进行登录！", Toast.LENGTH_SHORT).show();
                }
            });
            holder.contentTextView.setText(String.format("%s", userSportMoment.getContent()));
            if (userSportMoment.getImagePaths() != null && userSportMoment.getImagePaths().length() != 0) {
                String[] imagePaths = userSportMoment.getImagePaths().split(",");
                Glide.with(inflater.getContext())
                        .load(ServerSettingActivity.getServerHostUrl() + imagePaths[0])
                        .into(holder.oneImageView);
                if (imagePaths.length > 1) {
                    Glide.with(inflater.getContext())
                            .load(ServerSettingActivity.getServerHostUrl() + imagePaths[1])
                            .into(holder.twoImageView);
                    holder.twoImageView.setVisibility(View.VISIBLE);
                } else {
                    holder.twoImageView.setVisibility(View.INVISIBLE);
                }
                if (imagePaths.length > 2) {
                    Glide.with(inflater.getContext())
                            .load(ServerSettingActivity.getServerHostUrl() + imagePaths[2])
                            .into(holder.threeImageView);
                    holder.threeImageView.setVisibility(View.VISIBLE);
                } else {
                    holder.threeImageView.setVisibility(View.INVISIBLE);
                }
                holder.imageConstraintLayout.setVisibility(View.VISIBLE);
            } else {
                holder.imageConstraintLayout.setVisibility(View.GONE);
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
            holder.sentTimeTextView.setText(String.format("%s", simpleDateFormat.format(userSportMoment.getSentTime())));

            // 举报
            holder.reportTextView.setOnClickListener(v -> {
                Toast.makeText(activityContext, "点击了举报 sportMomentId = " + userSportMoment.getSportMomentId(), Toast.LENGTH_SHORT).show();
            });
            // 点赞
            holder.setLikeCount(userSportMoment.getLikeCount());
            holder.likeTextView.setOnClickListener(v -> {
                sportMomentLikeAction(holder, position);
            });
            // 判断当前登录账号是否点过赞
            if (LoginUserInfoUtil.getLoginUser() != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("sportMomentId", "" + userSportMoment.getSportMomentId())
                        .build();
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/hasLiked.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        activityContext.runOnUiThread(()->{
                            Toast.makeText(activityContext, "网络访问失败！", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // 转json
                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                        Type type =  new TypeToken<ResponseResult<Boolean>>(){}.getType();
                        final ResponseResult<Boolean> responseResult = gson.fromJson(responseString, type);
                        Log.w("ResponseResult", "" + responseResult);
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            Boolean flag = responseResult.getData();
                            if (flag) {
                                // 当前登录账号已点赞
                                activityContext.runOnUiThread(()->{
                                    Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_liked);
                                    holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                    // 修改点击事件(修改为取消点赞)
                                    holder.likeTextView.setOnClickListener(v -> {
                                        sportMomentDislikeAction(holder, position);
                                    });
                                });
                                return;
                            }
                        }
                        activityContext.runOnUiThread(()->{
                            Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_like);
                            holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                        });
                    }
                });
            }
            // 评论
            if (userSportMoment.getCommentCount() > 0) {
                holder.commentTextView.setText(String.format(" %d", userSportMoment.getCommentCount()));
            } else {
                holder.commentTextView.setText("抢沙发");
            }
            holder.commentTextView.setOnClickListener(v -> {
                // 跳转到详情页面
                if (LoginUserInfoUtil.getLoginUser() != null) {
                    Intent intent = new Intent(activityContext, UserSportMomentDetailActivity.class);
                    intent.putExtra("UserSportMoment", userSportMoment);
                    activityContext.startActivity(intent);
                } else {
                    Toast.makeText(activityContext, "请先进行登录！", Toast.LENGTH_SHORT).show();
                }
            });
            // 编辑action
            holder.editTextView.setOnClickListener(v -> {
                // 跳转到编辑页面
                Intent intent = new Intent(activityContext, MySportMomentEditActivity.class);
                intent.putExtra("UserSportMoment", userSportMoment);
                activityContext.startActivity(intent);
            });
            // 删除action
            holder.deleteTextView.setOnClickListener(v -> {
                // 弹窗提示
                AlertDialog alertDialog = new AlertDialog.Builder(activityContext)
                        .setTitle("提示信息")
                        .setMessage("您确定要删除这个动态吗？\n注意：删除后将无法恢复！")
                        .setNegativeButton("取消", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setPositiveButton("删除", (dialog, which) -> {
                            dialog.dismiss();
                            FormBody formBody = new FormBody.Builder().add("sportMomentId", userSportMoment.getSportMomentId() + "").build();
                            OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/deleteMySportMoment.do", null, formBody, new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    activityContext.runOnUiThread(() -> {
                                        Toast.makeText(activityContext, "网络错误，登录注销失败！", Toast.LENGTH_LONG).show();
                                    });
                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    String responseString = response.body().string();
                                    Gson gson = new Gson();
                                    Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                                    final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                                    activityContext.runOnUiThread(() -> {
                                        Toast.makeText(activityContext, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                                            // 删除成功
                                            dialog.dismiss();
                                            MySportMomentRecycleAdapter.this.userSportMomentList.remove(position);
                                            MySportMomentRecycleAdapter.this.notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                        })
                        .create();
                alertDialog.show();
            });
        }

        /**
         * 点赞
         * @param holder        holder
         * @param position      position
         */
        private void sportMomentLikeAction(MySportMomentRecycleAdapter.MySportMomentViewHolder holder, int position) {
            UserSportMoment userSportMoment = userSportMomentList.get(position);
            Integer sportMomentId = userSportMoment.getSportMomentId();
            if (sportMomentId != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("sportMomentId", "" + sportMomentId)
                        .build();
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/like.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        activityContext.runOnUiThread(()->{
                            Toast.makeText(activityContext, "网络访问失败！", Toast.LENGTH_SHORT).show();
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
                            activityContext.runOnUiThread(()->{
                                Toast.makeText(activityContext, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                            return;
                        } else {
                            // 点赞成功，修改已点赞的图标
                            activityContext.runOnUiThread(()->{
                                userSportMoment.setLikeCount(userSportMoment.getLikeCount() + 1);
                                Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_liked);
                                holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                // 更新点赞数量
                                holder.setLikeCount(userSportMoment.getLikeCount());
                                // 点赞取消后，修改点击事件为取消点赞
                                holder.likeTextView.setOnClickListener(v -> {
                                    sportMomentDislikeAction(holder, position);
                                });
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(activityContext, "程序发生未知错误！", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * 取消点赞
         * @param holder        holder
         * @param position      position
         */
        private void sportMomentDislikeAction(MySportMomentRecycleAdapter.MySportMomentViewHolder holder, int position) {
            UserSportMoment userSportMoment = userSportMomentList.get(position);
            Integer sportMomentId = userSportMoment.getSportMomentId();
            if (sportMomentId != null) {
                FormBody formBody = new FormBody.Builder()
                        .add("sportMomentId", "" + sportMomentId)
                        .build();
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/dislike.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        activityContext.runOnUiThread(()->{
                            Toast.makeText(activityContext, "网络访问失败！", Toast.LENGTH_SHORT).show();
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
                            activityContext.runOnUiThread(()->{
                                Toast.makeText(activityContext, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                            return;
                        } else {
                            // 点赞取消成功，修改为未点赞的图标
                            activityContext.runOnUiThread(()->{
                                userSportMoment.setLikeCount(userSportMoment.getLikeCount() - 1);
                                Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_like);
                                holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                // 更新点赞数量
                                holder.setLikeCount(userSportMoment.getLikeCount());
                                // 点赞取消后，修改点击事件为点赞
                                holder.likeTextView.setOnClickListener(v -> {
                                    sportMomentLikeAction(holder, position);
                                });
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(activityContext, "程序发生未知错误！", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        public int getItemCount() {
            // 返回Item总条数
            if (userSportMomentList != null) {
                return userSportMomentList.size();
            } else {
                return 0;
            }
        }

        // 内部类，绑定控件
        class MySportMomentViewHolder extends RecyclerView.ViewHolder {
            public ConstraintLayout rightActionConstraintLayout;
            public TextView editTextView;
            public TextView deleteTextView;
            public ConstraintLayout userSportMomentInfoConstraintLayout;
            public TextView contentTextView;
            public ConstraintLayout imageConstraintLayout;
            public ImageView oneImageView;
            public ImageView twoImageView;
            public ImageView threeImageView;
            public TextView sentTimeTextView;
            public TextView reportTextView;
            public TextView likeTextView;
            public TextView commentTextView;

            public MySportMomentViewHolder(View itemView) {
                super(itemView);
                rightActionConstraintLayout = itemView.findViewById(R.id.rightActionConstraintLayout);
                editTextView = itemView.findViewById(R.id.editTextView);
                deleteTextView = itemView.findViewById(R.id.deleteTextView);
                userSportMomentInfoConstraintLayout = itemView.findViewById(R.id.userSportMomentInfoConstraintLayout);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                imageConstraintLayout = itemView.findViewById(R.id.imageConstraintLayout);
                oneImageView = itemView.findViewById(R.id.oneImageView);
                twoImageView = itemView.findViewById(R.id.twoImageView);
                threeImageView = itemView.findViewById(R.id.threeImageView);
                sentTimeTextView = itemView.findViewById(R.id.sentTimeTextView);
                reportTextView = itemView.findViewById(R.id.reportTextView);
                likeTextView = itemView.findViewById(R.id.likeTextView);
                commentTextView = itemView.findViewById(R.id.commentTextView);
            }

            /**
             * 设置点赞数量
             * @param likeCount     点赞数量
             */
            public void setLikeCount(Integer likeCount) {
                if (likeCount == null || likeCount == 0) {
                    likeTextView.setText("抢首赞");
                } else {
                    likeTextView.setText(String.format(" %d", likeCount));
                }
            }
        }
    }
}