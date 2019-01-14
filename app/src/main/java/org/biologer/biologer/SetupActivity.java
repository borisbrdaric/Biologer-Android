package org.biologer.biologer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.biologer.biologer.model.UserData;
import org.biologer.biologer.model.network.UserDataResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetupActivity extends AppCompatActivity {

    ProgressBar progressBarTaxa;
    private int oldProgress = 0;

    // Get the user data from Dao database
    List<UserData> list = App.get().getDaoSession().getUserDataDao().loadAll();
    UserData userdata = list.get(0);
    final String email = userdata.getEmail();
    final String name = userdata.getUsername();
    final Long uid = userdata.getId();
    final int data_license = userdata.getData_license();
    final int image_license = userdata.getImage_license();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Ovo dodaje dugme za nazad u traku sa alatima
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressBarTaxa = findViewById(R.id.progress_bar_taxa);
        Button btn = findViewById(R.id.btn);

        // Fill in the data for Data and Image Licenses
        List<String> dataLicenses =  new ArrayList<String>();
        dataLicenses.add(getString(R.string.license_default));
        dataLicenses.add(getString(R.string.license10));
        dataLicenses.add(getString(R.string.license20));
        dataLicenses.add(getString(R.string.license30));
        dataLicenses.add(getString(R.string.license40));
        ArrayAdapter<String> data_licenses = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, dataLicenses);
        data_licenses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Create drop-down list for Data licenses
        AppCompatSpinner spinner_data_license = (AppCompatSpinner) findViewById(R.id.spinner_data_license);
        spinner_data_license.setAdapter(data_licenses);
        spinner_data_license.setSelection(Integer.valueOf(SettingsManager.getCustomDataLicense()));
        spinner_data_license.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    SettingsManager.setCustomDataLicense("0");
                    updateDataLicenseFromServer();
                } if (position == 1) {
                    SettingsManager.setCustomDataLicense("1");
                    UserData uData = new UserData(uid, email, name, 10, image_license);
                    App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                } if (position == 2) {
                    SettingsManager.setCustomDataLicense("2");
                    UserData uData = new UserData(uid, email, name, 20, image_license);
                    App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                } if (position == 3) {
                    SettingsManager.setCustomDataLicense("3");
                    UserData uData = new UserData(uid, email, name, 30, image_license);
                    App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                } if (position == 4) {
                    SettingsManager.setCustomDataLicense("4");
                    UserData uData = new UserData(uid, email, name, 40, image_license);
                    App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Create drop-down list for Image licenses
        AppCompatSpinner spinner_image_license = (AppCompatSpinner) findViewById(R.id.spinner_image_license);
        spinner_image_license.setAdapter(data_licenses);
        spinner_image_license.setSelection(Integer.valueOf(SettingsManager.getCustomImageLicense()));
        spinner_image_license.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    SettingsManager.setCustomImageLicense("0");
                    updateImageLicenseFromServer();
                } if (position == 1) {
                    SettingsManager.setCustomImageLicense("1");
                    UserData uData = new UserData(uid, email, name, data_license, 10);
                    App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                } if (position == 2) {
                    SettingsManager.setCustomImageLicense("2");
                    UserData uData = new UserData(uid, email, name, data_license, 20);
                    App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                } if (position == 3) {
                    SettingsManager.setCustomImageLicense("3");
                    UserData uData = new UserData(uid, email, name, data_license, 30);
                    App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                } if (position == 4) {
                    SettingsManager.setCustomImageLicense("4");
                    UserData uData = new UserData(uid, email, name, data_license, 40);
                    App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBarTaxa.setVisibility(View.VISIBLE);

                Thread updateStatusBar = new Thread() {

                    @Override
                    public void run() {
                        try {
                            sleep(1000);
                            while (progressBarTaxa.getProgress() < 100) {
                                int progress_value = FetchTaxa.getProgressStatus();
                                if (progress_value != oldProgress) {
                                    oldProgress = progress_value;
                                    progressBarTaxa.setProgress(progress_value);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBarTaxa.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                };

                updateStatusBar.start();
                FetchTaxa.fetchAll(1);

            }
        });
    }

    private void updateDataLicenseFromServer() {
        Call<UserDataResponse> call = App.get().getService().getUserData();
        call.enqueue(new Callback<UserDataResponse>() {
            @Override
            public void onResponse(Call<UserDataResponse> call, Response<UserDataResponse> response) {
                int server_data_license = response.body().getData().getSettings().getDataLicense();
                UserData uData = new UserData(uid, email, name, server_data_license, image_license);
                App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
                int image_license = response.body().getData().getSettings().getImageLicense();
            }

            @Override
            public void onFailure(Call<UserDataResponse> call, Throwable t) {
                Log.e("Taxa database: ", "Application could not get user data from a server!");
            }
        });
    }

    private void updateImageLicenseFromServer() {
        Call<UserDataResponse> call = App.get().getService().getUserData();
        call.enqueue(new Callback<UserDataResponse>() {
            @Override
            public void onResponse(Call<UserDataResponse> call, Response<UserDataResponse> response) {
                int server_image_license = response.body().getData().getSettings().getImageLicense();
                UserData uData = new UserData(uid, email, name, data_license, server_image_license);
                App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
            }
            @Override
            public void onFailure(Call<UserDataResponse> call, Throwable t) {
                Log.e("Taxa database: ", "Application could not get user data from a server!");
            }
        });
    }

}
