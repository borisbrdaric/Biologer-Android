package org.biologer.biologer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class AboutFragment extends android.support.v4.app.Fragment {

    TextView tv_database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            tv_database = getActivity().findViewById(R.id.currentDatabase);
            tv_database.setText(SettingsManager.getDatabaseName());
        }
    }


}
