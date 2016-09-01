package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.MedalsAdapter;
import com.destiny.event.scheduler.models.MedalModel;
import com.destiny.event.scheduler.models.MemberModel;

import java.util.ArrayList;

public class MyMedalsFragment extends ListFragment {

    private static final String TAG = "MyMedalsFragment";

    MedalsAdapter medalsAdapter;
    MemberModel member;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.medals_list_layout, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        member = (MemberModel) bundle.getSerializable("member");
        if (member != null){
            setMedals();
        } else Log.w(TAG, "member is null (onViewCreated)");
    }

    private void setMedals(){
        ArrayList<MedalModel> medalList = createMedals();
        if (medalsAdapter != null){
            medalsAdapter.setMedalList(medalList);
        } else {
            medalsAdapter = new MedalsAdapter(getActivity(), medalList);
            setListAdapter(medalsAdapter);
        }
    }

    private ArrayList<MedalModel> createMedals() {
        ArrayList<MedalModel> list = new ArrayList<>();
        String[] nameList = getActivity().getResources().getStringArray(R.array.medal_names);
        String[] descList = getActivity().getResources().getStringArray(R.array.medal_desc);

        for (int i=0;i<8;i++){
            MedalModel model = new MedalModel();
            model.setName(nameList[i]);
            model.setDesc(descList[i]);
            model.setIcon(i);
            switch (i){
                case 0:
                    model.setValue(member.getGamesCreated());
                    break;
                case 1:
                    int played = member.getGamesCreated();
                    played = played + member.getGamesPlayed();
                    model.setValue(played);
                    break;
                case 2:
                    model.setValue(member.getEvaluationsMade());
                    break;
                case 3:
                    model.setValue(member.getLikes());
                    break;
                case 4:
                    model.setValue(member.getDislikes());
                    break;
                case 5:
                    model.setValue(countEventType(2));
                    break;
                case 6:
                    int value = countEventType(7);
                    value = value + countEventType(8);
                    model.setValue(value);
                    break;
                case 7:
                    model.setValue(countEventType(5));
                    break;
            }
            list.add(model);
        }
        return list;
    }

    private int countEventType(int type) {
        int count = 0;
        for (int i=0;i<member.getTypesPlayed().size();i++){
            if (member.getTypesPlayed().get(i).getTypeId() == type){
                count = member.getTypesPlayed().get(i).getTimesPlayed();
            }
        }
        return count;
    }
}
