package com.destiny.event.scheduler.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;

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
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return super.newView(context, cursor, parent);
    }
}
