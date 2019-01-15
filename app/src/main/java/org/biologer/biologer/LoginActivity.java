package org.biologer.biologer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.biologer.biologer.model.LoginResponse;
import org.biologer.biologer.model.UserData;
import org.biologer.biologer.model.network.UserDataResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText et_username;
    EditText et_password;
    TextView tv_wrongPass;
    TextView tv_wrongEmail;
    TextView tv_devDatabase;
    // Get the value for KEY.DATABASE_NAME
    String key_databasename = SettingsManager.getDatabaseName();
    Call login;

    /*
     Get the keys for client applications. Separate client key should be given for each Biologer server
     application. This workaround is used to hide secret client_key from the source code. The developers
     should put the key in ~/.gradle/gradle.properties.
      */
    String rsKey = BuildConfig.BiologerRS_Key;
    String hrKey = BuildConfig.BiologerHR_Key;


    //regex za email
    String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
    // Initialise list for Database selection
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        tv_wrongPass = (TextView) findViewById(R.id.tv_wrongPass);
        tv_wrongEmail = (TextView) findViewById(R.id.tv_wrongEmail);
        tv_devDatabase = (TextView) findViewById(R.id.tv_devDatabase);

        // Fill in the data for database list
        spinner = (Spinner) findViewById(R.id.spinner_databases);
        ArrayAdapter<CharSequence> ourDatabases = ArrayAdapter.createFromResource(this, R.array.databases, android.R.layout.simple_spinner_item);
        ourDatabases.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ourDatabases);
        int spinnerPosition = ourDatabases.getPosition(key_databasename);
        spinner.setSelection(spinnerPosition);

        // If item is chosen from the database list.
        spinner = (Spinner) findViewById(R.id.spinner_databases);
        spinner.setOnItemSelectedListener(new getDatabaseURL());
    }

    // Activity starting after user selects a Database from the list
    public class getDatabaseURL implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> getdatabase, View view, int pos,long id) {
            // Change the preference according to the user selection
            SettingsManager.setDatabaseName(getdatabase.getItemAtPosition(pos).toString());

            tv_devDatabase.setText("");
            if (SettingsManager.getDatabaseName().equals("https://dev.biologer.org")) {
                tv_devDatabase.setText(R.string.devDatabase);
            }

            // DIRTY JOOOOBBBB!!!! Must restart the app if database has bean changed
            // In future network services should be initialised after app has bean started
            if (!SettingsManager.getDatabaseName().equals(key_databasename)){
                finish();
                System.exit(0);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }

    }

    public void onLogin(View view) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(et_username.getText().toString());

        // On new login clear the previous error messages.
        tv_wrongPass.setText("");
        tv_wrongEmail.setText("");

        // Display error messages if email/password is mistyped.
        if (!(matcher.matches()))
        {
            tv_wrongEmail.setText(R.string.valid_email);
            return;
        }
        if (!(et_password.getText().length() > 3))
        {
            tv_wrongPass.setText(R.string.valid_pass);
            return;
        }

        if (SettingsManager.getDatabaseName().equals("https://biologer.hr")) {
            login = App.get().getService().login("password", "2", hrKey, "*", et_username.getText().toString(), et_password.getText().toString());
        } else {
            login = App.get().getService().login("password", "2", rsKey, "*", et_username.getText().toString(), et_password.getText().toString());
        }

        login.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response.isSuccessful()) {
                    LoginResponse response1 = response.body();
                    SettingsManager.setToken(response1.getAccessToken());
                    fillUserData();
                }

                else {
                    tv_wrongPass.setText(R.string.wrong_creds);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

            }
        });

    }

    // Get email and name and store it
    private void fillUserData(){
        Call<UserDataResponse> serv = App.get().getService().getUserData();
        serv.enqueue(new Callback<UserDataResponse>() {
            @Override
            public void onResponse(Call<UserDataResponse> serv, Response<UserDataResponse> response) {
                String email = response.body().getData().getEmail();
                String name = response.body().getData().getFullName();
                int data_license = response.body().getData().getSettings().getDataLicense();
                int image_license = response.body().getData().getSettings().getImageLicense();
                UserData uData = new UserData(null, email, name, data_license, image_license);
                App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                Intent intent = new Intent(LoginActivity.this, LandingActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<UserDataResponse> call, Throwable t) {
                String s = "ff";
            }
        });
    }

    public void onRegister(View view) {
        String url_register = SettingsManager.getDatabaseName() + "/register";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url_register));
        startActivity(browserIntent);
    }

    public void onForgotPass(View view) {
        String url_reset_pass = SettingsManager.getDatabaseName() + "/password/reset";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url_reset_pass));
        startActivity(browserIntent);
    }

}
