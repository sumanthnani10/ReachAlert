package com.ismartapps.reachalert;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserLatestData {

    public String email,name,targetName,targetAddress;
    public LatLng targetlatLng;
    public int hour,minute;

    public UserLatestData(){}

    public UserLatestData(String email,String name,String targetName,String targetAddress,LatLng targetlatLng,int hour,int minute)
    {
        this.email=email;
        this.name=name;
        this.targetName=targetName;
        this.targetAddress=targetAddress;
        this.targetlatLng=targetlatLng;
        this.hour=hour;
        this.minute=minute;
    }
}
