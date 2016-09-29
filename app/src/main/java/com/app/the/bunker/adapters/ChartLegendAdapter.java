package com.app.the.bunker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.the.bunker.R;
import com.app.the.bunker.models.ChartLegendModel;

import java.util.List;


public class ChartLegendAdapter extends BaseAdapter {

    private static final String TAG = "ChartLegendAdapter";

    private Context context;
    private List<ChartLegendModel> legendList;
    private LayoutInflater inflater;

    public ChartLegendAdapter(Context context, List<ChartLegendModel> legendList) {
        this.context = context;
        this.legendList = legendList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return legendList.size();
    }

    @Override
    public ChartLegendModel getItem(int position) {
        return legendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LegendChartViewHolder viewHolder;

        if (convertView == null){
            convertView = inflater.inflate(R.layout.chart_item_layout, parent, false);
            viewHolder = new LegendChartViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (LegendChartViewHolder) convertView.getTag();
        }

        ChartLegendModel currentLegend = getItem(position);

        viewHolder.colorBox.setBackgroundColor(currentLegend.getColor());
        viewHolder.colorBox.setContentDescription(currentLegend.getTitle());
        viewHolder.titleText.setText(currentLegend.getTitle());
        viewHolder.valueText.setText(String.valueOf(currentLegend.getValue()));
        viewHolder.percentText.setText(String.valueOf(currentLegend.getPercent()));

        return convertView;
    }

    private class LegendChartViewHolder{

        ImageView colorBox;
        TextView titleText;
        TextView valueText;
        TextView percentText;

        public LegendChartViewHolder(View item){
            colorBox = (ImageView) item.findViewById(R.id.column_color);
            titleText = (TextView) item.findViewById(R.id.column_text);
            valueText = (TextView) item.findViewById(R.id.value_text);
            percentText = (TextView) item.findViewById(R.id.percent_text);
        }
    }

}
