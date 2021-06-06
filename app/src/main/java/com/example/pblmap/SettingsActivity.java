package com.example.pblmap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedInstaceState) {

        super.onCreate(savedInstaceState);
        setContentView(R.layout.activity_settings);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(Color.rgb(35, 218, 250));
    }

    public void onBackClick(View view)
    {
        Intent intent = new Intent(view.getContext(), MenuActivity.class);
        startActivity(intent);
    }
}
