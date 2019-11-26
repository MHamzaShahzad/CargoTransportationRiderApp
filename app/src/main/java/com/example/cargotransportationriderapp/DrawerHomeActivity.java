package com.example.cargotransportationriderapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.example.cargotransportationriderapp.controllers.MyFirebaseDatabase;
import com.example.cargotransportationriderapp.controllers.PrefLocalStorage;
import com.example.cargotransportationriderapp.models.CurrentRideModel;
import com.example.cargotransportationriderapp.models.Driver;
import com.example.cargotransportationriderapp.models.DriverStatuses;
import com.example.cargotransportationriderapp.models.RideDetails;
import com.example.cargotransportationriderapp.models.UpdateLocationsModel;
import com.example.cargotransportationriderapp.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Distance;
import com.google.maps.model.Duration;
import com.google.maps.model.TravelMode;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.example.cargotransportationriderapp.CustomNotificationGenerator.getNextNotifId;

public class DrawerHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener {

    private static final String TAG = DrawerHomeActivity.class.getName();
    private Context context;

    // Layout Widgets
    private RelativeLayout layout_pick_drop, layout_pickup_loc, layout_drop_off_loc;
    private TextView textPickupPlace, textPickupCompleteAddress, textDropOffPlace, textDropOffCompleteAddress, place_ride_estimated_time, place_ride_estimated_distance, place_ride_estimated_fare;
    private Spinner selectVehicleSpinner;
    private Button btnConfirmPickUp, btnConfirmDropOff, btnCreateRide;
    private LinearLayout layout_ride_time_distance_fare;


    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest;

    private GoogleMap mMap;
    private LatLng latLng;
    private static MarkerOptions markerOptions;

    private static final int PICKUP_PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int DROP_OFF_PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;

    private static boolean shouldMoveToCurrentLocation = true;

    private FirebaseUser firebaseUser;

    private ValueEventListener userValueEventListener, generalStatusesListener, allDriversLocationListener, bookedDriverLocationsListener;

    // Get All Online Admins Locations
    private Timer allDriversLocationTimer;
    private TimerTask allDriversLocationTimerTask;
    //admin marker where it is (icon/image show)
    private List<Marker> allDriversMarkers;

    private LatLng pickUpLatLng, dropOffLatLng;
    private String pickUpAddress, dropOffAddress;

    private String OLD_KEY = null;
    private ProgressDialog progressDialog;

    private PrefLocalStorage localStorage;

    private Marker bookedDriverLocationMarker;
    private LinearLayout bottom_sheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_home);
        context = this;
        localStorage = PrefLocalStorage.getInstance(context);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            SignOut();
        }

        mLocationRequest = new LocationRequest();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        try {

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //initialize
        initLayoutWidgets();
        initUserDetailsListener();
        initGeneralStatusesListener();
        initProgressDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseUser != null)
            FirebaseMessaging.getInstance().subscribeToTopic(firebaseUser.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                        Log.d(TAG, "Topic subscribed!");
                    else
                        Log.e(TAG, "Can't subscribe to topic");
                }
            });
        else
            SignOut();
    }

    private void initSelectVehicleSpinner() {
        List<String> vehicles = new ArrayList<>();

        vehicles.add("Select Any Vehicle");
        vehicles.add(Constants.STRING_DRIVER_VEHICLE_TYPE_LOADER_RIKSHAW);
        vehicles.add(Constants.STRING_DRIVER_VEHICLE_TYPE_RAVI);
        vehicles.add(Constants.STRING_DRIVER_VEHICLE_TYPE_SHAZOR);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.layout_spinner_style, vehicles);
        selectVehicleSpinner.setAdapter(arrayAdapter);
        selectVehicleSpinner.setPrompt("Select Any Vehicle");

        selectVehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                if (layout_ride_time_distance_fare.getVisibility() == View.VISIBLE && position > 0) {

                    getRideEstimations(position, pickUpLatLng, dropOffLatLng);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            SignOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home, new FragmentRidesHistory()).addToBackStack(null).commit();

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "onMapReady: ");
        mMap = googleMap;
        setGoogleClientForMap();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onConnected : Permission not granted!");
            //Permission not granted by user so cancel the further execution.
            return;
        }
        mMap.setMyLocationEnabled(true);
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, DrawerHomeActivity.this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (generalStatusesListener == null)
            initGeneralStatusesListener();
        if (firebaseUser == null) {
            startMainActivity();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Toast.makeText(context, "Could not get Location", Toast.LENGTH_SHORT).show();
        } else {

            latLng = new LatLng(location.getLatitude(), location.getLongitude());

            /*
            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Your current Location!");
            mMap.addMarker(markerOptions);
            mMap.clear();
            mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(100)
                    .strokeColor(Color.BLUE)
                    .strokeWidth(1f)
                    .fillColor(0x550000FF));*/

            if (shouldMoveToCurrentLocation) {

                float zoomLevel = 16.0f; //This goes up to 21
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                shouldMoveToCurrentLocation = false;

                pickUpLatLng = latLng;
                pickUpAddress = getCompleteAddressString(context, pickUpLatLng.latitude, pickUpLatLng.longitude);
                getCurrentPlace(location);
            }

            updateLocationOnFirebase(latLng.latitude, latLng.longitude, location.getBearing());

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocationUpdates();
        stopAllDriversLocationTimer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKUP_PLACE_AUTOCOMPLETE_REQUEST_CODE || requestCode == DROP_OFF_PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                Place place = PlacePicker.getPlace(data, this);

                if (requestCode == PICKUP_PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                    pickUpLatLng = place.getLatLng();
                    pickUpAddress = place.getName() + "-" + place.getAddress();
                    textPickupPlace.setText(place.getName());
                    textPickupCompleteAddress.setText(place.getName() + "-" + place.getAddress());
                }

                if (requestCode == DROP_OFF_PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                    dropOffLatLng = place.getLatLng();
                    dropOffAddress = place.getName() + "-" + place.getAddress();
                    textDropOffPlace.setText(place.getName());
                    textDropOffCompleteAddress.setText(place.getName() + "-" + place.getAddress());
                }

            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_pickup_loc:
                setSelect_location(PICKUP_PLACE_AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.layout_drop_off_loc:
                setSelect_location(DROP_OFF_PLACE_AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.btnConfirmPickUp:
                setConfirmPickUp();
                break;
            case R.id.btnConfirmDropOff:
                setConfirmDropOff();
                break;
            case R.id.btnCreateRide:
                setCreateRide();
                break;
            default:
                Log.e(TAG, "onClick: UNKNOWN");
        }
    }

    // Custom Functions...
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void setConfirmPickUp() {

        if (selectVehicleSpinner.getSelectedItemPosition() > 0) {

            btnConfirmPickUp.setVisibility(View.GONE);
            selectVehicleSpinner.setVisibility(View.GONE);

            btnConfirmDropOff.setVisibility(View.VISIBLE);
            layout_drop_off_loc.setVisibility(View.VISIBLE);

        } else {
            Toast.makeText(context, "Select Vehicle first!", Toast.LENGTH_LONG).show();
        }

    }

    private void setConfirmDropOff() {

        btnConfirmDropOff.setVisibility(View.GONE);

        btnCreateRide.setVisibility(View.VISIBLE);
        selectVehicleSpinner.setVisibility(View.VISIBLE);
        layout_ride_time_distance_fare.setVisibility(View.VISIBLE);

        layout_pickup_loc.setClickable(false);
        layout_drop_off_loc.setClickable(false);

        getRideEstimations(selectVehicleSpinner.getSelectedItemPosition(), pickUpLatLng, dropOffLatLng);
    }

    private void setCreateRide() {

        btnCreateRide.setVisibility(View.GONE);
        layout_drop_off_loc.setVisibility(View.GONE);
        layout_pickup_loc.setVisibility(View.GONE);
        layout_ride_time_distance_fare.setVisibility(View.GONE);
        selectVehicleSpinner.setVisibility(View.GONE);

        createRideInstanceOnFirebase();
    }

    private void setToDefault() {

        layout_pickup_loc.setVisibility(View.VISIBLE);
        layout_pickup_loc.setClickable(true);
        textPickupCompleteAddress.setText("Complete Address");
        textPickupPlace.setText("Place Name");

        layout_drop_off_loc.setVisibility(View.GONE);
        layout_drop_off_loc.setClickable(true);
        textDropOffCompleteAddress.setText("Complete Address");
        textDropOffPlace.setText("Place Name");

        btnConfirmPickUp.setVisibility(View.VISIBLE);
        btnConfirmDropOff.setVisibility(View.GONE);
        btnCreateRide.setVisibility(View.GONE);

        selectVehicleSpinner.setVisibility(View.VISIBLE);
        selectVehicleSpinner.setSelection(0);

        if (bottom_sheet != null)
            bottom_sheet.setVisibility(View.INVISIBLE);

        pickUpLatLng = null;
        pickUpAddress = null;
        dropOffLatLng = null;
        dropOffAddress = null;
        OLD_KEY = null;

    }

    private void userOnRide() {

        layout_pickup_loc.setVisibility(View.GONE);
        layout_drop_off_loc.setVisibility(View.GONE);

        layout_ride_time_distance_fare.setVisibility(View.GONE);
        selectVehicleSpinner.setVisibility(View.GONE);

        btnConfirmPickUp.setVisibility(View.GONE);
        btnConfirmDropOff.setVisibility(View.GONE);
        btnCreateRide.setVisibility(View.GONE);

    }

    private void getRideEstimations(int position, LatLng pickUpLatLng, LatLng dropOffLatLng) {
        Log.e(TAG, "getRideEstimations: " + position + " : " + pickUpLatLng.latitude + " : " + pickUpLatLng.longitude + " : " + dropOffLatLng.latitude + " : " + dropOffLatLng.longitude);


        DirectionsResult result = getDirectionsResults(pickUpLatLng, dropOffLatLng);
        if (result != null) {

            DirectionsRoute route = result.routes[0];
            DirectionsLeg leg = route.legs[0];

            Distance myDistance = leg.distance;
            Duration duration = leg.durationInTraffic;

            place_ride_estimated_distance.setText(myDistance.humanReadable);
            place_ride_estimated_time.setText(duration.humanReadable);
            place_ride_estimated_fare.setText("" + getEstimatedFare(position, myDistance.inMeters));

            mMap.addMarker(new MarkerOptions().position(dropOffLatLng).title("Drop Off Location")).showInfoWindow();
            addPolyline(result, mMap);
            Log.e("@DurationAndDistance", "onClick: " + leg.duration.humanReadable + " : " + duration.humanReadable + " ; " + myDistance.humanReadable + " Fare : " + route.fare);
        }

    }

    private int getEstimatedFare(int position, long distanceInMeters) {
        int yourFare = 0;
        switch (String.valueOf(position)) {
            case Constants.DRIVER_VEHICLE_TYPE_LOADER_RIKSHAW:
                yourFare = 200;
                if (distanceInMeters > 3000) {

                    if (distanceInMeters < 5000) {

                        yourFare += (distanceInMeters - 3000) * 0.04;

                    } else {

                        yourFare += 80;
                        yourFare += (distanceInMeters - 5000) * 0.025;

                    }
                }
                break;
            case Constants.DRIVER_VEHICLE_TYPE_RAVI:
                yourFare = 500;
                if (distanceInMeters > 3000) {

                    if (distanceInMeters < 5000) {

                        yourFare += (distanceInMeters - 3000) * 0.1;

                    } else {

                        yourFare += 150;
                        yourFare += (distanceInMeters - 5000) * 0.063;

                    }
                }
                break;
            case Constants.DRIVER_VEHICLE_TYPE_SHAZOR:

                yourFare = 1000;
                if (distanceInMeters > 3000) {

                    if (distanceInMeters < 5000) {

                        yourFare += (distanceInMeters - 3000) * 0.15;

                    } else {

                        yourFare += 150;
                        yourFare += (distanceInMeters - 5000) * 0.0945;

                    }
                }

                break;
        }
        return yourFare;
    }

    private DirectionsResult getDirectionsResults(LatLng pickUpLatLng, LatLng dropOffLatLng) {

        DateTime now = new DateTime();
        DirectionsResult result = null;
        try {
            result = DirectionsApi.newRequest(getGeoContext())
                    .mode(TravelMode.DRIVING).origin(pickUpLatLng.latitude + "," + pickUpLatLng.longitude)
                    .destination(dropOffLatLng.latitude + "," + dropOffLatLng.longitude).departureTime(now)
                    .await();
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (com.google.maps.errors.ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(1)
                .setApiKey(getResources().getString(R.string.google_maps_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        try {
            List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
            //mMap.clear();
            mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.BLUE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void createRideInstanceOnFirebase() {
        String rideId = UUID.randomUUID().toString();
        RideDetails rideDetails = new RideDetails(
                rideId,
                firebaseUser.getUid(),
                String.valueOf(selectVehicleSpinner.getSelectedItemPosition()),
                pickUpAddress,
                dropOffAddress,
                String.valueOf(pickUpLatLng.latitude),
                String.valueOf(pickUpLatLng.longitude),
                String.valueOf(dropOffLatLng.latitude),
                String.valueOf(dropOffLatLng.longitude),
                Constants.STATUS_DEFAULT,
                new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime())
        );
        MyFirebaseDatabase.RIDES_REFERENCE.child(rideId).setValue(rideDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    getOnlineFreeDrivers(rideId, rideDetails);

                } else {
                    Log.e(TAG, "onComplete: " + task.getException().getMessage());
                }
            }
        });
    }

    private void getOnlineFreeDrivers(String rideId, RideDetails rideDetails) {
        MyFirebaseDatabase.DRIVERS_REFERENCE.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Float> driversDistance = new ArrayList<>();
                List<String> driversKeys = new ArrayList<>();

                Location myLocation = new Location("");

                myLocation.setLatitude(pickUpLatLng.latitude);
                myLocation.setLongitude(pickUpLatLng.longitude);

                Iterable<DataSnapshot> driverSnapshots = dataSnapshot.getChildren();
                for (DataSnapshot snapshot : driverSnapshots) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        String networkStatus = (String) snapshot.child(Constants.STRING_STATUSES).child(Constants.STRING_DRIVER_NETWORK_STATUS)
                                .getValue();
                        String currentStatus = (String) snapshot.child(Constants.STRING_STATUSES).child(Constants.STRING_CURRENT_STATUS)
                                .getValue();

                        String vehicleType = (String) snapshot.child(Constants.STRING_DETAILS).child(Constants.STRING_DRIVER_VEHICLE_TYPE).getValue();

                        if (vehicleType != null && vehicleType.equals(rideDetails.getVehicle()) && networkStatus != null && networkStatus.equals(Constants.DRIVER_ONLINE) && currentStatus != null && currentStatus.equals(Constants.STATUS_DEFAULT)) {

                            String driverLatitude = (String) snapshot.child(Constants.STRING_LOCATIONS).child(Constants.STRING_LOCATIONS_LATITUDE)
                                    .getValue();
                            String driverLongitude = (String) snapshot.child(Constants.STRING_LOCATIONS).child(Constants.STRING_LOCATIONS_LONGITUDE)
                                    .getValue();
                            if (driverLatitude != null && driverLongitude != null) {

                                try {
                                    LatLng adminLatLng = new LatLng(Double.parseDouble(driverLatitude), Double.parseDouble(driverLongitude));

                                    Location location = new Location("");

                                    location.setLatitude(adminLatLng.latitude);
                                    location.setLongitude(adminLatLng.longitude);

                                    driversDistance.add(location.distanceTo(myLocation));
                                    driversKeys.add(snapshot.getKey());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                Log.e(TAG, "onDataChange: " + driversDistance + "\n" + driversKeys);

                final List<String> driversToSendRequestList = sortDriversListAgainstDistance(driversDistance, driversKeys);

                if (driversToSendRequestList != null && driversToSendRequestList.size() > 0) {

                    final CurrentRideModel currentRideModel = new CurrentRideModel(rideId, firebaseUser.getUid(), null);
                    MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_CURRENT_RIDE_MODEL).setValue(currentRideModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sendRequestToDrivers(driversToSendRequestList, currentRideModel);
                        }
                    });

                } else {
                    Toast.makeText(context, "No drivers available yet, please try again!", Toast.LENGTH_LONG).show();
                    setToDefault();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private List<String> sortDriversListAgainstDistance(List<Float> distances, List<String> keys) {

        List<String> sortedDriversList = new ArrayList<>();
        if (distances.size() > 0) {

            do {

                int minDistanceIndex = distances.indexOf(Collections.min(distances));
                sortedDriversList.add(keys.get(minDistanceIndex));

                distances.remove(minDistanceIndex);
                keys.remove(minDistanceIndex);

            } while (distances.size() > 0 && sortedDriversList.size() < 3);

        }

        return sortedDriversList;

    }

    private void sendRequestToDrivers(final List<String> driversToSendRequestList, final CurrentRideModel model) {
        Log.e(TAG, "sendRequestToAdmins: ");
        progressDialog.setTitle("Sending Request...");

        updateMyStatus(Constants.STATUS_SENDING_RIDE);

        final Timer sendRequestToSelectedAdminsTimer = new Timer();
        TimerTask sendRequestToSelectedAdminsTask = new TimerTask() {
            @Override
            public void run() {

                if (driversToSendRequestList.size() > 0) {

                    if (OLD_KEY != null) {
                        updateDriverStatus(OLD_KEY, Constants.STATUS_DEFAULT);
                        removeDriverCurrentRideCredentials(OLD_KEY);
                    }

                    updateDriverStatus(driversToSendRequestList.get(0), Constants.DRIVER_STATUS_RECEIVING_RIDE);
                    createDriverCurrentRideCredentials(driversToSendRequestList.get(0), model);
                    setDriverIdInCurrentRideCredentials(driversToSendRequestList.get(0));

                    OLD_KEY = driversToSendRequestList.get(0);
                    driversToSendRequestList.remove(0);


                    final Timer checkIfMyStatusChangesTimer = new Timer();
                    TimerTask checkIfMyStatusChangesTask = new TimerTask() {
                        @Override
                        public void run() {

                            MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_STATUSES).child(Constants.STRING_CURRENT_STATUS).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                                        if (dataSnapshot.getValue().equals(Constants.STATUS_ACCEPTED_RIDE)) {


                                            cancelTimer(sendRequestToSelectedAdminsTimer);
                                            cancelTimer(checkIfMyStatusChangesTimer);
                                            progressDialog.dismiss();
                                            finish();


                                            Log.e(TAG, "onDataChange: " + OLD_KEY);

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    };
                    checkIfMyStatusChangesTimer.scheduleAtFixedRate(checkIfMyStatusChangesTask, 0, 1000);

                } else {
                    if (OLD_KEY != null) {
                        updateDriverStatus(OLD_KEY, Constants.STATUS_DEFAULT);
                        removeDriverCurrentRideCredentials(OLD_KEY);
                    }
                    updateMyStatus(Constants.STATUS_DEFAULT);
                    removeSelfCurrentRideCredentials();

                    progressDialog.dismiss();

                    new Thread() {
                        public void run() {
                            DrawerHomeActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CommonFunctionsClass.showCustomDialog(context, "Ride Could't be accepted!", "Sorry for your inconvenience, but no driver accepted your ride request, please try again later.");
                                }
                            });
                        }
                    }.run();

                    cancelTimer(sendRequestToSelectedAdminsTimer);
                }

            }
        };

        sendRequestToSelectedAdminsTimer.scheduleAtFixedRate(sendRequestToSelectedAdminsTask, 0, 10000);

    }

    private void getRideAndDriverDetails(String rideCurrentStatus) {

        userOnRide();

        MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_CURRENT_RIDE_MODEL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {

                    try {

                        CurrentRideModel model = dataSnapshot.getValue(CurrentRideModel.class);
                        if (model != null) {


                            if (Integer.parseInt(rideCurrentStatus) >= Integer.parseInt(Constants.STATUS_COMPLETED_RIDE)) {
                                FragmentReviewRide fragmentReviewRide = new FragmentReviewRide();
                                Bundle bundle = new Bundle();
                                bundle.putString(Constants.STRING_RIDE_ID, model.getRideId());
                                bundle.putString(Constants.STRING_DRIVER_ID, model.getDriverId());
                                fragmentReviewRide.setArguments(bundle);
                                getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragmentReviewRide).commit();

                            } else {
                                getBookingDetails(model.getDriverId(), model.getRideId());
                                initBookedDriverLocations(model.getDriverId());
                            }

                            localStorage.saveRideCredentials(model.getDriverId(), model.getRideId());
                            updateRideInstance(model.getRideId(), Constants.STRING_RIDE_STATUS, rideCurrentStatus);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getBookingDetails(final String driverId, String rideId) {
        MyFirebaseDatabase.DRIVERS_REFERENCE.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    Log.e(TAG, "onDataChange: BOOKED_ADMIN_SNAPSHOT" + dataSnapshot);
                    try {
                        Driver driver = dataSnapshot.child(Constants.STRING_DETAILS).getValue(Driver.class);
                        if (driver != null) {
                            Log.e(TAG, "onDataChange: " + driver.getName() + " : " + driver.getPhoneNumber());
                            getRideDetails(driver, rideId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showBottomUpSheet(final Driver driver, final RideDetails rideDetails) {
        // Layout Widgets
        TextView driverName, vehicleModel, vehicleNumber, messageDriver, callDriver, currentRideStatus;
        CircleImageView driverImage, vehicleImage;

        // initBottomUp Sheet
        bottom_sheet.setVisibility(View.VISIBLE);

        BottomSheetBehavior sheetBehavior = BottomSheetBehavior.from(bottom_sheet);

        // callback for do something
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        //bottom up sheet initialized
        currentRideStatus = findViewById(R.id.currentRideStatus);
        driverImage = findViewById(R.id.driverImage);
        vehicleImage = findViewById(R.id.vehicleImage);
        driverName = findViewById(R.id.driverName);
        vehicleModel = findViewById(R.id.vehicleModel);
        vehicleNumber = findViewById(R.id.vehicleNumber);
        messageDriver = findViewById(R.id.messageDriver);
        callDriver = findViewById(R.id.callDriver);

        currentRideStatus.setText(CommonFunctionsClass.getRideStringStatus(rideDetails.getRideStatus()));

        driverName.setText(driver.getName());
        vehicleModel.setText(driver.getVehicleModel());
        vehicleNumber.setText(driver.getVehicleNumber());

        if (driver.getImageUrl() != null)
            Picasso.get()
                    .load(driver.getImageUrl())
                    .error(R.drawable.avatar)
                    .placeholder(R.drawable.avatar)
                    .centerInside().fit()
                    .into(driverImage);

        callDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonFunctionsClass.call_to_owner(context, driver.getPhoneNumber());
            }
        });
        messageDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonFunctionsClass.send_sms_to_owner(context, driver.getPhoneNumber());
            }
        });

    }

    private void initBookedDriverLocations(String driverId) {
        if (bookedDriverLocationsListener == null) {
            bookedDriverLocationsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                        try {
                            String latitude = (String) dataSnapshot.child(Constants.STRING_LOCATIONS_LATITUDE).getValue();
                            String longitude = (String) dataSnapshot.child(Constants.STRING_LOCATIONS_LONGITUDE).getValue();

                            removeBookedAdminLocationMarker();

                            if (latitude != null && longitude != null) {

                                LatLng adminLatLng = new LatLng(Double.parseDouble(latitude),
                                        Double.parseDouble(longitude));

                                bookedDriverLocationMarker = mMap.addMarker(new MarkerOptions().position(adminLatLng));

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
            MyFirebaseDatabase.DRIVERS_REFERENCE.child(driverId).child(Constants.STRING_LOCATIONS).addValueEventListener(bookedDriverLocationsListener);
        }
    }

    private void removeBookedDriverLocationsListener(String driverId) {
        if (bookedDriverLocationsListener != null && driverId != null) {
            MyFirebaseDatabase.DRIVERS_REFERENCE.child(driverId).child(Constants.STRING_LOCATIONS).removeEventListener(bookedDriverLocationsListener);
            bookedDriverLocationsListener = null;
        }
    }

    private void getRideDetails(Driver driver, String rideId) {
        MyFirebaseDatabase.RIDES_REFERENCE.child(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    try {
                        RideDetails rideDetails = dataSnapshot.getValue(RideDetails.class);
                        showBottomUpSheet(driver, rideDetails);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateRideInstance(String rideId, String child, String value) {
        MyFirebaseDatabase.RIDES_REFERENCE.child(rideId).child(child).setValue(value);
    }

    private void removeBookedAdminLocationMarker() {
        if (bookedDriverLocationMarker != null)
            bookedDriverLocationMarker.remove();
    }


    private void updateMyStatus(String status) {
        MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_STATUSES).child(Constants.STRING_CURRENT_STATUS).setValue(status);
    }

    private void updateDriverStatus(String driverId, String status) {
        MyFirebaseDatabase.DRIVERS_REFERENCE.child(driverId).child(Constants.STRING_STATUSES).child(Constants.STRING_CURRENT_STATUS).setValue(status);
    }

    private void createDriverCurrentRideCredentials(String key, CurrentRideModel model) {
        MyFirebaseDatabase.DRIVERS_REFERENCE.child(key).child(Constants.STRING_CURRENT_RIDE_MODEL).setValue(model);
    }

    private void removeDriverCurrentRideCredentials(String key) {
        MyFirebaseDatabase.DRIVERS_REFERENCE.child(key).child(Constants.STRING_CURRENT_RIDE_MODEL).removeValue();
    }

    private void removeSelfCurrentRideCredentials() {
        MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_CURRENT_RIDE_MODEL).removeValue();
    }

    private void setDriverIdInCurrentRideCredentials(String driverId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.STRING_DRIVER_ID, driverId);
        MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_CURRENT_RIDE_MODEL).updateChildren(map);
    }

    private void cancelTimer(Timer timer) {
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
    }

    // Build and connect with Google Client for Map
    private void setGoogleClientForMap() {
        mLocationClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(5000);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient.connect();
    }

    // Initialize layout widgets
    private void initLayoutWidgets() {
        selectVehicleSpinner = (Spinner) findViewById(R.id.selectVehicleSpinner);

        layout_ride_time_distance_fare = findViewById(R.id.layout_ride_time_distance_fare);

        place_ride_estimated_fare = findViewById(R.id.place_ride_estimated_fare);
        place_ride_estimated_distance = findViewById(R.id.place_ride_estimated_distance);
        place_ride_estimated_time = findViewById(R.id.place_ride_estimated_time);

        layout_pick_drop = findViewById(R.id.layout_pick_drop);
        layout_pickup_loc = findViewById(R.id.layout_pickup_loc);
        layout_drop_off_loc = findViewById(R.id.layout_drop_off_loc);

        textPickupPlace = findViewById(R.id.textPickupPlace);
        textPickupCompleteAddress = findViewById(R.id.textPickupCompleteAddress);
        textDropOffPlace = findViewById(R.id.textDropOffPlace);
        textDropOffCompleteAddress = findViewById(R.id.textDropOffCompleteAddress);

        btnConfirmPickUp = findViewById(R.id.btnConfirmPickUp);
        btnConfirmDropOff = findViewById(R.id.btnConfirmDropOff);
        btnCreateRide = findViewById(R.id.btnCreateRide);

        bottom_sheet = findViewById(R.id.bottom_sheet);


        initClickListeners();
        initSelectVehicleSpinner();
    }

    // Initialize Click listeners
    private void initClickListeners() {

        layout_pickup_loc.setOnClickListener(this);
        layout_drop_off_loc.setOnClickListener(this);

        btnConfirmPickUp.setOnClickListener(this);
        btnConfirmDropOff.setOnClickListener(this);
        btnCreateRide.setOnClickListener(this);

    }

    private void setSelect_location(int requestCode) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(DrawerHomeActivity.this), requestCode);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    // Update my location on firebase database...
    private void updateLocationOnFirebase(double latitude, double longitude, float bearingTo) {
        UpdateLocationsModel locationsModel = new UpdateLocationsModel(String.valueOf(bearingTo), String.valueOf(latitude), String.valueOf(longitude));
        MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_LOCATIONS).setValue(locationsModel);
    }

    // Get current place on start...
    private void getCurrentPlace(Location location) {

        // Initialize the SDK
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));

        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(this);

        // Use fields to define the data types to return.
        List<com.google.android.libraries.places.api.model.Place.Field> placeFields = Collections.singletonList(com.google.android.libraries.places.api.model.Place.Field.NAME);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.newInstance(placeFields);

        // Call findCurrentPlace and handle the response
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }
                    if (response.getPlaceLikelihoods().get(0) != null) {
                        com.google.android.libraries.places.api.model.Place place = response.getPlaceLikelihoods().get(0).getPlace();

                        textPickupPlace.setText(place.getName());
                        textPickupCompleteAddress.setText(
                                place.getName() +
                                        "-" +
                                        (
                                                (place.getAddress() == null) ?
                                                        ((place.getLatLng() != null) ?
                                                                getCompleteAddressString(context, place.getLatLng().latitude, place.getLatLng().longitude)
                                                                : getCompleteAddressString(context, location.getLatitude(), location.getLongitude())
                                                        )
                                                        : place.getAddress())
                        );
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
            });
        }

    }

    // Get Complete Address as String...
    private String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {
        String strAdd = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                //cityName = returnedAddress.getLocality();
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.e("@LocationAddress", "My Current location address : \n" + addresses + "\n" + returnedAddress + "\n" + strReturnedAddress.toString());
            } else {
                Log.e("@AddressNotFound", "My Current location address No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("@ErrorInAAddress", "My Current location address Cannot get Address!");
        }
        return strAdd;
    }

    public void SignOut() {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(task -> {
                    Log.e(TAG, "SignOut: IS_SUCCESSFUL : " + task.isSuccessful());

                    removeGeneralStatusesListener();
                    removeUserDetailsListener();
                    removeLocationUpdates();
                    stopAllDriversLocationTimer();

                    startMainActivity();
                });
    }

    private void startMainActivity() {
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }

    /*Init or Set Listeners and Timers*/
    // my statuses life cycle...
    private void initGeneralStatusesListener() {
        generalStatusesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {

                    Log.e(TAG, "onDataChange: MY_STATUS : " + dataSnapshot.getValue());

                    switch ((String) dataSnapshot.getValue()) {
                        case Constants.STATUS_DEFAULT:

                            localStorage.clearRideCredentials();
                            mMap.clear();
                            setToDefault();
                            getAllOnlineDrivers();
                            shouldMoveToCurrentLocation = true;

                            break;
                        case Constants.STATUS_SENDING_RIDE:

                            stopAllDriversLocationTimer();

                            break;
                        case Constants.STATUS_ACCEPTED_RIDE:

                            getRideAndDriverDetails(Constants.STATUS_ACCEPTED_RIDE);

                            break;
                        case Constants.STATUS_DRIVER_ON_THE_WAY:

                            getRideAndDriverDetails(Constants.STATUS_DRIVER_ON_THE_WAY);

                            break;
                        case Constants.STATUS_DRIVER_REACHED:

                            getRideAndDriverDetails(Constants.STATUS_DRIVER_REACHED);

                            break;
                        case Constants.STATUS_START_LOADING:

                            getRideAndDriverDetails(Constants.STATUS_START_LOADING);

                            break;
                        case Constants.STATUS_END_LOADING:

                            getRideAndDriverDetails(Constants.STATUS_END_LOADING);

                            break;
                        case Constants.STATUS_START_RIDE:

                            getRideAndDriverDetails(Constants.STATUS_START_RIDE);

                            break;
                        case Constants.STATUS_END_RIDE:

                            getRideAndDriverDetails(Constants.STATUS_END_RIDE);

                            break;
                        case Constants.STATUS_START_UNLOADING:

                            getRideAndDriverDetails(Constants.STATUS_START_UNLOADING);

                            break;
                        case Constants.STATUS_END_UNLOADING:

                            getRideAndDriverDetails(Constants.STATUS_END_UNLOADING);

                            break;
                        case Constants.STATUS_COMPLETED_RIDE:

                            getRideAndDriverDetails(Constants.STATUS_COMPLETED_RIDE);
                            removeBookedDriverLocationsListener(localStorage.getBookedDriverId());

                            break;
                        case Constants.STATUS_RIDE_FARE_COLLECTED:

                            getRideAndDriverDetails(Constants.STATUS_RIDE_FARE_COLLECTED);


                            break;
                        case Constants.STATUS_RIDE_REVIEWED:

                            removeSelfCurrentRideCredentials();
                            updateMyStatus(Constants.STATUS_DEFAULT);

                            break;
                        default:
                            Log.e(TAG, "onDataChange: MY_STATUS_NOT_DEFINED : " + (String) dataSnapshot.getValue());

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_STATUSES).child(Constants.STRING_CURRENT_STATUS)
                .addValueEventListener(generalStatusesListener);
    }

    //to show the user name on nav_bar
    private void initUserDetailsListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        final TextView navUserName = headerView.findViewById(R.id.nav_name);
        final TextView navUserEmail = headerView.findViewById(R.id.nav_email);
        final CircularImageView navUserPhoto = headerView.findViewById(R.id.nav_photo);

        userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        User databaseUser = dataSnapshot.getValue(User.class);
                        if (databaseUser != null) {
                            navUserEmail.setText(databaseUser.getEmail());
                            navUserName.setText(databaseUser.getName());
                            // now we will use Glide to load user image
                            if (databaseUser.getImageUrl() != null && !databaseUser.getImageUrl().equals("null") && !databaseUser.getImageUrl().equals(""))
                                Picasso.get()
                                        .load(databaseUser.getImageUrl())
                                        .error(R.drawable.avatar)
                                        .placeholder(R.drawable.avatar)
                                        .centerInside().fit()
                                        .into(navUserPhoto);
                            if (databaseUser.getAccountStatus().equals(Constants.ACCOUNT_INACTIVE))
                                SignOut();
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
        MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_DETAILS)
                .addValueEventListener(userValueEventListener);
    }

    //show online admin
    private void getAllOnlineDrivers() {

        allDriversMarkers = new ArrayList<>();

        if (allDriversLocationTimer == null)
            allDriversLocationTimer = new Timer(String.valueOf(getNextNotifId(context)), true);

        if (allDriversLocationTimerTask == null)
            allDriversLocationTimerTask = new TimerTask() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Log.e(TAG, "run: ");
                            allDriversLocationListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {

                                        removeAllDriversMarker();

                                        Iterable<DataSnapshot> driverSnapshots = dataSnapshot.getChildren();
                                        for (DataSnapshot snapshot : driverSnapshots) {
                                            if (snapshot.exists() && snapshot.getValue() != null) {
                                                try {

                                                    Driver driver = snapshot.child(Constants.STRING_DETAILS).getValue(Driver.class);

                                                    if (driver != null) {

                                                        Log.e(TAG, "onDataChange: DRIVER_MARKER_DATA " +
                                                                driver.getName() + " : " +
                                                                selectVehicleSpinner.getSelectedItemPosition() + " : " +
                                                                driver.getVehicleType() + " : " +
                                                                driver.getVehicleType().equals(String.valueOf(selectVehicleSpinner.getSelectedItemPosition())
                                                                ));

                                                        if (layout_ride_time_distance_fare.getVisibility() != View.VISIBLE && selectVehicleSpinner.getSelectedItemPosition() == 0 || driver.getVehicleType().equals(String.valueOf(selectVehicleSpinner.getSelectedItemPosition()))) {

                                                            UpdateLocationsModel locationsModel = snapshot.child(Constants.STRING_LOCATIONS).getValue(UpdateLocationsModel.class);
                                                            DriverStatuses statuses = snapshot.child(Constants.STRING_STATUSES).getValue(DriverStatuses.class);

                                                            if (statuses != null) {

                                                                String accountStatus = statuses.getAccountStatus();
                                                                String networkStatus = statuses.getNetworkStatus();
                                                                String currentStatus = statuses.getCurrentStatus();

                                                                if (
                                                                        accountStatus != null && accountStatus.equals(Constants.ACCOUNT_ACTIVE) &&
                                                                                networkStatus != null && networkStatus.equals(Constants.DRIVER_ONLINE) &&
                                                                                currentStatus != null && currentStatus.equals(Constants.STATUS_DEFAULT)

                                                                ) {

                                                                    if (locationsModel != null) {

                                                                        LatLng latLng = new LatLng(Double.valueOf(locationsModel.getLatitude()), Double.valueOf(locationsModel.getLongitude()));

                                                                        allDriversMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng)));

                                                                    }

                                                                }
                                                            }
                                                        }

                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };
                            MyFirebaseDatabase.DRIVERS_REFERENCE.addListenerForSingleValueEvent(allDriversLocationListener);

                        }
                    });

                }
            };

        allDriversLocationTimer.scheduleAtFixedRate(allDriversLocationTimerTask, 0, 5000);

    }

    // Remove or Stop Listeners and Timers
    private void removeLocationUpdates() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
            mLocationClient.disconnect();
        }
    }

    private void removeUserDetailsListener() {
        if (userValueEventListener != null) {
            MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_DETAILS)
                    .removeEventListener(userValueEventListener);
        }
    }

    private void removeGeneralStatusesListener() {
        if (generalStatusesListener != null)
            MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_STATUSES)
                    .removeEventListener(generalStatusesListener);
    }

    private void removeAllDriversMarker() {
        if (allDriversMarkers != null) {
            for (Marker previousAdminLocationMarker : allDriversMarkers) {
                if (previousAdminLocationMarker != null) {
                    previousAdminLocationMarker.remove();
                }
            }
            allDriversMarkers.clear();
        }
    }

    private void stopAllDriversLocationTimer() {
        if (allDriversLocationTimer != null) {
            allDriversLocationTimer.cancel();
            allDriversLocationTimer.purge();
            allDriversLocationTimer = null;
            if (allDriversLocationTimerTask != null) {
                allDriversLocationTimerTask.cancel();
                allDriversLocationTimerTask = null;
            }
        }
    }

}
