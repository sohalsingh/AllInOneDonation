package com.khansaab.allinonedonation.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.khansaab.allinonedonation.Utils;
import com.khansaab.allinonedonation.databinding.ActivityDeleteAccountBinding;


public class DeleteAccountActivity extends AppCompatActivity {

    private ActivityDeleteAccountBinding binding;
    private static final String TAG = "DELETE_ACCOUNT_TAG";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Handle toolbar back button click using OnBackPressedDispatcher
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();  // Use dispatcher to handle back press
            }
        });

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startMainActivity();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
    }

    private void deleteAccount() {
        Log.d(TAG, "deleteAccount: ");

        String myUid = firebaseAuth.getUid();

        progressDialog.setMessage("Deleting User Account");
        progressDialog.show();

        if (firebaseUser != null) {
            firebaseUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "onSuccess: Account deleted");

                    progressDialog.setMessage("Deleting User Ads");

                    DatabaseReference refUserAds = FirebaseDatabase.getInstance().getReference("Ads");
                    refUserAds.orderByChild("uid").equalTo(myUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        ds.getRef().removeValue();
                                    }

                                    progressDialog.setMessage("Deleting User Data");

                                    DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
                                    refUsers.child(myUid)
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d(TAG, "onSuccess: User data deleted...");
                                                    progressDialog.dismiss();
                                                    startMainActivity();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "onFailure: ", e);
                                                    progressDialog.dismiss();
                                                    Utils.toast(DeleteAccountActivity.this, "Failed to delete user data due to " + e.getMessage());
                                                    startMainActivity();
                                                }
                                            });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "onCancelled: Database error", error.toException());
                                    progressDialog.dismiss();
                                    Utils.toast(DeleteAccountActivity.this, "Failed to delete ads: " + error.getMessage());
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: ", e);
                    progressDialog.dismiss();
                    Utils.toast(DeleteAccountActivity.this, "Failed to delete account due to " + e.getMessage());
                }
            });
        } else {
            Log.e(TAG, "deleteAccount: No authenticated user");
            Utils.toast(DeleteAccountActivity.this, "No authenticated user");
            progressDialog.dismiss();
        }
    }

    private void startMainActivity() {
        Log.d(TAG, "startMainActivity: ");
        startActivity(new Intent(DeleteAccountActivity.this, MainActivity.class));
        finishAffinity();  // Close all activities in the task
    }
}
