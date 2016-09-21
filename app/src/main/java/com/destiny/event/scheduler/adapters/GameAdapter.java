package com.destiny.event.scheduler.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.models.GameModel;
import com.destiny.event.scheduler.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = "GameAdapter";

    private Context context;
    private List<GameModel> gameList;
    private List<GameModel> filteredGameList;
    private LayoutInflater inflater;

    private GameFilter mFilter = new GameFilter();

    public GameAdapter(Context context, List<GameModel> gameList){
        this.context = context;
        this.filteredGameList = gameList;
        this.gameList = gameList;
        inflater = LayoutInflater.from(context);
    }

    public void setGameList(List<GameModel> gameList){
        this.gameList = gameList;
        this.filteredGameList = gameList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredGameList.size();
    }

    @Override
    public GameModel getItem(int position) {
        return filteredGameList.get(position);
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
        setViewIcon(viewHolder.icon,context.getResources().getIdentifier(currentGame.getEventIcon(),"drawable",context.getPackageName()),currentGame.getTypeIcon());
        viewHolder.eventName.setText(currentGame.getEventName());
        viewHolder.typeName.setText(currentGame.getTypeName());
        String creatorName = context.getString(R.string.created_by) + " " + currentGame.getCreatorName();
        viewHolder.creatorName.setText(creatorName);
        String insc = currentGame.getInscriptions() + "/" + currentGame.getMaxGuardians();
        viewHolder.inscriptions.setText(insc);
        viewHolder.time.setText(DateUtils.getTime(currentGame.getTime()));
        viewHolder.date.setText(DateUtils.onBungieDate(currentGame.getTime()));

        return convertView;
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

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class GameViewHolder{

        ImageView icon;
        TextView eventName;
        TextView typeName;
        TextView creatorName;
        TextView inscriptions;
        TextView time;
        TextView date;

        GameViewHolder(View item){
            icon = (ImageView) item.findViewById(R.id.game_image);
            eventName = (TextView) item.findViewById(R.id.primary_text);
            typeName = (TextView) item.findViewById(R.id.type_text);
            creatorName = (TextView) item.findViewById(R.id.secondary_text);
            inscriptions = (TextView) item.findViewById(R.id.game_max);
            time = (TextView) item.findViewById(R.id.game_time);
            date = (TextView) item.findViewById(R.id.game_date);
        }

    }

    private class GameFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String fullFilter = constraint.toString().toLowerCase();
            String prefix = fullFilter.substring(0,fullFilter.indexOf(":"));
            String filter = fullFilter.substring(fullFilter.indexOf(":")+1,fullFilter.length());
            Log.w(TAG, "prefix: " + prefix + " filter: " + filter);

            FilterResults results = new FilterResults();
            final List<GameModel> originalGameList = gameList;
            final ArrayList<GameModel> newGameList = new ArrayList<>(originalGameList.size());

            switch (prefix){
                case "type":
                    if (!filter.equals("all")){
                        int eventId = Integer.parseInt(filter);
                        for (int i=0;i<originalGameList.size();i++){
                            if (originalGameList.get(i).getTypeId() == eventId){
                                newGameList.add(originalGameList.get(i));
                            }
                        }
                        results.values = newGameList;
                        results.count = newGameList.size();
                    } else {
                        results.values = originalGameList;
                        results.count = originalGameList.size();
                    }
                    break;
                case "status":
                    if (!filter.equals("all")){
                        for (int i=0;i<originalGameList.size();i++){
                            Log.w(TAG, "status: " + String.valueOf(originalGameList.get(i).getStatus()));
                            if (String.valueOf(originalGameList.get(i).getStatus()).equals(filter)){
                                newGameList.add(originalGameList.get(i));
                            }
                        }
                        results.values = newGameList;
                        results.count = newGameList.size();
                    } else {
                        results.values = originalGameList;
                        results.count = originalGameList.size();
                    }
                    break;
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredGameList = (List<GameModel>) results.values;
            notifyDataSetChanged();
        }
    }

}
