package cn.edu.hestyle.bookstadiumonline.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.Chat;
import cn.edu.hestyle.bookstadiumonline.entity.ChatVO;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;

public class ChatVORecycleAdapter extends RecyclerView.Adapter<ChatVORecycleAdapter.ChatVOViewHolder> {
    private Activity activityContext;
    private View inflater;
    private List<ChatVO> chatVOList;

    public ChatVORecycleAdapter(Activity activityContext, List<ChatVO> chatVOList){
        this.activityContext = activityContext;
        this.chatVOList = chatVOList;
    }

    /**
     * 更新data
     * @param chatVOList        chatVOList
     */
    public void updateData(List<ChatVO> chatVOList) {
        this.chatVOList = chatVOList;
        this.notifyDataSetChanged();
    }


    @Override
    public ChatVORecycleAdapter.ChatVOViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(activityContext).inflate(R.layout.item_chatvo_recyclerview, parent, false);
        return new ChatVOViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(ChatVORecycleAdapter.ChatVOViewHolder holder, int position) {
        // 将数据和控件绑定
        ChatVO chatVO = chatVOList.get(position);
        if (LoginUserInfoUtil.getLoginUser() != null) {
            Integer userId = LoginUserInfoUtil.getLoginUser().getId();
            if ((chatVO.getChatType().equals(Chat.CHAT_TYPE_USER_TO_USER) || chatVO.getChatType().equals(Chat.CHAT_TYPE_USER_TO_MANAGER)) && chatVO.getFromAccountId().equals(userId)) {
                // 用户信息
                if (chatVO.getToAccountAvatarPath() != null && chatVO.getToAccountAvatarPath().length() != 0) {
                    Glide.with(inflater.getContext())
                            .load(ServerSettingActivity.getServerHostUrl() + chatVO.getToAccountAvatarPath())
                            .into(holder.userAvatarImageView);
                }
                holder.usernameTextView.setText(String.format("%s", chatVO.getToAccountUsername()));
            } else {
                // 用户信息
                if (chatVO.getFromAccountAvatarPath() != null && chatVO.getFromAccountAvatarPath().length() != 0) {
                    Glide.with(inflater.getContext())
                            .load(ServerSettingActivity.getServerHostUrl() + chatVO.getFromAccountAvatarPath())
                            .into(holder.userAvatarImageView);
                }
                holder.usernameTextView.setText(String.format("%s", chatVO.getFromAccountUsername()));
            }
        }

        if (chatVO.getLastChatMessage() != null) {
            holder.setModifiedTime(chatVO.getModifiedTime());
            holder.lastMessageContentTextView.setVisibility(View.VISIBLE);
            holder.lastMessageContentTextView.setText(String.format("%s", chatVO.getLastChatMessage().getContent()));
        } else {
            holder.setModifiedTime(chatVO.getCreatedTime());
            holder.lastMessageContentTextView.setVisibility(View.INVISIBLE);
        }
        if (chatVO.getFromUnreadCount() == null || chatVO.getFromUnreadCount() == 0) {
            holder.messageCountBackgroundImageView.setVisibility(View.INVISIBLE);
            holder.messageCountTextView.setVisibility(View.INVISIBLE);
        } else {
            holder.messageCountBackgroundImageView.setVisibility(View.VISIBLE);
            holder.messageCountTextView.setText(String.format("%d", chatVO.getFromUnreadCount()));
            holder.messageCountTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        // 返回Item总条数
        if (chatVOList != null) {
            return chatVOList.size();
        } else {
            return 0;
        }
    }

    //内部类，绑定控件
    static class ChatVOViewHolder extends RecyclerView.ViewHolder {
        public ImageView userAvatarImageView;
        public TextView usernameTextView;
        public TextView lastMessageContentTextView;
        public TextView modifiedTimeTextView;
        public ImageView messageCountBackgroundImageView;
        public TextView messageCountTextView;

        public ChatVOViewHolder(View itemView) {
            super(itemView);
            userAvatarImageView = itemView.findViewById(R.id.userAvatarImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            lastMessageContentTextView = itemView.findViewById(R.id.lastMessageContentTextView);
            modifiedTimeTextView = itemView.findViewById(R.id.modifiedTimeTextView);
            messageCountBackgroundImageView = itemView.findViewById(R.id.messageCountBackgroundImageView);
            messageCountTextView = itemView.findViewById(R.id.messageCountTextView);
        }

        /**
         * 设置时间
         * @param date     最后一条消息时间
         */
        public void setModifiedTime(Date date) {
            if (date != null) {
                Date now = new Date();
                SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");
                if (dayDateFormat.format(date).equals(dayDateFormat.format(now))) {
                    // 当天的消息，只显示时间
                    SimpleDateFormat timeDateFormat = new SimpleDateFormat("HH:mm");
                    modifiedTimeTextView.setText(String.format("%s", timeDateFormat.format(date)));
                } else if (yearDateFormat.format(date).equals(yearDateFormat.format(now))) {
                    // 同一年的消息，只显示月份、日期
                    SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM-dd");
                    modifiedTimeTextView.setText(String.format("%s", monthDateFormat.format(date)));
                } else {
                    modifiedTimeTextView.setText(String.format("%s", dayDateFormat.format(date)));
                }
                modifiedTimeTextView.setVisibility(View.VISIBLE);
            } else {
                modifiedTimeTextView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
