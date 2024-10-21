package com.khansaab.allinonedonation.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.khansaab.allinonedonation.R;
import com.khansaab.allinonedonation.Utils;
import com.khansaab.allinonedonation.databinding.ActivityLoginOptionsBinding;

import com.google.android.gms.tasks.Task;

import java.util.HashMap;

public class LoginOptionsActivity extends AppCompatActivity {

    private ActivityLoginOptionsBinding binding;

    private static final String TAG = "LOGIN_OPTIONS_TAG";

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private GoogleSignInClient mgoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);;

        firebaseAuth = FirebaseAuth.getInstance();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                                .build();

        mgoogleSignInClient = GoogleSignIn.getClient(this, gso);


        binding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.loginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                startActivity(new Intent(LoginOptionsActivity.this, LoginEmailActivity.class));
            }
        });

        binding.loginPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginOptionsActivity.this, LoginPhoneActivity.class));
            }
        });

        binding.loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginGoogleLogin();
            }
        });
    }

    private void beginGoogleLogin(){
        Log.d(TAG, "beginGoogleLogin: ");
        Intent googleSignInIntent = mgoogleSignInClient.getSignInIntent();
        googleSignInnARL.launch(googleSignInIntent);
    }

    private ActivityResultLauncher<Intent> googleSignInnARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result){
                    Log.d(TAG, "onActivityResult: ");

                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                        try{
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "onActivityResult: Account ID: "+account.getId());
                            firebaseAuthWithGoogleAccount(account.getIdToken());

                        }
                        catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    }
                    else {
                        Log.d(TAG, "onActivityResult: Cancelled");
                        Utils.toast(LoginOptionsActivity.this, "Cancelled...");
                    }
                }
            }
    );

    private void firebaseAuthWithGoogleAccount(String idToken){
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken: "+idToken);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        
                        if(authResult.getAdditionalUserInfo().isNewUser()){
                            Log.d(TAG, "onSuccess: New User, Account Created...");
                            updateUserInfoDb();
                        }
                        else {
                            Log.d(TAG, "onSuccess: Existing User, Logged In");

                            startActivity(new Intent(LoginOptionsActivity.this, MainActivity.class));
                            finishAffinity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        
                    }
                });
    }

    private void updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ");
        progressDialog.setMessage("Saving User Info");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
//        String registerUserEmail = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid = firebaseAuth.getUid();
        String name = firebaseAuth.getCurrentUser().getDisplayName();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", ""+name);
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "Google");
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: User info saved...");
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginOptionsActivity.this, MainActivity.class));
                        finishAffinity();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(LoginOptionsActivity.this, "failed to save user info fue to "+e.getMessage());
                    }
                });
    }
}