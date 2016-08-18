package com.destiny.event.scheduler.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.adapters.MedalsAdapter;
import com.destiny.event.scheduler.models.MedalModel;

import java.util.ArrayList;
import java.util.Random;

public class MyMedalsFragment extends ListFragment {

    MedalsAdapter medalsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.medals_list_layout, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<MedalModel> medalList = createMedals();
        medalsAdapter = new MedalsAdapter(getActivity(),medalList);
        setListAdapter(medalsAdapter);
    }

    private ArrayList<MedalModel> createMedals() {
        ArrayList<MedalModel> list = new ArrayList<>();
        String[] nameList = getActivity().getResources().getStringArray(R.array.medal_names);
        String[] descList = getActivity().getResources().getStringArray(R.array.medal_desc);
        for (int i=0;i<6;i++){
            MedalModel model = new MedalModel();
            model.setName(nameList[i]);
            model.setDesc(descList[i]);
            model.setIcon(i);
            Random r = new Random();
            model.setValue(r.nextInt(600));
            list.add(model);
        }
        return list;
    }
}
