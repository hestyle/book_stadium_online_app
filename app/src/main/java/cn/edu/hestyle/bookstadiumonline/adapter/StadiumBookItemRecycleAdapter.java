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
import cn.edu.hestyle.bookstadiumonline.entity.StadiumBookItem;
import cn.edu.hestyle.bookstadiumonline.ui.message.ChattingActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;

public class StadiumBookItemRecycleAdapter extends RecyclerView.Adapter<StadiumBookItemRecycleAdapter.StadiumBookItemViewHolder> {
    private Context context;
    private View inflater;
    private List<StadiumBookItem> stadiumBookItemList;

    public StadiumBookItemRecycleAdapter(Context context, List<StadiumBookItem> stadiumBookItemList){
        this.context = context;
        this.stadiumBookItemList = stadiumBookItemList;
    }

    /**
     * 更新data
     * @param stadiumBookItemList    stadiumBookItemList
     */
    public void updateData(List<StadiumBookItem> stadiumBookItemList) {
        this.stadiumBookItemList = stadiumBookItemList;
        this.notifyDataSetChanged();
    }


    @Override
    public StadiumBookItemRecycleAdapter.StadiumBookItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.item_stadium_book_item_recyclerview, parent, false);
        return new StadiumBookItemViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(StadiumBookItemRecycleAdapter.StadiumBookItemViewHolder holder, int position) {
        // 将数据和控件绑定
        StadiumBookItem stadiumBookItem = stadiumBookItemList.get(position);
        // 用户头像
        if (stadiumBookItem.getUserAvatarPath() != null && stadiumBookItem.getUserAvatarPath().length() != 0) {
            Glide.with(inflater.getContext())
                    .load(ServerSettingActivity.getServerHostUrl() + stadiumBookItem.getUserAvatarPath())
                    .into(holder.userAvatarImageView);
        }
        // 用户名
        holder.usernameTextView.setText(String.format("%s", stadiumBookItem.getUsername()));
        // 预约时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
        holder.bookedTimeTextView.setText(String.format("%s", simpleDateFormat.format(stadiumBookItem.getBookedTime())));
        // 私信
        holder.chatActionTextView.setOnClickListener(v -> {
            if (LoginUserInfoUtil.getLoginUser() == null) {
                Toast.makeText(context, "请先进行登录！", Toast.LENGTH_SHORT).show();
                return;
            }
            // 跳转到聊天页面
            Intent intent = new Intent(context, ChattingActivity.class);
            intent.putExtra("otherUserId", stadiumBookItem.getUserId());
            context.startActivity(intent);
        });
        // 如果该SportMoment是当前用户发表的，则不显示"私信"
        if (LoginUserInfoUtil.getLoginUser() != null && stadiumBookItem.getUserId().equals(LoginUserInfoUtil.getLoginUser().getId())) {
            holder.chatActionTextView.setVisibility(View.INVISIBLE);
        } else {
            holder.chatActionTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        // 返回Item总条数
        if (stadiumBookItemList != null) {
            return stadiumBookItemList.size();
        } else {
            return 0;
        }
    }

    //内部类，绑定控件
    static class StadiumBookItemViewHolder extends RecyclerView.ViewHolder{
        public ImageView userAvatarImageView;
        public TextView usernameTextView;
        public TextView bookedTimeTextView;
        public TextView chatActionTextView;

        public StadiumBookItemViewHolder(View itemView) {
            super(itemView);
            userAvatarImageView = itemView.findViewById(R.id.userAvatarImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            bookedTimeTextView = itemView.findViewById(R.id.bookedTimeTextView);
            chatActionTextView = itemView.findViewById(R.id.chatActionTextView);
        }
    }
}
