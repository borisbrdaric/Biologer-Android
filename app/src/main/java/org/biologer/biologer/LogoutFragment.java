package org.biologer.biologer;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.internal.InternalTokenResult;

import org.biologer.biologer.model.RetrofitClient;
import org.biologer.biologer.model.UserData;

import java.io.File;
import java.util.List;

import okhttp3.Cache;
import okhttp3.CacheControl;

public class LogoutFragment extends Fragment {

    private AppCompatButton btn_logout;
    private TextView tv_username;
    private TextView tv_email;
    private TextView tv_database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_logout, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();

        if (view != null) {
            btn_logout = getActivity().findViewById(R.id.btn_logout);
            tv_database = getActivity().findViewById(R.id.currentDatabase);
            tv_username = getActivity().findViewById(R.id.tv_currentlyLogged_username);
            tv_email = getActivity().findViewById(R.id.tv_currentlyLogged_email);

            List<UserData> list = App.get().getDaoSession().getUserDataDao().loadAll();
            UserData ud = list.get(0);
            tv_database.setText(SettingsManager.getDatabaseName());
            tv_username.setText(ud.getUsername());
            tv_email.setText(ud.getEmail());

            btn_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SettingsManager.deleteToken();
                    // Maybe also to delete database!
                    App.get().getDaoSession().getTaxonDao().deleteAll();
                    App.get().getDaoSession().getStageDao().deleteAll();
                    App.get().getDaoSession().getUserDataDao().deleteAll();
                    SettingsManager.setDatabaseVersion("0");
                    SettingsManager.setCustomDataLicense("0");
                    SettingsManager.setCustomImageLicense("0");
                    // Kill the app on logout, since new login request does not work on normal logout... :/
                    System.exit(0);
/*
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
*/
                }
            });
        }
    }
}
