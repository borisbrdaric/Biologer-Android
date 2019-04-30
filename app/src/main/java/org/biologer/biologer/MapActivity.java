package org.biologer.biologer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.biologer.biologer.model.RetrofitClient;
import org.biologer.biologer.model.UserData;
import org.biologer.biologer.model.network.ElevationResponse;
import org.biologer.biologer.model.network.UserDataResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import freemarker.template.utility.StringUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Biologer.GoogleMaps";

    private GoogleMap mMap;
    private String acc = "0.0";
    private String elevation = "0.0";
    ImageView fbtn_mapType;
    private EditText text_imput_acc;
    private LatLng latlong;
    String google_map_type = SettingsManager.getGoogleMapType();
    private static final int cTimeOutMs = 30 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Add a toolbar to the Activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        // Add the back button to the toolbar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(true);
            actionbar.setTitle(R.string.google_maps_title);
        }

        text_imput_acc = findViewById(R.id.et_setAccuracy);

        Bundle bundle = getIntent().getExtras();
        latlong = bundle.getParcelable("latlong");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fbtn_mapType = findViewById(R.id.fbtn_mapType);
        fbtn_mapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMapTypeSelectorDialog();
            }
        });
    }

    // Add Save button in the right part of the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        MenuItem item = menu.findItem(R.id.action_save);
        item.getIcon().setAlpha(255);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Select the type of the map according to the user’s settings
        if (google_map_type.equals("NORMAL")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } if (google_map_type.equals("SATELLITE")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } if (google_map_type.equals("TERRAIN")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } if (google_map_type.equals("HYBRID")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        if (mMap != null) {
            // Add marker at the GPS position on the map
            if (latlong.latitude == 0.0) {
                if (SettingsManager.getDatabaseName().equals("https://biologer.hr")) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(16.377937, 16.377937)).title(getString(R.string.you_are_here)).draggable(true));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.377937, 16.377937), 7));
                    mMap.animateCamera(CameraUpdateFactory.zoomIn());
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(7), 1000, null);
                }

                if (SettingsManager.getDatabaseName().equals("https://biologer.org")) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(44.150681, 20.725708)).title(getString(R.string.you_are_here)).draggable(true));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.150681, 20.725708), 7));
                    mMap.animateCamera(CameraUpdateFactory.zoomIn());
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(7), 1000, null);
                }

                if (SettingsManager.getDatabaseName().equals("https://dev.biologer.org")) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(44.150681, 20.725708)).title(getString(R.string.you_are_here)).draggable(true));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.150681, 20.725708), 7));
                    mMap.animateCamera(CameraUpdateFactory.zoomIn());
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(7), 1000, null);
                }

            } else {
                mMap.addMarker(new MarkerOptions().position(latlong).title(getString(R.string.you_are_here)).draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 16));
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16), 1000, null);
            }

            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    setLatLong(marker.getPosition().latitude, marker.getPosition().longitude);
                }

            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return false;
                }
            });
        }
    }

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = getString(R.string.select_map_type);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Create array to fill in the dropdown list
        CharSequence[] list_of_map_types =
                {"Normal", "Hybrid", "Terrain", "Satellite"};
        list_of_map_types[0] = getString(R.string.normal);
        list_of_map_types[1] = getString(R.string.hybrid);
        list_of_map_types[2] = getString(R.string.terrain);
        list_of_map_types[3] = getString(R.string.sattelite);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mMap.getMapType() - 1;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                list_of_map_types,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Locally create a finalised object.

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 1:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                SettingsManager.setGoogleMapType("HYBRID");
                                break;
                            case 2:
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                SettingsManager.setGoogleMapType("TERRAIN");
                                break;
                            case 3:
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                SettingsManager.setGoogleMapType("SATELLITE");
                                break;
                            default:
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                SettingsManager.setGoogleMapType("NORMAL");
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    // Process running after clicking the toolbar buttons (back and save)
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        if (id == R.id.action_save) {
            // Get the accuracy value from the text field
            if (text_imput_acc.getText().toString().length() != 0) {
                setAcc(text_imput_acc.getText().toString());
            }

            // Get elevation from biologer server, save all data and exit
            updateElevationAndSave(latlong);
        }
        return true;
    }

    // This function calls Google Elevation API
    public void getGoogleElevation(LatLng location) throws IOException, JSONException {

        final URL url = new URL("https://maps.googleapis.com/maps/api/elevation/json?locations=" +
                String.valueOf(location.latitude) + "," +
                String.valueOf(location.longitude) +
                "&key=AIzaSyDsLjNreiHg47Mif-CheIYB3uGtXQekTtY");
        Log.d(TAG, "url=" + url);

        Thread query_altitude = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

                    urlConnection.setConnectTimeout(cTimeOutMs);
                    urlConnection.setReadTimeout(cTimeOutMs);
                    urlConnection.setRequestProperty("Accept", "application/json");

                    // Set request type
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(false);
                    urlConnection.setDoInput(true);

                    // Check for errors
                    int code = urlConnection.getResponseCode();
                    Log.d(TAG, "Response code: " + String.valueOf(code));
                    if (code != HttpsURLConnection.HTTP_OK)
                        throw new IOException("HTTP error " + urlConnection.getResponseCode());

                    // Get response
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                        json.append(line);
                    Log.d(TAG, json.toString());

                    // Decode result
                    JSONObject jroot = new JSONObject(json.toString());
                    String status = jroot.getString("status");
                    if ("OK".equals(status)) {
                        JSONArray results = jroot.getJSONArray("results");
                        if (results.length() > 0) {
                            elevation = results.getJSONObject(0).getString("elevation");
                            Log.i(TAG, "Server returned elevation of: " + elevation + "m.");
                        } else
                            throw new IOException("JSON no results");
                    } else
                        throw new JSONException("JSON status " + status);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        query_altitude.start();

        // Wait until we get the altitude...
        try {
            query_altitude.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateElevationAndSave(LatLng coordinates) {
        Call<ElevationResponse> call = RetrofitClient.getService(SettingsManager.getDatabaseName()).getElevation(coordinates.latitude, coordinates.longitude);
        Log.d(TAG, "Requesting altitude for Latitude: " + coordinates.latitude + "; Longitude: " + coordinates.longitude);
        call.enqueue(new Callback<ElevationResponse>() {
            @Override
            public void onResponse(Call<ElevationResponse> call, Response<ElevationResponse> response) {
                if (response.body() != null) {
                    elevation = String.valueOf(response.body().getElevation());
                    Log.d(TAG, "Elevation for this point is " + String.valueOf(elevation) + ".");
                    saveAndExit();
                } else {
                    elevation = "0.0";
                    saveAndExit();
                }
            }

            @Override
            public void onFailure(Call<ElevationResponse> call, Throwable t) {
                Log.d(TAG, "No elevation returned from server...");
                elevation = "0.0";

                saveAndExit();
            }
        });
    }

    private void saveAndExit() {
        // Forward the result to previous Activity
        Intent returnLocation = new Intent();
        returnLocation.putExtra("google_map_accuracy", acc);
        returnLocation.putExtra("google_map_latlong", latlong);
        returnLocation.putExtra("google_map_elevation", elevation);
        setResult(3, returnLocation);

        Log.d(TAG, "Latitude: " + latlong.latitude);
        Log.d(TAG, "Longitude: " + latlong.longitude);
        Log.d(TAG, "Accuracy: " + acc);
        Log.d(TAG, "Elevation: " + elevation);

        finish();
    }

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public LatLng getLatLong() {
        return latlong;
    }

    public void setLatLong(double lat, double lon) {
        this.latlong = new LatLng(lat, lon);
    }
}
