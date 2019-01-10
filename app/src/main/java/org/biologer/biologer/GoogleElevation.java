package org.biologer.biologer;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GoogleElevation {

    private static final String TAG = "Biologer.GoogleElev";
    private static final int cTimeOutMs = 30 * 1000;
    private static double elevation = 0.0;

    public static void getElevation(Location location, Context context) throws IOException, JSONException {
        // https://developers.google.com/maps/documentation/elevation/

        final URL url = new URL("https://maps.googleapis.com/maps/api/elevation/json?locations=" +
                String.valueOf(location.getLatitude()) + "," +
                String.valueOf(location.getLongitude()) +
                "&key=AIzaSyB_LNOvpi3XK6U3eU2IoAtCrlcr_pjqkro");
        Log.d(TAG, "url=" + url);

        new Thread() {
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
                    Log.d(TAG, String.valueOf(code));
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
                            double elevation = results.getJSONObject(0).getDouble("elevation");
                            Log.i(TAG, "Elevation " + elevation);
                        } else
                            throw new IOException("JSON no results");
                    } else
                        throw new IOException("JSON status " + status);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        location.setAltitude(elevation);
    }
}