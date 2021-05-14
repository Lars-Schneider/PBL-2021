package com.example.pblmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import java.util.List;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    //GPS & Map variables
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation = new Location("");
    FusedLocationProviderClient mFusedLocationClient;

    ArrayList<Marker> mMarkerArray = new ArrayList<Marker>(); //Holds all the letter markers
    String nearestLetter = ""; //Stores the letter of the nearest marker
    String message = ""; //Stores the message spelled so far


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location mLastLocation = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());

                Marker closestMarker = findClosestMarker(mMarkerArray, mLastLocation);
                nearestLetter = closestMarker.getTitle();
                closestMarker.setIcon(makeTextIcon(nearestLetter,-256));

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //getSupportActionBar().setTitle("Map Location Activity");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

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

        //TEST: Adds 2 markers in the world
        LatLng sydney = new LatLng(-34, 151);
        Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("B").icon(makeTextIcon("B",-1)));
        mMarkerArray.add(marker);
        LatLng seattle = new LatLng(47, -122);
        Marker newmarker = mGoogleMap.addMarker(new MarkerOptions().position(seattle).title("A").icon(makeTextIcon("A",-1)));
        mMarkerArray.add(newmarker);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000); // sets GPS refresh interval to 15 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

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
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
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
                                           String permissions[], int[] grantResults) {
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

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



    //Makes a letter icon with the inputted text
    public BitmapDescriptor makeTextIcon(String text, int color) {

        Paint textPaint = new Paint();
        textPaint.setTextSize(70);
        textPaint.setColor(Color.BLACK);

        int width = (int) textPaint.measureText(text);
        int height = (int) textPaint.getTextSize();

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        canvas.translate(0, height - 5);
        canvas.drawColor(color);

        canvas.drawText(text, 0, 0, textPaint);
        return BitmapDescriptorFactory.fromBitmap(image);
    }

    //Returns the nearest marker to inputted location
    public Marker findClosestMarker(ArrayList<Marker> markers, Location location) {
        LatLng locLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        double closest = 1000000000;
        Marker closest_marker = null;

        for (int i = 0; i < markers.size(); i++) {
            double distance = SphericalUtil.computeDistanceBetween(markers.get(i).getPosition(), locLatLng);
            if (distance < closest) {
                closest = distance;
                closest_marker = markers.get(i);
            }
        }

        return closest_marker;
    }


    //////////////////////
    // BUTTON FUNCTIONS //
    //////////////////////

    public void onAddLetterPressed(View view) {

        message += nearestLetter;
        TextView tv1 = findViewById(R.id.message);
        tv1.setText(message);
    }

    public void onDelete(View view) {

        //If message isn't empty, removes its last character
        if (message != null && message.length() > 0) {
            message = message.substring(0, message.length() - 1);
        }
        TextView tv1 = findViewById(R.id.message);
        tv1.setText(message);
    }




}