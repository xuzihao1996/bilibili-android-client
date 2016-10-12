package com.hotbitmapgg.ohmybilibili.module.user;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.hotbitmapgg.ohmybilibili.R;
import com.hotbitmapgg.ohmybilibili.adapter.UserContributeVideoAdapter;
import com.hotbitmapgg.ohmybilibili.adapter.helper.EndlessRecyclerOnScrollListener;
import com.hotbitmapgg.ohmybilibili.adapter.helper.HeaderViewRecyclerAdapter;
import com.hotbitmapgg.ohmybilibili.base.RxLazyFragment;
import com.hotbitmapgg.ohmybilibili.entity.user.UserContributeInfo;
import com.hotbitmapgg.ohmybilibili.network.RetrofitHelper;
import com.hotbitmapgg.ohmybilibili.widget.CircleProgressView;
import com.hotbitmapgg.ohmybilibili.widget.CustomEmptyView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hcc on 2016/10/12 13:30
 * 100332338@qq.com
 * <p>
 * 用户详情界面的投稿
 */

public class UserContributeFragment extends RxLazyFragment
{


    @Bind(R.id.recycle)
    RecyclerView mRecyclerView;

    @Bind(R.id.circle_progress)
    CircleProgressView mCircleProgressView;

    @Bind(R.id.empty_view)
    CustomEmptyView mCustomEmptyView;

    private static final String EXTRA_MID = "extra_mid";

    private int mid;

    private int pageNum = 1;

    private int pageSize = 10;

    private HeaderViewRecyclerAdapter mHeaderViewRecyclerAdapter;

    private UserContributeVideoAdapter mAdapter;

    private View loadMoreView;

    private List<UserContributeInfo.DataBean.VlistBean> userVideoList = new ArrayList<>();


    public static UserContributeFragment newInstance(int mid)
    {

        UserContributeFragment mFragment = new UserContributeFragment();
        Bundle mBundle = new Bundle();
        mBundle.putInt(EXTRA_MID, mid);
        mFragment.setArguments(mBundle);
        return mFragment;
    }


    @Override
    public int getLayoutResId()
    {

        return R.layout.fragment_user_contribute;
    }

    @Override
    public void finishCreateView(Bundle state)
    {

        mid = getArguments().getInt(EXTRA_MID);

        showProgressBar();
        getUserVideoList();
        initRecyclerView();
    }

    private void getUserVideoList()
    {

        RetrofitHelper.getUserContributeVideoApi()
                .getUserContributeVideos(mid, pageNum, pageSize)
                .compose(this.bindToLifecycle())
                .map(userContributeInfo -> userContributeInfo.getData().getVlist())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listBeans -> {

                    if (listBeans.size() < pageSize)
                        loadMoreView.setVisibility(View.GONE);

                    userVideoList.addAll(listBeans);
                    finishTask();
                }, throwable -> {

                    loadMoreView.setVisibility(View.GONE);
                    hideProgressBar();
                    initEmptyLayout();
                });
    }


    private void initRecyclerView()
    {

        mRecyclerView.setHasFixedSize(true);
        mAdapter = new UserContributeVideoAdapter(mRecyclerView, userVideoList);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mHeaderViewRecyclerAdapter = new HeaderViewRecyclerAdapter(mAdapter);
        mRecyclerView.setAdapter(mHeaderViewRecyclerAdapter);
        createLoadMoreView();
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLinearLayoutManager)
        {

            @Override
            public void onLoadMore(int i)
            {

                pageNum++;
                getUserVideoList();
                loadMoreView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void finishTask()
    {

        hideProgressBar();
        loadMoreView.setVisibility(View.GONE);

        if (pageNum * pageSize - pageSize - 1 > 0)
            mAdapter.notifyItemRangeChanged(pageNum * pageSize - pageSize - 1, pageSize);
        else
            mAdapter.notifyDataSetChanged();

        if (userVideoList.isEmpty())
            initEmptyLayout();
    }

    private void createLoadMoreView()
    {

        loadMoreView = LayoutInflater.from(getActivity())
                .inflate(R.layout.layout_load_more, mRecyclerView, false);
        mHeaderViewRecyclerAdapter.addFooterView(loadMoreView);
        loadMoreView.setVisibility(View.GONE);
    }

    public void showProgressBar()
    {

        mCircleProgressView.setVisibility(View.VISIBLE);
        mCircleProgressView.spin();
    }

    public void hideProgressBar()
    {

        mCircleProgressView.setVisibility(View.GONE);
        mCircleProgressView.stopSpinning();
    }

    private void initEmptyLayout()
    {

        mCustomEmptyView.setEmptyImage(R.drawable.img_tips_error_space_no_data);
        mCustomEmptyView.setEmptyText("ㄟ( ▔, ▔ )ㄏ 再怎么找也没有啦");
        mCustomEmptyView.hideReloadButton();
    }
}
