package org.biologer.biologer;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.design.widget.NavigationView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.biologer.biologer.bus.DeleteEntryFromList;
import org.biologer.biologer.model.Entry;
import org.biologer.biologer.model.APIEntry;
import org.biologer.biologer.model.Stage;
import org.biologer.biologer.model.UploadFileResponse;
import org.biologer.biologer.model.UserData;
import org.biologer.biologer.model.network.APIEntryResponse;
import org.biologer.biologer.model.network.Stage6;
import org.biologer.biologer.model.network.TaksoniResponse;
import org.biologer.biologer.model.network.Taxa;
import org.biologer.biologer.model.network.UserDataResponse;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<String> slike = new ArrayList<>();
    int n = 0;
    int m = 0;
    ArrayList<Entry> entryList;
    List<APIEntry.Photo> photos = null;
    private UserData loggedUser = new UserData();

    private DrawerLayout drawer;

    private FrameLayout progressBar;

    private FrameLayout progressBar4Taxa;
    private String lastUpdatedAt;
    private ProgressBar progressBarTaxa;
    private int oldProgress = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //App.get().getDaoSession().getUserDataDao().deleteAll();
        //navDrawerFill();

        progressBar = findViewById(R.id.progress);
        progressBar4Taxa = findViewById(R.id.progress_taxa);
        progressBarTaxa = findViewById(R.id.progress_bar_taxa1);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

        View header = navigationView.getHeaderView(0);
        TextView tv_username = header.findViewById(R.id.tv_username);
        TextView tv_email = header.findViewById(R.id.tv_email);
        List<UserData> list = App.get().getDaoSession().getUserDataDao().loadAll();

        if (list != null && list.size() > 0) {
            UserData loggedUser = list.get(0);

            tv_username.setText(loggedUser.getUsername());
            tv_email.setText(loggedUser.getEmail());
        }

        android.support.v4.app.Fragment fragment = new LandingFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content_frame, fragment);
        ft.addToBackStack("fragment");
        ft.commit();

        updateTaxa();
    }

    private void updateTaxa() {

        Call<TaksoniResponse> call = App.get().getService().getTaxons(1, 1);
        call.enqueue(new Callback<TaksoniResponse>() {
            @Override
            public void onResponse(Call<TaksoniResponse> call, Response<TaksoniResponse> response) {
                // Get the version of the taxa database from server
                lastUpdatedAt = Long.toString(response.body().getMeta().getLastUpdatedAt());
                String previousVersion = SettingsManager.getDatabaseVersion();
                if (lastUpdatedAt.equals(previousVersion)) {
                    Log.i("TAXA DATABASE ","It looks like this taxonomic database is already up to date. Nothing to do here!");
                } else {
                    Log.i("TAXA DATABASE ","Taxa database on the server and android app didn’t match!");
                    buildAlertMessageNewTaxa();
                }
            }
            @Override
            public void onFailure(Call<TaksoniResponse> call, Throwable t) {
                // Inform the user on failure and write log message
                //Toast.makeText(getActivity(), getString(R.string.database_connect_error), Toast.LENGTH_LONG).show();
                Log.e("Fetching taxa > ", "Application could not get data from a server!");
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        android.support.v4.app.Fragment fragment = null;
        Intent intent = null;

        switch (id) {
            case R.id.nav_setup:
                fragment = new SetupFragment();
                break;
            case R.id.nav_about:
                fragment = new AboutFragment();
                break;
            case R.id.nav_list:
                fragment = new LandingFragment();
                break;
            case R.id.nav_logout:
                fragment = new LogoutFragment();
                break;
            case R.id.nav_help:
                Intent intent1 = new Intent(LandingActivity.this, IntroActivity.class);
                startActivity(intent1);
            default:
                fragment = new LandingFragment();
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            ft.add(R.id.content_frame, fragment);
            ft.addToBackStack("fragment");
            ft.commit();
        } else {
            startActivity(intent);
            //super.onNavigationItemSelected();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
//        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
//            finish();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.confirmExit))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                //finish();
                                finishAffinity();
                                System.exit(0);
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            } else {
                super.onBackPressed();
            }
        }
        //super.onBackPressed();

    }

    @Override
    public void onResume() {
        //navDrawerFill();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

        View header = navigationView.getHeaderView(0);
        TextView tv_username = header.findViewById(R.id.tv_username);
        TextView tv_email = header.findViewById(R.id.tv_email);
        List<UserData> list = App.get().getDaoSession().getUserDataDao().loadAll();
        if (list != null && list.size() != 0) {
            loggedUser = list.get(0);
            tv_username.setText(loggedUser.getUsername());
            tv_email.setText(loggedUser.getEmail());
        } else {
            Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        super.onResume();
    }

    //desni meni  -- upload entries
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.upload_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveEntries();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveEntries() {
        progressBar.setVisibility(View.VISIBLE);
        entryList = (ArrayList<Entry>) App.get().getDaoSession().getEntryDao().loadAll();
        uploadEntry_step1();
    }


    private void uploadEntry_step1() {
        n = 0;
        ArrayList<String> nizSlika = new ArrayList<>();
        slike.clear();

        if (entryList.size() == 0) {
            progressBar.setVisibility(View.GONE);
            App.get().getDaoSession().getEntryDao().deleteAll();
            return;
        }
        Entry entry = entryList.get(0);

        if (entry.getSlika1() != null) {
            n++;
            nizSlika.add(entry.getSlika1());
        }
        if (entry.getSlika2() != null) {
            n++;
            nizSlika.add(entry.getSlika2());
        }
        if (entry.getSlika3() != null) {
            n++;
            nizSlika.add(entry.getSlika3());
        }

        if (n == 0) {
            uploadEntry_step2();
        } else {
            for (int i = 0; i < n; i++) {
                File file = new File(nizSlika.get(i));
                uploadFile(file, i);
            }
        }

    }

    private void uploadEntry_step2() {
        APIEntry apiEntry = new APIEntry();
        photos = new ArrayList<>();
        //napravi objekat apiEntry
        Entry entry = entryList.get(0);
        apiEntry.setTaxonId((int) entry.getTaxon());
        apiEntry.setTaxonSuggestion(entry.getTaxon_suggestion());
        apiEntry.setYear(entry.getYear());
        apiEntry.setMonth(entry.getMonth());
        apiEntry.setDay(entry.getDay());
        apiEntry.setLatitude(entry.getLattitude());
        apiEntry.setLongitude(entry.getLongitude());
        apiEntry.setAccuracy((int) entry.getAccuracy());
        apiEntry.setLocation(entry.getLocationDesc());
        apiEntry.setElevation((int) entry.getElevation());
        apiEntry.setNote(entry.getComment());
        apiEntry.setSex(entry.getSex());
        apiEntry.setNumber(entry.getNumber() == 0 ? 1 : entry.getNumber());
        apiEntry.setProject(entry.getProjectId());
        apiEntry.setFoundOn(entry.getFoundOn());
        apiEntry.setStageId(entry.getStage());
        apiEntry.setFoundDead(entry.getDeadOrAlive().equals("true") ? 0 : 1);
        apiEntry.setFoundDeadNote(entry.getCauseOfDeath());
        apiEntry.setDataLicense(entry.getData_licence());
        apiEntry.setImageLicense(entry.getImage_licence());
        apiEntry.setTime(entry.getTime());
        for (int i = 0; i < n; i++) {
            APIEntry.Photo p = new APIEntry.Photo();
            p.setPath(slike.get(i));
            photos.add(p);
        }
        apiEntry.setPhotos(photos);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String s = mapper.writeValueAsString(apiEntry);
            Log.i("API_ENTRY", s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Call<APIEntryResponse> call = App.get().getService().uploadEntry(apiEntry);
        call.enqueue(new Callback<APIEntryResponse>() {
            @Override
            public void onResponse(Call<APIEntryResponse> call, Response<APIEntryResponse> response) {
                App.get().getDaoSession().getEntryDao().delete(entryList.get(0));
                if (response.isSuccessful()) {
                    entryList.remove(0);
                    EventBus.getDefault().post(new DeleteEntryFromList());
                    m = 0;
                }
                uploadEntry_step1();
            }

            @Override
            public void onFailure(Call<APIEntryResponse> call, Throwable t) {
                Log.i("GRESKA", t.getLocalizedMessage());
            }
        });
    }

    private void uploadFile(File file, final int i) {
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);

        Call<UploadFileResponse> call = App.get().getService().uploadFile(body);

        call.enqueue(new Callback<UploadFileResponse>() {
            @Override
            public void onResponse(Call<UploadFileResponse> call, Response<UploadFileResponse> response) {

                slike.add(response.body().getFile());
                m++;
                if (m == n) {
                    uploadEntry_step2();
                }
                Log.d("file", response.body().getFile());
            }

            @Override
            public void onFailure(Call<UploadFileResponse> call, Throwable t) {
                Log.d("file", t.getLocalizedMessage());
            }
        });
    }


    private void navDrawerFill() {
        Call<UserDataResponse> serv = App.get().getService().getUserData();
        serv.enqueue(new Callback<UserDataResponse>() {
            @Override
            public void onResponse(Call<UserDataResponse> serv, Response<UserDataResponse> response) {
                App.get().getDaoSession().getUserDataDao().deleteAll();
                String email = response.body().getData().getEmail();
                String name = response.body().getData().getFullName();
                int data_license = response.body().getData().getSettings().getDataLicense();
                int image_license = response.body().getData().getSettings().getImageLicense();
                UserData uData = new UserData(null, email, name, data_license, image_license);
                App.get().getDaoSession().getUserDataDao().insertOrReplace(uData);
            }

            @Override
            public void onFailure(Call<UserDataResponse> call, Throwable t) {
                String s = "ff";
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            List<Fragment> fl = getSupportFragmentManager().getFragments();
            for (int i = 0; i < fl.size(); i++) {
                if (fl.get(i) instanceof LandingFragment) {
                    ((LandingFragment) fl.get(i)).updateData();
                }
            }
        }
    }

    protected void buildAlertMessageNewTaxa() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.new_database_available))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        progressBar4Taxa.setVisibility(View.VISIBLE);

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
                                            progressBar4Taxa.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }
                        };

                        updateStatusBar.start();

                        FetchTaxa.fetchAll(1);

                        SettingsManager.setDatabaseVersion(lastUpdatedAt);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
