package com.destiny.event.scheduler.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.MemberTable;

public class CustomCursorAdapter extends SimpleCursorAdapter {

    private Context context;
    private int layout;
    private Cursor c;
    private final LayoutInflater inflater;
    private int code;

    public CustomCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, int code){
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

        switch (layout){
            case R.layout.event_list_item_layout:
                TextView title = (TextView) view.findViewById(R.id.primary_text);
                ImageView icon = (ImageView) view.findViewById(R.id.icon);

                String textName = "";
                String iconName = "";

                switch (code){
                    case 10:
                        textName = cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME));
                        iconName = cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_ICON));
                        break;
                    case 20:
                        textName = cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_NAME));
                        iconName = cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ICON));
                        break;
                }

                title.setText(context.getResources().getIdentifier(textName,"string",context.getPackageName()));
                icon.setImageResource(context.getResources().getIdentifier(iconName,"drawable",context.getPackageName()));
                break;
            case R.layout.member_list_item_layout:
                TextView name = (TextView) view.findViewById(R.id.primary_text);
                ImageView profile = (ImageView) view.findViewById(R.id.profile_pic);
                TextView memberSince = (TextView) view.findViewById(R.id.secondary_text);
                TextView likeText = (TextView) view.findViewById(R.id.like_text);
                TextView dislikeText = (TextView) view.findViewById(R.id.dislike_text);
                ImageView likeImg = (ImageView) view.findViewById(R.id.like_icon);
                ImageView dislikeImg = (ImageView) view.findViewById(R.id.dislike_icon);

                name.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                String since = "Member since: " + cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_SINCE));
                memberSince.setText(since);
                likeText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_LIKES))));
                dislikeText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_DISLIKES))));
                likeImg.setColorFilter(ContextCompat.getColor(context,R.color.liveColor), PorterDuff.Mode.SRC_IN);
                dislikeImg.setColorFilter(ContextCompat.getColor(context,R.color.redFilter), PorterDuff.Mode.SRC_IN);
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return super.newView(context, cursor, parent);
    }
}
