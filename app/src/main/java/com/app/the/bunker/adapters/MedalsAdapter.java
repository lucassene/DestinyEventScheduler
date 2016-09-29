package com.app.the.bunker.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.the.bunker.R;
import com.app.the.bunker.models.MedalModel;

import java.util.ArrayList;

public class MedalsAdapter extends BaseAdapter{

    private static final String TAG = "MedalsAdapter";

    private Context context;
    private ArrayList<MedalModel> medalsList;
    private LayoutInflater inflater;

    public MedalsAdapter(Context context, ArrayList<MedalModel> medalsList){
        this.context = context;
        this.medalsList = medalsList;
        inflater = LayoutInflater.from(context);
    }

    public void setMedalList(ArrayList<MedalModel> medalsList){
        this.medalsList = medalsList;
    }

    @Override
    public int getCount() {
        return medalsList.size();
    }

    @Override
    public MedalModel getItem(int position) {
        return medalsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MedalViewHolder viewHolder;
        if (convertView == null){
            convertView = inflater.inflate(R.layout.medal_item_layout, parent, false);
            viewHolder = new MedalViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MedalViewHolder) convertView.getTag();
        }

        TypedArray icons = context.getResources().obtainTypedArray(R.array.medals_icons);

        MedalModel currentMedal = getItem(position);
        viewHolder.icon.setImageResource(icons.getResourceId(currentMedal.getIcon(),0));
        viewHolder.icon.setContentDescription(currentMedal.getName());
        viewHolder.medalTitle.setText(currentMedal.getName());
        viewHolder.medalDesc.setText(currentMedal.getDesc());
        int value = currentMedal.getValue();
        int nextValue = getNextValue(value);
        String valuesText = value + "/" + nextValue;
        viewHolder.medalValues.setText(valuesText);
        getHighlightedStars(viewHolder, value);
        icons.recycle();

        return convertView;
    }

    private void getHighlightedStars(MedalViewHolder viewHolder, int value) {
        if (value < 1){
            setStarts(viewHolder, 0.1f,0.1f,0.1f,0.1f,0.1f);
        } else if (value < 10){
            setStarts(viewHolder, 1.0f,0.1f,0.1f,0.1f,0.1f);
        } else if (value <50){
            setStarts(viewHolder, 1.0f,1.0f,0.1f,0.1f,0.1f);
        } else if (value <250){
            setStarts(viewHolder, 1.0f,1.0f,1.0f,0.1f,0.1f);
        } else if (value <500){
            setStarts(viewHolder, 1.0f,1.0f,1.0f,1.0f,0.1f);
        } else setStarts(viewHolder, 1.0f,1.0f,1.0f,1.0f,1.0f);
    }

    private void setStarts(MedalViewHolder viewHolder, float v1, float v2, float v3, float v4, float v5) {
        viewHolder.star1.setAlpha(v1);
        viewHolder.star2.setAlpha(v2);
        viewHolder.star3.setAlpha(v3);
        viewHolder.star4.setAlpha(v4);
        viewHolder.star5.setAlpha(v5);
    }

    private int getNextValue(int value) {
        if (value < 1){
            return 1;
        } else if (value < 10){
            return 10;
        } else if (value <50){
            return 50;
        } else if (value <250){
            return 250;
        } else return 500;
    }

    private class MedalViewHolder {

        ImageView icon;
        TextView medalTitle;
        TextView medalDesc;
        TextView medalValues;
        ImageView star1;
        ImageView star2;
        ImageView star3;
        ImageView star4;
        ImageView star5;

        public MedalViewHolder(View item){
            icon = (ImageView) item.findViewById(R.id.medal_img);
            medalTitle = (TextView) item.findViewById(R.id.primary_text);
            medalDesc = (TextView) item.findViewById(R.id.secondary_text);
            medalValues = (TextView) item.findViewById(R.id.terciary_text);
            star1 = (ImageView) item.findViewById(R.id.star1);
            star2 = (ImageView) item.findViewById(R.id.star2);
            star3 = (ImageView) item.findViewById(R.id.star3);
            star4 = (ImageView) item.findViewById(R.id.star4);
            star5 = (ImageView) item.findViewById(R.id.star5);
        }

    }
}
