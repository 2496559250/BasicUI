package com.peakmain.ui.recyclerview.itemdecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

import com.peakmain.ui.R;
import com.peakmain.ui.recyclerview.group.GroupRecyclerBean;
import com.peakmain.ui.utils.SizeUtils;

import java.util.List;

/**
 * author ：Peakmain
 * createTime：2020/2/29
 * mail:2726449200@qq.com
 * describe：基本悬浮列表
 */
public abstract class BaseSuspenisonItemDecoration2<T extends GroupRecyclerBean> extends RecyclerView.ItemDecoration {
    private List<T> mData;
    private Paint mBgPaint;
    private TextPaint mTextPaint;
    private Rect mBounds;
    //置顶距离文字的高度 默认是30
    private int mSectionHeight;
    private int mBgColor;
    private int mTextColor;
    private int mTextSize;
    //两个置顶模块之间的距离，默认是10
    private int topHeight;
    private int mPaddingLeft;

    public BaseSuspenisonItemDecoration2(BaseSuspenisonItemDecoration2.Builder builder) {
        this.mData = builder.mData;

        mBgColor = builder.mBgColor != 0 ? builder.mBgColor : ContextCompat.getColor(builder.mContext, android.R.color.white);
        mSectionHeight = builder.mSectionHeight != 0 ? builder.mSectionHeight : SizeUtils.dp2px(builder.mContext, 30);
        topHeight = builder.topHeight != 0 ? builder.topHeight : SizeUtils.dp2px(builder.mContext, 10);
        mTextSize = builder.mTextSize != 0 ? builder.mTextSize : SizeUtils.dp2px(builder.mContext, 10);
        mTextColor = builder.mTextColor != 0 ? builder.mTextColor : ContextCompat.getColor(builder.mContext, R.color.color_4A4A4A);
        mPaddingLeft = builder.mPaddingLeft != 0 ? builder.mPaddingLeft : SizeUtils.dp2px(builder.mContext, 10);
        initPaint();

        mBounds = new Rect();
    }

    private void initPaint() {
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(mBgColor);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
    }


    public void setData(List<T> data) {
        this.mData = data;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            int position = params.getViewLayoutPosition();
            if (mData != null && !mData.isEmpty() && position <= mData.size() - 1 && position > -1) {
                if (null != getTopText(mData, position)
                        && !getTopText(mData, position).equals(getTopText(mData, position - 1))) {
                    drawSection(c, left, right, child, params, position);
                }
            }
        }
    }


    private void drawSection(Canvas c, int left, int right, View child,
                             RecyclerView.LayoutParams params, int position) {
        String topText = getTopText(mData, position);
        if (!TextUtils.isEmpty(topText)) {
            if (position != 1) {
                c.drawRect(left,
                        child.getTop() - params.topMargin - mSectionHeight,
                        right,
                        child.getTop() - params.topMargin, mBgPaint);
                mTextPaint.getTextBounds(topText,
                        0,
                        topText.length(),
                        mBounds);
                c.drawText(topText,
                        child.getPaddingLeft() + mPaddingLeft,
                        child.getTop() - params.topMargin - mSectionHeight / 2 + mBounds.height() / 2,
                        mTextPaint);
            }
        }

    }

    @Override
    public void onDrawOver(@NonNull Canvas c, RecyclerView parent, @NonNull RecyclerView.State state) {
        int pos = 0;
        pos = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
        if (pos < 0) {
            return;
        }
        if (mData == null || mData.isEmpty()) {
            return;
        }
        String section = getTopText(mData, pos);
        View child = parent.findViewHolderForLayoutPosition(pos).itemView;

        boolean flag = false;
        if ((pos + 1) < mData.size()) {
            if (null != section && !section.equals(getTopText(mData, pos + 1))) {
                if (child.getHeight() + child.getTop() < mSectionHeight) {
                    c.save();
                    flag = true;
                    c.translate(0, child.getHeight() + child.getTop() - mSectionHeight);
                }
            }
        }
        c.drawRect(parent.getPaddingLeft(),
                parent.getPaddingTop(),
                parent.getRight() - parent.getPaddingRight(),
                parent.getPaddingTop() + mSectionHeight, mBgPaint);

        if (!TextUtils.isEmpty(section)) {

            mTextPaint.getTextBounds(section, 0, section.length(), mBounds);
            c.drawText(section,
                    child.getPaddingLeft() + mPaddingLeft,
                    parent.getPaddingTop() + mSectionHeight - (mSectionHeight / 2 - mBounds.height() / 2),
                    mTextPaint);
        } else if (pos == 0||mData.get(pos).isHeader) {
            section = getTopText(mData, pos + 1);
            if (!TextUtils.isEmpty(section)) {
                mTextPaint.getTextBounds(section, 0, section.length(), mBounds);
                c.drawText(section,
                        child.getPaddingLeft() + mPaddingLeft,
                        parent.getPaddingTop() + mSectionHeight - (mSectionHeight / 2 - mBounds.height() / 2),
                        mTextPaint);
            }
        }

        if (flag) {
            c.restore();
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        if (mData != null && !mData.isEmpty() && position <= mData.size() - 1 && position > -1) {
            if (position / (getSpanCount(parent) + 1) == 0) {
                outRect.set(0, topHeight
                        , 0, 0);
            } else {
                if (mData.get(position).isHeader) {
                    outRect.set(0, mSectionHeight + topHeight / 2, 0, 0);
                }
            }
        }
    }

    /**
     * 设置置顶的文字
     */
    public abstract String getTopText(List<T> data, int position);

    public abstract static class Builder<B extends BaseSuspenisonItemDecoration2.Builder, T> {
        protected Context mContext;
        private List<T> mData;
        private int mBgColor;
        private int mSectionHeight;
        private int topHeight;
        private int mTextSize;
        private int mTextColor;
        private int mPaddingLeft;

        public Builder(Context context, List<T> data) {
            mContext = context;
            mData = data;
        }

        public B setBgColor(int bgColor) {
            this.mBgColor = bgColor;

            return (B) this;
        }

        /**
         * 置顶距离文字的高度 默认是30
         */
        public B setSectionHeight(int sectionHeight) {
            this.mSectionHeight = sectionHeight;
            return (B) this;
        }

        /**
         * 两个置顶模块之间的距离，默认是10
         *
         * @param topHeight topHeight
         */
        public B setTopHeight(int topHeight) {
            this.topHeight = topHeight;
            return (B) this;
        }

        /**
         * 设置文字的大小
         *
         * @param textSize 文字大小
         */
        public B setTextSize(int textSize) {
            this.mTextSize = textSize;
            return (B) this;
        }

        /**
         * 设置文字的颜色
         *
         * @param textColor 文字的颜色
         */
        public B setTextColor(int textColor) {
            mTextColor = textColor;

            return (B) this;
        }

        public B setPaddingLeft(int paddingLeft) {
            mPaddingLeft = paddingLeft;
            return (B) this;
        }

        protected abstract BaseSuspenisonItemDecoration2 create();
    }

    public int getSpanCount(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            int spanCount = gridLayoutManager.getSpanCount();
            return spanCount;
        }
        return 1;
    }

}