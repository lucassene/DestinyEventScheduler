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
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.utils.DateUtils;
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
            case R.layout.game_list_item_layout:
                getGames(view, context, cursor);
                break;
            case R.layout.done_game_item:
                getDoneGames(view, context, cursor);
                break;
        }

    }

    private void getDoneGames(View view, Context context, Cursor cursor) {

        ImageView gameIcon = (ImageView) view.findViewById(R.id.game_image);
        TextView gameTitle = (TextView) view.findViewById(R.id.primary_text);
        TextView gameCreator = (TextView) view.findViewById(R.id.secondary_text);
        TextView gameType = (TextView) view.findViewById(R.id.type_text);
        ImageView statusImg = (ImageView) view.findViewById(R.id.status_img);

        gameIcon.setImageResource(context.getResources().getIdentifier(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ICON)), "drawable", context.getPackageName()));
        gameTitle.setText(context.getResources().getIdentifier(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_NAME)), "string", context.getPackageName()));
        gameType.setText(context.getResources().getIdentifier(cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)), "string", context.getPackageName()));

        String creator = context.getResources().getString(R.string.created_by) + " " + cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_CREATOR_NAME));
        gameCreator.setText(creator);

        String gameStatus = cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_STATUS));
        if (gameStatus.equals(GameTable.STATUS_WAITING)){
            statusImg.setImageResource(R.drawable.ic_waiting);
        } else statusImg.setImageResource(R.drawable.ic_done);

    }


    private void getEvents(View view, Context context, Cursor cursor) {

        TextView title = (TextView) view.findViewById(R.id.primary_text);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);

        String textName = "";
        String iconName = "";

        switch (code) {
            case 10:
                textName = cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME));
                iconName = cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_ICON));
                break;
            case 20:
                textName = cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_NAME));
                iconName = cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ICON));
                break;
        }

        title.setText(context.getResources().getIdentifier(textName, "string", context.getPackageName()));
        icon.setImageResource(context.getResources().getIdentifier(iconName, "drawable", context.getPackageName()));

    }

    private void getGames(View view, Context context, Cursor cursor) {

        ImageView gameIcon = (ImageView) view.findViewById(R.id.game_image);
        TextView gameTitle = (TextView) view.findViewById(R.id.primary_text);
        TextView gameCreator = (TextView) view.findViewById(R.id.secondary_text);
        TextView gameDate = (TextView) view.findViewById(R.id.game_date);
        TextView gameTime = (TextView) view.findViewById(R.id.game_time);
        TextView gameMax = (TextView) view.findViewById(R.id.game_max);
        TextView gameInsc = (TextView) view.findViewById(R.id.game_actual);
        TextView gameType = (TextView) view.findViewById(R.id.type_text);

        gameIcon.setImageResource(context.getResources().getIdentifier(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ICON)), "drawable", context.getPackageName()));
        gameTitle.setText(context.getResources().getIdentifier(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_NAME)), "string", context.getPackageName()));
        gameType.setText(context.getResources().getIdentifier(cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)), "string", context.getPackageName()));

        String creator = context.getResources().getString(R.string.created_by) + " " + cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_CREATOR_NAME));
        gameCreator.setText(creator);

        gameDate.setText(DateUtils.onBungieDate(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
        gameTime.setText(DateUtils.getTime(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_TIME))));
        int max = cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.COLUMN_GUARDIANS));
        String maxS = " / " + max;
        gameMax.setText(maxS);
        int insc = cursor.getInt(cursor.getColumnIndexOrThrow(GameTable.COLUMN_INSCRIPTIONS));
        gameInsc.setText(String.valueOf(insc));

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
        String memberTitle = MemberTable.getMemberTitle(context, xp, cursor.getInt(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_TITLE)));
        points.setText(StringUtils.parseString(lvl));

        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
        title.setText(memberTitle);

        //String sinceString = DateUtils.onBungieDate(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_SINCE)));
        //memberSince.setText(sinceString);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return super.newView(context, cursor, parent);
    }

}