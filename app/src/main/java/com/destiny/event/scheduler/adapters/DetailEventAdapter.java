package com.destiny.event.scheduler.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.models.EntryModel;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.io.IOException;
import java.util.List;

public class DetailEventAdapter extends BaseAdapter {

    private static final String TAG = "GameAdapter";

    private static final int TYPE_CREATOR = 0;
    private static final int TYPE_NORMAL = 1;
    private static final int TYPE_WAITING = 2;

    private Context context;
    private List<EntryModel> entryList;
    private LayoutInflater inflater;
    private int maxGuardians;

    public DetailEventAdapter(Context context, List<EntryModel> entryList, int maxGuardians){
        this.context = context;
        this.entryList = entryList;
        this.maxGuardians = maxGuardians;
        inflater = LayoutInflater.from(context);
    }

    public void setEntryList(List<EntryModel> entryList){
        this.entryList = entryList;
        //notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return entryList.size();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return TYPE_CREATOR;
        } else if (position == maxGuardians){
            return TYPE_WAITING;
        } else return TYPE_NORMAL;
    }

    @Override
    public EntryModel getItem(int position) {
        return entryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EntryViewHolder viewHolder;
        if (convertView == null){
            int viewType = getItemViewType(position);
            switch (viewType){
                case TYPE_CREATOR:
                    convertView = inflater.inflate(R.layout.creator_list_item, parent, false);
                    break;
                case TYPE_NORMAL:
                    convertView = inflater.inflate(R.layout.member_list_item_layout, parent, false);
                    break;
                case TYPE_WAITING:
                    convertView = inflater.inflate(R.layout.wait_list_section_layout, parent, false);
                    break;
                default:
                    convertView = inflater.inflate(R.layout.member_list_item_layout, parent, false);
                    break;
            }
            viewHolder = new EntryViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (EntryViewHolder) convertView.getTag();
        }

        EntryModel currentMember = getItem(position);
        try {
            viewHolder.iconPath.setImageBitmap(ImageUtils.loadImage(context,ImageUtils.getIconName(currentMember.getIconPath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        viewHolder.memberName.setText(currentMember.getName());
        viewHolder.memberTitle.setText(currentMember.getTitle());
        viewHolder.memberLvl.setText(currentMember.getLvl());

        return convertView;
    }

    private class EntryViewHolder {

        ImageView iconPath;
        TextView memberName;
        TextView memberTitle;
        TextView memberLvl;

        public EntryViewHolder(View item){
            iconPath = (ImageView) item.findViewById(R.id.profile_pic);
            memberName = (TextView) item.findViewById(R.id.primary_text);
            memberTitle = (TextView) item.findViewById(R.id.secondary_text);
            memberLvl = (TextView) item.findViewById(R.id.text_points);
        }

    }

}
