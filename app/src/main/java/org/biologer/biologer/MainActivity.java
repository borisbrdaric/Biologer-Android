package org.biologer.biologer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn_unos = (Button) findViewById(R.id.button);
        Button btn_landing = (Button) findViewById(R.id.landing);
        TextView tw = (TextView) findViewById(R.id.textView9);

        String token = SettingsManager.getToken();

        if (token != null) {
            tw.setText(token);
        }
        else {
            tw.setText("Nema tokena");
        }

        btn_unos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EntryActivity.class);
                startActivity(intent);
            }
        });

        btn_landing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                startActivity(intent);
            }
        });
    }
}
