package com.khansaab.allinonedonation;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import android.content.Context;

import android.text.format.DateFormat;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.internal.Util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Utils{

    //Constants for ad statuses
    public static final String AD_STATUS_AVAILABLE="AVAILABLE";
    public static final String AD_STATUS_DONATED="DONATED";

    //Array of ad categories
    public static final String[] categories={
            "Mobile",
            "Computer/Laptop",
            "Electronics & Home Appliances",
            "Vehicles",
            "Furniture & Home Decor",
            "Fashion & Beauty",
            "Books",
            "Sports",
            "Animal",
            "Business",
            "Agriculture"
    };

    //Array of category icons [resource IDs]
    public static final int[] categoryIcons={
            R.drawable.ic_category_mobiles,
            R.drawable.ic_category_computer,
            R.drawable.ic_category_electronics,
            R.drawable.ic_category_vehicles,
            R.drawable.ic_category_furniture,
            R.drawable.ic_category_fashion,
            R.drawable.ic_category_books,
            R.drawable.ic_category_sports,
            R.drawable.ic_category_animals,
            R.drawable.ic_category_business,
            R.drawable.ic_category_agriculture
    };

    //Array of ad conditions
    public static final String[] conditions={"New", "Used", "Refurbished"};

    //Method to display a toast message
    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    //Method to get current time stamp
    public static long getTimestamp(){
        return System.currentTimeMillis();
    }

    //Method to format a timestamp into a date string
    public static String formatTimestampDate(Long timestamp){
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date=DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }

    //Method to add an ad to favorites
    public static void addToFavorite(Context context, String adId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        //Check if user is logged in
        if (firebaseAuth.getCurrentUser() == null){
            Utils.toast(context, "You're not logged in!");
        }else {
            long timestamp = Utils.getTimestamp();
            //Create a HashMap to store ad data
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("adId", adId);
            hashMap.put("timestamp", timestamp);
            //Get a reference to the "Users" node in the database
            DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Users");
            //Add the ad to the user's favorites
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>(){
                        @Override
                        public void onSuccess(Void unused){

                            Utils.toast(context, "Added to favorite...!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener(){
                        @Override
                        public void onFailure(@NonNull Exception e){
                            Utils.toast(context, "Failed to add to favorite due to "+e.getMessage());
                        }
                    });
        }
    }

    //Method tio remove ad's from the favorites
    public static void removeFromFavorite(Context context, String adId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        //Check if user is logged in
        if (firebaseAuth.getCurrentUser() == null ){

            Utils.toast(context, "You're not logged in");
        } else {
            // Get a reference to the database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>(){
                        @Override
                        public void onSuccess(Void unused){

                            Utils.toast(context, "Removed from Favorites");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener(){
                        @Override
                        public void onFailure(@NonNull Exception e){

                            Utils.toast(context, "Failed to remove from favorites due to "+e.getMessage());
                        }
                    });
        }
    }


    public static void callIntent(Context context, String phone){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+Uri.encode(phone)));
        context.startActivity(intent);
    }

    public static void smsIntent(Context context, String phone){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+Uri.encode(phone)));
        context.startActivity(intent);
    }

    public static void mapIntent(Context context, double latitude, double longitude){

        Uri gmmIntentUri = Uri.parse("https://maps.google.com/maps?daddr=" + latitude+","+longitude);


        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        mapIntent.setPackage("com.google.android.apps.maps");

        if(mapIntent.resolveActivity(context.getPackageManager()) != null){
            context.startActivity(mapIntent);
        } else {

            Utils.toast(context, "Google MAP Not installed");
        }
    }
}
