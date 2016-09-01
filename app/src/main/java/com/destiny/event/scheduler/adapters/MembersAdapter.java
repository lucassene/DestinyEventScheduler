package com.destiny.event.scheduler.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.utils.ImageUtils;
import com.destiny.event.scheduler.utils.StringUtils;

import java.io.IOException;

public class MembersAdapter extends SimpleCursorAdapter {

    private static final String TAG = "MembersAdapter";

    private Context context;
    private int layout;
    private Cursor c;
    private final LayoutInflater inflater;
    private int max;

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_CREATOR = 1;
    private static final int VIEW_TYPE_WAIT_SECTION = 2;


    public MembersAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, int max) {
        super(context, layout, c, from, to, flags);
        this.layout = layout;
        this.context = context;
        this.c = c;
        this.inflater = LayoutInflater.from(context);
        this.max = max;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        int viewType = getItemViewType(cursor.getPosition());

        if (viewType == VIEW_TYPE_CREATOR){
            getMembers(view, context, cursor);
        } else if (viewType == VIEW_TYPE_WAIT_SECTION){
            getWaitSection(view, context);
            getMembers(view, context, cursor);
        } else getMembers(view, context, cursor);

    }

    private void getWaitSection(View view, Context context) {
        TextView section = (TextView) view.findViewById(R.id.section_wait_list);
        section.setText(context.getResources().getString(R.string.wait_list));
    }


    private void getMembers(View view, Context context, Cursor cursor) {

        TextView name = (TextView) view.findViewById(R.id.primary_text);
        ImageView profile = (ImageView) view.findViewById(R.id.profile_pic);
        TextView title = (TextView) view.findViewById(R.id.secondary_text);
        TextView points = (TextView) view.findViewById(R.id.text_points);


        try {
            profile.setImageBitmap(ImageUtils.loadImage(context, cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_ICON))));
        } catch (IOException e) {
            Log.w(TAG, "Image Bitmap not Found");
            e.printStackTrace();
        }

        int xp = cursor.getInt(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_EXP));
        int lvl = MemberTable.getMemberLevel(xp);
        points.setText(StringUtils.parseString(lvl));

        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
        title.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_TITLE)));

        //String sinceString = DateUtils.onBungieDate(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_SINCE)));
        //memberSince.setText(sinceString);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType){
            case VIEW_TYPE_CREATOR:
                layoutId = R.layout.creator_list_item;
                break;
            case VIEW_TYPE_NORMAL:
                layoutId = layout;
                break;
            case VIEW_TYPE_WAIT_SECTION:
                layoutId = R.layout.wait_list_section_layout;
                break;
        }

        return LayoutInflater.from(context).inflate(layoutId, parent, false);

        //return super.newView(context, cursor, parent);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return VIEW_TYPE_CREATOR;
        } else if (position == max){
            return VIEW_TYPE_WAIT_SECTION;
        } else return VIEW_TYPE_NORMAL;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }
}