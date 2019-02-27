package org.biologer.biologer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;import android.util.Log;

import org.biologer.biologer.model.RetrofitClient;
import org.biologer.biologer.model.Stage;
import org.biologer.biologer.model.network.Stage6;
import org.biologer.biologer.model.network.TaksoniResponse;
import org.biologer.biologer.model.network.Taxa;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FetchTaxa extends Service {

    private static final String TAG = "Biologer.FetchTaxa";

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_CANCEL = "ACTION_CANCEL";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    private String stop_fetching = "no";
    private static FetchTaxa instance = null;

    private static int totalPages = 1;
    private static int currentPage = Integer.valueOf(SettingsManager.getTaxaLastPageUpdated());
    private static int progressStatus = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Running onCreate()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null)
        {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_START:
                        Log.i(TAG, "Action start selected, starting foreground service.");
                        // Clean previous data just in case
                        SettingsManager.setDatabaseVersion("0");
                        App.get().getDaoSession().getTaxonDao().deleteAll();
                        App.get().getDaoSession().getStageDao().deleteAll();
                        stop_fetching = "no";
                        currentPage = 1;
                        SettingsManager.setTaxaLastPageUpdated(String.valueOf(currentPage));
                        // Start the service
                        notificationInitiate();
                        break;
                    case ACTION_STOP:
                        Log.i(TAG, "Action stop selected, pausing foreground service.");
                        SettingsManager.setTaxaLastPageUpdated(String.valueOf(currentPage));
                        stop_fetching = "stop";
                        stopForeground(true);
                        break;
                    case ACTION_CANCEL:
                        // If paused we have to kill the Service, else we continue with the loop and the
                        // service will be killed after fetching the current page...
                        if(stop_fetching.equals("no")) {
                            Log.i(TAG, "Action cancel selected, killing the paused foreground service.");
                            stop_fetching = "cancel";
                            cleanDatabase();
                            stopForeground(true);
                            notificationUpdateText(getString(R.string.notify_title_taxa_canceled), getString(R.string.notify_desc_taxa_canceled));
                            stopSelf();
                        } else {
                            Log.i(TAG, "Action cancel selected, killing the running foreground service.");
                            stop_fetching = "cancel";
                            cleanDatabase();
                            stopForeground(true);
                        }
                        break;
                    case ACTION_RESUME:
                        Log.i(TAG, "Action resume selected, continuing the foreground service.");
                        stop_fetching = "no";
                        currentPage = Integer.valueOf(SettingsManager.getTaxaLastPageUpdated());
                        notificationInitiate();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "Running onDestroy(). Last page fetched was " + String.valueOf(currentPage) + " out of " + String.valueOf(totalPages) + " total pages.");
    }

    private void cleanDatabase() {
        SettingsManager.setDatabaseVersion("0");
        currentPage = 1;
        SettingsManager.setTaxaLastPageUpdated(String.valueOf(currentPage));
        App.get().getDaoSession().getTaxonDao().deleteAll();
        App.get().getDaoSession().getStageDao().deleteAll();
    }

    private void notificationInitiate() {
        // Start the fetching and display notification
        Log.i(TAG, "Service for fetching taxa started.");

        // Create initial notification to be set to Foreground
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "biologer_taxa")
                .setSmallIcon(R.drawable.ic_kornjaca)
                .setContentTitle(getString(R.string.notify_title_taxa))
                .setContentText(getString(R.string.notify_desc_taxa))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        Notification notification = mBuilder.build();
        startForeground(1, notification);

        // Get the last downloaded page from saved the preferences and continue downloading from this page.
        if (currentPage == 1) {
            fetchAll(currentPage);
            Log.d(TAG, "Continuing fetching taxa from the page 1");
        } else {
            fetchAll(currentPage + 1);
            Log.d(TAG, "Continuing fetching taxa from the page " + String.valueOf(currentPage +1));
        }
    }

    private void notificationUpdateProgress(int progressStatus) {
        // To do something if notification is taped, we must set up an intent
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Add Pause button intent in notification.
        Intent pauseIntent = new Intent(this, FetchTaxa.class);
        pauseIntent.setAction(ACTION_STOP);
        PendingIntent pendingPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
        NotificationCompat.Action pauseAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, getString(R.string.pause_action), pendingPauseIntent);

        // Add Cancel button intent in notification.
        Intent cancelIntent = new Intent(this, FetchTaxa.class);
        cancelIntent.setAction(ACTION_CANCEL);
        PendingIntent pendingCancelIntent = PendingIntent.getService(this, 0, cancelIntent, 0);
        NotificationCompat.Action cancelAction = new NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.cancel), pendingCancelIntent);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "biologer_taxa")
                .setSmallIcon(R.drawable.ic_kornjaca)
                .setContentTitle(getString(R.string.notify_title_taxa))
                .setContentText(getString(R.string.notify_desc_taxa))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100, progressStatus, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .addAction(pauseAction)
                .addAction(cancelAction);

        Notification notification = mBuilder.build();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }

    private void notificationResumeFetchButton(int progressStatus) {
        // To do something if notification is taped, we must set up an intent
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Add Resume button intent in notification.
        Intent resumeIntent = new Intent(this, FetchTaxa.class);
        resumeIntent.setAction(ACTION_RESUME);
        PendingIntent pendingResumeIntent = PendingIntent.getService(this, 0, resumeIntent, 0);
        NotificationCompat.Action resumeAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, getString(R.string.resume_action), pendingResumeIntent);


        // Add Cancel button intent in notification.
        Intent cancelIntent = new Intent(this, FetchTaxa.class);
        cancelIntent.setAction(ACTION_CANCEL);
        PendingIntent pendingCancelIntent = PendingIntent.getService(this, 0, cancelIntent, 0);
        NotificationCompat.Action cancelAction = new NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.cancel), pendingCancelIntent);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "biologer_taxa")
                .setSmallIcon(R.drawable.ic_kornjaca)
                .setContentTitle(getString(R.string.notify_title_taxa))
                .setContentText(getString(R.string.notify_desc_taxa))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100, progressStatus, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .addAction(resumeAction)
                .addAction(cancelAction);

        Notification notification = mBuilder.build();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }

    private void notificationUpdateText(String title, String description) {
        // To do something if notification is taped, we must set up an intent
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "biologer_taxa")
                    .setSmallIcon(R.drawable.ic_kornjaca)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(false)
                    .setContentIntent(pendingIntent)
                    .setOnlyAlertOnce(false)
                    .setAutoCancel(true);

            Notification notification = mBuilder.build();

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notification);
    }

    public void fetchAll(final int page) {
        if (page > totalPages) {
            return;
        }

        Call<TaksoniResponse> call = RetrofitClient.getService(SettingsManager.getDatabaseName()).getTaxons(page, 40);

        call.enqueue(new CallbackWithRetry<TaksoniResponse>(call) {
            @Override
            public void onResponse(Call<TaksoniResponse> call, Response<TaksoniResponse> response) {
                // If user selected pause or cancel we will stop the script
                if (stop_fetching.equals("stop")) {
                    Log.d(TAG, "Fetching of taxa data is paused by the user!");
                    notificationResumeFetchButton(progressStatus);
                    stopSelf();
                }
                if (stop_fetching.equals("cancel")) {
                    Log.d(TAG, "Fetching of taxa data is canceled by the user!");
                    notificationUpdateText(getString(R.string.notify_title_taxa_canceled), getString(R.string.notify_desc_taxa_canceled));
                    stopSelf();
                }
                // Else fetch the next page of data
                if (stop_fetching.equals("no")) {
                    if (1 == page) {
                        App.get().getDaoSession().getStageDao().deleteAll();
                        totalPages = response.body().getMeta().getLastPage();
                    }

                    List<Taxa> taxa = response.body().getData();

                    // Variables used to update the Progress Bar status
                    progressStatus = (page * 100 / totalPages);
                    currentPage = page;
                    notificationUpdateProgress(progressStatus);

                    for (Taxa taxon : taxa) {
                        App.get().getDaoSession().getTaxonDao().insertOrReplace(taxon.toTaxon());

                        List<Stage6> stages = taxon.getStages();

                        for (Stage6 stage : stages) {
                            App.get().getDaoSession().getStageDao().insert(new Stage(null, stage.getName(), stage.getId(), taxon.getId()));
                        }
                    }

                    Log.i(TAG, "Fetching page No. " + String.valueOf(page) + " of total " + String.valueOf(totalPages) + " pages");

                    // If we just finished fetching taxa data for the last page, we can stop showing
                    // loader. Otherwise we continue fetching taxa from the API on the next page.
                    if (isLastPage(page)) {
                        // Inform the user of success
                        Log.i(TAG, "All taxa were successfully updated from the server!");
                        stopForeground(true);
                        notificationUpdateText(getString(R.string.notify_title_taxa_updated), getString(R.string.notify_desc_taxa_updated));
                        // Set the preference to know when the taxonomic data was updates
                        SettingsManager.setDatabaseVersion(Long.toString(response.body().getMeta().getLastUpdatedAt()));
                        stopSelf();
                    } else {
                        fetchAll(page + 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<TaksoniResponse> call, Throwable t) {
                // Remove partially retrieved data from the database
                App.get().getDaoSession().getTaxonDao().deleteAll();
                App.get().getDaoSession().getStageDao().deleteAll();
                SettingsManager.setDatabaseVersion("0");
                // Inform the user on failure and write log message
                //Toast.makeText(getActivity(), getString(R.string.database_connect_error), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Application could not get data from a server!");
                notificationUpdateText(getString(R.string.notify_title_taxa_failed), getString(R.string.notify_desc_taxa_failed));
            }
        });
    }

    private static boolean isLastPage(int page) {
        return page == totalPages;
    }

    public static int getProgressStatus() {
        return progressStatus;
    }

    // to check if the service is still running
    public static boolean isInstanceCreated() {
        return instance != null;
    }
}