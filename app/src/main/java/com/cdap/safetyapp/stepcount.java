package com.cdap.safetyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.TextView;

import com.cdap.safetyapp.viewmodels.MainViewModel;

import net.danlew.android.joda.BuildConfig;
import net.danlew.android.joda.JodaTimeAndroid;

import timber.log.Timber;

public class stepcount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        JodaTimeAndroid.init(this);

        TextView textViewCount = findViewById(R.id.count);
        TextView textViewAvg = findViewById(R.id.avg);
        TextView textViewStatus = findViewById(R.id.status);

        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.getStepCount().observe(this, count -> {
            textViewCount.setText(String.valueOf(count));
        });

        mainViewModel.getAverageLiveData().observe(this, aDouble -> {
            textViewAvg.setText(String.valueOf(aDouble));
        });

        mainViewModel.getIsRunning().observe(this, aBoolean -> {
            if (aBoolean) {
                textViewStatus.setText("Running");
            } else {
                textViewStatus.setText("Not Running");
            }
        });
    }
}