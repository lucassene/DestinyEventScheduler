package com.destiny.event.scheduler.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.List;

public class GameAdapter extends BaseAdapter {

    private static final String TAG = "GameAdapter";

    private static final int TYPE_NEW = 0;
    private static final int TYPE_SCHEDULED = 1;

    private Context context;
    private List<GameModel> gameList;
    private LayoutInflater inflater;

    public GameAdapter(Context context, List<GameModel> gameList){
        this.context = context;
        this.gameList = gameList;
        inflater = LayoutInflater.from(context);
    }

    public void setGameList(List<GameModel> gameList){
        this.gameList = gameList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return gameList.size();
    }

    @Override
    public GameModel getItem(int position) {
        return gameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameViewHolder viewHolder;
        if (convertView == null){
            convertView = inflater.inflate(R.layout.game_list_item_layout, parent, false);
            viewHolder = new GameViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GameViewHolder) convertView.getTag();
        }

        GameModel currentGame = getItem(position);
        viewHolder.icon.setImageResource(context.getResources().getIdentifier(currentGame.getEventIcon(),"drawable",context.getPackageName()));
        viewHolder.eventName.setText(context.getResources().getIdentifier(currentGame.getEventName(),"string",context.getPackageName()));
        viewHolder.typeName.setText(context.getResources().getIdentifier(currentGame.getTypeName(),"string",context.getPackageName()));
        String creatorName = context.getString(R.string.created_by) + " " + currentGame.getCreatorName();
        viewHolder.creatorName.setText(creatorName);
        String insc = currentGame.getInscriptions() + "/";
        viewHolder.inscriptions.setText(insc);
        viewHolder.maxGuardians.setText(currentGame.getMaxGuardians());
        viewHolder.time.setText(DateUtils.getTime(currentGame.getTime()));
        viewHolder.date.setText(DateUtils.onBungieDate(currentGame.getTime()));

        return convertView;
    }

    private class GameViewHolder{

        ImageView icon;
        TextView eventName;
        TextView typeName;
        TextView creatorName;
        TextView inscriptions;
        TextView maxGuardians;
        TextView time;
        TextView date;

        public GameViewHolder(View item){
            icon = (ImageView) item.findViewById(R.id.game_image);
            eventName = (TextView) item.findViewById(R.id.primary_text);
            typeName = (TextView) item.findViewById(R.id.type_text);
            creatorName = (TextView) item.findViewById(R.id.secondary_text);
            inscriptions = (TextView) item.findViewById(R.id.game_actual);
            maxGuardians = (TextView) item.findViewById(R.id.game_max);
            time = (TextView) item.findViewById(R.id.game_time);
            date = (TextView) item.findViewById(R.id.game_date);
        }

    }

}
