package com.app.the.bunker.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.the.bunker.BuildConfig;
import com.app.the.bunker.R;
import com.app.the.bunker.activities.DrawerActivity;
import com.app.the.bunker.interfaces.ToActivityListener;

public class AboutSettingsFragment extends Fragment {

    private ToActivityListener callback;

    LinearLayout emailLayout;
    LinearLayout helpLayout;
    LinearLayout patreonLayout;
    LinearLayout inviteLayout;
    LinearLayout policyLayout;
    TextView versionTxt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about_settings_layout, container, false);
        emailLayout = (LinearLayout) v.findViewById(R.id.email_layout);
        helpLayout = (LinearLayout) v.findViewById(R.id.help_layout);
        patreonLayout = (LinearLayout) v.findViewById(R.id.patreon_layout);
        inviteLayout = (LinearLayout) v.findViewById(R.id.share_layout);
        policyLayout = (LinearLayout) v.findViewById(R.id.policy_layout);
        versionTxt = (TextView) v.findViewById(R.id.versionTxt);
        versionTxt.setText(BuildConfig.VERSION_NAME);

        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.callAndroidIntent(DrawerActivity.TYPE_EMAIL_INTENT, getString(R.string.email));
            }
        });

        helpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.callAndroidIntent(DrawerActivity.TYPE_BROWSER_INTENT, getString(R.string.website));
            }
        });

        patreonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.callAndroidIntent(DrawerActivity.TYPE_BROWSER_INTENT, getString(R.string.patreon_website));
            }
        });

        inviteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.callAndroidIntent(DrawerActivity.TYPE_SHARE_INTENT, "");
            }
        });

        policyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.callAndroidIntent(DrawerActivity.TYPE_BROWSER_INTENT, "https://thebunkerapp.wordpress.com/privacy-policy/");
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        callback = (ToActivityListener) getActivity();
        callback.setFragmentType(DrawerActivity.FRAGMENT_TYPE_WITHOUT_BACKSTACK);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        callback.setToolbarTitle(getString(R.string.about));
        getActivity().getMenuInflater().inflate(R.menu.empty_menu, menu);
    }

}
