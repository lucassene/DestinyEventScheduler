package com.destiny.event.scheduler.views;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CustomSwipeLayout extends SwipeRefreshLayout {
    private ViewGroup container;

    public CustomSwipeLayout(Context context) {
        super(context);
    }

    public CustomSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        ViewGroup container = getContainer();
        if (container == null){
            return false;
        }
        if (container.getChildCount() != 2){
            throw new RuntimeException("SwipeRefreshLayout container must have two child views.");
        }
        View view = container.getChildAt(0);
        if (view.getVisibility() != View.VISIBLE){
            view = container.getChildAt(1);
        }
        return ViewCompat.canScrollVertically(view, -1);
    }

    private ViewGroup getContainer() {
        if (container != null) {
            return container;
        }

        for (int i=0; i<getChildCount(); i++) {
            if (getChildAt(i) instanceof ViewGroup) {
                container = (ViewGroup) getChildAt(i);
                break;
            }
        }

        if (container == null) {
            throw new RuntimeException("Container view not found");
        }

        return container;
    }
}
