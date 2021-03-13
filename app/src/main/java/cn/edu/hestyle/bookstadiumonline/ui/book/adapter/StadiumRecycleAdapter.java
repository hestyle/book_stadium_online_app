package cn.edu.hestyle.bookstadiumonline.ui.book.adapter;

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

import java.util.List;

import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.Stadium;
import cn.edu.hestyle.bookstadiumonline.ui.book.StadiumDetailActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;

public class StadiumRecycleAdapter extends RecyclerView.Adapter<StadiumRecycleAdapter.StadiumViewHolder> {
    private Context context;
    private View inflater;
    private List<Stadium> stadiumList;

    public StadiumRecycleAdapter(Context context, List<Stadium> stadiumList){
        this.context = context;
        this.stadiumList = stadiumList;
    }

    /**
     * 更新data
     * @param stadiumList   stadiumList
     */
    public void updateData(List<Stadium> stadiumList) {
        this.stadiumList = stadiumList;
        this.notifyDataSetChanged();
    }


    @Override
    public StadiumRecycleAdapter.StadiumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.item_stadium_recyclerview, parent, false);
        return new StadiumViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(StadiumRecycleAdapter.StadiumViewHolder holder, int position) {
        // 将数据和控件绑定
        Stadium stadium = stadiumList.get(position);
        Log.i("Stadium", stadium.toString());
        // 加载网络图片(只加载第一张)
        if (stadium.getImagePaths() != null && stadium.getImagePaths().length() != 0) {
            String[] imagePaths = stadium.getImagePaths().split(",");
            Glide.with(inflater.getContext())
                    .load(ServerSettingActivity.getServerHostUrl() + imagePaths[0])
                    .into(holder.stadiumImageView);
        }
        holder.stadiumTitleTextView.setText(String.format("%s", stadium.getName()));
        holder.stadiumDescriptionTextView.setText(String.format("%s", stadium.getDescription()));
        holder.stadiumAddressTextView.setText(String.format("地址：%s", stadium.getAddress()));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StadiumDetailActivity.class);
            intent.putExtra("Stadium", stadium);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // 返回Item总条数
        if (stadiumList != null) {
            return stadiumList.size();
        } else {
            return 0;
        }
    }

    //内部类，绑定控件
    static class StadiumViewHolder extends RecyclerView.ViewHolder{
        public ImageView stadiumImageView;
        public TextView stadiumTitleTextView;
        public TextView stadiumDescriptionTextView;
        public TextView stadiumAddressTextView;

        public StadiumViewHolder(View itemView) {
            super(itemView);
            stadiumImageView = itemView.findViewById(R.id.stadiumImageView);
            stadiumTitleTextView = itemView.findViewById(R.id.stadiumTitleTextView);
            stadiumDescriptionTextView = itemView.findViewById(R.id.stadiumDescriptionTextView);
            stadiumAddressTextView = itemView.findViewById(R.id.stadiumAddressTextView);
        }
    }
}
