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
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.io.IOException;

public class HistoryAdapter extends SimpleCursorAdapter {

    private static final String TAG = "HistoryAdapter";

    private Context context;
    private int layout;
    private Cursor c;
    private final LayoutInflater inflater;

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_CREATOR = 1;

    public HistoryAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.layout = layout;
        this.context = context;
        this.c = c;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        getMembers(view, context, cursor);
    }

    private void getMembers(View view, Context context, Cursor cursor) {

        TextView name = (TextView) view.findViewById(R.id.primary_text);
        ImageView profile = (ImageView) view.findViewById(R.id.profile_pic);
        TextView likesText = (TextView) view.findViewById(R.id.txt_likes);
        TextView dislikesText = (TextView) view.findViewById(R.id.txt_dislikes);
        TextView xpText = (TextView) view.findViewById(R.id.txt_xp);

        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));

        try {
            profile.setImageBitmap(ImageUtils.loadImage(context, cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_ICON))));
        } catch (IOException e) {
            Log.w(TAG, "Image Bitmap not Found");
            e.printStackTrace();
        }

        likesText.setText(cursor.getString(cursor.getColumnIndexOrThrow("likes")));
        dislikesText.setText(cursor.getString(cursor.getColumnIndexOrThrow("dislikes")));

        int xp = (Integer.parseInt((String)likesText.getText())*10)-(Integer.parseInt((String)dislikesText.getText())*10);
        if (cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_CREATOR)).equals(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_MEMBERSHIP)))) {
            xp = xp + Integer.parseInt(MemberTable.CREATOR_MODIFIER);
        } else xp = xp + Integer.parseInt(MemberTable.PLAYED_MODIFIER);

        String xpString = "+" + String.valueOf(xp) + " XP";
        xpText.setText(xpString);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType){
            case VIEW_TYPE_CREATOR:
                layoutId = R.layout.history_creator_member_item;
                break;
            case VIEW_TYPE_NORMAL:
                layoutId = R.layout.history_member_item;
                break;
        }

        return LayoutInflater.from(context).inflate(layoutId, parent, false);

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return VIEW_TYPE_CREATOR;
        } else return VIEW_TYPE_NORMAL;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}