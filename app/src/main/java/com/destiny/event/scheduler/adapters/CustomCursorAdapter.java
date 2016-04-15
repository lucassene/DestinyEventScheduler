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
import com.destiny.event.scheduler.utils.DateUtils;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomCursorAdapter extends SimpleCursorAdapter {

    private static final String TAG = "CustomCursorAdapter";

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
                TextView points = (TextView) view.findViewById(R.id.text_points);


                try {
                    profile.setImageBitmap(ImageUtils.loadImage(context, cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_ICON))));
                } catch (IOException e){
                    Log.w(TAG, "Image Bitmap not Found");
                    e.printStackTrace();
                }

                float likes = (float) cursor.getInt(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_LIKES));
                float dislikes = (float) cursor.getInt(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_DISLIKES));
                float created = (float) cursor.getInt(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_CREATED));
                float played = (float) cursor.getInt(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_PLAYED));

                double totalPoints = (likes/played)*100;
                totalPoints = totalPoints + (created*0.5);
                totalPoints = totalPoints - dislikes;

                if (Math.round(totalPoints) >= 100){
                    points.setText("99");
                } else if (Math.round(totalPoints) < 10){
                    points.setText("0" + Math.round(totalPoints));
                }

                name.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                
                String sinceString = DateUtils.onBungieDate(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_SINCE)));

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date sinceDate = df.parse(sinceString);
                    df = new SimpleDateFormat("dd/MM/yyyy");
                    String finalDate = df.format(sinceDate);
                    String since = context.getString(R.string.member_since_label) + finalDate;
                    memberSince.setText(since);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return super.newView(context, cursor, parent);
    }
}
