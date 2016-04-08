package com.destiny.event.scheduler.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] contentArray;
    private String[] imageArray;

    public SpinnerAdapter(Context context, int resource, String[] objects, String[] imageArray) {
        super(context,  R.layout.one_line_icon_list_layout, R.id.primary_text, objects);
        this.context = context;
        this.contentArray = objects;
        this.imageArray = imageArray;
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.one_line_icon_list_layout, parent, false);

        int textId = context.getResources().getIdentifier(contentArray[position],"string", context.getPackageName());
        TextView textView = (TextView) row.findViewById(R.id.primary_text);
        textView.setText(textId);

        int imageId = context.getResources().getIdentifier(imageArray[position],"drawable", context.getPackageName());
        ImageView imageView = (ImageView)row.findViewById(R.id.icon_list);
        imageView.setImageResource(imageId);

        return row;
    }
}