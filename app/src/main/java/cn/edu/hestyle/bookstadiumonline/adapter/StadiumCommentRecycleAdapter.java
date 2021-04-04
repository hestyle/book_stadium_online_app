package cn.edu.hestyle.bookstadiumonline.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.Report;
import cn.edu.hestyle.bookstadiumonline.entity.StadiumComment;
import cn.edu.hestyle.bookstadiumonline.entity.User;
import cn.edu.hestyle.bookstadiumonline.ui.book.StadiumDetailActivity;
import cn.edu.hestyle.bookstadiumonline.ui.moment.ReportActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;

public class StadiumCommentRecycleAdapter extends RecyclerView.Adapter<StadiumCommentRecycleAdapter.StadiumCommentViewHolder> {
    private Context context;
    private View inflater;
    private List<StadiumComment> stadiumCommentList;

    public StadiumCommentRecycleAdapter(Context context, List<StadiumComment> stadiumCommentList){
        this.context = context;
        this.stadiumCommentList = stadiumCommentList;
    }

    /**
     * 更新data
     * @param stadiumCommentList    stadiumCommentList
     */
    public void updateData(List<StadiumComment> stadiumCommentList) {
        this.stadiumCommentList = stadiumCommentList;
        this.notifyDataSetChanged();
    }


    @Override
    public StadiumCommentRecycleAdapter.StadiumCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.item_stadium_comment_recyclerview, parent, false);
        return new StadiumCommentViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(StadiumCommentRecycleAdapter.StadiumCommentViewHolder holder, int position) {
        // 将数据和控件绑定
        StadiumComment stadiumComment = stadiumCommentList.get(position);
        User commentUser = stadiumComment.getCommentUser();
        Log.i("StadiumComment", stadiumComment + "");
        if (commentUser != null) {
            // 头像
            if (commentUser.getAvatarPath() != null) {
                Glide.with(inflater.getContext())
                        .load(ServerSettingActivity.getServerHostUrl() + commentUser.getAvatarPath())
                        .into(holder.userAvatarImageView);
            }
            // 用户名
            holder.usernameTextView.setText(String.format("%s", commentUser.getUsername()));
        }
        // 星数
        holder.setStarCount(stadiumComment.getStarCount());
        // 评论日期
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
        holder.commentedTimeTextView.setText(simpleDateFormat.format(stadiumComment.getCommentedTime()));
        // 评论内容
        holder.commentContentTextView.setText(String.format("%s", stadiumComment.getContent()));
        // 官方回复
        String managerReply = stadiumComment.getManagerReply();
        if (managerReply != null && managerReply.length() != 0) {
            holder.managerReplyTextView.setText(String.format("官方回复：%s", managerReply));
            holder.managerReplyTextView.setVisibility(View.VISIBLE);
        } else {
            holder.managerReplyTextView.setVisibility(View.GONE);
        }
        // 举报
        holder.reportTextView.setOnClickListener(v -> {
            if (LoginUserInfoUtil.getLoginUser() == null) {
                Toast.makeText(context, "请先进行登录！", Toast.LENGTH_SHORT).show();
                return;
            }
            // 跳转到举报页面
            Intent intent = new Intent(context, ReportActivity.class);
            intent.putExtra("reportContentType", Report.REPORT_CONTENT_TYPE_STADIUM_COMMENT);
            intent.putExtra("reportContentId", stadiumComment.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // 返回Item总条数
        if (stadiumCommentList != null) {
            return stadiumCommentList.size();
        } else {
            return 0;
        }
    }

    //内部类，绑定控件
    static class StadiumCommentViewHolder extends RecyclerView.ViewHolder{
        public ImageView userAvatarImageView;
        public TextView usernameTextView;
        public ImageView oneStarImageView;
        public ImageView twoStarImageView;
        public ImageView threeStarImageView;
        public ImageView fourStarImageView;
        public ImageView fiveStarImageView;
        public TextView commentedTimeTextView;
        public TextView commentContentTextView;
        public TextView managerReplyTextView;
        public TextView reportTextView;

        public StadiumCommentViewHolder(View itemView) {
            super(itemView);
            userAvatarImageView = itemView.findViewById(R.id.userAvatarImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            oneStarImageView = itemView.findViewById(R.id.oneStarImageView);
            twoStarImageView = itemView.findViewById(R.id.twoStarImageView);
            threeStarImageView = itemView.findViewById(R.id.threeStarImageView);
            fourStarImageView = itemView.findViewById(R.id.fourStarImageView);
            fiveStarImageView = itemView.findViewById(R.id.fiveStarImageView);
            commentedTimeTextView = itemView.findViewById(R.id.commentedTimeTextView);
            commentContentTextView = itemView.findViewById(R.id.commentContentTextView);
            managerReplyTextView = itemView.findViewById(R.id.managerReplyTextView);
            reportTextView = itemView.findViewById(R.id.reportTextView);
        }

        public void setStarCount(Integer starCount) {
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
    }
}
