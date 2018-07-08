package com.edu.licenta.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.delta.activities.R;
import com.edu.licenta.utils.BubbleLevelCompass;
import com.edu.licenta.utils.Constants;
import com.hoan.dsensor_master.DSensorEvent;
import com.hoan.dsensor_master.DSensorManager;
import com.hoan.dsensor_master.interfaces.DProcessedEventListener;
import com.hoan.dsensor_master.interfaces.DSensorEventListener;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapActivity extends Activity implements SensorEventListener, DProcessedEventListener, LocationListener {

    private BubbleLevelCompass bubbleLevelCompass;
    private SensorManager sensorManager = null;
    boolean mInitialized = false;
    double mLastX, mLastY, mLastZ;
    float posX = 540;
    float posY = 880;
    float azimuthValue;
    float alpha = 0.8f;
    float[] gravity = {0, 0, 0};
    int stepsCount = 0;
    float angle;
    float testAngle = 0;

    float lastAngle = 0;
    int filterIterations = 5;
    float angleValues;

    @Override
    public void onCreate(Bundle savedInstanceState)  throws SecurityException{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        registerSensorManagerListeners();
        Constants.firstEnter = true;
        bubbleLevelCompass = this.findViewById(R.id.SensorFusionView);

        Constants.userPositionX = 0;
        Constants.userPositionY = 0;
        /*Constants.lastX = posX;
        Constants.lastY = posY;*/

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);

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
        bubbleLevelCompass = this.findViewById(R.id.SensorFusionView);
        DSensorManager.startDProcessedSensor(this, 1, this);
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
                            bubbleLevelCompass.setValues(Constants.userPositionX, Constants.userPositionY, angle);
                        }
                    }
                }
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                float[] result = new float[3];
                calculateAngles(result, event.values.clone());
                angle = result[0];

                bubbleLevelCompass.setValues(Constants.userPositionX, Constants.userPositionY, angle);

                break;
        }
    }

    private float[] rMatrix = new float[9];

    public void calculateAngles(float[] result, float[] rVector) {
        SensorManager.getRotationMatrixFromVector(rMatrix, rVector);
        SensorManager.getOrientation(rMatrix, result);
    }

    @Override
    public void onProcessedValueChanged(DSensorEvent dSensorEvent) {

        if (Float.isNaN(dSensorEvent.values[0])) {
            System.out.println("Device is not flat no compass value");
        } else {
            azimuthValue = (float) Math.toDegrees(dSensorEvent.values[0]);
            if (azimuthValue < 0) {
                azimuthValue = (azimuthValue + 360) % 360;
            }
            //bubbleLevelCompass.setValues(Constants.userPositionX, Constants.userPositionY, azimuthValue);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("LAT: " + location.getLatitude());
        System.out.println("LONG: " + location.getLongitude());
        System.out.println("BEARING: " + location.getBearing());
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}