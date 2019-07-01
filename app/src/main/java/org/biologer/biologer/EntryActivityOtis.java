package org.biologer.biologer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;

import org.biologer.biologer.model.Entry;
import org.biologer.biologer.model.Stage;
import org.biologer.biologer.model.StageDao;
import org.biologer.biologer.model.Taxon;
import org.biologer.biologer.model.TaxonDao;
import org.biologer.biologer.model.TaxonLocalization;
import org.biologer.biologer.model.TaxonLocalizationDao;
import org.biologer.biologer.model.UserData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntryActivityOtis extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "Biologer.EntryOtis";

    /**
     * Regular expression used to parse CSV string found in comment field.
     * Consists of three alternations: the first matches a quoted field, the second unquoted and the
     * third a null field.
     */
    private static final String CSV_PATTERN = "(?:^|,)(\\\"(?:[^\\\"]+|\\\"\\\")*\\\"|[^,]*)";

    /**
     * {@link Pattern} instance used to parse CSV string.
     */
    private static final Pattern csvPattern = Pattern.compile(CSV_PATTERN);
    /**
     * A character used to separate fields in CSV string.
     */
    private static final String CSV_DELIMITER = ",";
    /**
     * A character used to "quote" fields in CSV string (used when field contains separator character).
     */
    private static final String CSV_ENCLOSING = "\"";

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 1005;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1006;
    private static final int REQUEST_LOCATION = 1;

    private String mCurrentPhotoPath;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LocationManager observerLocationManager;
    private LocationListener observerLocationListener;
//    String latitude = "0", longitude = "0";
    private double elev = 0.0;
    private LatLng nLokacija = new LatLng(0.0, 0.0);
    private Double acc = 0.0;
    private LatLng observerLocation = new LatLng(0.0, 0.0);
    private int GALLERY = 1, CAMERA = 2, MAP = 3, MAP_OBSERVER = 4;
    private TextView tvTakson, tv_gps, tvStage, tv_latitude, tv_longitude, select_sex;
    private TextView tv_observer_latitude, tv_observer_longitude;
    private EditText et_razlogSmrti, et_komentar, et_brojJedinki, et_habitat, et_observation_length;
    AutoCompleteTextView acTextView;
    FrameLayout ib_pic1_frame, ib_pic2_frame, ib_pic3_frame;
    ImageView ib_pic1, ib_pic1_del, ib_pic2, ib_pic2_del, ib_pic3, ib_pic3_del, iv_map, iv_observer_map, iconTakePhotoCamera, iconTakePhotoGallery;
    private CheckBox check_dead;
    LinearLayout detailedEntry;
    private boolean save_enabled = true;
    Uri contentURI;
    private String slika1, slika2, slika3;
    private SwipeRefreshLayout swipe;
    private Entry currentItem;
    private String locale_script = "en";
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    // Get the data from the GreenDao database
    List<UserData> userDataList = App.get().getDaoSession().getUserDataDao().loadAll();
    List<Stage> stageList = App.get().getDaoSession().getStageDao().loadAll();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_otis);

        // Add a toolbar to the Activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setTitle(getString(R.string.entry_title) + ": " + SettingsManager.PREFERRED_TAXON_NAME);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(true);
        }

        checkWriteStoragePermission();

        /*
         * Get the view...
         */
        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(this);
        tv_latitude = findViewById(R.id.tv_latitude);
        tv_longitude = findViewById(R.id.tv_longitude);
        tv_gps = findViewById(R.id.tv_gps);
        tv_observer_latitude = findViewById(R.id.tv_observer_latitude);
        tv_observer_longitude = findViewById(R.id.tv_observer_longitude);
        tvStage = findViewById(R.id.text_view_stages);
        tvStage.setOnClickListener(this);
        et_razlogSmrti = (EditText) findViewById(R.id.edit_text_death_comment);
        et_komentar = (EditText) findViewById(R.id.et_komentar);
        et_brojJedinki = (EditText) findViewById(R.id.et_brojJedinki);
        et_habitat = (EditText) findViewById(R.id.et_habitat);
        et_observation_length = (EditText) findViewById(R.id.et_observation_length);
        // In order not to use spinner to choose sex, we will put this into EditText
        select_sex = findViewById(R.id.text_view_sex);
        select_sex.setOnClickListener(this);
        check_dead = (CheckBox) findViewById(R.id.dead_specimen);
        check_dead.setOnClickListener(this);
        // Buttons to add images
        ib_pic1_frame = (FrameLayout) findViewById(R.id.ib_pic1_frame);
        ib_pic1 = (ImageView) findViewById(R.id.ib_pic1);
        ib_pic1_del = (ImageView) findViewById(R.id.ib_pic1_del);
        ib_pic1_del.setOnClickListener(this);
        ib_pic2_frame = (FrameLayout) findViewById(R.id.ib_pic2_frame);
        ib_pic2 = (ImageView) findViewById(R.id.ib_pic2);
        ib_pic2_del = (ImageView) findViewById(R.id.ib_pic2_del);
        ib_pic2_del.setOnClickListener(this);
        ib_pic3_frame = (FrameLayout) findViewById(R.id.ib_pic3_frame);
        ib_pic3 = (ImageView) findViewById(R.id.ib_pic3);
        ib_pic3_del = (ImageView) findViewById(R.id.ib_pic3_del);
        ib_pic3_del.setOnClickListener(this);
        iconTakePhotoCamera = (ImageView) findViewById(R.id.image_view_take_photo_camera);
        iconTakePhotoCamera.setOnClickListener(this);
        iconTakePhotoGallery = (ImageView) findViewById(R.id.image_view_take_photo_gallery);
        iconTakePhotoGallery.setOnClickListener(this);
        // Map icon
        iv_map = (ImageView) findViewById(R.id.iv_map);
        iv_map.setOnClickListener(this);
        iv_observer_map = (ImageView) findViewById(R.id.iv_observer_map);
        iv_observer_map.setOnClickListener(this);
        // Show advanced options for data entry if selected in preferences
        detailedEntry = (LinearLayout) findViewById(R.id.detailed_entry);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("advanced_interface", false)) {
            detailedEntry.setVisibility(View.VISIBLE);
        }

        // Get the system locale to translate names of the taxa
        final Locale locale = getCurrentLocale();
        // Workaround to get the taxon names for Serbian Latin locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (locale.getLanguage().equals("sr") && locale.getScript().equals("Latn")) {
                locale_script = "sr-Latn";
            } else {
                locale_script = locale.getLanguage();
            }
        } else {
            locale_script = locale.getLanguage();
        }

        // Fill in the drop down menu with list of taxa
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new String[1]);
//        acTextView = findViewById(R.id.textview_list_of_taxa);
//        acTextView.setAdapter(adapter);
//        acTextView.setThreshold(2);
        // This linear layout holds the stages. We will hide it before the taxon is not selected.
        final TextInputLayout stages = findViewById(R.id.text_input_stages);
        // When user type taxon name...
//        acTextView.addTextChangedListener(new TextWatcher() {
//            final Handler handler = new Handler();
//            Runnable runnable;
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                handler.removeCallbacks(runnable);
//
//                final String input_text = String.valueOf(s);
//
//                runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        /*
//                        Get the list of taxa from the GreenDao database
//                         */
//                        List<TaxonLocalization> taxaList = App.get().getDaoSession().getTaxonLocalizationDao()
//                                .queryBuilder()
//                                .where(TaxonLocalizationDao.Properties.Locale.eq(locale_script),
//                                        TaxonLocalizationDao.Properties.LatinAndNativeName.like("%" + String.valueOf(input_text) + "%"))
//                                .limit(10)
//                                .list();
//                        String[] taxaNames = new String[taxaList.size()];
//                        for (int i = 0; i < taxaList.size(); i++) {
//                            // Get the latin names
//                            taxaNames[i] = taxaList.get(i).getLatinAndNativeName();
//                        }
//                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EntryActivityOtis.this, android.R.layout.simple_dropdown_item_1line, taxaNames);
//                        acTextView.setAdapter(adapter);
//                        adapter.notifyDataSetChanged();
//
//                        /*
//                        Update the UI elements
//                         */
//                        // Enable stage entry
//                        if (getSelectedTaxonId() != null) {
//                            // Check if the taxon has stages. If not hide the stages dialog.
//                            if (isStageAvailable()) {
//                                stages.setVisibility(View.VISIBLE);
//                            }
//                            Log.d(TAG, "Taxon is selected from the list. Enabling Stages for this taxon.");
//                        } else {
//                            stages.setVisibility(View.GONE);
//                            Log.d(TAG, "Taxon is not selected from the list. Disabling Stages for this taxon.");
//                        }
//                        // Enable/disable Save button in Toolbar
//                        if (acTextView.getText().toString().length() > 1) {
//                            save_enabled = true;
//                            Log.d(TAG, "Taxon is set to: " + acTextView.getText());
//                            invalidateOptionsMenu();
//                        } else {
//                            save_enabled = false;
//                            Log.d(TAG, "Taxon entry field is empty.");
//                            invalidateOptionsMenu();
//                        }
//                    }
//                };
//                handler.postDelayed(runnable, 600);
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });

        // Define locatonListener and locationManager in order to
        // to receive the Location.
        // Call the function updateLocation() to do all the magic...
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                nLokacija = new LatLng(location.getLatitude(), location.getLongitude());
                setLocationValues(location.getLatitude(), location.getLongitude());
                elev = location.getAltitude();
                acc = Double.valueOf(location.getAccuracy());
//                tv_gps.setText(String.format(Locale.ENGLISH, "%.0f", acc));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                //buildAlertMessageNoGps();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Set observer location listener and manager.
        observerLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                observerLocation = new LatLng(location.getLatitude(), location.getLongitude());
                setObserverLocationValues(location.getLatitude(), location.getLongitude());
                elev = location.getAltitude();
                acc = Double.valueOf(location.getAccuracy());
                tv_gps.setText(String.format(Locale.ENGLISH, "%.0f", acc));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                //buildAlertMessageNoGps();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        };

        observerLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Finally to start the gathering of data...
        startEntryActivity();

    }
/*
    // Start a thread to monitor taxa update and remove the progress bar when updated
    Thread getTaxaForList = new Thread() {
        @Override
        public void run() {
            try {
                while (FetchTaxa.isInstanceCreated()) {
                    // Do domething
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Set the view
                    }
                });
            }
        }
    };
*/

    /*
    /  If new entry just get the coordinates.
    /  If existing entry get the known values from the entry.
    */
    private void startEntryActivity() {
        Long existing_entry_id = getIntent().getLongExtra("ENTRY_ID", 0);
        if (isNewEntry()) {
            Log.i(TAG, "Starting new entry.");
            getLocation(100, 2);
        } else {
            currentItem = App.get().getDaoSession().getEntryDao().load(existing_entry_id);
            Log.i(TAG, "Opening existing entry with ID: " + String.valueOf(existing_entry_id) + ".");
            // Get the latitude, longitude, coordinate precision and elevation...
            nLokacija = new LatLng(currentItem.getLattitude(), currentItem.getLongitude());
            elev = currentItem.getElevation();
            acc = currentItem.getAccuracy();
            tv_latitude.setText(String.format(Locale.ENGLISH, "%.4f", currentItem.getLattitude()));
            tv_longitude.setText(String.format(Locale.ENGLISH, "%.4f", currentItem.getLongitude()));
            tv_gps.setText(String.format(Locale.ENGLISH, "%.0f", currentItem.getAccuracy()));
            // Get the name of the taxon for this entry
//            acTextView.setText(currentItem.getTaxonSuggestion());
//            acTextView.dismissDropDown();
            // Get the name of the stage for the entry from the database
            if (currentItem.getStage() != null) {
                String stageName = (App.get().getDaoSession().getStageDao().queryBuilder()
                        .where(StageDao.Properties.StageId.eq(currentItem.getStage()))
                        .list().get(1).getName());
                Long stage_id = (App.get().getDaoSession().getStageDao().queryBuilder()
                        .where(StageDao.Properties.StageId.eq(currentItem.getStage()))
                        .list().get(1).getStageId());
                Stage stage = new Stage(null, stageName, stage_id, currentItem.getTaxonId());
                tvStage.setTag(stage);
                tvStage.setText(stageName);
            }
            if (currentItem.getCauseOfDeath().length() != 0) {
                et_razlogSmrti.setText(currentItem.getCauseOfDeath());
            }
            // Multiple fields are stored in comment string in CSV format.
            CsvComment csvComment = new CsvComment();
            csvComment.parse(currentItem.getComment());
            String userComment = csvComment.getComment();
            if (userComment != null && !userComment.isEmpty()) {
                et_komentar.setText(userComment);
            }
            String observationLength = csvComment.getObservationLength();
            if (observationLength != null && !observationLength.isEmpty()) {
                et_observation_length.setText(observationLength);
            }
            String observerLongitude = csvComment.getObserverLongitude();
            if (observerLongitude != null && !observerLongitude.isEmpty()) {
                tv_observer_longitude.setText(observerLongitude);
            }
            String observerLatitude = csvComment.getObserverLatitude();
            if (observerLatitude != null && !observerLatitude.isEmpty()) {
                tv_observer_latitude.setText(observerLatitude);
            }
            if (currentItem.getNumber() != null) {
                et_brojJedinki.setText(String.valueOf(currentItem.getNumber()));
            }
            // Get the selected sex. If not selected set spinner to default...
            Log.d(TAG, "Sex of individual from previous entry is " + currentItem.getSex());
            if (currentItem.getSex().equals("male")) {
                Log.d(TAG, "Setting spinner selected item to male.");
                select_sex.setText(getString(R.string.is_male));
            } if (currentItem.getSex().equals("female")) {
                Log.d(TAG, "Setting spinner selected item to female.");
                select_sex.setText(getString(R.string.is_female));
            }
            if (currentItem.getDeadOrAlive().equals("true")) {
                // Specimen is a live
                check_dead.setChecked(false);
            } else {
                // Specimen is dead, Checkbox should be activated and Dead Comment shown
                check_dead.setChecked(true);
                showDeadComment();
            }
            slika1 = currentItem.getSlika1();
            if (slika1 != null) {
                Glide.with(this)
                        .load(slika1)
                        .into(ib_pic1);
                ib_pic1_frame.setVisibility(View.VISIBLE);
            }
            slika2 = currentItem.getSlika2();
            if (slika2 != null) {
                Glide.with(this)
                        .load(slika2)
                        .into(ib_pic2);
                ib_pic2_frame.setVisibility(View.VISIBLE);
            }
            slika3 = currentItem.getSlika3();
            if (slika3 != null) {
                Glide.with(this)
                        .load(slika3)
                        .into(ib_pic3);
                ib_pic3_frame.setVisibility(View.VISIBLE);
            }

            if (slika1 == null || slika2 == null || slika3 == null) {
                disablePhotoButtons(false);
            } else {
                disablePhotoButtons(true);
            }

            if (currentItem.getHabitat() != null) {
                et_habitat.setText(currentItem.getHabitat());
            }
        }
    }

    private Boolean isNewEntry() {
        String is_new_entry = getIntent().getStringExtra("IS_NEW_ENTRY");
        return is_new_entry.equals("YES");
    }

    // Add Save button in the right part of the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    // Customize Save item to enable if when needed
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_save);
        if (save_enabled) {
            item.setEnabled(true);
            item.getIcon().setAlpha(255);
        } else {
            // disabled
            item.setEnabled(false);
            item.getIcon().setAlpha(30);
        }
        return true;
    }

    // Process running after clicking the toolbar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        if (id == R.id.action_save) {
                saveEntry();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // On click
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_view_stages:
                getStageForTaxon();
                break;
            case R.id.text_view_sex:
                getSexForList();
                break;
            case R.id.ib_pic1_del:
                ib_pic1_frame.setVisibility(View.GONE);
                disablePhotoButtons(false);
                slika1 = null;
                break;
            case R.id.ib_pic2_del:
                ib_pic2_frame.setVisibility(View.GONE);
                disablePhotoButtons(false);
                slika2 = null;
                break;
            case R.id.ib_pic3_del:
                ib_pic3_frame.setVisibility(View.GONE);
                disablePhotoButtons(false);
                slika3 = null;
                break;
            case R.id.dead_specimen:
                showDeadComment();
                break;
            case R.id.iv_map:
                showMap();
                break;
            case R.id.iv_observer_map:
                showObserverMap();
                break;
            case R.id.image_view_take_photo_camera:
                takePhotoFromCamera();
                break;
            case R.id.image_view_take_photo_gallery:
                takePhotoFromGallery();
                break;
        }
    }

    /*
    /  This calls other functions to get the values, check the validity of
    /  the data and to finally save it into the entry
    */
    private void saveEntry() {
        // Insure that the taxon is entered correctly
        Long taxonID = getSelectedTaxonId();
        if (taxonID == null) {
            buildAlertMessageInvalidTaxon();
        } else {
            // If the location is not loaded, warn the user and
            // don’t send crappy data into the online database!
            if (nLokacija.latitude == 0) {
                buildAlertMessageNoCoordinates();
            } else {
                // If the location is not precise ask the user to
                // wait for a precise location, or to go for it anyhow...
                if (nLokacija.latitude > 0 && acc >= 25) {
                    buildAlertMessageUnpreciseCoordinates();
                }
            }

            if (nLokacija.latitude > 0 && (acc <= 25)) {
                // Save the taxon
                Taxon taxon = App.get().getDaoSession().getTaxonDao().queryBuilder()
                        .where(TaxonDao.Properties.Id.eq(taxonID))
                        .unique();
                entrySaver(taxon);
            }
        }
    }

    //  Gather all the data into the Entry and wright it into the GreenDao database.
    private void entrySaver(final Taxon taxon) {
        Stage stage = (tvStage.getTag() != null) ? (Stage) tvStage.getTag() : null;
//        String komentar = (et_komentar.getText().toString() != null) ? et_komentar.getText().toString() : "";
        CsvComment csvComment = new CsvComment();
        csvComment.setComment(et_komentar.getText() != null ? et_komentar.getText().toString() : "");
        csvComment.setObservationLength(et_observation_length.getText() != null ? et_observation_length.getText().toString() : "");
        csvComment.setObserverLongitude(tv_observer_longitude.getText() != null ? tv_observer_longitude.getText().toString() : "");
        csvComment.setObserverLatitude(tv_observer_latitude.getText() != null ? tv_observer_latitude.getText().toString() : "");
        String comment = csvComment.create();
        Integer brojJedinki = (et_brojJedinki.getText().toString().trim().length() > 0) ? Integer.valueOf(et_brojJedinki.getText().toString()) : null;
        Long selectedStage = (stage != null) ? stage.getStageId() : null;
        String razlogSmrti = (et_razlogSmrti.getText() != null) ? et_razlogSmrti.getText().toString() : "";
        String habitat = et_habitat.getText() != null ? et_habitat.getText().toString() : "";

        if (isNewEntry()) {
            calendar = Calendar.getInstance();
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String fullDate = simpleDateFormat.format(calendar.getTime());
            String day = fullDate.substring(0, 2);
            String month = fullDate.substring(3, 5);
            String year = fullDate.substring(6, 10);
            String time = fullDate.substring(11, 16);
            Long taxon_id = taxon.getId();
            String taxon_name = taxon.getName();
            String project_name = PreferenceManager.getDefaultSharedPreferences(this).getString("project_name", "0");

            // Get the data structure and save it into a database Entry
            Entry entry1 = new Entry(null, taxon_id, taxon_name, year, month, day,
                    comment, brojJedinki, maleFemale(), selectedStage, String.valueOf(!check_dead.isChecked()), razlogSmrti,
                    nLokacija.latitude, nLokacija.longitude, acc, elev, "", slika1, slika2, slika3,
                    project_name, "", String.valueOf(getGreenDaoDataLicense()), getGreenDaoImageLicense(), time, habitat);
            App.get().getDaoSession().getEntryDao().insertOrReplace(entry1);
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }

        else { // if the entry exist already
            currentItem.setTaxonId(taxon.getId());
            currentItem.setTaxonSuggestion(taxon.getName().toString());
            currentItem.setComment(comment);
            currentItem.setNumber(brojJedinki);
            currentItem.setSex(maleFemale());
            currentItem.setStage(selectedStage);
            currentItem.setDeadOrAlive(String.valueOf(!check_dead.isChecked()));
            currentItem.setCauseOfDeath(razlogSmrti);
            currentItem.setLattitude(nLokacija.latitude);
            currentItem.setLongitude(nLokacija.longitude);
            currentItem.setElevation(elev);
            currentItem.setAccuracy(acc);
            currentItem.setSlika1(slika1);
            currentItem.setSlika2(slika2);
            currentItem.setSlika3(slika3);
            currentItem.setHabitat(habitat);

            // Now just update the database with new data...
            App.get().getDaoSession().getEntryDao().updateInTx(currentItem);
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            //Intent intent1 = new Intent(this, LandingActivity.class);
            //startActivity(intent1);

            setResult(RESULT_OK);
            finish();
        }
    }

    private String maleFemale() {
        String return_sex = "";
        String[] sex = {getString(R.string.unknown_sex), getString(R.string.is_male), getString(R.string.is_female)};
        String sex_is = select_sex.getText().toString();
        int sex_id = Arrays.asList(sex).indexOf(sex_is);
        if (sex_id == 1) {
            Log.d(TAG, "Sex from spinner index 1 selected with value " + sex_is);
            return_sex = "male";
        } else if (sex_id == 2) {
            Log.d(TAG, "Sex from spinner index 2 selected with value " + sex_is);
            return_sex = "female";
        }
        return return_sex;
    }

    private Boolean isStageAvailable() {
        Taxon taxon = App.get().getDaoSession().getTaxonDao().queryBuilder()
                .where(TaxonDao.Properties.Name.eq(getLatinName()))
                .unique();
        stageList = (ArrayList<Stage>) App.get().getDaoSession().getStageDao().queryBuilder()
                .where(StageDao.Properties.TaxonId.eq(taxon.getId()))
                .list();
        return stageList.size() != 0;
    }

    private void getStageForTaxon() {
        Taxon taxon = App.get().getDaoSession().getTaxonDao().queryBuilder()
                .where(TaxonDao.Properties.Name.eq(getLatinName()))
                .unique();
        if (taxon == null) {
            tvStage.setEnabled(false);
            Log.d(TAG, "Taxon not found: " + getLatinName());
            return;
        }
        stageList = (ArrayList<Stage>) App.get().getDaoSession().getStageDao().queryBuilder()
                .where(StageDao.Properties.TaxonId.eq(taxon.getId()))
                .list();
        if (stageList != null) {
            final String[] stadijumi = new String[stageList.size()];
            for (int i = 0; i < stageList.size(); i++) {
                stadijumi[i] = stageList.get(i).getName();
                // Translate this to interface...
                if (stadijumi[i].equals("egg")) {stadijumi[i] = getString(R.string.stage_egg);}
                if (stadijumi[i].equals("larva")) {stadijumi[i] = getString(R.string.stage_larva);}
                if (stadijumi[i].equals("pupa")) {stadijumi[i] = getString(R.string.stage_pupa);}
                if (stadijumi[i].equals("adult")) {stadijumi[i] = getString(R.string.stage_adult);}
                if (stadijumi[i].equals("juvenile")) {stadijumi[i] = getString(R.string.stage_juvenile);}
            }
            if (stadijumi.length == 0) {
                Log.d(TAG, "No stages are available for " + getLatinName() + ".");
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(stadijumi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tvStage.setText(stadijumi[i]);
                        tvStage.setTag(stageList.get(i));
                    }
                });
                builder.show();
                Log.d(TAG, "Available stages for " + getLatinName() + " include: " + Arrays.toString(stadijumi));
            }
        } else {
            tvStage.setEnabled(false);
            Log.d(TAG, "Stage list from GreenDao is empty for taxon " + getLatinName() + ".");
        }
    }

    private void getSexForList() {
        final String[] sex = {getString(R.string.unknown_sex), getString(R.string.is_male), getString(R.string.is_female)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(sex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (sex[i].equals(getString(R.string.unknown_sex))) {
                    select_sex.setText(null);
                    Log.d(TAG, "No sex is selected.");
                } else {
                    select_sex.setText(sex[i]);
                    Log.d(TAG, "Selected sex for this entry is " + sex[i] + ".");
                }
            }
        });
        builder.show();
    }

    private void showMap() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("latlong", nLokacija);
        startActivityForResult(intent, MAP);
    }

    private void showObserverMap() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("latlong", observerLocation);
        startActivityForResult(intent, MAP_OBSERVER);
    }

    public void takePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    // Check for camera permission and run function to take photo
    private void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                Log.d(TAG, "Could not show camera permission dialog.");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            takePhoto();
        }
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // startActivityForResult(takePictureIntent, CAMERA);
            dispatchTakePictureIntent();
        } else {
            Log.d(TAG, "Take picture intent could not start for some reason.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        
        if (requestCode == GALLERY) {
            if (data != null) {
                contentURI = data.getData();
                try {
                    File file = createImageFile();
                    copyFile(new File(getPath(data.getData())), file);
                    entryAddPic();
                    if (slika1 == null) {
                        slika1 = resizeImage(mCurrentPhotoPath);
                        Glide.with(this)
                                .load(slika1)
                                .into(ib_pic1);
                        ib_pic1_frame.setVisibility(View.VISIBLE);
                    } else if (slika2 == null) {
                        slika2 = resizeImage(mCurrentPhotoPath);
                        Glide.with(this)
                                .load(slika2)
                                .into(ib_pic2);
                        ib_pic2_frame.setVisibility(View.VISIBLE);
                    } else if (slika3 == null) {
                        slika3 = resizeImage(mCurrentPhotoPath);
                        Glide.with(this)
                                .load(slika3)
                                .into(ib_pic3);
                        ib_pic3_frame.setVisibility(View.VISIBLE);
                        iconTakePhotoGallery.setEnabled(false);
                        iconTakePhotoGallery.setImageAlpha(20);
                        iconTakePhotoCamera.setEnabled(false);
                        iconTakePhotoCamera.setImageAlpha(20);
                    }
                }

                catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(EntryActivityOtis.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
                    entryAddPic();
                    if (slika1 == null) {
                        slika1 = resizeImage(mCurrentPhotoPath);
                        Glide.with(this)
                                .load(slika1)
                                .into(ib_pic1);
                        ib_pic1_frame.setVisibility(View.VISIBLE);
                    } else if (slika2 == null) {
                        slika2 = resizeImage(mCurrentPhotoPath);
                        Glide.with(this)
                                .load(slika2)
                                .into(ib_pic2);
                        ib_pic2_frame.setVisibility(View.VISIBLE);
                    } else if (slika3 == null) {
                        slika3 = resizeImage(mCurrentPhotoPath);
                        Glide.with(this)
                                .load(slika3)
                                .into(ib_pic3);
                        ib_pic3_frame.setVisibility(View.VISIBLE);
                        disablePhotoButtons(true);
                    }

            Toast.makeText(EntryActivityOtis.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }

        // Get data from Google MapActivity.java and save it as local variables
        else if (requestCode == MAP) {
            locationManager.removeUpdates(locationListener);
            if(data != null) {
                nLokacija = data.getParcelableExtra("google_map_latlong");
                setLocationValues(nLokacija.latitude, nLokacija.longitude);
                String accString = data.getExtras().getString("google_map_accuracy");
                if (accString != null && !accString.isEmpty() && !accString.equals("null")) {
                    acc = Double.valueOf(accString);
                }
                String elevString = data.getExtras().getString("google_map_elevation");
                if (elevString != null && !elevString.isEmpty() && !elevString.equals("null")) {
                    elev = Double.valueOf(elevString);
                }
            }
        }
        else if (requestCode == MAP_OBSERVER) {
            observerLocationManager.removeUpdates(observerLocationListener);
            if(data != null) {
                observerLocation = data.getParcelableExtra("google_map_latlong");
                setObserverLocationValues(observerLocation.latitude, observerLocation.longitude);
                String accString = data.getExtras().getString("google_map_accuracy");
                if (accString != null && !accString.isEmpty() && !accString.equals("null")) {
                    acc = Double.valueOf(accString);
                }
                String elevString = data.getExtras().getString("google_map_elevation");
                if (elevString != null && !elevString.isEmpty() && !elevString.equals("null")) {
                    elev = Double.valueOf(elevString);
                }
            }
            if (data.getExtras().getString("google_map_accuracy").equals("0.0")) {
                tv_gps.setText(R.string.not_available);
            } else {
                tv_gps.setText(String.format(Locale.ENGLISH, "%.0f", acc));
            }
        }
    }

    private void disablePhotoButtons(Boolean value) {
        if (value) {
            iconTakePhotoGallery.setEnabled(false);
            iconTakePhotoGallery.setImageAlpha(20);
            iconTakePhotoCamera.setEnabled(false);
            iconTakePhotoCamera.setImageAlpha(20);
        } else {
            iconTakePhotoGallery.setEnabled(true);
            iconTakePhotoGallery.setImageAlpha(255);
            iconTakePhotoCamera.setEnabled(true);
            iconTakePhotoCamera.setImageAlpha(255);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "biologer");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                mediaStorageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                /*Uri photoURI = Uri.fromFile(photoFile);*//* FileProvider.getUriForFile(this,
                        "org.biologeruntill upload.biologer.fileprovider",
                        photoFile)*//*;*/
                Uri photoURI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoURI = FileProvider.getUriForFile(EntryActivityOtis.this, "org.biologer.otis.fileprovider", photoFile);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    photoURI = Uri.fromFile(photoFile);
                }

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA);
            }
        }
    }

    private void entryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    // Resize the picture and save it in biologer folder
    private String resizeImage(String path_to_image) {
        Bitmap input_image = BitmapFactory.decodeFile(path_to_image);
        Log.d(TAG, "Input image path is " + String.valueOf(path_to_image));
        Bitmap output_image;
        int longer_side = 800;
        if (input_image.getHeight() < input_image.getWidth()) {
            int output_height = input_image.getHeight() * longer_side / input_image.getWidth();
            output_image = Bitmap.createScaledBitmap(input_image, longer_side, output_height, false);
        } else {
            int output_width = input_image.getWidth() * longer_side / input_image.getHeight();
            output_image = Bitmap.createScaledBitmap(input_image, output_width, longer_side, false);
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "biologer");

        String input_image_name = path_to_image.substring(path_to_image.lastIndexOf("/")+1);
        Log.d(TAG, "Input image name is " + String.valueOf(input_image_name));
        String output_image_name = input_image_name.split(".jpg")[0] + "_res.jpg";
        Log.d(TAG, "Output image name is " + String.valueOf(output_image_name));

        File image = new File(mediaStorageDir, output_image_name);

        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(image);
            output_image.compress(Bitmap.CompressFormat.JPEG, 70, fOut);
            fOut.flush();
            fOut.close();
            input_image.recycle();
            output_image.recycle();
        } catch (Exception e) {
            // Do something
        }

        // Return the path to the image file
        Log.d(TAG, "Output image path is " + image.getPath());
        return image.getPath();
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

    public void showDeadComment() {
        if (check_dead.isChecked()) {
            et_razlogSmrti.setVisibility(View.VISIBLE);
        } else {
            et_razlogSmrti.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Not possible to get permission to write external storage.");
                } else {
                    finish();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    Log.d(TAG, "Not possible to get permission to use camera.");
                }
            }
        }
    }

    // Function used to retrieve the location
    private void getLocation(int time, int distance) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EntryActivityOtis.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            // Sometimes there is a problem with first run of the program. So, request location again in 10 secounds...
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLocation(100, 2);
                }
            }, 10000);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, locationListener);
            observerLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, observerLocationListener);
        }
    }

    private void setLocationValues(double latti, double longi) {
        String latitude = String.format(Locale.ENGLISH, "%.4f", (latti));
        String longitude = String.format(Locale.ENGLISH, "%.4f", (longi));
        tv_latitude.setText(latitude);
        tv_longitude.setText(longitude);
    }

    private void setObserverLocationValues(double latti, double longi) {
        String latitude = String.format(Locale.ENGLISH, "%.4f", (latti));
        String longitude = String.format(Locale.ENGLISH, "%.4f", (longi));
        tv_observer_latitude.setText(latitude);
        tv_observer_longitude.setText(longitude);
    }

    /*
    protected void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder_gps = new AlertDialog.Builder(this);
        builder_gps.setMessage(getString(R.string.enable_location))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.enable_location_yes), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getString(R.string.enable_location_no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder_gps.create();
        alert.show();
    }
    */

    // Show the message if the taxon is not chosen from the taxonomic list
    protected void buildAlertMessageInvalidTaxon() {
        final AlertDialog.Builder builder_taxon = new AlertDialog.Builder(EntryActivityOtis.this);
        builder_taxon.setMessage(getString(R.string.invalid_taxon_name))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.save_anyway), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        // Save custom taxon with no ID
                        Taxon taxon = new Taxon(null, getLatinName());
                        entrySaver(taxon);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        finish();
                    }
                });
        final AlertDialog alert = builder_taxon.create();
        alert.show();
    }

    // Show the message if the location is not loaded
    protected void buildAlertMessageNoCoordinates() {
        final AlertDialog.Builder builder_no_coords = new AlertDialog.Builder(this);
        builder_no_coords.setMessage(getString(R.string.location_is_zero))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.wait), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        getLocation(0, 0);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        finish();
                    }
                });
        final AlertDialog alert = builder_no_coords.create();
        alert.show();
    }

    protected void buildAlertMessageUnpreciseCoordinates() {
        final AlertDialog.Builder builder_unprecise_coords = new AlertDialog.Builder(this);
        builder_unprecise_coords.setMessage(getString(R.string.unprecise_coordinates))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.wait), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        getLocation(0, 0);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.save_anyway), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        // Save the taxon
                        Taxon taxon = App.get().getDaoSession().getTaxonDao().queryBuilder()
                                .where(TaxonDao.Properties.Id.eq(getSelectedTaxonId()))
                                .unique();
                        entrySaver(taxon);
                    }
                });
        final AlertDialog alert = builder_unprecise_coords.create();
        alert.show();
    }

    // Get Location if user refresh the view
    @Override
    public void onRefresh() {
        if (isNewEntry()) {
            swipe.setRefreshing(true);
            getLocation(0, 0);
            swipe.setRefreshing(false);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.gps_update))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            swipe.setRefreshing(true);
                            getLocation(0, 0);
                            swipe.setRefreshing(false);
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                            swipe.setRefreshing(false);
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EntryActivityOtis.this, LandingActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    // Check for permissions and add them if required
    private void checkWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "No permission to write external storage");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.d(TAG, "User does not allow to set write permission for this application.");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL);
                Log.d(TAG, "Requesting permission to write external storage.");
            }
        } else {
            Log.d(TAG, "Permission to write external storage was already granted");
        }
    }

    private int getGreenDaoDataLicense() {
        if(userDataList !=null) {
            if(!userDataList.isEmpty()) {
                return userDataList.get(0).getData_license();
            }
            return 0;
        }
        return 0;
    }

    private int getGreenDaoImageLicense() {
        if(userDataList != null) {
            if (!userDataList.isEmpty()) {
                return userDataList.get(0).getImage_license();
            }
            return 0;
        }
        return 0;
    }

    Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            Locale locale = getResources().getConfiguration().getLocales().get(0);
            //Log.i(TAG, "Current System locale is set to " + locale.getDisplayLanguage() + " (" + locale.getLanguage() + "-" + locale.getScript() + ").");
            return locale;
        } else{
            Locale locale = getResources().getConfiguration().locale;
            Log.i(TAG, "Current System locale is set to " + locale.getLanguage());
            return locale;
        }
    }

    private Long getSelectedTaxonId() {
        Taxon taxon = App.get().getDaoSession().getTaxonDao().queryBuilder()
                .where(TaxonDao.Properties.Name.eq(getLatinName()))
                .unique();
        if (taxon != null) {
            Log.d(TAG, "Selected taxon latin name is: " + taxon.getName() + ". Taxon ID: " + String.valueOf(taxon.getId()));
            return taxon.getId();
        } else {
            return null;
        }
    }

    private String getLatinName() {
//        String entered_taxon_name = acTextView.getText().toString();
//        return entered_taxon_name.split(" \\(")[0];
        return SettingsManager.PREFERRED_TAXON_NAME;
    }

    /**
     * This class is used to parse and create CSV string returned by {@link Entry#getComment()} method.
     */
    private class CsvComment {

        /** Comment entered by the user. */
        private String comment;

        /** Number of minutes used to observe the specimen. */
        private String observationLength;

        /** Latitude of the observer. */
        private String observerLatitude;

        /** Longitude of the observer. */
        private String observerLongitude;

        /**
         * Parse CSV string returned by {@link Entry#getComment()} method.<br>
         * Field values are obtained using corresponding get methods.
         * @param csvString CSV string returned by {@link Entry#getComment()} method.
         */
        public void parse(String csvString) {
            Matcher matcher = csvPattern.matcher(csvString);
            int index = 0;
            while (matcher.find()) {
                String match = matcher.group();
                if (match == null || match.isEmpty()) {
                    break;
                } else if (match.startsWith(",")) {  // trim trailing ,
                    match = match.substring(1);
                } else if (match.startsWith("\"") && match.endsWith("\"")) { // trim enclosing ""
                    match = match.substring(1, match.length() - 1);
                } else {
                    // Invalid match.
                    match = "";
                }
                match = match.replaceAll("\"\"", "\"");
                switch(index) {
                    case 0:
                        comment = match;
                        break;
                    case 1:
                        observationLength = match;
                        break;
                    case 2:
                        observerLatitude = match;
                        break;
                    case 3:
                        observerLongitude = match;
                        break;
                    default:
                        Log.w(TAG,"Wrong number of CSV values found: " + index);
                        return;
                }
                index++;
            }
        }

        /**
         * Creates CSV string used to store multiple fields in comment attribute of {@link Entry} class.<br>
         * Field values are set using corresponding set methods.
         * @return CSV string.
         */
        public String create() {
            return  createCsvField(comment) + CSV_DELIMITER +
                    createCsvField(observationLength) + CSV_DELIMITER +
                    createCsvField(observerLatitude) + CSV_DELIMITER +
                    createCsvField(observerLongitude);
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getObservationLength() {
            return observationLength;
        }

        public void setObservationLength(String observationLength) {
            this.observationLength = observationLength;
        }

        public String getObserverLatitude() {
            return observerLatitude;
        }

        public void setObserverLatitude(String observerLatitude) {
            this.observerLatitude = observerLatitude;
        }

        public String getObserverLongitude() {
            return observerLongitude;
        }

        public void setObserverLongitude(String observerLongitude) {
            this.observerLongitude = observerLongitude;
        }

        /**
         * Applies CSV rules to parameter string.
         * @param field String which will be processed.
         * @return String which matches CSV rules.
         */
        private String createCsvField(String field) {
            String csvField = field;
            if (field.contains(CSV_ENCLOSING)) {
                csvField = csvField.replaceAll("\"", "\"\"");
            }
            if (field.contains(CSV_ENCLOSING) || field.contains(CSV_DELIMITER)) {
                csvField = CSV_ENCLOSING + csvField + CSV_ENCLOSING;
            }
            return csvField;
        }
    }
}