package com.destiny.event.scheduler.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.models.CompleteMemberModel;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.io.IOException;
import java.util.List;

public class SimpleMemberAdapter extends BaseAdapter {

    private static final String TAG = "SimpleMemberAdapter";

    private static final int TYPE_CREATOR = 0;
    private static final int TYPE_MEMBER = 1;

    private Context context;
    private List<CompleteMemberModel> memberList;
    private LayoutInflater inflater;

    public SimpleMemberAdapter(Context context, List<CompleteMemberModel> memberList){
        this.context = context;
        this.memberList = memberList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public CompleteMemberModel getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return TYPE_CREATOR;
        } else return TYPE_MEMBER;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ValidateViewHolder vViewHolder;

        if (convertView == null){
            if (getItemViewType(position) == TYPE_CREATOR){
                convertView = inflater.inflate(R.layout.simple_creator_item_layout, parent, false);
            } else convertView = inflater.inflate(R.layout.simple_member_item_layout, parent, false);
            vViewHolder = new ValidateViewHolder(convertView);
            convertView.setTag(vViewHolder);
        } else {
            vViewHolder = (ValidateViewHolder) convertView.getTag();
        }

        CompleteMemberModel currentMember = getItem(position);

        vViewHolder.memberName.setText(currentMember.getName());
        vViewHolder.memberTitle.setText(currentMember.getTitle());
        try {
            vViewHolder.memberIcon.setImageBitmap(ImageUtils.loadImage(context, currentMember.getIconPath()));
        } catch (IOException e) {
            Log.w(TAG, "Image not found!");
            e.printStackTrace();
        }
        vViewHolder.memberChecked = currentMember.isChecked();

        if (currentMember.isChecked()){
            switch (currentMember.getRating()) {
                case -1:
                    vViewHolder.memberRating.setVisibility(View.VISIBLE);
                    vViewHolder.memberRating.setImageResource(R.drawable.ic_dislike);
                    //vViewHolder.memberRating.setColorFilter(R.color.redFilter, PorterDuff.Mode.SRC_IN);
                    break;
                case 0:
                    vViewHolder.memberRating.setVisibility(View.GONE);
                    break;
                case 1:
                    vViewHolder.memberRating.setVisibility(View.VISIBLE);
                    vViewHolder.memberRating.setImageResource(R.drawable.ic_like);
                   //vViewHolder.memberRating.setColorFilter(R.color.psnColor, PorterDuff.Mode.SRC_IN);
                    break;
            }
        } else {
                vViewHolder.memberRating.setVisibility(View.VISIBLE);
                vViewHolder.memberRating.setImageResource(R.drawable.ic_error);
            }

/*        if (vViewHolder.memberChecked){
            convertView.setAlpha(1.0f);
        } else convertView.setAlpha(0.3f);*/

/*        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox check = (CheckBox) v;
                SimpleMemberModel member = (SimpleMemberModel) check.getTag();
                member.setChecked(check.isChecked());
            }
        });*/

        return convertView;
    }

    public void setRating(int memberPos, int rating){
        memberList.get(memberPos).setRating(rating);
    }

    private class ValidateViewHolder {

        TextView memberName;
        ImageView memberIcon;
        boolean memberChecked;
        ImageView memberRating;
        TextView memberTitle;

        public ValidateViewHolder(View item){
            memberTitle = (TextView) item.findViewById(R.id.secondary_text);
            memberName = (TextView) item.findViewById(R.id.primary_text);
            memberIcon = (ImageView) item.findViewById(R.id.profile_pic);
            memberRating = (ImageView) item.findViewById(R.id.rate_img);
        }

    }

}