package org.biologer.biologer;

import android.app.Activity;
import android.util.Log;

import org.biologer.biologer.model.Stage;
import org.biologer.biologer.model.network.Stage6;
import org.biologer.biologer.model.network.TaksoniResponse;
import org.biologer.biologer.model.network.Taxa;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public abstract class FetchTaxa extends Activity {

    private static int totalPages = 1;
    private static int progressStatus = 0;

    public static void fetchAll(final int page) {
        if (page > totalPages) {
            return;
        }

        Call<TaksoniResponse> call = App.get().getService().getTaxons(page, 50);

        call.enqueue(new CallbackWithRetry<TaksoniResponse>(call) {
            @Override
            public void onResponse(Call<TaksoniResponse> call, Response<TaksoniResponse> response) {
                if (1 == page) {
                    App.get().getDaoSession().getStageDao().deleteAll();
                    totalPages = response.body().getMeta().getLastPage();
                }

                List<Taxa> taxa = response.body().getData();

                int lastPage = response.body().getMeta().getLastPage();
                int currentPage = response.body().getMeta().getCurrentPage();

                // Variables used to update the Progress Bar status
                progressStatus = (page * 100 / totalPages);

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
                    //Toast.makeText(getActivity(), getString(R.string.database_updated), Toast.LENGTH_LONG).show();
                    Log.i("Taxa database: ", "All taxa were successfully updated from the server!");
                } else {
                    fetchAll(page + 1);
                }
            }

            @Override
            public void onFailure(Call<TaksoniResponse> call, Throwable t) {
                // Remove partially retrieved data from the database
                App.get().getDaoSession().getStageDao().deleteAll();
                SettingsManager.setDatabaseVersion("0");
                // Inform the user on failure and write log message
                //Toast.makeText(getActivity(), getString(R.string.database_connect_error), Toast.LENGTH_LONG).show();
                Log.e("Taxa database: ", "Application could not get data from a server!");
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