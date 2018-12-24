package org.biologer.biologer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.auth0.android.jwt.JWT;

import org.biologer.biologer.model.UserData;
import org.w3c.dom.Text;

import java.util.List;

public class LogoutFragment extends Fragment {

    private CustomButton btn_logout;
    private TextView tv_username;
    private TextView tv_email;
    private TextView tv_database;
    //private TextView tv_user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_logout, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        //tv_user = getActivity().findViewById(R.id.tv_user);
        //String token = SettingsManager.getToken();
        //JWT jwt = new JWT(token);
        //long userId = Long.valueOf(jwt.getSubject());
        //SettingsManager.setActiveAccountId(userId);
        //tv_user.setText(String.valueOf(userId));

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
                    SettingsManager.setDatabaseVersion("0");
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    //getContext().finish();
                }
            });
        }
    }
}
