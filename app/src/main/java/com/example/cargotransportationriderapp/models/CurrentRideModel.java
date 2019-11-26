package com.example.cargotransportationriderapp.models;

public class CurrentRideModel {

    private String rideId, userId, driverId;

    public CurrentRideModel() {
    }

    public CurrentRideModel(String rideId, String userId, String driverId) {
        this.rideId = rideId;
        this.userId = userId;
        this.driverId = driverId;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRideId() {
        return rideId;
    }
}
