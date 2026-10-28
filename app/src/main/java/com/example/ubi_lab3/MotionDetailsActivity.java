
package com.example.ubi_lab3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MotionDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_details);

        TextView detailsText =
                findViewById(R.id.motionDetailsText);

        Button backButton =
                findViewById(R.id.backButton);

        String motionTime = getIntent().getStringExtra("motionTime");

        if (motionTime != null) {
            detailsText.setText(
                    "Phone movement detected!\n\n" +
                            "Time: " + motionTime
            );
        } else {
            detailsText.setText("No motion details available.");
        }

        backButton.setOnClickListener(v -> finish());
    }
}
