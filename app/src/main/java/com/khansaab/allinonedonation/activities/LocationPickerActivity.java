package com.khansaab.allinonedonation.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.khansaab.allinonedonation.R;
import com.khansaab.allinonedonation.Utils;
import com.khansaab.allinonedonation.databinding.ActivityLocationPickerBinding;

import java.util.Arrays;
import java.util.List;


public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityLocationPickerBinding binding;

    private static final String TAG = "LOCATION_PICKER_TAG";

    private static final int DEFAULT_ZOOM = 15;
    private GoogleMap mMap = null;

    private PlacesClient mPlaceClient;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location mLastKnownLocation = null;
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;
    private String selectedAddress = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //hide the doneLl for now. We will show when user select or search location
        binding.doneLl.setVisibility(View.GONE);

        //Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        //Initialize the Places client
        Places.initialize(this, getString(R.string.google_map_api_key));

        // Create a new Places Client instance
        mPlaceClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the AutocompleteSupportFragment to search place on map
        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // List of location fields we need in search result e.g. Place.Field.Id, Place.Field.Name, Place.Field.Address, Place.Field.LAT_LNG
        Place.Field[] placesList = new Place.Field[]{Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG};
        //set location fields list to the autocompleteSupportFragment
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(placesList));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                //exception occurred while searching/selecting location
                Log.d (TAG, "onError: status: "+status);
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //place selected. The param "place" contain all fields that we set as list. Now return the requested location fields back to the requesting activity/fragment

                String id = place.getId();
                String title = place.getName();
                LatLng latLng = place.getLatLng();
                selectedLatitude = latLng.latitude;
                selectedLongitude = latLng.longitude;
                selectedAddress = place.getAddress();

                Log.d(TAG, "onPlaceSelected: ID: "+id);
                Log.d(TAG, "onPlaceSelected: Title: "+title);
                Log.d(TAG, "onPlaceSelected: Latitude: "+selectedLatitude);
                Log.d(TAG, "onPlaceSelected: Longitude: "+selectedLongitude);
                Log.d(TAG, "onPlaceSelected: Address: "+selectedAddress);

                addMaker(latLng, title, selectedAddress);

            }
        });

        //handle toolbarBackBtn click, go-back
        binding.toolbarBackBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                getOnBackPressedDispatcher ().onBackPressed ();

            }
        });

        //handle toolbarGpsBtn click, if GPS enabled get and show user's current location
        binding.toolbarGpsBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //check if location enabled
                if (isGPSEnabled ()){//GPS location is enabled
                    requestLocationPermission.launch (Manifest.permission.ACCESS_FINE_LOCATION);
                }else { //GPS Location not enabled
                    Utils.toast (LocationPickerActivity.this, "Location is not on! Turn it on to show current location...");
                }
            }
        });

        //handle doneBtn click, get the selected location back to requesting activity/fragment class
        binding.doneBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                //put data to intent to get in previous activity
                Intent intent = new Intent ();
                intent.putExtra ("latitude", selectedLatitude);
                intent.putExtra ("longitude", selectedLongitude);
                intent.putExtra ("address", selectedAddress);
                setResult (RESULT_OK, intent);
                //finishing this activity
                finish ();
            }
        });




    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d (TAG, "onMapReady: ");

        mMap = googleMap;

        //prompt the user for permission.
        requestLocationPermission.launch (Manifest.permission.ACCESS_FINE_LOCATION);

        //handle mMap click, get latitude, longitude when of where user clicked on map
        mMap.setOnMapClickListener (new GoogleMap.OnMapClickListener () {
            @Override
            public void onMapClick (@NonNull LatLng latLng) {
                //get latitude and longitude from the param latLng
                selectedLatitude = latLng.latitude;
                selectedLongitude = latLng.longitude;

                Log.d (TAG, "onMapClick: selectedLatitude: "+selectedLatitude);
                Log.d (TAG, "onMapClick: selectedLongitude: "+selectedLongitude);

                //function call to get the address position from the latLng
                addressFromLatLng (latLng);

            }
        });
    }

    @SuppressLint("MissingPermission")
       private ActivityResultLauncher<String> requestLocationPermission = registerForActivityResult (
            new ActivityResultContracts.RequestPermission (),
            new ActivityResultCallback<Boolean> () {

                @Override
                public void onActivityResult (Boolean isGranted) {
                    Log.d (TAG, "onActivityResult: ");

                    if (isGranted){
                        mMap.setMyLocationEnabled (true);
                        pickCurrentPlace ();
                    } else {

                        Utils.toast (LocationPickerActivity.this, "Permission denied...");
                    }
                }
            }
    );




    private void addressFromLatLng(LatLng latLng){
        Log.d (TAG, "addressFromLatLng: ");
        //Geocoder class to get the address details from LatLng
        Geocoder geocoder = new Geocoder (this);

        try {//get maximum 1 result (Address) from the list of available address list of addresses on basis of latitude and longitude we passed
            List<Address> addressList = geocoder.getFromLocation (latLng.latitude, latLng.longitude, 1);
            //get Address object from the list addressList of type List<Address>
            Address address = addressList.get (0);
            //get the address details
            String addressLine = address.getAddressLine (0);
            String countryName = address.getCountryName ();
            String adminArea = address.getAdminArea ();
            String subAdminArea = address.getSubAdminArea ();
            String locality = address.getLocality ();
            String subLocality = address.getSubLocality ();
            String postalCode = address.getPostalCode ();

            //save address in selectedAddress variable
            selectedAddress = ""+addressLine;

            //add marker on map
            addMaker (latLng, ""+subLocality, ""+addressLine);
        } catch (Exception e){
            Log.e (TAG, "addressFromLatLng: ", e);
        }
    }

    /*This function will be called only if location permission is granted.
    * We will only check if map object is not null then proceed to show location on map
    */

    private void pickCurrentPlace(){
        Log.d (TAG, "pickCurrentPlace: ");
        if (mMap == null){
            return;
        }

        detectAndShowDeviceLocationMap();
    }

    /*
    * Get the current location of the device, and position the map's camera
    */
    @SuppressLint("MissingPermission")
    private void detectAndShowDeviceLocationMap(){
        /*
        * Get the best and most recent location of the device, which may be null in rare
        * cases when a location is not available
        */
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation ();
            locationResult.addOnSuccessListener (new OnSuccessListener<Location> () {
                @Override
                public void onSuccess (Location location) {
                    if (location != null){
                        //location got, save the location in mLastLocation
                        mLastKnownLocation = location;
                        //get latitude and longitude from location param
                        selectedLatitude = location.getLatitude ();
                        selectedLongitude = location.getLongitude ();

                        Log.d (TAG, "onSuccess: selectedLatitude: "+selectedLatitude);
                        Log.d (TAG, "onSuccess: selectedLongitude: "+selectedLongitude);

                        //setup Latlng from selectedLongitude and selectedLatitude
                        LatLng latLng = new LatLng (selectedLatitude, selectedLongitude);
                        mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (latLng, DEFAULT_ZOOM));
                        mMap.animateCamera (CameraUpdateFactory.zoomTo (DEFAULT_ZOOM));

                        //function call to retrieve the address from the latLng
                        addressFromLatLng(latLng);
                    }
                    else {
                        Log.d (TAG, "onSuccess: Location is Null");
                    }
                }
            }).addOnFailureListener (new OnFailureListener () {
                @Override
                public void onFailure (@NonNull Exception e) {
                    Log.e (TAG, "onFailure: ", e);
                }
            });

        } catch (Exception e){
            Log.e(TAG, "detectAndShowDeviceLocationMap: ", e);
        }
    }

    /*
    * Check if GPS/Location is enabled or not
    */

    private boolean isGPSEnabled(){
        //init LocationManager
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        //boolean variabled to return values as true/false
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        //Check if GPS_PROVIDER enabled
        try{
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e){
            Log.e(TAG, "isGPSEnabled: ", e);
        }
        //Check if NETWORK_PROVIDER enabled
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        } catch (Exception e){
            Log.e(TAG, "isGPSEnabled: ", e);
        }
        //return results
        return !(!gpsEnabled&& !networkEnabled);
    }

    /*
    * Add Marker on map after searching/picking location
    *
    * @param latLng latLng of the location picked
    * @param title Title of the location picked
    * @param address Address of the location picked
    */

    private void addMaker(LatLng latLng, String title, String address){
        Log.d(TAG, "addMaker: latitude: "+latLng.latitude);
        Log.d(TAG, "addMaker: longitude: "+latLng.longitude);
        Log.d(TAG, "addMaker: title: "+title);
        Log.d(TAG, "addMaker: address: "+address);
        //clear map before adding new marker. As we only need one Location marker on map so if there is an already one clear it before adding new
        mMap.clear();

        try {
            //Setup marker options with latLng, address, title and complete address
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(""+title);
            markerOptions.snippet(""+address);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            //add marker to the map and move camera to the newly added marker
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            //show the doneLl, so user can go back (with selected location) to the activity/fragment class that is requesting the location
            binding.doneLl.setVisibility(View.VISIBLE);
            //set selected location complete address
            binding.selectedPalceTv.setText(address);

        } catch (Exception e){
            Log.e(TAG, "addMaker: ", e);
        }
    }
}