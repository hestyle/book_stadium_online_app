package cn.edu.hestyle.bookstadiumonline.ui.my;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.SportKnowledge;

public class SportKnowledgeDetailActivity extends BaseActivity {
    private SportKnowledge sportKnowledge;

    private TextView contentTextView;
    private TextView footTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_knowledge_detail);

        contentTextView = findViewById(R.id.contentTextView);
        footTextView = findViewById(R.id.footTextView);

        navigationBarInit("运动常识详情");

        Intent intent = getIntent();
        this.sportKnowledge = (SportKnowledge) intent.getSerializableExtra("SportKnowledge");
        if (this.sportKnowledge != null) {
            navigationBarInit(sportKnowledge.getTitle() + "");
            contentTextView.setText(String.format("%s", sportKnowledge.getContent()));
            // 显示编辑日期
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (sportKnowledge.getModifiedTime() != null) {
                footTextView.setText(String.format("系统管理员 %s 编辑于 %s", this.sportKnowledge.getModifiedUser(), formatter.format(this.sportKnowledge.getModifiedTime())));
            } else {
                footTextView.setText(String.format("系统管理员 %s 创建于 %s", this.sportKnowledge.getCreatedUser(), formatter.format(this.sportKnowledge.getCreatedTime())));
            }
        } else {
            Toast.makeText(this, "程序内部发生错误！", Toast.LENGTH_SHORT).show();
            finish();
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