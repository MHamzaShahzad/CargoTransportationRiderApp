package com.example.cargotransportationriderapp;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cargotransportationriderapp.adapters.AdapterRidesHistory;
import com.example.cargotransportationriderapp.controllers.MyFirebaseDatabase;
import com.example.cargotransportationriderapp.models.RidesHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentRidesHistory extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    Context context;
    View view;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView ridesHistoryRecyclerView;
    private AdapterRidesHistory adapterRidesHistory;

    private List<RidesHistory> ridesHistoryList;

    public FragmentRidesHistory() {
        // Required empty public constructor
        ridesHistoryList = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = container.getContext();
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_rides_history, container, false);

            ridesHistoryRecyclerView = view.findViewById(R.id.rides_history_recycler_view);
            ridesHistoryRecyclerView.setHasFixedSize(true);
            ridesHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            adapterRidesHistory = new AdapterRidesHistory(context, ridesHistoryList);
            ridesHistoryRecyclerView.setAdapter(adapterRidesHistory);

            initSwipeRefreshLayout();


        }
        return view;
    }

    @Override
    public void onRefresh() {
        loadRidesHistoryRecyclerViewData();
    }

    private void initSwipeRefreshLayout(){
        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        /*
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);

                // Fetching data from server
                loadRidesHistoryRecyclerViewData();
            }
        });
    }

    private void loadRidesHistoryRecyclerViewData() {

        // Showing refresh animation before making http call
        mSwipeRefreshLayout.setRefreshing(true);

        MyFirebaseDatabase.RIDES_REFERENCE.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Stopping swipe refresh
                mSwipeRefreshLayout.setRefreshing(false);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Stopping swipe refresh
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

}
