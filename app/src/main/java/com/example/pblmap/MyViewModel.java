package com.example.pblmap;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class MyViewModel extends ViewModel {
    private ArrayList<Marker> mMarkerArray; //Holds all the letter markers
    public ArrayList<Marker> getMarkers() {
        return mMarkerArray;
    }
    public void setMarkers(ArrayList<Marker> markers) {
        mMarkerArray = markers;
    }

    private Marker mNearestMarker; //Stores the nearest marker
    public Marker getNearestMarker() {
        return mNearestMarker;
    }
    public void setNearestMarker(Marker marker) {
        mNearestMarker = marker;
    }

    private String mMessage = "";
    public String getMessage() {
        return mMessage;
    }
    public void setMessage(String message) {
        this.mMessage = message;
    }

    long mReshuffleCountdown = 0;
    long getReshuffleCountdown() {
        return mReshuffleCountdown;
    }
    void setReshuffleCountdown(long countdown) {
        mReshuffleCountdown = countdown;
    }

    public boolean isEmpty() {
        return mMarkerArray == null;
    }
}
