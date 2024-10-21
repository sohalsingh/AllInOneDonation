package com.khansaab.allinonedonation.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.khansaab.allinonedonation.RvListenerCategory;
import com.khansaab.allinonedonation.databinding.RowCategoryBinding;
import com.khansaab.allinonedonation.models.ModelCategory;

import java.util.ArrayList;
import java.util.Random;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory>{

    //View Binding
    private RowCategoryBinding binding;

    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList the list of the categories
    private ArrayList<ModelCategory> categoryArrayList;
    //RvListenerCategory interface to handle the category click event in it's calling class instead of this class
    private RvListenerCategory rvListenerCategory;

    /**
     * Constructor * @param context The context of activity/fragment from where instance of AdapterCategory class is created @param categoryArrayList the List of categories
     */
    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList, RvListenerCategory rvListenerCategory){
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.rvListenerCategory = rvListenerCategory;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        //inflate/bind the row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderCategory(binding.getRoot());
    }


    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position){

        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelCategory modelCategory = categoryArrayList.get(position);

        //get data from modelCategory
        String category = modelCategory.getCategory();
        int icon = modelCategory.getIcon();

        //get random color to set as background color of the categoryIconIv
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));

        holder.categoryIconIv.setImageResource(icon);
        holder.categoryTitleTv.setText(category);
        holder.categoryIconIv.setBackgroundColor(color);


        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                rvListenerCategory.onCategoryClick(modelCategory);
            }
        });

    }

    @Override
    public int getItemCount(){
        return categoryArrayList.size();
    }


    class HolderCategory extends RecyclerView.ViewHolder{
        //UI views of the row_category.xml
        ShapeableImageView categoryIconIv;
        TextView categoryTitleTv;

        public HolderCategory(@NonNull View itemView){
            super(itemView);

            //init UI views of the row_category.xml
            categoryIconIv = binding.categoryIconIv;
            categoryTitleTv = binding.categoryTitleTv;
        }
    }
}
