package com.example.pblmap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(Color.rgb(35, 218, 250));

    }

    public void onPlayClick(View view)
    {
        Intent intent = new Intent(view.getContext(), MapsActivity.class);
        startActivity(intent);
    }

    public void onSettingsClick(View view)
    {
        Intent intent = new Intent(view.getContext(), AboutActivity.class);
        startActivity(intent);
    }
}
