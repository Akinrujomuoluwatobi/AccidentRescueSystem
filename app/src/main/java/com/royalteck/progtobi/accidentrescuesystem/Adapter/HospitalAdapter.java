package com.royalteck.progtobi.accidentrescuesystem.Adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.royalteck.progtobi.accidentrescuesystem.R;

import java.util.List;

import Model.HospitalModel;

import static android.location.Location.distanceBetween;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.MyViewHolder> {

    private List<HospitalModel> eventslist;
    //private List<EventModel> mFilteredDeveloperList;
    Activity context;
    Filter filter;
    double latitude, longitude;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView hospitalname, distance, imageparse;

        public MyViewHolder(View view) {
            super(view);
            hospitalname = (TextView) view.findViewById(R.id.hospitalname);
            distance = (TextView) view.findViewById(R.id.distance);
            imageparse = (TextView) view.findViewById(R.id.text_view_user_alphabet);

        }
    }


    public HospitalAdapter(List<HospitalModel> developerList, Activity context, double latitude, double longitude) {
        this.eventslist = developerList;
        //this.mFilteredDeveloperList = developerList;
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
        //setFilter();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hospital_each, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        HospitalModel event = eventslist.get(position);
        holder.hospitalname.setText(event.getHospitalName());
        holder.imageparse.setText(event.getHospitalName().substring(0, 1));
        float[] result = new float[10];
        Location.distanceBetween(latitude, longitude, event.getPositionLatitude(), event.getPositionLongitude(), result);
        holder.distance.setText(Float.toString((float) (result[0] * 0.001)) + " KM");
    }

    @Override
    public int getItemCount() {
        try {
            return eventslist.size();
        } catch (Exception e) {
            return 0;
        }

    }


    public void filterList(String text) {
        filter.filter(text);
    }

    /*private void setFilter() {
        filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<Developer> newFilters = new ArrayList<>();
                FilterResults results = new FilterResults();
                for (Developer developer : mDeveloperList) {
                    if (developer.getUserName().toLowerCase().trim().contains(constraint)) {
                        newFilters.add(developer);
                    }
                }
                results.values = newFilters;
                results.count = newFilters.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredDeveloperList = (ArrayList<Developer>) results.values;
                DevelopersAdapter.this.notifyDataSetChanged();
            }
        };
    }*/

}
