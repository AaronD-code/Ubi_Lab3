
package com.example.ubi_lab3;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class MotionService extends Service implements SensorEventListener {

    private static final String TAG = "MotionService";
    private static final float THRESHOLD = 2.5f;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private HandlerThread sensorThread;

    private float lastX, lastY, lastZ;
    private boolean firstReading = true;
    private long lastMotionTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        sensorThread = new HandlerThread("MotionSensorThread");
        sensorThread.start();

        Log.d(TAG, "Motion Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    new Handler(sensorThread.getLooper())
            );

            Log.d(TAG, "Accelerometer monitoring started");
        } else {
            Log.e(TAG, "Accelerometer not available");
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (firstReading) {
            lastX = x;
            lastY = y;
            lastZ = z;
            firstReading = false;
            return;
        }

        float change = (float) Math.sqrt(
                Math.pow(x - lastX, 2) +
                        Math.pow(y - lastY, 2) +
                        Math.pow(z - lastZ, 2)
        );

        long currentTime = System.currentTimeMillis();

        if (change > THRESHOLD &&
                currentTime - lastMotionTime > 3000) {

            lastMotionTime = currentTime;

            Log.d(TAG, "MOTION DETECTED! Change: " + change);
        }

        lastX = x;
        lastY = y;
        lastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        sensorThread.quitSafely();

        Log.d(TAG, "Motion Service stopped");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
