package com.example.cargotransportationriderapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cargotransportationriderapp.R;
import com.example.cargotransportationriderapp.common.CommonFunctionsClass;
import com.example.cargotransportationriderapp.common.Constants;
import com.example.cargotransportationriderapp.controllers.MyFirebaseDatabase;
import com.example.cargotransportationriderapp.controllers.MyFirebaseStorage;
import com.example.cargotransportationriderapp.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateNewAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = CreateNewAccountActivity.class.getName();
    private Context context;

    CircleImageView profileImage;
    TextView userDoB, userPhoneNumber;
    TextInputEditText userName, userEmail, userAddress;
    RadioGroup radioGroupGender;
    RadioButton radioButtonMale, radioButtonFemale, radioButtonOthers;
    Button btnSubmitUser;

    private static final int GALLERY_REQUEST_CODE = 1;
    private Bitmap profileImageBitmap;
    private Uri profileImageUri;
    private ProgressDialog progressDialog;

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_account);
        context = this;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        initProgressDialog();
        initLayoutWidgets();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SignOut();
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void initLayoutWidgets() {
        profileImage = findViewById(R.id.profileImage);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPhoneNumber = findViewById(R.id.userPhoneNumber);
        userAddress = findViewById(R.id.userAddress);

        userDoB = findViewById(R.id.userDoB);

        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);
        radioButtonOthers = findViewById(R.id.radioButtonOthers);

        btnSubmitUser = findViewById(R.id.btnSubmitUser);

        userPhoneNumber.setText(firebaseUser.getPhoneNumber());
        setClickListeners();
    }

    private void setClickListeners() {
        profileImage.setOnClickListener(this);
        btnSubmitUser.setOnClickListener(this);
        userDoB.setOnClickListener(this);
    }

    private String getSelectedGender() {
        int id = radioGroupGender.getCheckedRadioButtonId();
        switch (id) {
            case R.id.radioButtonMale:
                return Constants.USER_GENDER_MALE;
            case R.id.radioButtonFemale:
                return Constants.USER_GENDER_FEMALE;
            case R.id.radioButtonOthers:
                return Constants.USER_GENDER_OTHERS;
            default:
                return null;
        }
    }


    public void SignOut() {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(task -> {
                    startActivity(new Intent(context, MainActivity.class));
                    Log.e(TAG, "SignOut: IS_SUCCESSFUL : " + task.isSuccessful());
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profileImage:
                pickFromGallery();
                break;
            case R.id.userDoB:

                break;
            case R.id.btnSubmitUser:
                if (isFormValid()) {
                    progressDialog.show();
                    uploadImageAndThenUser();
                }
                break;
            default:
                Log.e(TAG, "onClick: UnknownEvent Performed!");
        }
    }

    private void uploadImageAndThenUser() {

        MyFirebaseStorage.USER_IMAGES_REFERENCE.child(profileImageUri.getLastPathSegment() + ".jpg").putFile(profileImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content

                        final Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();

                        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uploadUserInDatabase(uri.toString());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                                CommonFunctionsClass.showCustomDialog(context, "", e.getMessage());
                                Toast.makeText(context, "Can't download image url, something went wrong , please try again!", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        progressDialog.dismiss();
                        exception.printStackTrace();
                        CommonFunctionsClass.showCustomDialog(context, "", exception.getMessage());
                        Toast.makeText(context, "Can't upload image, something went wrong , please try again!", Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                        .getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            }
        });
    }

    private void uploadUserInDatabase(String imageUrl) {

        User newUser = new User(
                imageUrl,
                userName.getText().toString(),
                userEmail.getText().toString(),
                userPhoneNumber.getText().toString(),
                userAddress.getText().toString(),
                "",
                getSelectedGender(),
                Constants.ACCOUNT_ACTIVE,
                new Date()
        );

        MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_DETAILS).setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_LONG).show();
                MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).child(Constants.STRING_STATUSES).child(Constants.STRING_CURRENT_STATUS).setValue(Constants.STATUS_DEFAULT);
                startHomeActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            profileImageUri = data.getData();
            //------------1
            try {
                profileImageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), profileImageUri);
                profileImage.setImageBitmap(profileImageBitmap);
                Log.e("bitmap", "onActivityResult: " + profileImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //-----------2
            //imageBitmap = (Bitmap) data.getExtras().get("data");

            //------------3
            //imageBitmap = BitmapFactory.decodeFile(selectedImage.getEncodedPath());

        }
    }

    private boolean isFormValid() {
        boolean result = true;

        if (profileImageBitmap == null) {
            Toast.makeText(context, "Select your image first!", Toast.LENGTH_LONG).show();
            result = false;
        }

        if (userName.length() == 0) {
            userName.setError("Field is required");
            result = false;
        }

        if (userEmail.length() == 0) {
            userEmail.setError("Field is required");
            result = false;
        }

        if (userPhoneNumber.length() == 0) {
            userPhoneNumber.setError("Field is required");
            result = false;
        }

        if (userAddress.length() == 0) {
            userAddress.setError("Field is required");
            result = false;
        }

        if (getSelectedGender() == null) {
            Toast.makeText(context, "Select your gender first!", Toast.LENGTH_LONG).show();
            result = false;
        }


        return result;
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    private void startHomeActivity() {
        startActivity(new Intent(context, DrawerHomeActivity.class));
        finish();
    }


    // Useless

    private void checkIfAccountAlreadyCreated() {
        MyFirebaseDatabase.USER_REFERENCE.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    progressDialog.dismiss();
                    CommonFunctionsClass.showCustomDialog(context, "Account Already Exist!", "Try with another mobile number!");
                } else
                    checkIfAccountAlreadyCreatedForDriver();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkIfAccountAlreadyCreatedForDriver() {
        MyFirebaseDatabase.DRIVERS_REFERENCE.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null)
                    CommonFunctionsClass.showCustomDialog(context, "Account Already Exist For Driver!", "Try with another mobile number!");
                else
                    uploadImageAndThenUser();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
