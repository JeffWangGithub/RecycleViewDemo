package com.meilishuo.recycleviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @title:
 * @description:
 * @company: 美丽说（北京）网络科技有限公司
 * Created by Glan on 15/8/27.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    public int mOrientation = VERTICAL_LIST; //默认是垂直的

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider //系统的listView的分割线
    };
    private final Drawable mDivider;

    public DividerItemDecoration(Context context, int orientation){

        TypedArray ta = context.obtainStyledAttributes(ATTRS);
        mDivider = ta.getDrawable(0);
        ta.recycle();
        if(orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST){
            orientation = VERTICAL_LIST;
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        super.onDraw(c, parent);
        if(mOrientation == HORIZONTAL_LIST){
            drawHorizontal(c,parent);
        } else if(mOrientation == VERTICAL_LIST){
            drawVertical(c, parent);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {

        int parentPaddingLeft = parent.getPaddingLeft();
        int parentPaddingRight = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        for(int i = 0; i < childCount; i++){
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = params.bottomMargin + child.getBottom(); //分割线的top = item的bottomMargin + item的getBottom的值
            int bottom = top + mDivider.getIntrinsicHeight(); //分割线的下边缘等于top+分割线实际的高度
            mDivider.setBounds(parentPaddingLeft,top,parentPaddingRight,bottom);
            mDivider.draw(c);//绘制分割线
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        int top = parent.getPaddingTop();
        int bottom = parent.getHeight() - parent.getPaddingBottom();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = layoutParams.leftMargin + child.getRight();
            int right = left + mDivider.getIntrinsicWidth();
            mDivider.setBounds(left,top,right,bottom);
            mDivider.draw(c);
        }
    }
}
