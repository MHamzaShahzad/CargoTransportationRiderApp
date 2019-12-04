package com.example.cargotransportationriderapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.cargotransportationriderapp.R;
import com.example.cargotransportationriderapp.common.CommonFunctionsClass;
import com.example.cargotransportationriderapp.common.Constants;
import com.example.cargotransportationriderapp.controllers.MyFirebaseDatabase;
import com.example.cargotransportationriderapp.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getName();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = this;

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.e(TAG, "onStart: USER_EXISTS");
            checkIfUserExistInDatabaseOnStart(user.getUid());
        }else
            startMainActivity();
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
