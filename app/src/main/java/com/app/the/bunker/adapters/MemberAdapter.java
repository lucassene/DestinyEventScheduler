package com.app.the.bunker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.the.bunker.R;
import com.app.the.bunker.models.MemberModel;
import com.app.the.bunker.utils.ImageUtils;

import java.io.IOException;
import java.util.List;

public class MemberAdapter extends BaseAdapter {

    private static final String TAG = "MemberAdapter";
    private Context context;
    private List<MemberModel> memberList;
    private LayoutInflater inflater;

    public MemberAdapter(Context context, List<MemberModel> memberList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.memberList = memberList;
    }

    public void setMemberList(List<MemberModel> memberList){
        this.memberList = memberList;
    }

    public void toggleMemberCheck(int position){
        memberList.get(position).setChecked(!memberList.get(position).isChecked());
    }

    public int getCheckedMemberCount(){
        int count = 0;
        for (int i=0;i<memberList.size();i++){
            if (memberList.get(i).isChecked()) count++;
        }
        return count;
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public MemberModel getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MemberHolder mHolder;
        if (convertView == null){
            convertView = inflater.inflate(R.layout.member_list_item_layout, parent, false);
            mHolder = new MemberHolder(convertView);
            convertView.setTag(mHolder);
        } else{
            mHolder = (MemberHolder) convertView.getTag();
        }

        MemberModel currentMember = getItem(position);
        mHolder.name.setText(currentMember.getName());
        mHolder.title.setText(currentMember.getTitle());
        currentMember.setLvl(currentMember.getLikes(), currentMember.getDislikes(), currentMember.getGamesPlayed(), currentMember.getGamesCreated());
        mHolder.xp.setText(currentMember.getLvl());
        try {
            mHolder.icon.setImageBitmap(ImageUtils.loadImage(context, ImageUtils.getIconName(currentMember.getIconPath())));
        } catch (IOException e) {
            mHolder.icon.setImageResource(R.drawable.ic_default_avatar);
            e.printStackTrace();
        }
        if (currentMember.isChecked()){
            mHolder.layout.setAlpha(0.50f);
        } else mHolder.layout.setAlpha(0.05f);
        return convertView;
    }

    private class MemberHolder{
        FrameLayout layout;
        ImageView icon;
        TextView name;
        TextView title;
        TextView xp;

        MemberHolder(View view){
            layout = (FrameLayout) view.findViewById(R.id.member_layout);
            icon = (ImageView) view.findViewById(R.id.profile_pic);
            name = (TextView) view.findViewById(R.id.primary_text);
            title = (TextView) view.findViewById(R.id.secondary_text);
            xp = (TextView) view.findViewById(R.id.text_points);
        }
    }

}
