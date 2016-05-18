package com.destiny.event.scheduler.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.data.ClanTable;
import com.destiny.event.scheduler.data.EntryTable;
import com.destiny.event.scheduler.data.EvaluationTable;
import com.destiny.event.scheduler.data.EventTable;
import com.destiny.event.scheduler.data.EventTypeTable;
import com.destiny.event.scheduler.data.GameTable;
import com.destiny.event.scheduler.data.LoggedUserTable;
import com.destiny.event.scheduler.data.MemberTable;
import com.destiny.event.scheduler.data.NotificationTable;

public class DBViewerCursorAdapter extends SimpleCursorAdapter {

    private static final String TAG = "DBViewerCursorAdapter";

    private Context context;
    private int layout;
    private Cursor c;
    private final LayoutInflater inflater;
    private String tableName;

    private TextView lbl1;
    private TextView lbl2;
    private TextView lbl3;
    private TextView lbl4;
    private TextView lbl5;
    private TextView lbl6;
    private TextView lbl7;
    private TextView lbl8;
    private TextView lbl9;
    private TextView lbl10;
    private TextView lbl11;

    private TextView txt1;
    private TextView txt2;
    private TextView txt3;
    private TextView txt4;
    private TextView txt5;
    private TextView txt6;
    private TextView txt7;
    private TextView txt8;
    private TextView txt9;
    private TextView txt10;
    private TextView txt11;

    public DBViewerCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, String tableName) {
        super(context, layout, c, from, to, flags);
        this.layout = layout;
        this.context = context;
        this.c = c;
        this.inflater = LayoutInflater.from(context);
        this.tableName = tableName;

        Log.w(TAG, "to count: " + to.length);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        TextView lbl1 = (TextView) view.findViewById(R.id.lbl1);
        TextView lbl2 = (TextView) view.findViewById(R.id.lbl2);
        TextView lbl3 = (TextView) view.findViewById(R.id.lbl3);
        TextView lbl4 = (TextView) view.findViewById(R.id.lbl4);
        TextView lbl5 = (TextView) view.findViewById(R.id.lbl5);
        TextView lbl6 = (TextView) view.findViewById(R.id.lbl6);
        TextView lbl7 = (TextView) view.findViewById(R.id.lbl7);
        TextView lbl8 = (TextView) view.findViewById(R.id.lbl8);
        TextView lbl9 = (TextView) view.findViewById(R.id.lbl9);
        TextView lbl10 = (TextView) view.findViewById(R.id.lbl10);
        TextView lbl11 = (TextView) view.findViewById(R.id.lbl11);

        TextView txt1 = (TextView) view.findViewById(R.id.txt1);
        TextView txt2 = (TextView) view.findViewById(R.id.txt2);
        TextView txt3 = (TextView) view.findViewById(R.id.txt3);
        TextView txt4 = (TextView) view.findViewById(R.id.txt4);
        TextView txt5 = (TextView) view.findViewById(R.id.txt5);
        TextView txt6 = (TextView) view.findViewById(R.id.txt6);
        TextView txt7 = (TextView) view.findViewById(R.id.txt7);
        TextView txt8 = (TextView) view.findViewById(R.id.txt8);
        TextView txt9 = (TextView) view.findViewById(R.id.txt9);
        TextView txt10 = (TextView) view.findViewById(R.id.txt10);
        TextView txt11 = (TextView) view.findViewById(R.id.txt11);

        switch (tableName) {
            case ClanTable.TABLE_NAME:
                lbl1.setText(ClanTable.COLUMN_ID);
                lbl2.setText(ClanTable.COLUMN_BUNGIE_ID);
                lbl3.setText(ClanTable.COLUMN_NAME);
                lbl4.setText(ClanTable.COLUMN_ICON);
                lbl5.setText(ClanTable.COLUMN_BACKGROUND);
                lbl6.setText(ClanTable.COLUMN_DESC);
                lbl7.setVisibility(View.GONE);
                lbl8.setVisibility(View.GONE);
                lbl9.setVisibility(View.GONE);
                lbl10.setVisibility(View.GONE);
                lbl11.setVisibility(View.GONE);
                txt1.setText(cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_ID)));
                txt2.setText(cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_BUNGIE_ID)));
                txt3.setText(cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_NAME)));
                txt4.setText(cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_ICON)));
                txt5.setText(cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_BACKGROUND)));
                txt6.setText(cursor.getString(cursor.getColumnIndexOrThrow(ClanTable.COLUMN_DESC)));
                txt7.setVisibility(View.GONE);
                txt8.setVisibility(View.GONE);
                txt9.setVisibility(View.GONE);
                txt10.setVisibility(View.GONE);
                txt11.setVisibility(View.GONE);
                break;
            case EntryTable.TABLE_NAME:
                lbl1.setText(EntryTable.COLUMN_ID);
                lbl2.setText(EntryTable.COLUMN_MEMBERSHIP);
                lbl3.setText(EntryTable.COLUMN_GAME);
                lbl4.setText(EntryTable.COLUMN_TIME);
                lbl5.setVisibility(View.GONE);
                lbl6.setVisibility(View.GONE);
                lbl7.setVisibility(View.GONE);
                lbl8.setVisibility(View.GONE);
                lbl9.setVisibility(View.GONE);
                lbl10.setVisibility(View.GONE);
                lbl11.setVisibility(View.GONE);
                txt1.setText(cursor.getString(cursor.getColumnIndexOrThrow(EntryTable.COLUMN_ID)));
                txt2.setText(cursor.getString(cursor.getColumnIndexOrThrow(EntryTable.COLUMN_MEMBERSHIP)));
                txt3.setText(cursor.getString(cursor.getColumnIndexOrThrow(EntryTable.COLUMN_GAME)));
                txt4.setText(cursor.getString(cursor.getColumnIndexOrThrow(EntryTable.COLUMN_TIME)));
                txt5.setVisibility(View.GONE);
                txt6.setVisibility(View.GONE);
                txt7.setVisibility(View.GONE);
                txt8.setVisibility(View.GONE);
                txt9.setVisibility(View.GONE);
                txt10.setVisibility(View.GONE);
                txt11.setVisibility(View.GONE);
                break;
            case EvaluationTable.TABLE_NAME:
                lbl1.setText(EvaluationTable.COLUMN_ID);
                lbl2.setText(EvaluationTable.COLUMN_GAME);
                lbl3.setText(EvaluationTable.COLUMN_MEMBERSHIP_A);
                lbl4.setText(EvaluationTable.COLUMN_EVALUATION);
                lbl5.setText(EvaluationTable.COLUMN_MEMBERSHIP_B);
                lbl6.setVisibility(View.GONE);
                lbl7.setVisibility(View.GONE);
                lbl8.setVisibility(View.GONE);
                lbl9.setVisibility(View.GONE);
                lbl10.setVisibility(View.GONE);
                lbl11.setVisibility(View.GONE);
                txt1.setText(cursor.getString(cursor.getColumnIndexOrThrow(EvaluationTable.COLUMN_ID)));
                txt2.setText(cursor.getString(cursor.getColumnIndexOrThrow(EvaluationTable.COLUMN_GAME)));
                txt3.setText(cursor.getString(cursor.getColumnIndexOrThrow(EvaluationTable.COLUMN_MEMBERSHIP_A)));
                txt4.setText(cursor.getString(cursor.getColumnIndexOrThrow(EvaluationTable.COLUMN_EVALUATION)));
                txt5.setText(cursor.getString(cursor.getColumnIndexOrThrow(EvaluationTable.COLUMN_MEMBERSHIP_B)));
                txt6.setVisibility(View.GONE);
                txt7.setVisibility(View.GONE);
                txt8.setVisibility(View.GONE);
                txt9.setVisibility(View.GONE);
                txt10.setVisibility(View.GONE);
                txt11.setVisibility(View.GONE);
                break;
            case EventTable.TABLE_NAME:
                lbl1.setText(EventTable.COLUMN_ID);
                lbl2.setText(EventTable.COLUMN_NAME);
                lbl3.setText(EventTable.COLUMN_ICON);
                lbl4.setText(EventTable.COLUMN_TYPE);
                lbl5.setText(EventTable.COLUMN_LIGHT);
                lbl6.setText(EventTable.COLUMN_GUARDIANS);
                lbl7.setVisibility(View.GONE);
                lbl8.setVisibility(View.GONE);
                lbl9.setVisibility(View.GONE);
                lbl10.setVisibility(View.GONE);
                lbl11.setVisibility(View.GONE);
                txt1.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ID)));
                txt2.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_NAME)));
                txt3.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_ICON)));
                txt4.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_TYPE)));
                txt5.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_LIGHT)));
                txt6.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_GUARDIANS)));
                txt7.setVisibility(View.GONE);
                txt8.setVisibility(View.GONE);
                txt9.setVisibility(View.GONE);
                txt10.setVisibility(View.GONE);
                txt11.setVisibility(View.GONE);
                break;
            case EventTypeTable.TABLE_NAME:
                lbl1.setText(EventTypeTable.COLUMN_ID);
                lbl2.setText(EventTypeTable.COLUMN_NAME);
                lbl3.setText(EventTypeTable.COLUMN_ICON);
                lbl4.setVisibility(View.GONE);
                lbl5.setVisibility(View.GONE);
                lbl6.setVisibility(View.GONE);
                lbl7.setVisibility(View.GONE);
                lbl8.setVisibility(View.GONE);
                lbl9.setVisibility(View.GONE);
                lbl10.setVisibility(View.GONE);
                lbl11.setVisibility(View.GONE);
                txt1.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_ID)));
                txt2.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_NAME)));
                txt3.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_ICON)));
                txt4.setVisibility(View.GONE);
                txt5.setVisibility(View.GONE);
                txt6.setVisibility(View.GONE);
                txt7.setVisibility(View.GONE);
                txt8.setVisibility(View.GONE);
                txt9.setVisibility(View.GONE);
                txt10.setVisibility(View.GONE);
                txt11.setVisibility(View.GONE);
                break;
            case GameTable.TABLE_NAME:
                lbl1.setText(GameTable.COLUMN_ID);
                lbl2.setText(GameTable.COLUMN_CREATOR);
                lbl3.setText(GameTable.COLUMN_CREATOR_NAME);
                lbl4.setText(GameTable.COLUMN_EVENT_ID);
                lbl5.setText(GameTable.COLUMN_TIME);
                lbl6.setText(GameTable.COLUMN_LIGHT);
                lbl7.setText(GameTable.COLUMN_INSCRIPTIONS);
                lbl8.setText(GameTable.COLUMN_STATUS);
                lbl9.setVisibility(View.GONE);
                lbl10.setVisibility(View.GONE);
                lbl11.setVisibility(View.GONE);
                txt1.setText(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_ID)));
                txt2.setText(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_CREATOR)));
                txt3.setText(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_CREATOR_NAME)));
                txt4.setText(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_EVENT_ID)));
                txt5.setText(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_TIME)));
                txt6.setText(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_LIGHT)));
                txt7.setText(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_INSCRIPTIONS)));
                txt8.setText(cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COLUMN_STATUS)));
                txt9.setVisibility(View.GONE);
                txt10.setVisibility(View.GONE);
                txt11.setVisibility(View.GONE);
                break;
            case LoggedUserTable.TABLE_NAME:
                lbl1.setText(LoggedUserTable.COLUMN_ID);
                lbl2.setText(LoggedUserTable.COLUMN_NAME);
                lbl3.setText(LoggedUserTable.COLUMN_MEMBERSHIP);
                lbl4.setText(LoggedUserTable.COLUMN_CLAN);
                lbl5.setText(LoggedUserTable.COLUMN_PLATFORM);
                lbl6.setVisibility(View.GONE);
                lbl7.setVisibility(View.GONE);
                lbl8.setVisibility(View.GONE);
                lbl9.setVisibility(View.GONE);
                lbl10.setVisibility(View.GONE);
                lbl11.setVisibility(View.GONE);
                txt1.setText(cursor.getString(cursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_ID)));
                txt2.setText(cursor.getString(cursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_NAME)));
                txt3.setText(cursor.getString(cursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_MEMBERSHIP)));
                txt4.setText(cursor.getString(cursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_CLAN)));
                txt5.setText(cursor.getString(cursor.getColumnIndexOrThrow(LoggedUserTable.COLUMN_PLATFORM)));
                txt6.setVisibility(View.GONE);
                txt7.setVisibility(View.GONE);
                txt8.setVisibility(View.GONE);
                txt9.setVisibility(View.GONE);
                txt10.setVisibility(View.GONE);
                txt11.setVisibility(View.GONE);
                break;
            case MemberTable.TABLE_NAME:
                lbl1.setText(MemberTable.COLUMN_ID);
                lbl2.setText(MemberTable.COLUMN_NAME);
                lbl3.setText(MemberTable.COLUMN_MEMBERSHIP);
                lbl4.setText(MemberTable.COLUMN_CLAN);
                lbl5.setText(MemberTable.COLUMN_ICON);
                lbl6.setText(MemberTable.COLUMN_PLATFORM);
                lbl7.setText(MemberTable.COLUMN_LIKES);
                lbl8.setText(MemberTable.COLUMN_DISLIKES);
                lbl9.setText(MemberTable.COLUMN_CREATED);
                lbl10.setText(MemberTable.COLUMN_PLAYED);
                //lbl11.setText(MemberTable.COLUMN_SINCE);

                txt1.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_ID)));
                txt2.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_NAME)));
                txt3.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_MEMBERSHIP)));
                txt4.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_CLAN)));
                txt5.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_ICON)));
                txt6.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_PLATFORM)));
                txt7.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_LIKES)));
                txt8.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_DISLIKES)));
                txt9.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_CREATED)));
                txt10.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_PLAYED)));
                //txt11.setText(cursor.getString(cursor.getColumnIndexOrThrow(MemberTable.COLUMN_SINCE)));
                break;
            case NotificationTable.TABLE_NAME:
                lbl1.setText(NotificationTable.COLUMN_ID);
                lbl2.setText(NotificationTable.COLUMN_GAME);
                lbl3.setText(NotificationTable.COLUMN_EVENT);
                lbl4.setText(NotificationTable.COLUMN_TYPE);
                lbl5.setText(NotificationTable.COLUMN_ICON);
                lbl6.setText(NotificationTable.COLUMN_TIME);
                lbl7.setVisibility(View.GONE);
                lbl8.setVisibility(View.GONE);
                lbl9.setVisibility(View.GONE);
                lbl10.setVisibility(View.GONE);
                lbl11.setVisibility(View.GONE);
                txt1.setText(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ID)));
                txt2.setText(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_GAME)));
                txt3.setText(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_EVENT)));
                txt4.setText(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TYPE)));
                txt5.setText(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_ICON)));
                txt6.setText(cursor.getString(cursor.getColumnIndexOrThrow(NotificationTable.COLUMN_TIME)));
                txt7.setVisibility(View.GONE);
                txt8.setVisibility(View.GONE);
                txt9.setVisibility(View.GONE);
                txt10.setVisibility(View.GONE);
                txt11.setVisibility(View.GONE);
                break;
        }

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return super.newView(context, cursor, parent);
    }
}