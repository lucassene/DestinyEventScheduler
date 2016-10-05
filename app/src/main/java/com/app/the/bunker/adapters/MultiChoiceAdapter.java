package com.app.the.bunker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.app.the.bunker.R;
import com.app.the.bunker.models.MultiChoiceItemModel;

import java.util.ArrayList;
import java.util.List;


public class MultiChoiceAdapter extends BaseAdapter {

    private static final String TAG = "MultiChoiceAdapter";

    private List<MultiChoiceItemModel> list;
    private List<MultiChoiceItemModel> oldList;
    private LayoutInflater inflater;

    public MultiChoiceAdapter(Context context, List<MultiChoiceItemModel> list){
        this.list = list;
        oldList = new ArrayList<>();
        oldList.addAll(list);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public MultiChoiceItemModel getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MultiChoiceViewHolder viewHolder;
        if (convertView == null){
            convertView = inflater.inflate(R.layout.multi_dialog_item_layout, parent, false);
            viewHolder = new MultiChoiceViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else viewHolder = (MultiChoiceViewHolder) convertView.getTag();

        MultiChoiceItemModel currentItem = getItem(position);
        viewHolder.checkBox.setChecked(currentItem.isChecked());
        viewHolder.textView.setText(currentItem.getText());
        return convertView;
    }

    public void toogleItemChecked(int position){
        list.get(position).setChecked(!list.get(position).isChecked());
        //Log.w(TAG, list.get(position).getText() + " state changed to " + list.get(position).isChecked());
    }

    public List<MultiChoiceItemModel> getItemList(){
        return list;
    }

    private class MultiChoiceViewHolder{

        CheckBox checkBox;
        TextView textView;

        MultiChoiceViewHolder(View item){
            checkBox = (CheckBox) item.findViewById(R.id.checkbox);
            textView = (TextView) item.findViewById(R.id.text);
        }

    }

}
