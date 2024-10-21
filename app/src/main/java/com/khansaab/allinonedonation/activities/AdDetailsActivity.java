package com.khansaab.allinonedonation.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.khansaab.allinonedonation.R;
import com.khansaab.allinonedonation.Utils;
import com.khansaab.allinonedonation.adapters.AdapterImageSlider;
import com.khansaab.allinonedonation.databinding.ActivityAdDetailsBinding;
import com.khansaab.allinonedonation.models.ModelAd;
import com.khansaab.allinonedonation.models.ModelImageSlider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AdDetailsActivity extends AppCompatActivity{

    private ActivityAdDetailsBinding binding;

    private static final String TAG = "AD_DETAILS_TAG";

    private FirebaseAuth firebaseAuth;

    private String adId = "";

    private double adLatitude = 0;

    private double adLongitude = 0;

    private String donorUid = null;

    private String donorPhone = "";

    private boolean favorite = false;

    private ArrayList<ModelImageSlider> imageSliderArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityAdDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.toolbarEditBtn.setVisibility(View.GONE);
        binding.toolbarDeleteBtn.setVisibility(View.GONE);
        binding.chatBtn.setVisibility(View.GONE);
        binding.callBtn.setVisibility(View.GONE);
        binding.smsBtn.setVisibility(View.GONE);

        adId = getIntent().getStringExtra("adId");

        Log.d(TAG, "onCreate: adId: "+adId);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadAdDetails();
        loadAdImages();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.toolbarDeleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(AdDetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Delete Ad")
                        .setMessage("Are you sure you want to delete this Ad? ")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                deleteAd();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        binding.toolbarEditBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                editOptions();

            }
        });

        binding.toolbarFavBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                if(favorite){

                    Utils.removeFromFavorite(AdDetailsActivity.this, adId);
                } else {

                    Utils.addToFavorite(AdDetailsActivity.this, adId);
                }
            }
        });

        binding.donorProfileCv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });

        binding.chatBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });

        binding.callBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.callIntent(AdDetailsActivity.this, donorPhone);
            }
        });

        binding.smsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.smsIntent(AdDetailsActivity.this, donorPhone);
            }
        });

        binding.mapBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.mapIntent(AdDetailsActivity.this, adLatitude, adLongitude);
            }
        });
    }

    private void editOptions(){
        Log.d(TAG, "editOptions: ");

        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarEditBtn);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Mark As Donated");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item){
                int itemId = item.getItemId();

                if(itemId == 0){

                    Intent intent = new Intent(AdDetailsActivity.this, AdCreateActivity.class);
                    intent.putExtra("isEditMode", true);
                    intent.putExtra("adId", adId);
                    startActivity(intent);
                } else if (itemId == 1){

                    showMarkAsDonateddialog();
                }

                return true;
            }
        });

    }


    private void showMarkAsDonateddialog(){

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Mark as Donated")
                .setMessage("Are you sure you want to mark this Ad as Donated? ")
                .setPositiveButton("DONATED", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        Log.d(TAG, "onClick: Donated Clicked...");

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", ""+ Utils.AD_STATUS_DONATED);

                        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Ads");
                        ref.child(adId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>(){
                                    @Override
                                    public void onSuccess(Void unused){
                                        Log.d(TAG, "onSuccess: Marked as donated");
                                        Utils.toast(AdDetailsActivity.this, "Marked as donated");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener(){
                                    @Override
                                    public void onFailure(@NonNull Exception e){
                                        Log.e(TAG, "onFailure: ", e);
                                        Utils.toast(AdDetailsActivity.this, "Failed to mark as donated due to "+e.getMessage());
                                    }
                                });
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        Log.d(TAG, "onClick: Cancel Clicked...");
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void loadAdDetails(){
        Log.d(TAG, "loadAdDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot){

                        try{
                            ModelAd modelAd = snapshot.getValue(ModelAd.class);


                            donorUid = modelAd.getUid();
                            String title = modelAd.getTitle();
                            String description = modelAd.getDescription();
                            String address = modelAd.getAddress();
                            String condition = modelAd.getCondition();
                            String category = modelAd.getCategory();
                            adLatitude = modelAd.getLatitude();
                            adLongitude = modelAd.getLongitude();
                            long timestamp = modelAd.getTimestamp();

                            String formattedDate = Utils.formatTimestampDate(timestamp);

                            if (donorUid.equals(firebaseAuth.getUid())){

                                binding.toolbarEditBtn.setVisibility(View.VISIBLE);
                                binding.toolbarDeleteBtn.setVisibility(View.VISIBLE);

                                binding.chatBtn.setVisibility(View.VISIBLE);
                                binding.callBtn.setVisibility(View.VISIBLE);
                                binding.smsBtn.setVisibility(View.VISIBLE);

                            } else {

                                binding.toolbarEditBtn.setVisibility(View.GONE);
                                binding.toolbarDeleteBtn.setVisibility(View.GONE);

                                binding.chatBtn.setVisibility(View.GONE);
                                binding.callBtn.setVisibility(View.GONE);
                                binding.smsBtn.setVisibility(View.GONE);
                            }

                            binding.titleTv.setText(title);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);
                            binding.conditionTv.setText(condition);
                            binding.categoryTv.setText(category);
                            binding.dateTv.setText(formattedDate);

                            laodDonorDetails();

                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error){

                    }
                });
    }

    private void laodDonorDetails() {
        Log.d(TAG, "laodDonatedDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(donorUid)
                .addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot){


                        String phoneCode = ""+ snapshot.child("phoneCode").getValue();
                        String phoneNumber = ""+ snapshot.child("phoneNumber").getValue();
                        String name = ""+ snapshot.child("name").getValue();
                        String profileImageUrl = ""+ snapshot.child("profileImageUrl").getValue();
                        long timestamp = (Long) snapshot.child("timestamp").getValue();
                        String formattedDate = Utils.formatTimestampDate(timestamp);

                        donorPhone = phoneCode +""+phoneNumber;

                        binding.donorNameTv.setText(name);
                        binding.memberSinceTv.setText(formattedDate);
                        try{
                            Glide.with(AdDetailsActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.donorProfileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error){

                    }
                });
    }


    private void checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                .addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot){

                        favorite = snapshot.exists();

                        Log.d(TAG, "onDataChange: favorite: "+favorite);

                        if(favorite){
                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_yes);
                        } else {

                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error){

                    }
                });
    }

    private void loadAdImages(){
        Log.d(TAG, "loadAdImages: ");


        imageSliderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId).child("Images")
                .addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot){
                        imageSliderArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);

                            imageSliderArrayList.add(modelImageSlider);
                        }

                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(AdDetailsActivity.this, imageSliderArrayList);
                        binding.imageSliderVp.setAdapter(adapterImageSlider);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error){

                    }
                });
    }

    private void deleteAd(){
        Log.d(TAG, "deleteAd: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>(){
                    @Override
                    public void onSuccess(Void unused){
                        Log.d(TAG, "onSuccess: Deleted");
                        Utils.toast(AdDetailsActivity.this, "Deleted");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e){
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(AdDetailsActivity.this, "Failed to delete due to "+e.getMessage());
                    }
                });
    }
}