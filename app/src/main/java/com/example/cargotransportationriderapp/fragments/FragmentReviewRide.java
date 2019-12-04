package com.example.cargotransportationriderapp.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cargotransportationriderapp.R;
import com.example.cargotransportationriderapp.activities.DrawerHomeActivity;
import com.example.cargotransportationriderapp.common.Constants;
import com.example.cargotransportationriderapp.controllers.MyFirebaseDatabase;
import com.example.cargotransportationriderapp.models.RideDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentReviewRide extends Fragment {

    private static final String TAG = FragmentReviewRide.class.getName();
    private Context context;
    private View view;

    private TextView placeRideFare;
    private Button btnSubmit;

    private ValueEventListener rideValueEventListener;
    private String rideId;

    public FragmentReviewRide() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_review_ride, container, false);

            placeRideFare = view.findViewById(R.id.placeRideFare);
            btnSubmit = view.findViewById(R.id.btnSubmit);


            getArgumentsData();
        }
        return view;
    }

    private void updateMyStatus(String uId, String status) {
        MyFirebaseDatabase.USER_REFERENCE.child(uId).child(Constants.STRING_STATUSES).child(Constants.STRING_CURRENT_STATUS).setValue(status);
    }

    private void updateDriverStatus(String driverId, String status) {
        MyFirebaseDatabase.DRIVERS_REFERENCE.child(driverId).child(Constants.STRING_STATUSES).child(Constants.STRING_CURRENT_STATUS).setValue(status);
    }

    private void updateRideStatus(String rideId, String status) {
        MyFirebaseDatabase.RIDES_REFERENCE.child(rideId).child(RideDetails.RIDE_STATUS_REF).setValue(status);
    }

    private void getArgumentsData() {
        Bundle bundleArguments = getArguments();
        if (bundleArguments != null) {

            try {
                rideId = bundleArguments.getString(Constants.STRING_RIDE_ID);
                if (rideId != null)
                    initRideDetailsListener();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setBtnSubmit(String uId, String driverId) {
        btnSubmit.setEnabled(true);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateMyStatus(uId, Constants.STATUS_RIDE_REVIEWED);
                updateDriverStatus(driverId, Constants.STATUS_RIDE_REVIEWED);
                updateRideStatus(rideId, Constants.STATUS_RIDE_REVIEWED);
                ((Activity) context).finish();
                startActivity(new Intent(context, DrawerHomeActivity.class));
            }
        });
    }

    private void initRideDetailsListener() {
        rideValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.exists()) {

                    try {

                        RideDetails details = dataSnapshot.getValue(RideDetails.class);
                        if (details != null) {
                            placeRideFare.setText(details.getRideFare());
                            if (details.getRideStatus().equals(Constants.STATUS_RIDE_FARE_COLLECTED))
                                setBtnSubmit(details.getUserId(), details.getDriverId());

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        MyFirebaseDatabase.RIDES_REFERENCE.child(rideId).addValueEventListener(rideValueEventListener);
    }


    private void removeRideValueEventListener() {
        if (rideId != null && rideValueEventListener != null)
            MyFirebaseDatabase.RIDES_REFERENCE.child(rideId).removeEventListener(rideValueEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeRideValueEventListener();
    }
}
