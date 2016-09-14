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
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.List;

public class DoneGamesAdapter extends BaseAdapter {

    private static final String TAG = "GameAdapter";

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_WAITING = 1;
    private static final int VIEW_TYPE_EVAL = 2;

    private Context context;
    private List<GameModel> gameList;
    private LayoutInflater inflater;
    private int waitingStart;
    private int evalStart;

    public DoneGamesAdapter(Context context, List<GameModel> gameList, int waitingStart, int evalStart){
        this.context = context;
        this.gameList = gameList;
        this.waitingStart = waitingStart;
        this.evalStart = evalStart;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (gameList.get(position).getStatus() == GameModel.STATUS_WAITING){
            if (position == waitingStart){
                return VIEW_TYPE_WAITING;
            } else return VIEW_TYPE_NORMAL;
        } else {
            if (position == evalStart){
                return VIEW_TYPE_EVAL;
            } else return VIEW_TYPE_NORMAL;
        }
    }

    public void setGameList(List<GameModel> gameList){
        this.gameList = gameList;
    }

    public void setStartPositions(int waitingStart, int evalStart){
        this.waitingStart = waitingStart;
        this.evalStart = evalStart;
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
            switch (getItemViewType(position)){
                case VIEW_TYPE_NORMAL:
                    convertView = inflater.inflate(R.layout.game_list_item_layout, parent, false);
                    viewHolder = new GameViewHolder(convertView);
                    convertView.setTag(viewHolder);
                    getNormalView(position, viewHolder);
                    break;
                case VIEW_TYPE_WAITING:
                    convertView = inflater.inflate(R.layout.waiting_game_item_layout, parent, false);
                    viewHolder = new GameViewHolder(convertView);
                    convertView.setTag(viewHolder);
                    getNormalView(position, viewHolder);
                    break;
                case VIEW_TYPE_EVAL:
                    convertView = inflater.inflate(R.layout.validated_game_item_layout, parent, false);
                    viewHolder = new GameViewHolder(convertView);
                    convertView.setTag(viewHolder);
                    getNormalView(position, viewHolder);
                    break;
            }
        } else {
            viewHolder = (GameViewHolder) convertView.getTag();
            getNormalView(position, viewHolder);
        }
        return convertView;
    }

    public void getNormalView(int position, GameViewHolder viewHolder){
        GameModel currentGame = getItem(position);
        setViewIcon(viewHolder.icon,context.getResources().getIdentifier(currentGame.getEventIcon(),"drawable",context.getPackageName()),currentGame.getTypeIcon());
        viewHolder.eventName.setText(currentGame.getEventName());
        viewHolder.typeName.setText(currentGame.getTypeName());
        String creatorName = context.getString(R.string.created_by) + " " + currentGame.getCreatorName();
        viewHolder.creatorName.setText(creatorName);
        String insc = currentGame.getInscriptions() + "/" + currentGame.getMaxGuardians();
        viewHolder.inscriptions.setText(insc);
        viewHolder.time.setText(DateUtils.getTime(currentGame.getTime()));
        viewHolder.date.setText(DateUtils.onBungieDate(currentGame.getTime()));
    }

    private void setViewIcon(ImageView view, int resId, String typeIcon){
        if (resId != 0){
            view.setImageResource(resId);
        } else {
            int typeRes = context.getResources().getIdentifier(typeIcon,"drawable",context.getPackageName());
            if (typeRes != 0){
                Log.w(TAG, "Event icon not found. Using Type icon instead");
                view.setImageResource(typeRes);
            } else{
                Log.w(TAG, "Drawable resource not found.");
                view.setImageResource(R.drawable.ic_missing);
            }
        }
    }

    private class GameViewHolder{

        ImageView icon;
        TextView eventName;
        TextView typeName;
        TextView creatorName;
        TextView inscriptions;
        TextView time;
        TextView date;

        public GameViewHolder(View item){
            icon = (ImageView) item.findViewById(R.id.game_image);
            eventName = (TextView) item.findViewById(R.id.primary_text);
            typeName = (TextView) item.findViewById(R.id.type_text);
            creatorName = (TextView) item.findViewById(R.id.secondary_text);
            inscriptions = (TextView) item.findViewById(R.id.game_max);
            time = (TextView) item.findViewById(R.id.game_time);
            date = (TextView) item.findViewById(R.id.game_date);
        }

    }

}
