package com.mxin.jdweb.utils;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * good programmer.
 *
 * @data : 2018-01-11 下午 03:28
 * @author: futia
 * @email : futianyi1994@126.com
 * @description : RecyclerView 设置间距
 */


public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    public static final int LINEARLAYOUT = 0;
    public static final int GRIDLAYOUT = 1;
    public static final int STAGGEREDGRIDLAYOUT = 2;

    //限定为LINEARLAYOUT,GRIDLAYOUT,STAGGEREDGRIDLAYOUT
    @IntDef({LINEARLAYOUT, GRIDLAYOUT,STAGGEREDGRIDLAYOUT})
    //表示注解所存活的时间,在运行时,而不会存在. class 文件.
    @Retention(RetentionPolicy.SOURCE)
    public @interface LayoutManager {
        public int type() default LINEARLAYOUT;
    }


    private int leftRight;
    private int topBottom;
    /**
     * 头布局个数
     */
    private int headItemCount;
    /**
     * 边距
     */
    private int space;
    /**
     * 是否包含边距
     */
    private boolean includeEdge;
    /**
     * 烈数
     */
    private int spanCount;

    /**
     * 绘制顶部的间距
     */
    private boolean drawTopSpace = true;

    private @LayoutManager int layoutManager;

    /**
     * GridLayoutManager or StaggeredGridLayoutManager spacing
     * @param leftRight
     * @param topBottom
     * @param headItemCount
     * @param layoutManager
     */
    public SpaceItemDecoration(int leftRight, int topBottom, int headItemCount, @LayoutManager int layoutManager) {
        this.leftRight = leftRight;
        this.topBottom = topBottom;
        this.headItemCount = headItemCount;
        this.layoutManager = layoutManager;
    }

    /**
     * GridLayoutManager or StaggeredGridLayoutManager spacing
     * @param space
     * @param includeEdge
     * @param layoutManager
     */
    public SpaceItemDecoration(int space, boolean includeEdge, @LayoutManager int layoutManager) {
        this(space, 0, includeEdge, layoutManager);
    }
    /**
     * GridLayoutManager or StaggeredGridLayoutManager spacing
     * @param space
     * @param headItemCount
     * @param includeEdge
     * @param layoutManager
     */
    public SpaceItemDecoration(int space, int headItemCount, boolean includeEdge, @LayoutManager int layoutManager) {
        this.space = space;
        this.headItemCount = headItemCount;
        this.includeEdge = includeEdge;
        this.layoutManager = layoutManager;
    }

    /**
     * GridLayoutManager or StaggeredGridLayoutManager spacing
     * @param space
     * @param headItemCount
     * @param layoutManager
     */
    public SpaceItemDecoration(int space, int headItemCount, @LayoutManager int layoutManager) {
        this(space, headItemCount, true, layoutManager);
    }


    /**
     * LinearLayoutManager or GridLayoutManager or StaggeredGridLayoutManager spacing
     * @param space
     * @param layoutManager
     */
    public SpaceItemDecoration(int space, @LayoutManager int layoutManager) {
        this(space, 0, true, layoutManager);
    }

    public SpaceItemDecoration setDrawTopSpace(boolean drawTopSpace) {
        this.drawTopSpace = drawTopSpace;
        return this;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        switch (layoutManager) {
            case LINEARLAYOUT:
                setLinearLayoutSpaceItemDecoration(outRect,view,parent,state);
                break;
            case GRIDLAYOUT:
                GridLayoutManager gridLayoutManager = (GridLayoutManager) parent.getLayoutManager();
                //列数
                spanCount = gridLayoutManager.getSpanCount();
                setNGridLayoutSpaceItemDecoration(outRect,view,parent,state);
                break;
            case STAGGEREDGRIDLAYOUT:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) parent.getLayoutManager();
                //列数
                spanCount = staggeredGridLayoutManager.getSpanCount();
                setNGridLayoutSpaceItemDecoration(outRect,view,parent,state);
                break;
            default:
                break;
        }
    }

    /**
     * LinearLayoutManager spacing
     *
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    private void setLinearLayoutSpaceItemDecoration(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = space;
        } else {
            outRect.top = 0;
        }
    }

    /**
     * GridLayoutManager or StaggeredGridLayoutManager spacing
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    private void setNGridLayoutSpaceItemDecoration(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
        int position = -1;
        if (headItemCount != 0 && (position = parent.getChildAdapterPosition(view) - headItemCount) ==  - headItemCount){
            return;
        }

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        if(params instanceof StaggeredGridLayoutManager.LayoutParams){
            position = ((StaggeredGridLayoutManager.LayoutParams)params).getSpanIndex(); // 用这个方法可以防止瀑布流填充的时候间距错乱问题
        }else if(position == -1){
            position = parent.getChildAdapterPosition(view) - headItemCount;
        }
        int column = position % spanCount;
        if(parent.getLayoutManager() instanceof GridLayoutManager){
            column = ((GridLayoutManager)parent.getLayoutManager()).getSpanSizeLookup().getSpanIndex(position, spanCount);
        }


        if (includeEdge) {
            outRect.left = space - column * space / spanCount;
            outRect.right = (column + 1) * space / spanCount;
            if (drawTopSpace && position < spanCount) {
                outRect.top = space;
            }
            outRect.bottom = space;
        } else {
            outRect.left = column * space / spanCount;
            outRect.right = space - (column + 1) * space / spanCount;
            if (position >= spanCount) {
                outRect.top = space;
            }
        }

    }

    /**
     * GridLayoutManager设置间距（此方法最左边和最右边间距为设置的一半）
     *
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    private void setGridLayoutSpaceItemDecoration(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        //判断总的数量是否可以整除
        int totalCount = layoutManager.getItemCount();
        int surplusCount = totalCount % layoutManager.getSpanCount();
        int childPosition = parent.getChildAdapterPosition(view);
        //竖直方向的
        if (layoutManager.getOrientation() == GridLayoutManager.VERTICAL) {
            if (surplusCount == 0 && childPosition > totalCount - layoutManager.getSpanCount() - 1) {
                //后面几项需要bottom
                outRect.bottom = topBottom;
            } else if (surplusCount != 0 && childPosition > totalCount - surplusCount - 1) {
                outRect.bottom = topBottom;
            }
            //被整除的需要右边
            if ((childPosition + 1 - headItemCount) % layoutManager.getSpanCount() == 0) {
                //加了右边后最后一列的图就非宽度少一个右边距
                //outRect.right = leftRight;
            }
            outRect.top = topBottom;
            outRect.left = leftRight / 2;
            outRect.right = leftRight / 2;
        } else {
            if (surplusCount == 0 && childPosition > totalCount - layoutManager.getSpanCount() - 1) {
                //后面几项需要右边
                outRect.right = leftRight;
            } else if (surplusCount != 0 && childPosition > totalCount - surplusCount - 1) {
                outRect.right = leftRight;
            }
            //被整除的需要下边
            if ((childPosition + 1) % layoutManager.getSpanCount() == 0) {
                outRect.bottom = topBottom;
            }
            outRect.top = topBottom;
            outRect.left = leftRight;
        }
    }
}
