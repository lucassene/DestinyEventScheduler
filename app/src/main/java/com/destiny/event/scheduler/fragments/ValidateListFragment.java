package com.destiny.event.scheduler.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.destiny.event.scheduler.R;
import com.destiny.event.scheduler.interfaces.RefreshDataListener;
import com.destiny.event.scheduler.interfaces.ToActivityListener;
import com.destiny.event.scheduler.interfaces.UserDataListener;

public class ValidateListFragment extends ListFragment implements RefreshDataListener, UserDataListener{

    private static final String TAG = "ValidateListFragment";

    private ToActivityListener callback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callback.deleteRefreshListener(this);
        callback.deleteUserDataListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (ToActivityListener) getActivity();
        callback.registerRefreshListener(this);
        callback.registerUserDataListener(this);
        Log.w(TAG, "ValidateListFragment attached!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.validate_list_layout, container, false);
    }

    @Override
    public void onRefreshData() {

    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onUserDataLoaded() {

    }
}
