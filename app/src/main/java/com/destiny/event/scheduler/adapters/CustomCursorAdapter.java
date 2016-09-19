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
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.utils.ImageUtils;
import com.destiny.event.scheduler.utils.StringUtils;

import java.io.IOException;

public class CustomCursorAdapter extends SimpleCursorAdapter {

    private static final String TAG = "CustomCursorAdapter";

    private Context context;
    private int layout;
    private Cursor c;
    private final LayoutInflater inflater;
    private int code;

    public CustomCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, int code) {
        super(context, layout, c, from, to, flags);
        this.layout = layout;
        this.context = context;
        this.c = c;
        this.inflater = LayoutInflater.from(context);
        this.code = code;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        switch (layout) {
            case R.layout.event_list_item_layout:
                getEvents(view, context, cursor);
                break;
            case R.layout.member_list_item_layout:
                getMembers(view, context, cursor);
                break;
        }

    }

    private void getEvents(View view, Context context, Cursor cursor) {

        TextView title = (TextView) view.findViewById(R.id.primary_text);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);

        String textName = "";
        String iconName = "";

        switch (code) {
            case 10:
                textName = EventTypeTable.getName(context, cursor);
                iconName = cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_ICON));
                break;
            case 20:
                textName = EventTable.getName(context, cursor);
                iconName = cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ICON));
                break;
        }

        title.setText(textName);
        int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        if (resId != 0){
            icon.setImageResource(resId);
        } else {
            Log.w(TAG, "Drawable not found.");
            icon.setImageResource(R.drawable.ic_missing);
        }

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
            profile.setImageResource(R.drawable.ic_missing);
            e.printStackTrace();
        }

        int xp = cursor.getInt(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_EXP));
        int lvl = MemberTable.getMemberLevel(xp);
        points.setText(StringUtils.parseString(lvl));

        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
        title.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_TITLE)));

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return super.newView(context, cursor, parent);
    }

}