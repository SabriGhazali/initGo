package com.satoripop.intigo_demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.MyViewHolder> {

    private final ArrayList<Locations> dataSet;



    public LocationsAdapter(ArrayList<Locations> locationsList) {
        this.dataSet = locationsList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.bind(dataSet.get(position) , position);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView number;



        public MyViewHolder(View itemView) {
            super(itemView);
            this.title =  itemView.findViewById(R.id.text_detail);
            this.number =  itemView.findViewById(R.id.number);
        }

        public void bind(Locations item, int position) {
            title.setText(item.getText());
            number.setText((getItemCount() - position)+"");

        }
    }


}

