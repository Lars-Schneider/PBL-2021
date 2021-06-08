package com.example.pblmap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedInstaceState) {

        super.onCreate(savedInstaceState);
        setContentView(R.layout.activity_about);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(Color.rgb(35, 218, 250));
        TextView t = findViewById(R.id.textView);
        t.setText(
                    "Welcome to TextGo! This is an exercise app for everyone." +
                            "Walk or run around to get near letters, then press the plus button to add the nearest to your current message." +
                            "Your goal is to spell the message indicated at the top of the screen. If you'd like to make the game easier, you can drag" +
                            "the slider to the left to make the letters closer together. If you'd like to make it harder, drag it to the right" +
                            "to spread them out. Enjoy!"

        );
        t.setTextSize(20f);
    }

    public void onBackClick(View view)
    {
        Intent intent = new Intent(view.getContext(), MenuActivity.class);
        startActivity(intent);
    }
}
