package com.destiny.event.scheduler.adapters;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.utils.ImageUtils;

import java.io.IOException;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {

    private static final String TAG = "DrawerAdapter";

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SECTION = 2;
    private static final int TYPE_SELECTED = 3;

    private String items[];
    private TypedArray icons;
    private String sections[];
    private String clanIcon;
    private String clanName;
    private String clanDesc;
    private String clanBanner;

    private Context context;

    private ToActivityListener callback;

    public static class ViewHolder extends RecyclerView.ViewHolder{

        int holderId;
        TextView itemView;
        ImageView iconView;
        TextView sectionView;
        TextView headerView;
        TextView descView;
        ImageView bannerView;
        ImageView logoView;
        FrameLayout itemLayout;

        public ViewHolder(View itemView, int ViewType){
            super(itemView);

            switch (ViewType){
                case TYPE_ITEM:
                    this.itemView = (TextView) itemView.findViewById(R.id.drawer_text);
                    iconView = (ImageView ) itemView.findViewById(R.id.drawer_icon);
                    holderId = 1;
                    break;
                case TYPE_HEADER:
                    bannerView = (ImageView) itemView.findViewById(R.id.clan_banner);
                    headerView = (TextView) itemView.findViewById(R.id.clan_name);
                    descView = (TextView) itemView.findViewById(R.id.clan_desc);
                    logoView = (ImageView) itemView.findViewById(R.id.clan_logo);
                    holderId = 0;
                    break;
                case TYPE_SECTION:
                    sectionView = (TextView) itemView.findViewById(R.id.drawer_section_text);
                    holderId = 2;
                    break;
                case TYPE_SELECTED:
                    itemLayout = (FrameLayout) itemView.findViewById(R.id.item_layout);
                    this.itemView = (TextView) itemView.findViewById(R.id.drawer_text);
                    iconView = (ImageView ) itemView.findViewById(R.id.drawer_icon);
                    holderId = 3;
                    break;
            }

        }
    }

    public DrawerAdapter(Context context, String[] sections, String[] items, String clanIcon, String clanName, String clanDesc, String clanBanner) {
        callback = (ToActivityListener) context;
        this.context = context;
        this.sections = sections;
        this.icons = context.getResources().obtainTypedArray(R.array.menu_icons);
        this.items = items;
        this.clanDesc = clanDesc;
        this.clanIcon = clanIcon;
        this.clanName = clanName;
        this.clanBanner = clanBanner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (viewType){
            case TYPE_ITEM:
                View vItem = layoutInflater.inflate(R.layout.drawer_item_layout, parent, false);
                return new ViewHolder(vItem, viewType);
            case TYPE_HEADER:
                View vHeader = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header_layout, parent, false);
                return new ViewHolder(vHeader, viewType);
            case TYPE_SECTION:
                View vSection = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_section_layout, parent, false);
                return new ViewHolder(vSection, viewType);
            case TYPE_SELECTED:
                View vSelected = layoutInflater.inflate(R.layout.drawer_item_layout, parent, false);
                return new ViewHolder(vSelected, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.holderId){
            case TYPE_ITEM:
                if (position <=5){
                    holder.iconView.setImageResource(icons.getResourceId(position-1,0));
                    holder.itemView.setText(items[position-1]);
                } else if(position <=8){
                    holder.itemView.setText(items[position -2]);
                    holder.iconView.setImageResource(icons.getResourceId(position-2,0));
                } else {
                    holder.itemView.setText(items[position -3]);
                    holder.iconView.setImageResource(icons.getResourceId(position-3,0));
                }
                break;
            case TYPE_HEADER:
                holder.headerView.setText(clanName);
                try {
                    holder.logoView.setImageBitmap(ImageUtils.loadImage(context,clanIcon));
                    holder.bannerView.setImageBitmap(ImageUtils.loadImage(context,clanBanner));
                } catch (IOException e) {
                    Log.w(TAG, "Image Bitmap not found.");
                    e.printStackTrace();
                }
                holder.descView.setText(clanDesc);
                break;
            case TYPE_SECTION:
                if (position == 6) {
                    holder.sectionView.setText(sections[0]);
                } else {
                    holder.sectionView.setText(sections[1]);
                }
                break;
            case TYPE_SELECTED:
                if (position <=5){
                    holder.iconView.setImageResource(icons.getResourceId(position-1,0));
                    holder.itemView.setText(items[position-1]);
                } else if(position <=8){
                    holder.itemView.setText(items[position -2]);
                    holder.iconView.setImageResource(icons.getResourceId(position-2,0));
                } else {
                    holder.itemView.setText(items[position -3]);
                    holder.iconView.setImageResource(icons.getResourceId(position-3,0));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    holder.itemLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_bg));
                } else {
                    holder.itemLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.selected_bg));
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.length+sections.length+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)){
            return TYPE_HEADER;
        } else if (isPositionSection(position)){
            return TYPE_SECTION;
        } else {
            if (callback.getSelectedItem() == position){
                return TYPE_SELECTED;
            } else return TYPE_ITEM;
        }
    }

    private boolean isPositionHeader(int position){
        return position == 0;
    }

    private boolean isPositionSection(int position){
        return position == 6 || position == 9;
    }
}
