package com.khansaab.allinonedonation.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.khansaab.allinonedonation.R;
import com.khansaab.allinonedonation.RvListenerCategory;
import com.khansaab.allinonedonation.Utils;
import com.khansaab.allinonedonation.activities.LocationPickerActivity;
import com.khansaab.allinonedonation.adapters.AdapterAd;
import com.khansaab.allinonedonation.adapters.AdapterCategory;
import com.khansaab.allinonedonation.databinding.FragmentHomeBinding;
import com.khansaab.allinonedonation.models.ModelAd;
import com.khansaab.allinonedonation.models.ModelCategory;


import java.util.ArrayList;

public class HomeFragment extends Fragment {

    //TAG to show logs in logcat
    private static final String TAG = "HOME_TAG";
    private static final int MAX_DISTANCE_TO_LOAD_ADS_KM = 10;
    //View binding
    private FragmentHomeBinding binding;
    //context for this fragment class
    private Context mContext;

    private ArrayList<ModelAd> adArrayList;

    private AdapterAd adapterAd;

    private SharedPreferences locationSp;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";

    @Override
    public void onAttach(@NonNull Context context){
        mContext = context;

        super.onAttach(context);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //Get SharedPreferences for location data
        locationSp = mContext.getSharedPreferences("LOCATION_SP", Context.MODE_PRIVATE);
        //Get current latitude from SharedPreferences
        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f);
        //Get current longitude from SharedPreferences
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f);
        //Get current Address from SharedPreferences
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "");

        //Check if location data is Available
        if(currentLatitude != 0.0 && currentLongitude !=0.0){
            //Set the location text in the UI
            binding.locationTv.setText(currentAddress);
        }

        //Load categories for filteringloadCategories();
        //Load all ads initially
        loadCategories();
        loadAds("All");

        //Set a TextWatcher to listen for search queries
        binding.searchEt.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //Log the search query
                Log.d(TAG, "onTextChanged: Query: "+s);

                try{
                    //Get the search query as a string
                    String query = s.toString();
                    adapterAd.getFilter().filter(query);
                    //Filter the ads based on the query

                } catch (Exception e){
                    //Log any exception during filtering
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s){

            }
        });

        binding.locationCv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Create an intent to launch the LocationPickerActivity
                Intent intent = new Intent(mContext, LocationPickerActivity.class);
                //Launch the activity using the ActivityResultLauncher
                locationPickerActivityResult.launch(intent);

            }
        });

    }


    private ActivityResultLauncher<Intent> locationPickerActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>(){
                @Override
                public void onActivityResult(ActivityResult result){
                    //check if from map, location is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: RESULT_OK");
                        //get location info from intent
                        Intent data = result.getData();

                        if(data !=null ){
                            //get location info from map
                            Log.d(TAG, "onActivityResult: Location picked");
                            currentLatitude = data.getDoubleExtra("latitude", 0.0);
                            currentLongitude = data.getDoubleExtra("longitude", 0.0);
                            currentAddress = data.getStringExtra("address");
                            //get location info to shared preferences so when we launch app next time we don't need to pick again
                            locationSp.edit()
                                    .putFloat("CURRENT_LATITUDE",Float.parseFloat("" +currentLatitude))
                                    .putFloat("CURRENT_LONGITUDE", Float.parseFloat("" +currentLongitude))
                                    .putString("CURRENT_ADDRESS", currentAddress)
                                    .apply();
                            //set the picked address
                            binding.locationTv.setText(currentAddress);
                            //after picking address reload all ads again based on newly picked location
                            loadAds("All");

                        }
                    }else {
                        Log.d(TAG, "onActivityResult: Cancelled!");
                        Utils.toast(mContext, "Cancelled");
                    }

                }
            }
    );

    private void loadCategories() {
        //init categorylist
        ArrayList<ModelCategory> categoryArrayList = new ArrayList<>();
        // ModelCategory instance to show all products
        ModelCategory modelCategoryAll = new ModelCategory("All", R.drawable.ic_category_all);
        categoryArrayList.add(modelCategoryAll);

        //get category from Utils class and add in categoryList
        for (int i=0; i<Utils.categories.length; i++){
            //ModelCategory instance to get/hold category from current index
            ModelCategory modelCategory = new ModelCategory(Utils.categories[i], Utils.categoryIcons[i]);
            //add modelCategory to categoryArrayList
            categoryArrayList.add(modelCategory);
        }
        //innit/setup AdapterCategory
        AdapterCategory adapterCategory = new AdapterCategory(mContext, categoryArrayList, new RvListenerCategory(){
            @Override
            public void onCategoryClick(ModelCategory modelCategory){
                loadAds(modelCategory.getCategory());
            }
        });

        //set adapter to the RecyclerView i.e. categoriesRv
        binding.categoriesRv.setAdapter(adapterCategory);
    }

    private void loadAds(String category){
        Log.d(TAG, "loadAds: Category: "+category);
        //init adArrayList before starting adding data into it
        adArrayList = new ArrayList<>();
        //Firebase DB Listener to load ads based on category & Distance
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Ads");
        ref.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot){
                //clear arraylist each time starting adding data into it
                adArrayList.clear();
                //load ads list
                for (DataSnapshot ds: snapshot.getChildren()){
                    //Prepare ModelAd with all data from Firebase Db
                    ModelAd modelAd = ds.getValue(ModelAd.class);
                    //Function call with Returned value as distance in kilometers
                    double distance = calculateDistanceKm(modelAd.getLatitude(), modelAd.getLongitude());
                    Log.d(TAG, "onDataChange: distance: "+distance);
                    //filters
                    if (category.equals("All")){
                        //Category all is selected, now check distance if is <= required e.g. 10km then show
                        if(distance <= MAX_DISTANCE_TO_LOAD_ADS_KM){
                            //the distance is <= required e.g. 10Km. Add to list
                            adArrayList.add(modelAd);
                        }
                    }else {
                        //Som category is selected e.g. Mobile
                        if (modelAd.getCategory().equals(category)){
                            //the distance is <= required e.g. 10Km. Add to list
                            if(distance <= MAX_DISTANCE_TO_LOAD_ADS_KM){
                                adArrayList.add(modelAd);
                            }
                        }
                    }
                }

                adapterAd = new AdapterAd(mContext, adArrayList);
                binding.adsRv.setAdapter(adapterAd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error){

            }
        });
    }

    private double calculateDistanceKm(double adLatitude, double adLongitude){
        Log.d(TAG, "calculateDistanceKm: currentLatitude: "+ currentLatitude);
        Log.d(TAG, "calculateDistanceKm: currentLongitude: "+ currentLongitude);
        Log.d(TAG, "calculateDistanceKm: adLatitude: "+adLatitude);
        Log.d(TAG, "calculateDistanceKm: adLongitude: "+adLongitude);

        Location startPoint = new Location(LocationManager.NETWORK_PROVIDER);
        startPoint.setLatitude(currentLatitude);
        startPoint.setLongitude(currentLongitude);

        Location endPoint = new Location(LocationManager.NETWORK_PROVIDER);
        endPoint.setLatitude(adLatitude);
        endPoint.setLongitude(adLongitude);

        double distanceInMeters = startPoint.distanceTo(endPoint);
        double distanceInKm = distanceInMeters / 1000;

        return distanceInKm;


    }
}