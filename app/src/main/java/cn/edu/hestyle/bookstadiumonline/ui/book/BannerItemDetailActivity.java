package cn.edu.hestyle.bookstadiumonline.ui.book;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.BannerItem;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;

public class BannerItemDetailActivity extends BaseActivity {
    private BannerItem bannerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_item_detail);
        ImageView bannerItemImageView = findViewById(R.id.bannerItemImageView);
        TextView bannerItemContentTextView = findViewById(R.id.bannerItemContentTextView);
        TextView footTextView = findViewById(R.id.footTextView);

        Intent intent = getIntent();
        this.bannerItem = (BannerItem) intent.getSerializableExtra("BannerItem");
        if (this.bannerItem != null) {
            // 显示title
            this.navigationBarInit(bannerItem.getTitle());
            // 显示图片
            if (this.bannerItem.getImagePath() != null) {
                Glide.with(BannerItemDetailActivity.this)
                        .load(ServerSettingActivity.getServerHostUrl() + this.bannerItem.getImagePath())
                        .into(bannerItemImageView);
            }
            // 显示正文
            bannerItemContentTextView.setText(bannerItem.getContent());
            // 显示编辑日期
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (this.bannerItem.getModifiedUser() != null) {
                footTextView.setText(String.format("系统管理员 %s 编辑于 %s", this.bannerItem.getModifiedUser(), formatter.format(this.bannerItem.getModifiedTime())));
            } else if (this.bannerItem.getCreatedUser() != null) {
                footTextView.setText(String.format("系统管理员 %s 编辑于 %s", this.bannerItem.getCreatedUser(), formatter.format(this.bannerItem.getCreatedTime())));
            }
        }
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit(String title) {
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
    }
}