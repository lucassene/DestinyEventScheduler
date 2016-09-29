package com.app.the.bunker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.the.bunker.R;
import com.app.the.bunker.data.MemberTable;
import com.app.the.bunker.models.MemberModel;
import com.app.the.bunker.utils.ImageUtils;

import java.io.IOException;
import java.util.List;

public class DetailHistoryAdapter extends BaseAdapter {

    private static final String TAG = "GameAdapter";

    private static final int TYPE_CREATOR = 0;
    private static final int TYPE_NORMAL = 1;

    private Context context;
    private List<MemberModel> entryList;
    private LayoutInflater inflater;

    public DetailHistoryAdapter(Context context, List<MemberModel> entryList){
        this.context = context;
        this.entryList = entryList;
        inflater = LayoutInflater.from(context);
    }

    public void setEntryList(List<MemberModel> entryList){
        this.entryList = entryList;
    }

    @Override
    public int getCount() {
        if (entryList == null){
            return 0;
        } else return entryList.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return TYPE_CREATOR;
        } else return TYPE_NORMAL;
    }

    @Override
    public MemberModel getItem(int position) {
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
                    convertView = inflater.inflate(R.layout.history_creator_member_item, parent, false);
                    break;
                case TYPE_NORMAL:
                    convertView = inflater.inflate(R.layout.history_member_item, parent, false);
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

        MemberModel currentMember = getItem(position);
        try {
            viewHolder.iconPath.setImageBitmap(ImageUtils.loadImage(context,ImageUtils.getIconName(currentMember.getIconPath())));
            viewHolder.iconPath.setContentDescription(currentMember.getName());
        } catch (IOException e) {
            e.printStackTrace();
            viewHolder.iconPath.setImageResource(R.drawable.ic_missing);
        }
        viewHolder.memberName.setText(currentMember.getName());
        viewHolder.memberTitle.setText(currentMember.getTitle());
        viewHolder.memberLikes.setText(String.valueOf(currentMember.getLikes()));
        viewHolder.memberDislikes.setText(String.valueOf(currentMember.getDislikes()));
        viewHolder.memberXP.setText(getGainedXP(position, currentMember.getLikes(), currentMember.getDislikes()));

        return convertView;
    }

    private String getGainedXP(int position, int likes, int dislikes) {
        int xp = 0;
        if (getItemViewType(position) == TYPE_CREATOR){
            xp = xp + Integer.parseInt(MemberTable.CREATOR_MODIFIER);
        } else {
            xp = xp + Integer.parseInt(MemberTable.PLAYED_MODIFIER);
        }
        xp = xp + (likes * Integer.parseInt(MemberTable.LIKE_MODIFIER));
        xp = xp - (dislikes * Integer.parseInt(MemberTable.DISLIKE_MODIFIER));
        String prefix;
        if (xp >= 0){
            prefix = "+ ";
        } else prefix = "- ";
        return prefix + String.valueOf(xp) + " " + context.getString(R.string.xp);
    }

    private class EntryViewHolder {

        ImageView iconPath;
        TextView memberName;
        TextView memberTitle;
        TextView memberXP;
        TextView memberLikes;
        TextView memberDislikes;

        public EntryViewHolder(View item){
            iconPath = (ImageView) item.findViewById(R.id.profile_pic);
            memberName = (TextView) item.findViewById(R.id.primary_text);
            memberTitle = (TextView) item.findViewById(R.id.secondary_text);
            memberXP = (TextView) item.findViewById(R.id.txt_xp);
            memberLikes = (TextView) item.findViewById(R.id.txt_likes);
            memberDislikes = (TextView) item.findViewById(R.id.txt_dislikes);
        }

    }

}
