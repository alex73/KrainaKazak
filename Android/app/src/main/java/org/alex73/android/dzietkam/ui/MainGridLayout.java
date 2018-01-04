package org.alex73.android.dzietkam.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MainGridLayout extends ViewGroup {
    public MainGridLayout(Context context) {
        super(context);
    }

    public MainGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public MainGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int w = right - left;
        int h = bottom - top;

        int childCount = getChildCount();
        int colCount = 0;
        int rowCount = 0;
        int sz;
        // find biggest icons
        for (sz = Math.max(w, h); sz > 0; sz--) {
            colCount = w / sz;
            rowCount = h / sz;
            if (colCount * rowCount >= childCount) {
                break;
            }
        }
        if (sz == 0) {
            return;
        }
        // try to decrease columns or rows count
        if (rowCount >= colCount) {
            for (; colCount * rowCount >= childCount; rowCount--) ;
            rowCount++;
        } else {
            for (; colCount * rowCount >= childCount; colCount--) ;
            colCount++;
        }

        for (int childIndex = 0, y = (h - sz * rowCount) / 2, r = 0; r < rowCount; r++, y += sz) {
            for (int x = (w - sz * colCount) / 2, c = 0; c < colCount && childIndex < childCount; c++, x += sz, childIndex++) {
                View child = getChildAt(childIndex);
                child.layout(x, y, x + sz, y + sz);
            }
        }
    }
}
