package com.example.cargotransportationriderapp.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class CommonFunctionsClass {

    public static void send_sms_to_owner(Context context, String phoneNumber) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", phoneNumber);
        smsIntent.putExtra("sms_body", "Body of Message");
        ((Activity) context).startActivity(smsIntent);
    }

    public static void call_to_owner(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        // Send phone number to intent as data
        intent.setData(Uri.parse("tel:" + phoneNumber));
        // Start the dialer app activity with number
        ((Activity) context).startActivity(intent);
    }

    public static void showCustomDialog(Context context, String title, String description) {
        AlertDialog dialog = null;
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(description);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                builder.setCancelable(true);
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    public static String getRideStringStatus(String status) {
        switch (status) {
            case Constants.STATUS_SENDING_RIDE:
                return "Ride Creating...";
            case Constants.STATUS_ACCEPTED_RIDE:
                return "Accepted.";
            case Constants.STATUS_DRIVER_ON_THE_WAY:
                return "Driver is coming to pickup location.";
            case Constants.STATUS_DRIVER_REACHED:
                return "Driver reached to your pick up location.";
            case Constants.STATUS_START_LOADING:
                return "Loading of your goods has been started by driver.";
            case Constants.STATUS_END_LOADING:
                return "Loading Finished.";
            case Constants.STATUS_START_RIDE:
                return "Driver is going to your drop off location.";
            case Constants.STATUS_END_RIDE:
                return "Driver reached to your drop off location.";
            case Constants.STATUS_START_UNLOADING:
                return "Unloading of your goods has been started by driver.";
            case Constants.STATUS_END_UNLOADING:
                return "Unloading Finished";
            case Constants.STATUS_COMPLETED_RIDE:
                return "Ride Completed";
            case Constants.STATUS_RIDE_FARE_COLLECTED:
                return "Fare Collected";
            case Constants.STATUS_RIDE_REVIEWED:
                return "Ride Reviewed";
            default:
                return status;

        }
    }

    public static String stringVehicleName(String vehicleType) {
        switch (vehicleType) {
            case Constants.DRIVER_VEHICLE_TYPE_LOADER_RIKSHAW:
                return Constants.STRING_DRIVER_VEHICLE_TYPE_LOADER_RIKSHAW;
            case Constants.DRIVER_VEHICLE_TYPE_RAVI:
                return Constants.STRING_DRIVER_VEHICLE_TYPE_RAVI;
            case Constants.DRIVER_VEHICLE_TYPE_SHAZOR:
                return Constants.STRING_DRIVER_VEHICLE_TYPE_SHAZOR;
            default:
                return "";
        }
    }

    public static boolean CheckGooglePlayServices(Context context) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(context);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                try {
                    googleAPI.getErrorDialog((Activity) context, result,
                            0).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
        return true;
    }

    public static void requestLocationPermissions(Context context, int requestCode) {
        ActivityCompat.requestPermissions(
                ((Activity) context),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                requestCode
        );
    }

    public static boolean checkPermissions(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public static boolean isLocationEnabled(Context context) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


}
