package com.cdap.safetyapp.viewmodels;

import android.app.Application;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.collect.EvictingQueue;

import org.joda.time.Duration;
import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.content.Context.SENSOR_SERVICE;

public class MainViewModel extends AndroidViewModel {

    private final double RUNNING_THRESHOLD = 900;
    private final int QUEUE_SIZE = 6;  // Should be a even number.

    private double MagnitudePrevious;

    private MutableLiveData<Integer> stepCount;

    private MutableLiveData<Queue<Instant>> stepQueue;

    private MutableLiveData<Double> averageLiveData;

    private MutableLiveData<Boolean> isRunning;

    private Disposable disposable;

    public MainViewModel(@NonNull Application application) {
        super(application);

        MagnitudePrevious = 6;

        stepCount = new MutableLiveData<>(0);

        stepQueue = new MutableLiveData<>(EvictingQueue.create(QUEUE_SIZE));

        averageLiveData = new MutableLiveData<>();

        isRunning = new MutableLiveData<>();

        initSensor(application);

        calculateResult();
    }

    private void calculateResult() {
        disposable = Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    Queue<Instant> instants = stepQueue.getValue();

                    if (instants.peek() != null) {
                        Timber.e("Timestamp : " + instants.peek().toDateTime().toString() + " Queue Size : " + String.valueOf(instants.size()));

                        if (instants.size() >= QUEUE_SIZE) {
                            List<Instant> instantList = new ArrayList<>(instants);

                            long sum = 0;

                            for (int i = 0; i < instantList.size() - 1; i++) {
                                Duration duration = new Duration(instantList.get(i), instantList.get(i + 1));

                                sum += duration.getMillis();
                            }

                            sum += new Duration(instantList.get(instantList.size() - 1), Instant.now()).getMillis();

                            double average = (double) sum / instantList.size() - 1;

                            Timber.e("avg :%s", average);

                            averageLiveData.postValue(average);

                            if (average <= RUNNING_THRESHOLD) {
                                isRunning.postValue(true);
                            } else {
                                isRunning.postValue(false);
                            }
                        }
                    }
                });
    }

    private void initSensor(@NonNull Application application) {
        SensorManager sensorManager = (SensorManager) application.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener stepDetector = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent != null) {
                    float x_acceleration = sensorEvent.values[0];
                    float y_acceleration = sensorEvent.values[1];
                    float z_acceleration = sensorEvent.values[2];

                    double Magnitude = Math.sqrt(x_acceleration * x_acceleration + y_acceleration * y_acceleration + z_acceleration * z_acceleration);
                    double MagnitudeDelta = Magnitude - MagnitudePrevious;
                    MagnitudePrevious = Magnitude;

                    if (MagnitudeDelta > 6) {
                        int count = stepCount.getValue();
                        stepCount.postValue(++count);

                        Timber.e("Step Count : %s", String.valueOf(count));

                        Queue<Instant> queue = stepQueue.getValue();

                        queue.add(Instant.now());

                        stepQueue.postValue(queue);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public LiveData<Integer> getStepCount() {
        return stepCount;
    }

    public LiveData<Queue<Instant>> getStepQueue() {
        return stepQueue;
    }

    public LiveData<Double> getAverageLiveData() {
        return averageLiveData;
    }

    public LiveData<Boolean> getIsRunning() {
        return isRunning;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();

        }
    }
}

