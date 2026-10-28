
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;



public class MotionService extends Service implements SensorEventListener {

    private static final String TAG = "MotionService";
    private static final String CHANNEL_ID = "motion_alerts";
    private int notificationId = 1;

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
        createNotificationChannel();

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

            String motionTime = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm:ss",
                    Locale.getDefault()
            ).format(new Date(currentTime));

            Log.d(TAG, "MOTION DETECTED!");
            Log.d(TAG, "Time: " + motionTime);
            Log.d(TAG, "Movement strength: " + change);
            sendMotionNotification(motionTime);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Motion Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Notifications when movement is detected");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    private void sendMotionNotification(String motionTime) {

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        Notification notification = builder
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Motion Detected!")
                .setContentText("Movement detected at " + motionTime)
                .setAutoCancel(true)
                .build();

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(notificationId++, notification);

        Log.d(TAG, "Notification sent: " + motionTime);
    }

}
