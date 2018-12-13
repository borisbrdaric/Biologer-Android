package org.biologer.biologer;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.biologer.biologer.model.Stage;
import org.biologer.biologer.model.network.Stage6;
import org.biologer.biologer.model.network.TaksoniResponse;
import org.biologer.biologer.model.network.Taxa;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetupFragment extends Fragment {
    private int totalPages = 1;

    private FrameLayout pbLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setup, container, false);
        pbLoading = root.findViewById(R.id.pbLoading);

        Button btn = root.findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbLoading.setVisibility(View.VISIBLE);

                totalPages = 1;
                fetchTaxa(1);
            }
        });


        return root;
    }

    public void fetchTaxa(final int page) {
        if (page > totalPages) {
            hideLoader();
            return;
        }

        Call<TaksoniResponse> call = App.get().getService().getTaxons(page, 300);

        call.enqueue(new Callback<TaksoniResponse>() {
            @Override
            public void onResponse(Call<TaksoniResponse> call, Response<TaksoniResponse> response) {
                if (1 == page) {
                    App.get().getDaoSession().getStageDao().deleteAll();
                    totalPages = response.body().getMeta().getLastPage();
                }

                List<Taxa> taxa = response.body().getData();

                // Log.w("Izlaz za logcat",new Gson().toJson(response));
                // Log.w("Izlaz za logcat",new GsonBuilder().setPrettyPrinting().create().toJson(response.body()));
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
                    hideLoader();
                } else {
                    fetchTaxa(page + 1);
                }
            }

            @Override
            public void onFailure(Call<TaksoniResponse> call, Throwable t) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getString(R.string.database_connect_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideLoader() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private boolean isLastPage(int page) {
        return page == this.totalPages;
    }

}
