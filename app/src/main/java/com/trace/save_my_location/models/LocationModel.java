package com.trace.save_my_location.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by akhil on 7/2/16.
 * This class holds the details of the location
 */
public class LocationModel implements Parcelable {

    private String address, notification;
    private double longitude, latitude;
    private long id;

    public LocationModel() {

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    protected LocationModel(Parcel in) {
        address = in.readString();
        notification = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        id = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(notification);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeLong(id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<LocationModel> CREATOR =
            new Parcelable.Creator<LocationModel>() {
        @Override
        public LocationModel createFromParcel(Parcel in) {
            return new LocationModel(in);
        }

        @Override
        public LocationModel[] newArray(int size) {
            return new LocationModel[size];
        }
    };
}
