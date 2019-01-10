package org.biologer.biologer;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String acc = "0.0";
    private FloatingActionButton fbtn_set;
    private ImageView fbtn_mapType;
    private EditText et_acc;
    private LatLng komplet;
    String google_map_type = SettingsManager.getGoogleMapType();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Ovo dodaje dugme za nazad u traku sa alatima
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.google_maps_title);

        et_acc = findViewById(R.id.et_setAccuracy);

        Bundle bundle = getIntent().getExtras();
        komplet = bundle.getParcelable("komplet");

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
            // Dodaj marker - trenutna lokacija
            LatLng lokacija = new LatLng(komplet.latitude, komplet.longitude);
            mMap.addMarker(new MarkerOptions().position(lokacija).title(getString(R.string.you_are_here)).draggable(true));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokacija, 16));
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16), 1000, null);

            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    setKomplet(marker.getPosition().latitude, marker.getPosition().longitude);
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
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                SettingsManager.setGoogleMapType("SATELLITE");
                                break;
                            case 2:
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                SettingsManager.setGoogleMapType("TERRAIN");
                                break;
                            case 3:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                SettingsManager.setGoogleMapType("HYBRID");
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

    // Add Save button in the right part of the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        MenuItem item = menu.findItem(R.id.action_save);
        item.getIcon().setAlpha(255);
        return true;
    }


    // Process running after clicking the toolbar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        if (id == R.id.action_save) {
            Intent _result = new Intent();
            if (et_acc.getText().toString().length() != 0) {
                setAcc(et_acc.getText().toString());
            }
            _result.putExtra("acc", acc);
            _result.putExtra("nLoc", komplet);
            setResult(3, _result);
            finish();
        }
        return true;
    }

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public LatLng getKomplet() {
        return komplet;
    }

    public void setKomplet(double lat, double lon) {
        this.komplet = new LatLng(lat, lon);
    }
}
