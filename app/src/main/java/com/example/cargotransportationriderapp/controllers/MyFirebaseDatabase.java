package com.example.cargotransportationriderapp.controllers;

import com.example.cargotransportationriderapp.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseDatabase {

    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference RIDES_REFERENCE = database.getReference(Constants.STRING_RIDES);
    public static DatabaseReference USER_REFERENCE = database.getReference(Constants.STRING_USERS);
    public static DatabaseReference DRIVERS_REFERENCE = database.getReference(Constants.STRING_DRIVERS);

}
