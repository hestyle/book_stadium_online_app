package cn.edu.hestyle.bookstadiumonline.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.Notice;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;

public class SystemNoticeRecycleAdapter extends RecyclerView.Adapter<SystemNoticeRecycleAdapter.SystemNoticeViewHolder> {
    private Context context;
    private View inflater;
    private List<Notice> noticeList;

    public SystemNoticeRecycleAdapter(Context context, List<Notice> noticeList){
        this.context = context;
        this.noticeList = noticeList;
    }

    /**
     * 更新data
     * @param noticeList    noticeList
     */
    public void updateData(List<Notice> noticeList) {
        this.noticeList = noticeList;
        this.notifyDataSetChanged();
    }


    @Override
    public SystemNoticeRecycleAdapter.SystemNoticeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.item_notice_item_recyclerview, parent, false);
        return new SystemNoticeViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(SystemNoticeRecycleAdapter.SystemNoticeViewHolder holder, int position) {
        // 将数据和控件绑定
        Notice notice = noticeList.get(position);
        holder.titleTextView.setText(String.format("%s", notice.getTitle()));
        holder.contentTextView.setText(String.format("%s", notice.getContent()));
        // 评论日期
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT);
        holder.generatedTimeTextView.setText(simpleDateFormat.format(notice.getGeneratedTime()));
    }

    @Override
    public int getItemCount() {
        // 返回Item总条数
        if (noticeList != null) {
            return noticeList.size();
        } else {
            return 0;
        }
    }

    //内部类，绑定控件
    static class SystemNoticeViewHolder extends RecyclerView.ViewHolder{
        public TextView titleTextView;
        public TextView contentTextView;
        public TextView generatedTimeTextView;

        public SystemNoticeViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            generatedTimeTextView = itemView.findViewById(R.id.generatedTimeTextView);
        }
    }
}
