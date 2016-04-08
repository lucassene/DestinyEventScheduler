package com.destiny.event.scheduler.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class NewEventAdapter extends BaseAdapter {

    private static final int TYPE_SECTION = 1;
    private static final int TYPE_SIMPLE_ITEM = 2;
    private static final int TYPE_ICON_ITEM = 3;

    private String section[];
    private String icon[];
    private String game_type[];
    private String game[];


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount(){
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
