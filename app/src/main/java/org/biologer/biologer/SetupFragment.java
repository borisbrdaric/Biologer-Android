package org.biologer.biologer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ProgressBar;

public class SetupFragment extends Fragment {

    private ProgressBar progressBar;
    private int oldProgress = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_setup, container, false);

        progressBar = root.findViewById(R.id.progress_bar_taxa);

        Button btn = root.findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);

                Thread updateStatusBar = new Thread() {

                    @Override
                    public void run() {
                        try {
                            sleep(1000);
                            while (progressBar.getProgress() < 100) {
                                int progress_value = FetchTaxa.getProgressStatus();
                                if (progress_value != oldProgress) {
                                    oldProgress = progress_value;
                                    progressBar.setProgress(progress_value);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                };

                updateStatusBar.start();
                FetchTaxa.fetchAll(1);

            }
        });

        return root;
    }
}
