package cn.edu.hestyle.bookstadiumonline.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.UserSportMoment;
import cn.edu.hestyle.bookstadiumonline.ui.message.ChattingActivity;
import cn.edu.hestyle.bookstadiumonline.ui.moment.UserSportMomentDetailActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class UserSportMomentRecycleAdapter extends RecyclerView.Adapter<UserSportMomentRecycleAdapter.UserSportMomentItemViewHolder> {
    private Activity activityContext;
    private View inflater;
    private List<UserSportMoment> userSportMomentList;

    public UserSportMomentRecycleAdapter(Activity activityContext, List<UserSportMoment> userSportMomentList){
        this.activityContext = activityContext;
        this.userSportMomentList = userSportMomentList;
    }

    /**
     * 更新data
     * @param userSportMomentList    userSportMomentList
     */
    public void updateData(List<UserSportMoment> userSportMomentList) {
        this.userSportMomentList = userSportMomentList;
        this.notifyDataSetChanged();
    }


    @Override
    public UserSportMomentRecycleAdapter.UserSportMomentItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(activityContext).inflate(R.layout.item_user_sport_moment_recyclerview, parent, false);
        return new UserSportMomentItemViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(UserSportMomentRecycleAdapter.UserSportMomentItemViewHolder holder, int position) {
        // 将数据和控件绑定
        UserSportMoment userSportMoment = userSportMomentList.get(position);
        holder.userInfoConstraintLayout.setOnClickListener(v -> {
            Toast.makeText(activityContext, "点击了查看用户 userId = " + userSportMoment.getUserId(), Toast.LENGTH_SHORT).show();
        });
        // 用户信息
        if (userSportMoment.getUserAvatarPath() != null && userSportMoment.getUserAvatarPath().length() != 0) {
            Glide.with(inflater.getContext())
                    .load(ServerSettingActivity.getServerHostUrl() + userSportMoment.getUserAvatarPath())
                    .into(holder.userAvatarImageView);
        }
        holder.usernameTextView.setText(String.format("%s", userSportMoment.getUsername()));
        holder.chatActionTextView.setOnClickListener(v -> {
            if (LoginUserInfoUtil.getLoginUser() == null) {
                Toast.makeText(activityContext, "请先进行登录！", Toast.LENGTH_SHORT).show();
                return;
            }
            // 跳转到聊天页面
            Intent intent = new Intent(activityContext, ChattingActivity.class);
            intent.putExtra("otherUserId", userSportMoment.getUserId());
            activityContext.startActivity(intent);
        });
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
    }


    /**
     * 点赞
     * @param holder        holder
     * @param position      position
     */
    private void sportMomentLikeAction(UserSportMomentRecycleAdapter.UserSportMomentItemViewHolder holder, int position) {
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
    private void sportMomentDislikeAction(UserSportMomentRecycleAdapter.UserSportMomentItemViewHolder holder, int position) {
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

    //内部类，绑定控件
    static class UserSportMomentItemViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout userInfoConstraintLayout;
        public ImageView userAvatarImageView;
        public TextView usernameTextView;
        public TextView chatActionTextView;
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

        public UserSportMomentItemViewHolder(View itemView) {
            super(itemView);
            userInfoConstraintLayout = itemView.findViewById(R.id.userInfoConstraintLayout);
            userAvatarImageView = itemView.findViewById(R.id.userAvatarImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            chatActionTextView = itemView.findViewById(R.id.chatActionTextView);
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
