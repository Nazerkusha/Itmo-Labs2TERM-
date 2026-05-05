package com.example.interpolation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnManual = findViewById(R.id.btnManual);
        Button btnFile = findViewById(R.id.btnFile);
        Button btnFunction = findViewById(R.id.btnFunction);

        btnManual.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManualInputActivity.class);
            startActivity(intent);
        });

        btnFile.setOnClickListener(v -> {
            Intent intent = new Intent(this, FileInputActivity.class);
            startActivity(intent);
        });

        btnFunction.setOnClickListener(v -> {
            Intent intent = new Intent(this, FunctionInputActivity.class);
            startActivity(intent);
        });
    }
}
