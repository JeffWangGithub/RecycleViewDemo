package com.meilishuo.recycleviewdemo.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * 可以添加header和footer的RecyclerView
 * 注意：
 * 目前暂时不支持LinearLayout和GridLayoutManager方向为水平方向的header，
 * 但是可以使用StaggeredGridLayoutManager来叨叨GridLayoutManager的效果
 * @description:
 * @company: 美丽说（北京）网络科技有限公司
 * Created by Glan on 15/8/31.
 */
public class MyRecyclerView extends RecyclerView implements Runnable{
    private Context mContext;

    private ArrayList<View> mHeaderViews = new ArrayList<View>(); //存储所有的添加的header
    private ArrayList<View> mLoadMoreFooterViews = new ArrayList<View>(); //加载更多是显示的footerView，暂时支持一个view
    private Adapter mAdapter;
    private LoadDataListener mLoadDataListener;

    private boolean isLoadingData = false; // 是否正在加载数据
    private int currentOrientation = OrientationHelper.VERTICAL; //默认的指定方向为垂直


    public MyRecyclerView(Context context) {
        this(context, null);
    }

    public MyRecyclerView(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
        super(context,attrs);
        init(context);
    }

    public MyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        post(this);
    }

    @Override
    public void run() {
        LayoutManager manager = getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            // ListView布局
//            ((LinearLayoutManager) manager).setOverScrollListener(mOverScrollListener);
            currentOrientation = ((LinearLayoutManager) manager).getOrientation();
        } else if (manager instanceof GridLayoutManager) {
            layoutGridAttach((GridLayoutManager) manager);
            currentOrientation = ((GridLayoutManager) manager).getOrientation();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            layoutStaggeredGridHeadAttach((StaggeredGridLayoutManager) manager);
            currentOrientation = ((StaggeredGridLayoutManager) manager).getOrientation();
        }
        if(((WrapAdapter)mAdapter).getFootersCount() > 0 && mLoadMoreFooterViews.size() > 0 ){
            //脚部先隐藏
            mLoadMoreFooterViews.get(0).setVisibility(GONE);
        }
    }

    /**
     * 给GridLayoutManager附加头部脚部和滑动过度监听
     *
     * @param manager
     */
    private void layoutGridAttach(final GridLayoutManager manager) {
        // GridView布局
//        manager.setOverScrollListener(mOverScrollListener);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return ((WrapAdapter) mAdapter).isHeader(position) ||
                        ((WrapAdapter) mAdapter).isFooter(position) ? manager.getSpanCount() : 1;
            }
        });
        requestLayout();
    }

    /**
     * 给StaggeredGridLayoutManager附加头部和滑动过度监听
     *
     * @param manager
     */
    private void layoutStaggeredGridHeadAttach(StaggeredGridLayoutManager manager) {
//        manager.setOverScrollListener(mOverScrollListener);
        // 从前向后查找Header并设置为充满一行
        View view;
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            if (((WrapAdapter) mAdapter).isHeader(i)) {
                view = getChildAt(i);
                ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams())
                        .setFullSpan(true);
                view.requestLayout();
            } else {
                break;
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mLoadMoreFooterViews.isEmpty()) {
            //新建Footer
            LinearLayout footerLayout = new LinearLayout(mContext);
            footerLayout.setGravity(Gravity.CENTER);
            footerLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mLoadMoreFooterViews.add(footerLayout);
            footerLayout.addView(new ProgressBar(mContext, null, android.R.attr.progressBarStyleSmall));
            //默认加载更多的footer
            TextView text = new TextView(mContext);
            text.setText("正在加载...");
            footerLayout.addView(text);
        }
        // 使用包装了头部和脚部的适配器
        adapter = new WrapAdapter(mHeaderViews, mLoadMoreFooterViews, adapter);
        super.setAdapter(adapter);
        mAdapter = adapter;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if(state == RecyclerView.SCROLL_STATE_IDLE && mLoadDataListener != null && !isLoadingData){
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition ;
            if(layoutManager instanceof GridLayoutManager){
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if(layoutManager instanceof StaggeredGridLayoutManager){
                //((StaggeredGridLayoutManager) layoutManager).getSpanCount() 获取当前跨越的列数
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            if(layoutManager.getChildCount() > 0 && lastVisibleItemPosition >= layoutManager.getItemCount() -1){
                if(mLoadMoreFooterViews.size() > 0){
                    mLoadMoreFooterViews.get(0).setVisibility(VISIBLE);
                }
                //加载更多
                isLoadingData = true;
                mLoadDataListener.onLoadMore();
            }
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private class WrapAdapter extends RecyclerView.Adapter<ViewHolder> {
        private RecyclerView.Adapter mAdapter;

        private ArrayList<View> mHeaderViews;

        private ArrayList<View> mFootViews;

        final ArrayList<View> EMPTY_INFO_LIST = new ArrayList<>();
        private int headerPosition = 0;

        public WrapAdapter(ArrayList<View> mHeaderViews, ArrayList<View> mFooterViews, RecyclerView.Adapter adapter) {
            this.mAdapter = adapter;
            if (mHeaderViews == null) {
                this.mHeaderViews = EMPTY_INFO_LIST;
            } else {
                this.mHeaderViews = mHeaderViews;
            }
            if(mFooterViews == null){
                this.mFootViews = EMPTY_INFO_LIST;
            } else {
                this.mFootViews = mFooterViews;
            }
        }

        @Override
        public int getItemCount() {
            if(mAdapter != null){
                return getHeadersCount() + getFootersCount() + mAdapter.getItemCount();
            } else {
                return getHeadersCount() + getFootersCount();
            }
        }

        @Override
        public long getItemId(int position) {
            int numHeaders = getHeadersCount();
            if (mAdapter != null && position >= numHeaders) {
                int adjPosition = position - numHeaders;
                int adapterCount = mAdapter.getItemCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        @Override
        public int getItemViewType(int position) {
            int headersCount = getHeadersCount();
            if(position < headersCount){
                return RecyclerView.INVALID_TYPE;//header类型
            }
            int adjPosition = position - headersCount;
            int adapaterCount ;
            if(mAdapter != null ){
                adapaterCount = mAdapter.getItemCount();
                if(adjPosition < adapaterCount){
                    return mAdapter.getItemViewType(adjPosition);
                }
            }
            return RecyclerView.INVALID_TYPE - 1;//footer类型
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == RecyclerView.INVALID_TYPE){
                //header类型
                HeaderViewHolder headerViewHolder = new HeaderViewHolder(mHeaderViews.get(headerPosition++));

                if(headerPosition > getHeadersCount()-1){
                    headerPosition = 0;
                }
                //此处需要根据RecyclerView的方向判断，header如何设置参数
                int width = 0, height = 0;
                if(currentOrientation == OrientationHelper.VERTICAL){
                    //垂直放系那个
                    width = ViewGroup.LayoutParams.MATCH_PARENT;
                    height = ViewGroup.LayoutParams.WRAP_CONTENT;
                } else {
                    width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                StaggeredGridLayoutManager.LayoutParams params = new StaggeredGridLayoutManager.LayoutParams(width,height);
                params.setFullSpan(true);
//                params.setLayoutDirection(currentOrientation);
                headerViewHolder.itemView.setLayoutParams(params);
                mFootViews.get(0).setLayoutParams(params);
                return headerViewHolder;
            } else if(viewType == RecyclerView.INVALID_TYPE -1){
                //加载更多的footer
                StaggeredGridLayoutManager.LayoutParams params = new StaggeredGridLayoutManager.LayoutParams(
                        StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT, StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT);
                params.setFullSpan(true);
                mFootViews.get(0).setLayoutParams(params);
                return new HeaderViewHolder(mFootViews.get(0));
            }
            return mAdapter.onCreateViewHolder(parent,viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int headersCount = getHeadersCount();
            if(position < headersCount){
                //头部不进行绑定数据
                return;
            }
            int adjPosition = position - headersCount;
            int adapterCount;
            if(mAdapter != null){
                adapterCount = mAdapter.getItemCount();
                if(adjPosition < adapterCount){
                    mAdapter.onBindViewHolder(holder,adjPosition);
                }
            }
        }

        /**
         * 当前布局是否为Header
         *
         * @param position
         * @return
         */
        public boolean isHeader(int position) {
            return position >= 0 && position < mHeaderViews.size();
        }

        /**
         * 当前布局是否为Footer
         *
         * @param position
         * @return
         */
        public boolean isFooter(int position) {
            return position < getItemCount() && position >= getItemCount() - mFootViews.size();
        }

        /**
         * Header的数量
         *
         * @return
         */
        public int getHeadersCount() {
            return mHeaderViews.size();
        }

        /**
         * Footer的数量
         *
         * @return
         */
        public int getFootersCount() {
            return mFootViews.size();
        }
        private class HeaderViewHolder extends RecyclerView.ViewHolder {
            public HeaderViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    /**
     * 添加一个header
     * @param headerView
     */
    public void addHeader(View headerView){
        mHeaderViews.add(headerView);
    }


    /**
     * 添加多个header
     * @param headerViews
     */
    public void addheaders(ArrayList<View> headerViews){
        mHeaderViews.addAll(headerViews);
    }


    /**
     * 添加加载更多的footer
     * @param footerView
     */
    public void addFooter(View footerView){
        mLoadMoreFooterViews.clear();
        mLoadMoreFooterViews.add(footerView);
    }

    /**
     * 设置刷新和加载更多数据的监听
     *
     * @param listener
     */
    public void setLoadDataListener(LoadDataListener listener) {
        mLoadDataListener = listener;
    }

    /**
     * 加载更多数据完成后调用，必须在UI线程中
     */
    public void loadMoreComplate() {
        isLoadingData = false;
        if (mLoadMoreFooterViews.size() > 0) {
            mLoadMoreFooterViews.get(0).setVisibility(GONE);
        }
    }
    /**
     * 刷新和加载更多数据的监听接口
     */
    public interface LoadDataListener {
        /**
         * 执行加载更多
         */
        void onLoadMore();

    }
}
