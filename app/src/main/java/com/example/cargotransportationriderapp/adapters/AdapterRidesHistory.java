package com.example.cargotransportationriderapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cargotransportationriderapp.R;
import com.example.cargotransportationriderapp.models.RidesHistory;

import java.util.List;

public class AdapterRidesHistory extends RecyclerView.Adapter<AdapterRidesHistory.Holder> {

    Context context;
    List<RidesHistory> list;

    public AdapterRidesHistory(Context context, List<RidesHistory> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AdapterRidesHistory.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_rides_history_card, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRidesHistory.Holder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class Holder extends RecyclerView.ViewHolder {
        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
