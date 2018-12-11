package org.biologer.biologer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.biologer.biologer.model.Stage;
import org.biologer.biologer.model.Taxon;
import org.biologer.biologer.model.UserData;
import org.biologer.biologer.model.network.TaksoniResponse;
import org.biologer.biologer.model.network.Taxa;
import org.biologer.biologer.model.network.UserDataResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetupFragment extends Fragment {
    private long c = 0;

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
                Call<TaksoniResponse> call = App.get().getService().getTaxons();
                call.enqueue(new Callback<TaksoniResponse>() {
                    @Override
                    public void onResponse(Call<TaksoniResponse> call, Response<TaksoniResponse> response) {
                        //  App.get().getDaoSession().getTaxonDao()
                        //Toast.makeText(getActivity(), "Updating..." + response.body().getData().size(), Toast.LENGTH_SHORT).show();
                        App.get().getDaoSession().getStageDao().deleteAll();
                        for (int i = 0; i < response.body().getData().size(); i++) {
                            Taxa taxa = response.body().getData().get(i);
                            App.get().getDaoSession().getTaxonDao().insertOrReplace(taxa.toTaxon());

                            for (int j = 0; j < taxa.getStages().size(); j++) {
                                App.get().getDaoSession().getStageDao().insert(new Stage(null, taxa.getStages().get(j).getName(), taxa.getStages().get(j).getId(), taxa.getId()));
                                c++;
                            }
                        }
                        c = 0;
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                pbLoading.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), getString(R.string.database_updated), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<TaksoniResponse> call, Throwable t) {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), getString(R.string.database_connect_error), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });


        return root;
    }


}
