package com.app.the.bunker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.the.bunker.R;
import com.app.the.bunker.models.MemberModel;
import com.app.the.bunker.utils.ImageUtils;

import java.io.IOException;
import java.util.List;

public class ValidationAdapter extends BaseAdapter {

    private static final String TAG = "ValidationAdapter";

    private static final int TYPE_CREATOR = 0;
    private static final int TYPE_MEMBER = 1;

    private Context context;
    private List<MemberModel> memberList;
    private LayoutInflater inflater;

    public ValidationAdapter(Context context, List<MemberModel> memberList){
        this.context = context;
        this.memberList = memberList;
        inflater = LayoutInflater.from(context);
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

        MemberModel currentMember = getItem(position);

        vViewHolder.memberName.setText(currentMember.getName());
        vViewHolder.memberTitle.setText(currentMember.getTitle());
        try {
            vViewHolder.memberIcon.setImageBitmap(ImageUtils.loadImage(context,ImageUtils.getIconName(currentMember.getIconPath())));
        } catch (IOException e) {
            Log.w(TAG, "Image not found");
            e.printStackTrace();
            vViewHolder.memberIcon.setImageResource(R.drawable.ic_default_avatar);
        }
        vViewHolder.memberIcon.setContentDescription(currentMember.getName());
        vViewHolder.memberChecked = currentMember.isChecked();

        if (currentMember.isChecked()){
            switch (currentMember.getRating()) {
                case -1:
                    vViewHolder.memberRating.setVisibility(View.VISIBLE);
                    vViewHolder.memberRating.setImageResource(R.drawable.ic_dislike);
                    vViewHolder.memberRating.setContentDescription(context.getString(R.string.dislikes));
                    //vViewHolder.memberRating.setColorFilter(R.color.redFilter, PorterDuff.Mode.SRC_IN);
                    break;
                case 0:
                    vViewHolder.memberRating.setVisibility(View.GONE);
                    break;
                case 1:
                    vViewHolder.memberRating.setVisibility(View.VISIBLE);
                    vViewHolder.memberRating.setImageResource(R.drawable.ic_like);
                    vViewHolder.memberRating.setContentDescription(context.getString(R.string.likes));
                   //vViewHolder.memberRating.setColorFilter(R.color.psnColor, PorterDuff.Mode.SRC_IN);
                    break;
            }
        } else {
                vViewHolder.memberRating.setVisibility(View.VISIBLE);
                vViewHolder.memberRating.setImageResource(R.drawable.ic_error);
            }

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

        ValidateViewHolder(View item){
            memberTitle = (TextView) item.findViewById(R.id.secondary_text);
            memberName = (TextView) item.findViewById(R.id.primary_text);
            memberIcon = (ImageView) item.findViewById(R.id.profile_pic);
            memberRating = (ImageView) item.findViewById(R.id.rate_img);
        }

    }

}