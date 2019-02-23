package org.biologer.biologer;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by brjovanovic on 12/24/2017.
 */

public class SettingsManager {

    private static final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());

    public enum KEY {
        token, FIRST_LAUNCH, DATABASE_NAME, DATABASE_VERSION, GOOGLE_MAP_TYPE, CUSTOM_DATA_LICENSE, CUSTOM_IMAGE_LICENSE, PROJECT_NAME, TAXA_LAST_PAGE_UPDATED
    }

    public static boolean isFirstLaunch()
    {
        return prefs.getBoolean(KEY.FIRST_LAUNCH.toString(), true);
    }

    public static void setFirstLaunch(boolean firstLaunch)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY.FIRST_LAUNCH.toString(), firstLaunch);
        editor.apply();
    }

    public static void setDatabaseName(String databaseName) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SettingsManager.KEY.DATABASE_NAME.toString(), databaseName);
        editor.commit();
    }

    public static String getDatabaseName() {
        return prefs.getString(KEY.DATABASE_NAME.toString(),"https://biologer.org");
    }

    public static void setDatabaseVersion(String databaseVersion) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SettingsManager.KEY.DATABASE_VERSION.toString(), databaseVersion);
        editor.apply();
    }

    public static String getDatabaseVersion() {
        return prefs.getString(KEY.DATABASE_VERSION.toString(),"0");
    }

    public static void deleteToken(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY.token.toString(), null);
        editor.commit();
    }

    public static void setToken(String token){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY.token.toString(), token);
        editor.apply();
    }

    public static String getToken(){
        return prefs.getString(KEY.token.toString(),null);
    }

    public static void setGoogleMapType(String google_map_type) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SettingsManager.KEY.GOOGLE_MAP_TYPE.toString(), google_map_type);
        editor.apply();
    }

    public static String getGoogleMapType() {
        return prefs.getString(KEY.GOOGLE_MAP_TYPE.toString(),"NORMAL");
    }

    public static void setCustomDataLicense(String custom_data_license) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY.CUSTOM_DATA_LICENSE.toString(), custom_data_license);
        editor.apply();
    }

    public static String getCustomDataLicense() {
        return prefs.getString(KEY.CUSTOM_DATA_LICENSE.toString(),"0");
    }

    public static void setCustomImageLicense(String custom_image_license) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY.CUSTOM_IMAGE_LICENSE.toString(), custom_image_license);
        editor.apply();
    }

    public static String getCustomImageLicense() {
        return prefs.getString(KEY.CUSTOM_IMAGE_LICENSE.toString(),"0");
    }

    public static void setProjectName(String project_name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY.PROJECT_NAME.toString(), project_name);
        editor.apply();
    }

    public static String getProjectName() {
        return prefs.getString(KEY.PROJECT_NAME.toString(),null);
    }

    public static void setTaxaLastPageUpdated(String last_page) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY.TAXA_LAST_PAGE_UPDATED.toString(), last_page);
        editor.apply();
    }

    public static String getTaxaLastPageUpdated() {
        return prefs.getString(KEY.TAXA_LAST_PAGE_UPDATED.toString(),"1");
    }

}
