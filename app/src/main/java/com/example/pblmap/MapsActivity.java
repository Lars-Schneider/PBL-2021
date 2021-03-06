package com.example.pblmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;


public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int WHITE = 0xBBDDDDDD;
    private static final int YELLOW = 0xDDDDDD00;

    //GPS & Map variables
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation = new Location("");
    FusedLocationProviderClient mFusedLocationClient;

    MyViewModel mModel;
    boolean mIsMapShowingMarkers = false;

    //MediaPlayer mSoundtrack;

    SeekBar mSeekBar;

    //This gets called every time the GPS location refreshes
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            mLastLocation = locationResult.getLastLocation();

            if (mModel.isEmpty()) {
                mModel.setMarkers(generateMarkers(mLastLocation));
                mIsMapShowingMarkers = true;
                LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.0f));
            } else if (!mIsMapShowingMarkers) {
                ArrayList<Marker> markers = mModel.getMarkers();
                for (int i = 0; i < markers.size(); i++) {
                    Marker oldMarker = markers.get(i);
                    String oldTitle = oldMarker.getTitle();
                    Marker newMarker = mGoogleMap.addMarker(
                            new MarkerOptions().position(oldMarker.getPosition()).title(oldTitle).icon(makeTextIcon(oldTitle, WHITE)));
                    markers.set(i, newMarker);
                }
                mIsMapShowingMarkers = true;
            }
            Log.i("MapsActivity", "Location: " + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());

            Marker newNearest = findClosestMarker(mModel.getMarkers(), mLastLocation);
            if (newNearest != mModel.getNearestMarker() && mModel.getMarkers().contains(mModel.getNearestMarker())) {
                Marker oldNearest = mModel.getNearestMarker();
                System.out.println(oldNearest.getTitle());
                oldNearest.setIcon(makeTextIcon(oldNearest.getTitle(), WHITE));
            }
            newNearest.setIcon(makeTextIcon(newNearest.getTitle(), YELLOW));
            mModel.setNearestMarker(newNearest);
        }
    };

    float map(float x, float in_min, float in_max, float out_min, float out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mModel = ViewModelProviders.of(this).get(MyViewModel.class);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFrag != null;
        mapFrag.getMapAsync(this);

        TextView tv1 = findViewById(R.id.message);
        tv1.setText(mModel.getMessage());

        String goal = mModel.getGoal();

        if (goal.equals("")) {
            goal = getRandomWord(mModel.getDifficulty());
            mModel.setGoal(goal);
        }
        tv1 = findViewById(R.id.label);
        tv1.setText("Try to spell: " + goal);


        if (mModel.getReshuffleCountdown() != 0) {
            startReshuffleTimer(mModel.getReshuffleCountdown());
        }

        /*
        //POP UP WINDOW
        if (mModel.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Welcome!")
                    .setMessage(("This is TextGo, an exercise app for everyone. Try to spell the goal word by running to each letter. If you can't reach a letter, you can reshuffle them. Have Fun!"))
                    .setNegativeButton("Begin", null)
                    .show();
        }
        */
        /*
        mSoundtrack = MediaPlayer.create(MapsActivity.this, R.raw.Soundtrack);
        mSoundtrack.setLooping(true);
        mSoundtrack.start();
        */

        mModel.setScale(1);
        mSeekBar = (SeekBar) findViewById(R.id.scale);
        mSeekBar.setProgress(50);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                float percent = (progress + 50f) / 100f;
                mModel.setScale(map(percent, 0.5f, 1.5f, 0.25f, 1.75f));

                for (Marker marker : mModel.getMarkers()) {
                    marker.remove();
                }
                mModel.getMarkers().clear();
                mModel.setMarkers(generateMarkersNotShuffled(mLastLocation));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000); // sets GPS refresh interval to 10 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This game requires location tracking. Accept to play.")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mGoogleMap.setMyLocationEnabled(true);
                }

            } else {

                // permission denied, shut down app.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Denied")
                        .setMessage("Closing app, since this game can't function without your location permission.")
                        .setPositiveButton("Ok", (dialogInterface,i) -> {
                            finish();
                            System.exit(0);
                        })
                        .show();

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    //Makes a letter icon with the inputted text
    private BitmapDescriptor makeTextIcon(String text, int color) {

        Paint textPaint = new Paint();
        textPaint.setTextSize(60);
        textPaint.setColor(Color.BLACK);

        int width = (int) textPaint.measureText(text);
        int height = (int) textPaint.getTextSize();

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        canvas.translate(0, height - 10);
        canvas.drawColor(color);

        canvas.drawText(text, 0, 0, textPaint);
        return BitmapDescriptorFactory.fromBitmap(image);
    }

    //Returns the nearest marker to inputted location
    private Marker findClosestMarker(ArrayList<Marker> markers, Location location) {
        LatLng locLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        double closest = 1000000000;
        Marker closestMarker = null;

        for (Marker marker : markers) {
            double distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), locLatLng);
            if (distance < closest) {
                closest = distance;
                closestMarker = marker;

            }
        }
        return closestMarker;
    }

    //Randomly generates all letters of alphabet around the inputted location
    private ArrayList<Marker> generateMarkers(Location center) {
        double x = center.getLatitude();
        double y = center.getLongitude();
        ArrayList<Marker> markers = new ArrayList<>();
        String[] alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G",
                "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
                "T", "U", "V", "W", "X", "Y", "Z", ",", ".", "?", "!"};


        double spacing = 0.0004;
        int width = 6;

        //for (int a = 0; a < 2; a++) {
        //    for (int b = 0; b < 2; b++) {

        Collections.shuffle(Arrays.asList(alphabet));
        mModel.setLetterArrangement((alphabet));
        for (int i = 0; i < alphabet.length; i++) {

            double markerX = x - ((spacing * mModel.getScale()) * width / 2.0) + (i % width) * (spacing * mModel.getScale());
            double markerY = y - ((spacing * mModel.getScale()) * width / 2.0) + (i / width) * (spacing * mModel.getScale());
            String letter = alphabet[i];

            LatLng position = new LatLng(markerX, markerY);
            Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(position).title(letter).icon(makeTextIcon(letter, WHITE)));
            markers.add(marker);


        }
        return markers;
    }

    private ArrayList<Marker> generateMarkersNotShuffled(Location center) {
        double x = center.getLatitude();
        double y = center.getLongitude();
        ArrayList<Marker> markers = new ArrayList<>();
        String[] alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G",
                "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
                "T", "U", "V", "W", "X", "Y", "Z", ",", ".", "?", "!"};


        double spacing = 0.0004;
        int width = 6;

        for (int i = 0; i < alphabet.length; i++) {

            double markerX = x - ((spacing * mModel.getScale()) * width / 2.0) + (i % width) * (spacing * mModel.getScale());
            double markerY = y - ((spacing * mModel.getScale()) * width / 2.0) + (i / width) * (spacing * mModel.getScale());
            String letter = mModel.getLetterArrangement()[i];

            LatLng position = new LatLng(markerX, markerY);
            Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(position).title(letter).icon(makeTextIcon(letter, WHITE)));
            markers.add(marker);
        }
        return markers;
    }

    public void startReshuffleTimer(long millis) {
        View view = findViewById(R.id.reshuffle);
        view.setClickable(false);
        new CountDownTimer(millis, 1000) {
            final TextView tv1 = (TextView) view;

            public void onTick(long millisUntilFinished) {
                mModel.setReshuffleCountdown(millisUntilFinished);
                tv1.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mModel.setReshuffleCountdown(0);
                tv1.setText("Reshuffle");
                view.setClickable(true);
            }

        }.start();

    }

    public String getRandomWord(int difficulty) {
        String[] words;
        System.out.println("Difficulty " + difficulty);
        if(difficulty == 1)
        {
            words = new String[]
                    {
                            "I", "A"
                    };
        }
        else if(difficulty == 2)
        {
            words = new String[]
                    {
                            "To", "Go", "Hi", "If", "In", "Of", "Am", "An", "Do", "He"
                    };
        }
        else if(difficulty == 3)
        {
            words = new String[]
                    {
                            "Hey", "The", "She", "Boy", "Toy", "You", "Got", "Hot", "Not", "Moo", "Too", "Tree", "Run"
                    };
        }
        else if(difficulty == 4)
        {
            words = new String[]
                    {
                            "They", "Girl", "This", "Born", "Torn", "Lamp", "Pass", "Ball"
                    };
        }
        else if(difficulty == 5)
        {
            words = new String[]
                    {
                            "Peace", "Peach", "Power", "Sieve", "Apple"
                    };
        }
        else if(difficulty == 6) {
            words = new String[]
                    {
                            "Health", "Sheath", "Wisdom", "Friend"
                    };
        }
        else
        {
            words = new String[]{
                    "Courage", "Exercise", "Running", "Explore", "Friends", "Community",
                    "Achievement", "Freedom", "Wisdom", "Health", "Fitness", "Endurance",
                    "Longevity", "Serenity", "Peace", "Power", "Happiness",
                    "Kindness", "Openness"};
        }

        int rnd = new Random().nextInt(words.length);
        return words[rnd];
    }


    //////////////////////
    // BUTTON FUNCTIONS //
    //////////////////////

    public void onAddLetterPressed(View view) {
        //If there's a nearestMarker, adds its title.
        if (mModel.getNearestMarker() != null) {
            String message = mModel.getMessage();
            message += mModel.getNearestMarker().getTitle();
            mModel.setMessage(message);
            TextView tv1 = findViewById(R.id.message);
            tv1.setText(message);

            if (message.toLowerCase().equals(mModel.getGoal().toLowerCase())) {
                new AlertDialog.Builder(this)
                        .setTitle("Bravo!")
                        .setMessage(("You spelled today's goal word, " + mModel.getGoal() + ", and got a lot of exercise! " +
                                "Great job, and keep it up!"))
                        .setNegativeButton("Ok", null)
                        .show();
                mModel.setDifficulty((mModel.getDifficulty() + 1));
                String goal = getRandomWord(mModel.getDifficulty());
                mModel.setGoal(goal);
                tv1 = findViewById(R.id.label);
                tv1.setText("Try to spell: " + goal);
                tv1 = findViewById(R.id.message);
                tv1.setText("");

            }
        }
    }

    public void onReshufflePressed(View view) {

        for (Marker marker : mModel.getMarkers()) {
            marker.remove();
        }
        mModel.getMarkers().clear();
        mModel.setMarkers(generateMarkers(mLastLocation));

        startReshuffleTimer(1);

    }

    public void onDelete(View view) {
        //If message isn't empty, removes its last character
        String message = mModel.getMessage();
        if (message.length() > 0) {
            message = message.substring(0, message.length() - 1);
            TextView tv1 = findViewById(R.id.message);
            tv1.setText(message);
            mModel.setMessage(message);
        }

    }

    public void onSpace(View view) {
        //Adds space to message if doesn't already end with space.
        String message = mModel.getMessage();
        if (!message.endsWith(" ")) {
            message += " ";
            mModel.setMessage(message);
        }
    }

    public void onPause(View view)
    {
        Intent intent = new Intent(view.getContext(), MenuActivity.class);
        startActivity(intent);
    }

}