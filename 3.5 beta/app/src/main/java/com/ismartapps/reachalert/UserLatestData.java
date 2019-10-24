package com.ismartapps.reachalert;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserLatestData {

    public String email,name,targetName,targetAddress,time;
    public LatLng targetlatLng;

    public UserLatestData(){}

    public UserLatestData(String email,String name,String targetName,String targetAddress,LatLng targetlatLng,String time)
    {
        this.email=email;
        this.name=name;
        this.targetName=targetName;
        this.targetAddress=targetAddress;
        this.targetlatLng=targetlatLng;
        this.time=time;
    }
}