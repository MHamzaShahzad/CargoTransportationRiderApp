package com.example.cargotransportationriderapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.cargotransportationriderapp.R;
import com.example.cargotransportationriderapp.common.CommonFunctionsClass;
import com.example.cargotransportationriderapp.common.Constants;
import com.example.cargotransportationriderapp.controllers.MyFirebaseDatabase;
import com.example.cargotransportationriderapp.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getName();
    private Context context;
    private static final int LOCATION_REQ_CODE = 108;
    private BroadcastReceiver networkReceiver;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = this;

        initSnackBar();
        initBroadcastReceiver();

        if (haveNetwork()) {

            start();

        } else
            showSnackBar("Sorry, no internet connection detected.", Snackbar.LENGTH_LONG);

    }

    private void initSnackBar() {
        snackbar = Snackbar.make(getRootView(), "", Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    private void showSnackBar(String msg, int duration) {
        if (snackbar != null) {
            snackbar.setText(msg);
            snackbar.setDuration(duration);
            snackbar.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (networkReceiver != null)
            unregisterReceiver(networkReceiver);
    }

    private void initBroadcastReceiver() {
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (haveNetwork())
                    start();

            }
        };
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void start() {
        if (CommonFunctionsClass.CheckGooglePlayServices(context)) {
            if (CommonFunctionsClass.checkPermissions(context)) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Log.e(TAG, "onStart: USER_EXISTS");
                    checkIfUserExistInDatabaseOnStart(user.getUid());
                } else
                    startMainActivity();

            } else CommonFunctionsClass.requestLocationPermissions(context, LOCATION_REQ_CODE);

        } else showSnackBar("Your device did't have google play services enabled.", Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQ_CODE && permissions.length > 0 && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                showSnackBar("Location permission required.", Snackbar.LENGTH_LONG);
            }
            start();
        }
    }

    private boolean haveNetwork() {
        boolean has_wifi = false;
        boolean has_mobile_data = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo info : networkInfos) {
            if (info.getTypeName().equalsIgnoreCase("Wifi")) {
                if (info.isConnected()) {
                    has_wifi = true;
                }
            }
            if (info.getTypeName().equalsIgnoreCase("Mobile")) {
                if (info.isConnected()) {
                    has_mobile_data = true;
                }
            }
        }
        return has_wifi || has_mobile_data;
    }

    private View getRootView() {
        final ViewGroup contentViewGroup = (ViewGroup) findViewById(android.R.id.content);
        View rootView = null;

        if (contentViewGroup != null)
            rootView = contentViewGroup.getChildAt(0);

        if (rootView == null)
            rootView = getWindow().getDecorView().getRootView();

        return rootView;
    }

    private void checkIfUserExistInDatabaseOnStart(String userId) {
        MyFirebaseDatabase.USER_REFERENCE.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {

                    try {
                        User user = dataSnapshot.child(Constants.STRING_DETAILS).getValue(User.class);
                        if (user != null)
                            if (user.getAccountStatus().equals(Constants.ACCOUNT_ACTIVE)) {
                                startHomeActivity();
                                return;
                            }
                            else
                                CommonFunctionsClass.showCustomDialog(context, "Account Is't Active", "Your account have been de-activated by admin!");
                        SignOut();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {

                    SignOut();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startHomeActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(context, DrawerHomeActivity.class));
                finish();
            }
        }, 3000);

    }
    private void startMainActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }
        }, 3000);

    }

    public void SignOut() {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(task -> {
                    startMainActivity();
                    Log.e(TAG, "SignOut: IS_SUCCESSFUL : " + task.isSuccessful());
                });
    }

}
