package com.edu.licenta.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.delta.activities.R;
import com.edu.licenta.utils.MuseumMap;
import com.edu.licenta.utils.Constants;

import butterknife.ButterKnife;

public class MapActivity extends Activity implements SensorEventListener {

    private MuseumMap museumMap;
    private SensorManager sensorManager = null;
    boolean mInitialized = false;
    double mLastX, mLastY, mLastZ;
    float alpha = 0.8f;
    float[] gravity = {0, 0, 0};
    int stepsCount = 0;
    float angle;

    @Override
    public void onCreate(Bundle savedInstanceState) throws SecurityException {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        registerSensorManagerListeners();
        Constants.firstEnter = true;
        museumMap = this.findViewById(R.id.SensorFusionView);

        //museumMap.setValues(Constants.userPositionX, Constants.userPositionY, (float)-Math.PI/2);

        ButterKnife.bind(this);
    }

    public void registerSensorManagerListeners() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerSensorManagerListeners();
        museumMap = this.findViewById(R.id.SensorFusionView);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x;
                float y;
                float z;

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                x = event.values[0] - gravity[0];
                y = event.values[1] - gravity[1];
                z = event.values[2] - gravity[2];

                if (!mInitialized) {
                    mLastX = x;
                    mLastY = y;
                    mLastZ = z;
                    mInitialized = true;
                } else {
                    double deltaX = Math.abs(mLastX - x);
                    double deltaY = Math.abs(mLastY - y);
                    double deltaZ = Math.abs(mLastZ - z);
                    if (deltaX < 0.8)
                        deltaX = (float) 0.0;
                    if (deltaY < 0.8)
                        deltaY = (float) 0.0;
                    if (deltaZ < 0.8)
                        deltaZ = (float) 0.0;
                    mLastX = x;
                    mLastY = y;
                    mLastZ = z;

                    if ((deltaZ > deltaX) && (deltaZ > deltaY)) {
                        stepsCount++;
                        if (stepsCount > 0) {
                            Constants.userPositionX += 20 * Math.sin(angle);
                            Constants.userPositionY -= 20 * Math.cos(angle);
                            museumMap.setValues(Constants.userPositionX, Constants.userPositionY, angle);
                        }
                    }
                }
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                float[] result = new float[3];
                calculateAngles(result, event.values.clone());
                angle = result[0];

                museumMap.setValues(Constants.userPositionX, Constants.userPositionY, angle);

                break;
        }
    }

    private float[] rMatrix = new float[9];

    public void calculateAngles(float[] result, float[] rVector) {
        SensorManager.getRotationMatrixFromVector(rMatrix, rVector);
        SensorManager.getOrientation(rMatrix, result);
    }
}