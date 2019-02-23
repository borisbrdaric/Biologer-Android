package org.biologer.biologer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

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

    private static int totalPages = 1;
    private static int currentPage = Integer.valueOf(SettingsManager.getTaxaLastPageUpdated());
    private static int progressStatus = 0;

    @Override
    public void onCreate() {
        this.startForeground();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        if (progressStatus == 100) {
            updateNotificationText(getString(R.string.notify_title_taxa_updated), getString(R.string.notify_desc_taxa_updated));
        } else {
            SettingsManager.setTaxaLastPageUpdated(String.valueOf(currentPage));
            updateNotificationText(getString(R.string.notify_title_taxa_partially_updated), getString(R.string.notify_desc_taxa_partially_updated));
            stopForeground(true);
            stopSelf();
        }
        super.onDestroy();
    }

    private void startForeground() {
        // Start the fetching and display notification
        Log.i(TAG, "Service for fetching taxa is started");
        startForeground(1, initialiseNotification(getString(R.string.notify_title_taxa), getString(R.string.notify_desc_taxa)));
        // Get the last downloaded page from saved the preferences and continue downloading from this page.
        fetchAll(currentPage);
    }

    // We will initialise notification with Foreground priority
    private Notification initialiseNotification(String title, String description){
        // To do something if notification is taped, we must set up an intent
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "biologer_taxa")
                .setSmallIcon(R.drawable.ic_kornjaca)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        return mBuilder.build();
    }

    private void updateNotificationBar(int progressStatus) {
        // To do something if notification is taped, we must set up an intent
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent retry = new Intent(this, FetchTaxa.class);
        PendingIntent retryPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "biologer_taxa")
                .setSmallIcon(R.drawable.ic_kornjaca)
                .setContentTitle(getString(R.string.notify_title_taxa))
                .setContentText(getString(R.string.notify_desc_taxa))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progressStatus, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        Notification notification = mBuilder.build();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }

    private void updateNotificationText(String title, String description) {
        // To do something if notification is taped, we must set up an intent
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "biologer_taxa")
                    .setSmallIcon(R.drawable.ic_kornjaca)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                    .setOngoing(false)
                    .setContentIntent(pendingIntent)
                    .setOnlyAlertOnce(false)
                    .setAutoCancel(true);

            Notification notification = mBuilder.build();

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notification);
        } else {
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
    }

    public void fetchAll(final int page) {
        if (page > totalPages) {
            return;
        }

        Call<TaksoniResponse> call = RetrofitClient.getService(SettingsManager.getDatabaseName()).getTaxons(page, 30);

        call.enqueue(new CallbackWithRetry<TaksoniResponse>(call) {
            @Override
            public void onResponse(Call<TaksoniResponse> call, Response<TaksoniResponse> response) {
                if (1 == page) {
                    App.get().getDaoSession().getStageDao().deleteAll();
                    totalPages = response.body().getMeta().getLastPage();
                }

                List<Taxa> taxa = response.body().getData();

                // Variables used to update the Progress Bar status
                progressStatus = (page * 100 / totalPages);
                currentPage = page;
                updateNotificationBar(progressStatus);

                for (Taxa taxon : taxa) {
                    App.get().getDaoSession().getTaxonDao().insertOrReplace(taxon.toTaxon());

                    List<Stage6> stages = taxon.getStages();

                    for (Stage6 stage : stages) {
                        App.get().getDaoSession().getStageDao().insert(new Stage(null, stage.getName(), stage.getId(), taxon.getId()));
                    }
                }

                // If we just finished fetching taxa data for the last page, we can stop showing
                // loader. Otherwise we continue fetching taxa from the API on the next page.
                if (isLastPage(page)) {
                    // Inform the user of success
                    Log.i(TAG, "All taxa were successfully updated from the server!");
                    stopForeground(true);
                    stopSelf();
                } else {
                    fetchAll(page + 1);
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
                updateNotificationText(getString(R.string.notify_title_taxa_failed), getString(R.string.notify_desc_taxa_failed));
            }
        });
    }

    private static boolean isLastPage(int page) {
        return page == totalPages;
    }

    public static int getProgressStatus() {
        return progressStatus;
    }
}