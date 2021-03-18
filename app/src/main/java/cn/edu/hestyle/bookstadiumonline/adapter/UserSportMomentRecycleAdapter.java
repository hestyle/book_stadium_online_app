package cn.edu.hestyle.bookstadiumonline.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
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
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
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
            Toast.makeText(activityContext, "点击了私信用户 userId = " + userSportMoment.getUserId(), Toast.LENGTH_SHORT).show();
        });
        // 动态信息
        holder.userSportMomentInfoConstraintLayout.setOnClickListener(v -> {
            Toast.makeText(activityContext, "点击了动态详情 sportMomentId = " + userSportMoment.getSportMomentId(), Toast.LENGTH_SHORT).show();
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
        if (userSportMoment.getLikeCount() > 0) {
            holder.likeTextView.setText(String.format("(%d)", userSportMoment.getLikeCount()));
        } else {
            holder.likeTextView.setText("抢首赞");
        }
        holder.likeTextView.setOnClickListener(v -> {
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
                            // 点赞成功，修改已店点赞图标
                            activityContext.runOnUiThread(()->{
                                userSportMoment.setLikeCount(userSportMoment.getLikeCount() + 1);
                                Drawable drawable = activityContext.getResources().getDrawable(R.drawable.ic_liked);
                                holder.likeTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                holder.likeTextView.setText(String.format("(%d)", userSportMoment.getLikeCount()));
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(activityContext, "程序发生未知错误！", Toast.LENGTH_SHORT).show();
            }
        });
        // 评论
        if (userSportMoment.getCommentCount() > 0) {
            holder.commentTextView.setText(String.format("(%d)", userSportMoment.getCommentCount()));
        } else {
            holder.commentTextView.setText("抢沙发");
        }
        holder.commentTextView.setOnClickListener(v -> {
            Toast.makeText(activityContext, "点击了评论 sportMomentId = " + userSportMoment.getSportMomentId(), Toast.LENGTH_SHORT).show();
        });
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
    }
}
