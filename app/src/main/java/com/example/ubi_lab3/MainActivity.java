
package com.example.ubi_lab3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button startButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MotionService.class);
            startService(intent);
            statusText.setText("Motion detection started");
        });

        stopButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MotionService.class);
            stopService(intent);
            statusText.setText("Motion detection stopped");
        });
    }
}
