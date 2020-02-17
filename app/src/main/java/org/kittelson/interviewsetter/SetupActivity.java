package org.kittelson.interviewsetter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Button skipButton = findViewById(R.id.skip_setup);
        skipButton.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class));
        });
    }
}
