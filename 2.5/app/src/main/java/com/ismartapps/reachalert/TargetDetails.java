package com.ismartapps.reachalert;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class TargetDetails implements Parcelable {

    private String name,type,address;
    private double[] current = new double[2],target = new double[2];
    private double radius;

    TargetDetails(String name,String type,String address,double[] current,double[] target)
    {
        this.name = name;
        this.type = type;
        this.address = address;
        this.current = current;
        this.target = target;
    }

    public TargetDetails(Parcel in)
    {
        String[] place = new String[3];
        double[] latLngs = new double[4];
        in.readStringArray(place);
        in.readDoubleArray(latLngs);
        this.radius = in.readDouble();
        this.name = place[0];
        this.type = place[1];
        this.address = place[2];
        this.current[0] = latLngs[0];
        this.current[1] = latLngs[1];
        this.target[0] = latLngs[2];
        this.target[1] = latLngs[3];

    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getCurrent() {
        LatLng latLng = new LatLng(current[0],current[1]);
        return latLng;
    }

    public LatLng getTarget() {
        LatLng latLng = new LatLng(target[0],target[1]);
        return latLng;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    public static final Creator<TargetDetails> CREATOR = new Creator<TargetDetails>() {
        @Override
        public TargetDetails createFromParcel(Parcel in) {
            return new TargetDetails(in);
        }

        @Override
        public TargetDetails[] newArray(int size) {
            return new TargetDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        double[] latLngs = new double[]{this.current[0], this.current[1], this.target[0], this.target[1]};
        parcel.writeStringArray(new String[]{this.name,this.type,this.address});
        parcel.writeDoubleArray(latLngs);
        parcel.writeDouble(this.radius);

    }
}
