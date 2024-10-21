package com.khansaab.allinonedonation;

import android.widget.Filter;

import com.khansaab.allinonedonation.adapters.AdapterAd;
import com.khansaab.allinonedonation.models.ModelAd;

import java.util.ArrayList;

public class FilterAd extends Filter{

    //declaring AdapterAd and ArrayList<ModelAd> instance that will be initialized in constructor of this class
    private AdapterAd adapter;
    private ArrayList<ModelAd> filterList;


    /*
    * Filter Ad Constructor
    *
    * @param adapter AdapterAd instance to be passed when this constructor is created
    * @param filterList ad arraylist to be passed when this constructor is created
    */
    public FilterAd(AdapterAd adapter, ArrayList<ModelAd> filterList){
        this.adapter=adapter;
        this.filterList=filterList;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint){
        //perform filter based on what user type
        FilterResults results = new FilterResults();


        if(constraint != null && constraint.length() > 0){
            //the search query is not null and not empty, we can perform filter
            //convert the typed query to upper case to make search not case sensitive e.g. Asus Monitor --> ASUS MONITOR
            constraint = constraint.toString().toUpperCase();
            //hold the filtered item of Ads based on user searched query
            ArrayList<ModelAd> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //Ad filter based on Brand, Category, Condition, title. if any of these Matches add it to the filteredmodels List
                if(filterList.get(i).getBrand().toUpperCase().contains(constraint) ||
                filterList.get(i).getCategory().toUpperCase().contains(constraint) ||
                filterList.get(i).getCondition().toUpperCase().contains(constraint) ||
                filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                //Filter matched add to filteredModels list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            //the search query is either null or empty, we can't perform filter,Return full/Original list
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results){
        //publish the filtered result
        adapter.adArrayList = (ArrayList<ModelAd>) results.values;

        adapter.notifyDataSetChanged();
    }
}
