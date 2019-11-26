package com.example.cargotransportationriderapp.controllers;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefLocalStorage {

    private static final String TAG = PrefLocalStorage.class.getName();
    private Context context;

    private static final String PREF_NAME = "RideData";
    private static final String BOOKED_DRIVER_ID = "booked_driver_id";
    private static final String RIDE_ID = "ride_id";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static PrefLocalStorage getInstance(Context context){
        return new PrefLocalStorage(context);
    }

    private PrefLocalStorage(Context context){
        this.context = context;
        initRawPrefs();
    }

    private void initRawPrefs(){
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveRideCredentials(String bookedDriverId, String rideId){
        editor.putString(BOOKED_DRIVER_ID, bookedDriverId).apply();
        editor.putString(RIDE_ID, rideId).apply();
    }

    public String getBookedDriverId(){
        return sharedPreferences.getString(BOOKED_DRIVER_ID, null);
    }

    public String getCurrentRideId(){
        return sharedPreferences.getString(RIDE_ID, null);
    }

    public void clearRideCredentials(){
        editor.clear().apply();
    }


}
