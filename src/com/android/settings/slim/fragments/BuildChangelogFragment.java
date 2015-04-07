package com.android.settings.slim.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.slim.util.Changelog;

public class BuildChangelogFragment extends SettingsPreferenceFragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.build_changelog, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Changelog rc = new Changelog() {
            @Override
            public void onResponseReceived(String result) {
                final TextView changelogTextView = (TextView) getView().findViewById(R.id.tv_changelog);
                if (changelogTextView == null) {
                    return;
                }
                changelogTextView.setText(result);
            }
        };
        rc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getActivity());
    }
}
