package com.khansaab.allinonedonation.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.khansaab.allinonedonation.FilterAd;
import com.khansaab.allinonedonation.R;
import com.khansaab.allinonedonation.Utils;
import com.khansaab.allinonedonation.activities.AdDetailsActivity;
import com.khansaab.allinonedonation.databinding.RowAdBinding;
import com.khansaab.allinonedonation.models.ModelAd;

import java.util.ArrayList;

public class AdapterAd extends RecyclerView.Adapter<AdapterAd.HolderAd> implements Filterable {
    //View Binding
    private RowAdBinding binding;

    private static final String TAG = "ADAPTER_AD_TAG";
    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;
    //context of activity/fragment from where instance of AdapterAd class is created
    private Context context;
    //adArrayList the list of the ads
    public ArrayList<ModelAd> adArrayList;

    private ArrayList<ModelAd> filterList;

    private FilterAd filter;
/*
*  Constructor
*
* @param context The context of activity/fragment from where instance of AdapterAd class is created
* @param adArraylist the list of ads
* */

    public AdapterAd(Context context, ArrayList<ModelAd> adArrayList){
        this.context=context;
        this.adArrayList=adArrayList;
        this.filterList = adArrayList;
        // get instance of firebase auth for Auth related tasks
        firebaseAuth =FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderAd onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        binding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderAd(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAd holder, int position){


        ModelAd modelAd = adArrayList.get(position);

        String title = modelAd.getTitle();
        String desctiption = modelAd.getDescription();
        String address = modelAd.getAddress();
        String condition = modelAd.getCondition();
        long timestamp =modelAd.getTimestamp();
        String formattedDate = Utils.formatTimestampDate(timestamp);

        loadAdFirstImage(modelAd, holder);
        //if user is logged in then check that if the Ad is in favorite of current user
        if (firebaseAuth.getCurrentUser() != null ){
            checkIsFavorite(modelAd, holder);
        }

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(desctiption);
        holder.addressTv.setText(address);
        holder.conditionTv.setText(condition);
        holder.dateTv.setText(formattedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(context, AdDetailsActivity.class);
                intent.putExtra("adId", modelAd.getId());
                context.startActivity(intent);
            }
        });


        holder.favBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                boolean favorite = modelAd.isFavorite();

                if(favorite){

                    Utils.removeFromFavorite(context, modelAd.getId());
                } else {

                    Utils.addToFavorite(context, modelAd.getId());
                }
            }
        });
    }

    private void checkIsFavorite(ModelAd modelAd, HolderAd holder){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelAd.getId())
                .addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot){

                        boolean favorite =snapshot.exists();

                        modelAd.setFavorite(favorite);

                        if (favorite){

                            holder.favBtn.setImageResource(R.drawable.ic_fav_yes);
                        } else {
                            holder.favBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error){

                    }
                });
    }





    private void loadAdFirstImage(ModelAd modelAd, HolderAd holder) {

        Log.d(TAG, "loadAdFirstImage: ");

        String adId = modelAd.getId();

        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Ads");
        reference.child(adId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot){

                        for (DataSnapshot ds: snapshot.getChildren()){
                            String imageUrl = ""+ ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: imageUrl: "+imageUrl);

                            try{
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_image_gray)
                                        .into(holder.imageIv);
                            } catch (Exception e){
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error){

                    }
                });
    }

    @Override
    public int getItemCount(){
        return adArrayList.size();
    }

    @Override
    public Filter getFilter(){

        if (filter == null){
            filter = new FilterAd(this, filterList);
        }
        return filter;
    }

    class HolderAd extends RecyclerView.ViewHolder{


        ShapeableImageView imageIv;
        TextView titleTv, descriptionTv, addressTv, conditionTv, dateTv;
        ImageButton favBtn;

        public HolderAd(@NonNull View itemView){
            super(itemView);

            //init UI views of the row_ad.xml
            imageIv = binding.imageIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            favBtn = binding.favBtn;
            addressTv = binding.addressTv;
            conditionTv = binding.conditionTv;
            dateTv = binding.dateTv;
        }
    }
}
