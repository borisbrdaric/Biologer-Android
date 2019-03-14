package org.biologer.biologer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.NavigationView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.biologer.biologer.bus.DeleteEntryFromList;
import org.biologer.biologer.model.Entry;
import org.biologer.biologer.model.APIEntry;
import org.biologer.biologer.model.RetrofitClient;
import org.biologer.biologer.model.Taxon;
import org.biologer.biologer.model.TaxonLocalization;
import org.biologer.biologer.model.TaxonLocalizationDao;
import org.biologer.biologer.model.UploadFileResponse;
import org.biologer.biologer.model.UserData;
import org.biologer.biologer.model.network.APIEntryResponse;
import org.biologer.biologer.model.network.TaksoniResponse;
import org.biologer.biologer.model.network.UserDataResponse;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Biologer.Landing";

    public static String[] full_taxa_names;

    ArrayList<String> slike = new ArrayList<>();
    int n = 0;
    int m = 0;
    ArrayList<Entry> entryList;
    List<APIEntry.Photo> photos = null;

    private DrawerLayout drawer;

    private FrameLayout progressBar;

    android.support.v4.app.Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        full_taxa_names = getTaxaNames();

        progressBar = findViewById(R.id.progress);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_open_drawer, R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

        View header = navigationView.getHeaderView(0);
        TextView tv_username = header.findViewById(R.id.tv_username);
        TextView tv_email = header.findViewById(R.id.tv_email);

        // Set the text for sidepanel
        tv_username.setText(getUserName());
        tv_email.setText(getUserEmail());

        showLandingFragment();

        if (isNetworkAvailable()) {
            updateTaxa();
            updateLicenses();
        } else {
            Log.d(TAG, "There is no network available. Application will not be able to get new data from the server.");
        }
    }

    // Send a short request to the server that will return if the taxonomic tree is up to date.
    private void updateTaxa() {
        Call<TaksoniResponse> call = RetrofitClient.getService(SettingsManager.getDatabaseName()).getTaxons(1, 1);
        call.enqueue(new Callback<TaksoniResponse>() {
            @Override
            public void onResponse(Call<TaksoniResponse> call, Response<TaksoniResponse> response) {
                // Check if version of taxa from Server and Preferences match. If server version is newer ask for update
                if (Long.toString(response.body().getMeta().getLastUpdatedAt()).equals(SettingsManager.getDatabaseVersion())) {
                    Log.i(TAG,"It looks like this taxonomic database is already up to date. Nothing to do here!");
                } else  {
                    Log.i(TAG,"Taxa database on the server seems to be newer that your version.");
                    if (SettingsManager.getDatabaseVersion().equals("0")) {
                        // If the database was never updated...
                        buildAlertMessageEmptyTaxaDb();
                    } else {
                        // If the online database is more recent...
                        buildAlertMessageNewerTaxaDb();
                    }
                }
            }
            @Override
            public void onFailure(Call<TaksoniResponse> call, Throwable t) {
                // Inform the user on failure and write log message
                //Toast.makeText(LandingActivity.this, getString(R.string.database_connect_error), Toast.LENGTH_LONG).show();
                Log.e("Taxa database: ", "Application could not get taxon version data from a server!");
            }
        });
    }

    private void updateLicenses() {
        // Check if the licence has shanged on the server and update if needed
        final Intent update_licenses = new Intent(LandingActivity.this, UpdateLicenses.class);
        startService(update_licenses);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        switch (id) {
            case R.id.nav_about:
                fragment = new AboutFragment();
                break;
            case R.id.nav_list:
                fragment = new LandingFragment();
                break;
            case R.id.nav_logout:
                fragment = new LogoutFragment();
                break;
            case R.id.nav_setup:
                fragment = new PreferencesFragment();
                break;
            case R.id.nav_help:
                startActivity(new Intent(LandingActivity.this, IntroActivity.class));
                finish();
                drawer.closeDrawer(GravityCompat.START);
                return true;
                /*
                startActivity(new Intent(LandingActivity.this, SetupActivity.class));
                drawer.closeDrawer(GravityCompat.START);
                return true;
                */
            default:
                fragment = new LandingFragment();
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, fragment);
            ft.addToBackStack("new fragment");
            ft.commit();
        } else {
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
    }

    @Override
    public void onResume() {
        //navDrawerFill();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

        View header = navigationView.getHeaderView(0);
        TextView tv_username = header.findViewById(R.id.tv_username);
        TextView tv_email = header.findViewById(R.id.tv_email);
        tv_username.setText(getUserName());
        tv_email.setText(getUserEmail());
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
        apiEntry.setTaxonId(entry.getTaxonId() != null ? entry.getTaxonId().intValue() : null);
        apiEntry.setTaxonSuggestion(entry.getTaxonSuggestion());
        apiEntry.setYear(entry.getYear());
        apiEntry.setMonth(entry.getMonth());
        apiEntry.setDay(entry.getDay());
        apiEntry.setLatitude(entry.getLattitude());
        apiEntry.setLongitude(entry.getLongitude());
        if (entry.getAccuracy() == 0.0) {
            apiEntry.setAccuracy(null);
        } else {
            apiEntry.setAccuracy((int) entry.getAccuracy());
        }
        apiEntry.setLocation(entry.getLocation());
        apiEntry.setElevation((int) entry.getElevation());
        apiEntry.setNote(entry.getComment());
        apiEntry.setSex(entry.getSex());
        apiEntry.setNumber(entry.getNumber());
        apiEntry.setProject(entry.getProjectId());
        apiEntry.setFoundOn(entry.getFoundOn());
        apiEntry.setStageId(entry.getStage());
        apiEntry.setFoundDead(entry.getDeadOrAlive().equals("true") ? 0 : 1);
        apiEntry.setFoundDeadNote(entry.getCauseOfDeath());
        apiEntry.setDataLicense(entry.getData_licence());
        apiEntry.setTime(entry.getTime());
        if (entry.getSlika1() != null || entry.getSlika2() != null || entry.getSlika3() != null) {
            int[] has_image = {1 ,2};
            apiEntry.setTypes(has_image);
        } else {
            int[] has_image = {1};
            apiEntry.setTypes(has_image);
        }
        for (int i = 0; i < n; i++) {
            APIEntry.Photo p = new APIEntry.Photo();
            p.setPath(slike.get(i));
            p.setLicense(entry.getImage_licence());
            photos.add(p);
        }
        apiEntry.setPhotos(photos);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String s = mapper.writeValueAsString(apiEntry);
            Log.i(TAG, "Upload Entry " + s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Call<APIEntryResponse> call = RetrofitClient.getService(SettingsManager.getDatabaseName()).uploadEntry(apiEntry);
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

        Call<UploadFileResponse> call = RetrofitClient.getService(SettingsManager.getDatabaseName()).uploadFile(body);

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
        Call<UserDataResponse> serv = RetrofitClient.getService(SettingsManager.getDatabaseName()).getUserData();
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

    protected void buildAlertMessageNewerTaxaDb() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // intent used to start service for fetching taxa
        final Intent fetchTaxa = new Intent(LandingActivity.this, FetchTaxa.class);
        fetchTaxa.setAction(FetchTaxa.ACTION_START);
        builder.setMessage(getString(R.string.new_database_available))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startService(fetchTaxa);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        // If user don’t update just ignore updates until next session
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    protected void buildAlertMessageEmptyTaxaDb() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // intent used to start service for fetching taxa
        final Intent fetchTaxa = new Intent(this, FetchTaxa.class);
        fetchTaxa.setAction(FetchTaxa.ACTION_START);
        builder.setMessage(getString(R.string.database_empty))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.contin), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startService(fetchTaxa);
                    }
                })
                .setNegativeButton(getString(R.string.skip), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        // If user don’t update just ignore updates until next session
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void cancelTaxaUpdate() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.should_cancel_taxa_update))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Intent fetchTaxa = new Intent(LandingActivity.this, FetchTaxa.class);
                        fetchTaxa.setAction(FetchTaxa.ACTION_CANCEL);
                        startService(fetchTaxa);
                    }
                })
                .setNegativeButton(getString(R.string.continue_update), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    // Get the data from GreenDao database
    private UserData getLoggedUser() {
        // Get the user data from a GreenDao database
        List<UserData> userdata_list = App.get().getDaoSession().getUserDataDao().loadAll();
        // If there is no user data we should logout the user
        if (userdata_list == null || userdata_list.isEmpty()) {
            // Delete user data
            clearUserData(this);
            // Go to login screen
            userLogOut();
            return null;
        } else {
            return userdata_list.get(0);
        }
    }

    public Long getUserID() {
        UserData userdata = getLoggedUser();
        if (userdata != null) {
            return userdata.getId();
        } else {
            return null;
        }
    }

    private int getUserDataLicense() {
        UserData userdata = getLoggedUser();
        if (userdata != null) {
            return userdata.getData_license();
        } else {
            return 0;
        }
    }

    private int getUserImageLicense() {
        UserData userdata = getLoggedUser();
        if (userdata != null) {
        return userdata.getImage_license();
    } else {
        return 0;
    }
    }

    private String getUserName() {
        UserData userdata = getLoggedUser();
        if (userdata != null) {
            return userdata.getUsername();
        } else {
            return "User is not logged in";
        }
    }

    private String getUserEmail() {
        UserData userdata = getLoggedUser();
        if (userdata != null) {
            return userdata.getEmail();
        } else {
            return "Couldn’t get email address.";
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivitymanager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivitymanager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void userLogOut() {
        Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public static void clearUserData(Context context) {
        // Delete user token
        SettingsManager.deleteToken();
        // Set the default preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().clear().apply();
        SettingsManager.setDatabaseVersion("0");
        SettingsManager.setProjectName(null);
        SettingsManager.setTaxaLastPageUpdated("1");
        // Maybe also to delete database...
        App.get().getDaoSession().getTaxonDao().deleteAll();
        App.get().getDaoSession().getStageDao().deleteAll();
        App.get().getDaoSession().getUserDataDao().deleteAll();
        App.get().getDaoSession().getTaxonLocalizationDao().deleteAll();
    }

    private void showLandingFragment() {
        fragment = new LandingFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content_frame, fragment);
        ft.addToBackStack("landing fragment");
        ft.commit();
    }

    public static String[] getTaxaNames() {
        // Get the system locale to translate names of the taxa
        Locale locale = getCurrentLocale();
        List<TaxonLocalization> taxaList = App.get().getDaoSession().getTaxonLocalizationDao()
                .queryBuilder()
                .where(TaxonLocalizationDao.Properties.Locale.eq(locale.getLanguage()))
                .list();

        // This should get the list of taxa from the database
        String[] full_names = new String[taxaList.size()];
        for (int i = 0; i < taxaList.size(); i++) {
            // Get the latin names
            full_names[i] = taxaList.get(i).getLatinAndNativeName();
        }

        return full_names;
    }

    private static Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            Locale locale = App.get().getResources().getConfiguration().getLocales().get(0);
            Log.i(TAG, "Current System locale is set to " + locale.getDisplayLanguage() + ".");
            return locale;
        } else{
            Locale locale = App.get().getResources().getConfiguration().locale;
            Log.i(TAG, "Current System locale is set to " + locale.getDisplayLanguage() + ".");
            return locale;
        }
    }
}