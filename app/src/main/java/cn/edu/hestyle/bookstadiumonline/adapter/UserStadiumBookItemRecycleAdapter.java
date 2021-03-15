package cn.edu.hestyle.bookstadiumonline.adapter;

import android.content.Context;
import android.content.Intent;
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
import cn.edu.hestyle.bookstadiumonline.entity.UserStadiumBookItem;
import cn.edu.hestyle.bookstadiumonline.ui.book.StadiumBookItemListActivity;
import cn.edu.hestyle.bookstadiumonline.ui.book.StadiumDetailActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.MyStadiumCommentDetailActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;

public class UserStadiumBookItemRecycleAdapter extends RecyclerView.Adapter<UserStadiumBookItemRecycleAdapter.UserStadiumBookItemViewHolder> {
    private Context context;
    private View inflater;
    private List<UserStadiumBookItem> userStadiumBookItemList;

    public UserStadiumBookItemRecycleAdapter(Context context, List<UserStadiumBookItem> userStadiumBookItemList){
        this.context = context;
        this.userStadiumBookItemList = userStadiumBookItemList;
    }

    /**
     * 更新data
     * @param userStadiumBookItemList    userStadiumBookItemList
     */
    public void updateData(List<UserStadiumBookItem> userStadiumBookItemList) {
        this.userStadiumBookItemList = userStadiumBookItemList;
        this.notifyDataSetChanged();
    }


    @Override
    public UserStadiumBookItemRecycleAdapter.UserStadiumBookItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.item_user_stadium_book_item_recyclerview, parent, false);
        return new UserStadiumBookItemViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(UserStadiumBookItemRecycleAdapter.UserStadiumBookItemViewHolder holder, int position) {
        // 将数据和控件绑定
        UserStadiumBookItem userStadiumBookItem = userStadiumBookItemList.get(position);
        // 场馆头像
        if (userStadiumBookItem.getStadiumImagePaths() != null && userStadiumBookItem.getStadiumImagePaths().length() != 0) {
            String[] imagePaths = userStadiumBookItem.getStadiumImagePaths().split(",");
            Glide.with(inflater.getContext())
                    .load(ServerSettingActivity.getServerHostUrl() + imagePaths[0])
                    .into(holder.stadiumImageView);
        }
        holder.stadiumNameTextView.setText(String.format("%s", userStadiumBookItem.getStadiumName()));
        holder.stadiumAddressTextView.setText(String.format("%s", userStadiumBookItem.getStadiumAddress()));
        // 预约时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
        holder.stadiumBookStartTimeTextView.setText(String.format("%s", simpleDateFormat.format(userStadiumBookItem.getStadiumBookStartTime())));
        holder.stadiumBookEndTimeTextView.setText(String.format("%s", simpleDateFormat.format(userStadiumBookItem.getStadiumBookEndTime())));
        holder.stadiumBookedTimeTextView.setText(String.format("%s", simpleDateFormat.format(userStadiumBookItem.getStadiumBookedTime())));
        // 查看场馆
        holder.checkStadiumTextView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StadiumDetailActivity.class);
            intent.putExtra("stadiumId", userStadiumBookItem.getStadiumId());
            context.startActivity(intent);
        });
        // 查看其他预约用户
        holder.checkHadBookedUserTextView.setOnClickListener(v -> {
            if (userStadiumBookItem.getStadiumBookId() != null) {
                Intent stadiumBookItemIntent = new Intent(context, StadiumBookItemListActivity.class);
                stadiumBookItemIntent.putExtra("stadiumBookId", userStadiumBookItem.getStadiumBookId());
                context.startActivity(stadiumBookItemIntent);
            } else {
                Toast.makeText(context, "数据错误，未传入stadiumBookId！userStadiumBookItem = " + userStadiumBookItem, Toast.LENGTH_SHORT).show();
            }
        });
        // 评论场馆or查看评论
        if (userStadiumBookItem.getStadiumCommentId() != null) {
            holder.stadiumCommentTextView.setText("查看我的评论");
            holder.stadiumCommentTextView.setOnClickListener(v -> {
                // 跳转到评论详情页面
                Intent stadiumCommentDetailIntent = new Intent(context, MyStadiumCommentDetailActivity.class);
                stadiumCommentDetailIntent.putExtra("UserStadiumBookItem", userStadiumBookItem);
                context.startActivity(stadiumCommentDetailIntent);
            });
        } else {
            holder.stadiumCommentTextView.setText("评论场馆");
            holder.stadiumCommentTextView.setOnClickListener(v -> {
                Toast.makeText(context, "点击了评论场馆 stadiumId = " + userStadiumBookItem.getStadiumId(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        // 返回Item总条数
        if (userStadiumBookItemList != null) {
            return userStadiumBookItemList.size();
        } else {
            return 0;
        }
    }

    //内部类，绑定控件
    static class UserStadiumBookItemViewHolder extends RecyclerView.ViewHolder{
        public ImageView stadiumImageView;
        public TextView stadiumNameTextView;
        public TextView stadiumAddressTextView;
        public TextView stadiumBookStartTimeTextView;
        public TextView stadiumBookEndTimeTextView;
        public TextView stadiumBookedTimeTextView;
        public TextView checkStadiumTextView;
        public TextView checkHadBookedUserTextView;
        public TextView stadiumCommentTextView;

        public UserStadiumBookItemViewHolder(View itemView) {
            super(itemView);
            stadiumImageView = itemView.findViewById(R.id.stadiumImageView);
            stadiumNameTextView = itemView.findViewById(R.id.stadiumNameTextView);
            stadiumAddressTextView = itemView.findViewById(R.id.stadiumAddressTextView);
            stadiumBookStartTimeTextView = itemView.findViewById(R.id.stadiumBookStartTimeTextView);
            stadiumBookEndTimeTextView = itemView.findViewById(R.id.stadiumBookEndTimeTextView);
            stadiumBookedTimeTextView = itemView.findViewById(R.id.stadiumBookedTimeTextView);
            checkStadiumTextView = itemView.findViewById(R.id.checkStadiumTextView);
            checkHadBookedUserTextView = itemView.findViewById(R.id.checkHadBookedUserTextView);
            stadiumCommentTextView = itemView.findViewById(R.id.stadiumCommentTextView);
        }
    }
}
