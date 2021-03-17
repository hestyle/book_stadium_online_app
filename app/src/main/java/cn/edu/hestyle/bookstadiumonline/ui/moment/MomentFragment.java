package cn.edu.hestyle.bookstadiumonline.ui.moment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.adapter.UserSportMomentRecycleAdapter;
import cn.edu.hestyle.bookstadiumonline.entity.UserSportMoment;
import cn.edu.hestyle.bookstadiumonline.ui.book.StadiumSearchActivity;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.LoginUserInfoUtil;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class MomentFragment extends Fragment {
    private View rootView;
    /** 一页的数量 */
    private static final Integer PER_PAGE_COUNT = 10;
    private Integer nextPageIndex;
    private List<UserSportMoment> userSportMomentList;
    private SmartRefreshLayout userSportMomentSmartRefreshLayout;
    private RecyclerView userSportMomentRecyclerView;
    private UserSportMomentRecycleAdapter userSportMomentRecycleAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_moment, container, false);

        this.navigationBarInit("运动动态");

        this.nextPageIndex = 1;
        this.userSportMomentList = null;

        userSportMomentSmartRefreshLayout = rootView.findViewById(R.id.userSportMomentSmartRefreshLayout);
        userSportMomentSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                MomentFragment.this.nextPageIndex = 1;
                MomentFragment.this.getNextPageUserSportMomentFromServer();
            }
        });
        userSportMomentSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                // 访问下一页
                MomentFragment.this.getNextPageUserSportMomentFromServer();
            }
        });
        userSportMomentRecyclerView = rootView.findViewById(R.id.userSportMomentRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        userSportMomentRecyclerView.setLayoutManager(linearLayoutManager);
        userSportMomentRecycleAdapter = new UserSportMomentRecycleAdapter(this.getContext(), userSportMomentList);
        userSportMomentRecyclerView.setAdapter(userSportMomentRecycleAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.userSportMomentList == null) {
            // 获取推荐的场馆
            this.nextPageIndex = 1;
            this.userSportMomentList = null;
            getNextPageUserSportMomentFromServer();
        }
    }

    /**
     * 获取下一页UserSportMoment
     */
    private void getNextPageUserSportMomentFromServer() {
        if (this.nextPageIndex == 0) {
            Toast.makeText(this.getContext(), "暂无更多内容！", Toast.LENGTH_SHORT).show();
            userSportMomentSmartRefreshLayout.finishLoadmore();
            userSportMomentSmartRefreshLayout.setLoadmoreFinished(true);
        }
        // 从服务器获取stadiumCategory
        FormBody formBody = new FormBody.Builder()
                .add("pageIndex", "" + nextPageIndex)
                .add("pageSize", "" + PER_PAGE_COUNT)
                .build();
        OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/userSportMoment/findByPage.do", null, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                MomentFragment.this.getActivity().runOnUiThread(()->{
                    Toast.makeText(MomentFragment.this.getContext(), "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<List<UserSportMoment>>>(){}.getType();
                final ResponseResult<List<UserSportMoment>> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    MomentFragment.this.getActivity().runOnUiThread(()->{
                        Toast.makeText(MomentFragment.this.getContext(), responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<UserSportMoment> userSportMomentList = responseResult.getData();
                Log.i("UserSportMoment", userSportMomentList + "");
                // 访问第一页，或者追加
                if (MomentFragment.this.nextPageIndex == 1) {
                    MomentFragment.this.userSportMomentList = userSportMomentList;
                } else {
                    MomentFragment.this.userSportMomentList.addAll(userSportMomentList);
                }
                // 判断是否有下一页
                boolean hasNextPage = true;
                if (userSportMomentList == null || userSportMomentList.size() < PER_PAGE_COUNT) {
                    hasNextPage = false;
                }
                boolean finalHasNextPage = hasNextPage;
                MomentFragment.this.getActivity().runOnUiThread(()->{
                    if (MomentFragment.this.nextPageIndex == 1) {
                        // 访问第一页，也可能是刷新
                        MomentFragment.this.userSportMomentSmartRefreshLayout.finishRefresh();
                        MomentFragment.this.userSportMomentSmartRefreshLayout.setLoadmoreFinished(false);
                    } else {
                        MomentFragment.this.userSportMomentSmartRefreshLayout.finishLoadmore();
                    }
                    // 根据是否有下一页，修改nextPageIndex
                    if (finalHasNextPage) {
                        MomentFragment.this.nextPageIndex += 1;
                    } else {
                        MomentFragment.this.nextPageIndex = 0;
                    }
                    MomentFragment.this.userSportMomentSmartRefreshLayout.setLoadmoreFinished(!finalHasNextPage);
                    // update
                    MomentFragment.this.userSportMomentRecycleAdapter.updateData(MomentFragment.this.userSportMomentList);
                });
            }
        });
    }

    /**
     * 设置navigationBar
     */
    private void navigationBarInit(String title) {
        ConstraintLayout commonTitleConstraintLayout = this.rootView.findViewById(R.id.user_sport_moment_list_navigation_bar);
        // 设置title
        TextView titleTextView = this.rootView.findViewById(R.id.titleTextView);
        titleTextView.setText(title);
        // 设置left add
        ImageButton leftAddImageButton = new ImageButton(getActivity());
        leftAddImageButton.setBackgroundColor(Color.TRANSPARENT);
        leftAddImageButton.setImageResource(R.drawable.ic_add_white);
        ConstraintLayout.LayoutParams leftAddLayoutParams = new ConstraintLayout.LayoutParams(56, 56);
        leftAddLayoutParams.leftMargin = 15;
        leftAddLayoutParams.startToStart = R.id.user_sport_moment_list_navigation_bar;
        leftAddLayoutParams.topToTop = R.id.user_sport_moment_list_navigation_bar;
        leftAddLayoutParams.bottomToBottom = R.id.user_sport_moment_list_navigation_bar;
        leftAddImageButton.setLayoutParams(leftAddLayoutParams);
        leftAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断是否登录
                if (LoginUserInfoUtil.getLoginUser() == null) {
                    Toast.makeText(getContext(), "请先进行登录！", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getContext(), UserSportMomentAddActivity.class);
                startActivity(intent);
            }
        });
        // 设置right search
        ImageButton rightSearchImageButton = new ImageButton(MomentFragment.this.getContext());
        rightSearchImageButton.setBackgroundColor(Color.TRANSPARENT);
        rightSearchImageButton.setImageResource(R.drawable.ic_search_white);
        ConstraintLayout.LayoutParams rightSearchLayoutParams = new ConstraintLayout.LayoutParams(56, 56);
        rightSearchLayoutParams.rightMargin = 15;
        rightSearchLayoutParams.endToEnd = R.id.user_sport_moment_list_navigation_bar;
        rightSearchLayoutParams.topToTop = R.id.user_sport_moment_list_navigation_bar;
        rightSearchLayoutParams.bottomToBottom = R.id.user_sport_moment_list_navigation_bar;
        rightSearchImageButton.setLayoutParams(rightSearchLayoutParams);
        rightSearchImageButton.setOnClickListener(v -> {
            // 判断是否登录
            if (LoginUserInfoUtil.getLoginUser() == null) {
                Toast.makeText(getContext(), "请先进行登录！", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MomentFragment.this.getContext(), UserSportMomentSearchActivity.class);
            startActivity(intent);
        });
        commonTitleConstraintLayout.addView(leftAddImageButton);
        commonTitleConstraintLayout.addView(rightSearchImageButton);
    }

}