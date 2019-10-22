package com.ismartapps.reachalert;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class MapsActivityFinal extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private TextView targetPlaceName,targetPlaceType,targetPlaceAddress,cancel;
    private CardView searchCard, radiusControlCard;
    private SeekBar radiusController;
    private Circle circle = null;
    private Marker marker = null;
    private ImageView mCurrLoc,zoomIn,zoomOut;
    private ImageView mCancel;
    private LatLng targetLatLng;
    private static FusedLocationProviderClient mFusedLocationClient;
    private static LocationRequest mLocationRequest;
    public static double[] tempd = new double[3];
    private static AlarmManager alarmManager;
    private static DatabaseReference databaseReference;
    private TargetDetails targetDetails;
    private DrawerLayout drawerLayout;
    private int clickCount=0;
    private String userName;
    //FACEBOOK-private InterstitialAd interstitialAd;

    private static final String TAG = MapsActivityFinal.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver = new MyReceiver();

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    private Activity activity = this;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setActivity(activity,targetLatLng,targetDetails.getRadius(),targetDetails.getName());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mService = null;
            mBound = false;
        }
    };
    private SharedPreferences settings;
    private boolean dark;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settings = getSharedPreferences("settings",MODE_PRIVATE);
        dark = settings.getBoolean("dark",false);
        if(dark)
            setContentView(R.layout.activity_maps_dark);
        else{
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_maps);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_primary);
        mapFragment.getMapAsync(this);
        //ACOLONY-AdColony.configure(this, "app72332c41df0a460897", "vze1d0cb71432c4109b3");

        /*FACEBOOK-AudienceNetworkAds.initialize(this);
        AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE);
        interstitialAd = new InterstitialAd(this, "478651842722184_480146339239401");
        interstitialAd.loadAd();*/

        Log.d(TAG, "onCreate: Final");
    }

    @Override
    protected void onDestroy() {
        stopTracking();
        if(mService.isCancelled)updateCancelled();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    private void initVars(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        targetPlaceName = (TextView) findViewById(R.id.place_name);
        targetPlaceType = (TextView) findViewById(R.id.place_type);
        targetPlaceAddress = (TextView) findViewById(R.id.place_address);
        cancel = findViewById(R.id.confirm);
        mCurrLoc = (ImageView) findViewById(R.id.location_btn_img);
        searchCard = (CardView) findViewById(R.id.searchbar_layout_card);;
        mCancel = (ImageView) findViewById(R.id.place_tick_image);
        radiusControlCard = (CardView) findViewById(R.id.radius_controller_container_card);
        zoomIn = (ImageView) findViewById(R.id.zoom_in);
        zoomIn.setVisibility(View.INVISIBLE);
        zoomOut = (ImageView) findViewById(R.id.zoom_ot);
        zoomOut.setVisibility(View.INVISIBLE);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        drawerLayout = findViewById(R.id.drawer_layout);
    }

    private void init() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Intent intent = getIntent();
        targetDetails = intent.getExtras().getParcelable("targetDetails");
        marker=null;
        cancel.setText("Stop");
        circle=null;
        if(dark)
            mCancel.setImageResource(R.mipmap.ic_launcher_cancel_dark);
        else
            mCancel.setImageResource(R.mipmap.ic_launcher_cancel);
        mCancel.setVisibility(View.VISIBLE);
        targetPlaceName.setText(targetDetails.getName());
        targetPlaceType.setText(getResources().getString(R.string.target_yet_to_reach));
        targetPlaceAddress.setText(targetDetails.getAddress());
        radiusControlCard.setVisibility(View.INVISIBLE);
        mCurrLoc.setVisibility(View.INVISIBLE);
        searchCard.setVisibility(View.INVISIBLE);
        targetLatLng = targetDetails.getTarget();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng,15f));
        marker = mMap.addMarker(new MarkerOptions().position(targetLatLng).title(targetPlaceName.getText().toString()).draggable(false));
        circle = mMap.addCircle(new CircleOptions()
                .center(targetLatLng)
                .strokeWidth(3)
                .radius(targetDetails.getRadius())
                .strokeColor(R.color.imageColor3));


        tempd[0]=targetLatLng.latitude;
        tempd[1]=targetLatLng.longitude;
        tempd[2]=circle.getRadius();

        Calendar calendar = Calendar.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("userdetails",MODE_PRIVATE);

        if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("") && FirebaseAuth.getInstance().getCurrentUser().getEmail()!=null) {
            uploadData(true,FirebaseAuth.getInstance().getCurrentUser().getEmail(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                    targetPlaceName.getText().toString(), targetPlaceAddress.getText().toString(), targetLatLng,
                    calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            userName = sharedPreferences.getString("dbname", "User Name");
        }
        else {
            uploadData(false,FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), sharedPreferences.getString("name", "User Name"),
                    targetPlaceName.getText().toString(), targetPlaceAddress.getText().toString(), targetLatLng,
                    calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            userName = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        }

        scheduleNotification();

        getLastLocation();

        mCancel.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivityFinal.this, "Cancel Tracking", Toast.LENGTH_SHORT).show();
            return true;
        });

        int i=0;
        ConfirmationDialog confirmationDialog = new ConfirmationDialog(this,"Cancel Tracking","Do you want to cancel tracking?",activity){
            @Override
            public void onClick(View view) {
                switch(view.getId())
                {
                    case R.id.confirm_button:
                        /*ADCOLONY-if(confirmAd!=null)
                        confirmAd.destroy();*/
                        /*FACEBOOK-if(adView!=null)adView.destroy();*/

                        Toast.makeText(MapsActivityFinal.this, "Stopped Tracking", Toast.LENGTH_SHORT).show();
                        mService.removeLocationUpdates();
                        updateCancelled();
                        stopTracking();

                        //Show Ad
                        showAd();

                        dismiss();
                        break;

                    case R.id.no_button:
                        /*ADCOLONY-if(confirmAd!=null)
                        confirmAd.destroy();*/
                        /*FACEBOOK-if(adView!=null)adView.destroy();*/
                        dismiss();
                        break;
                }
            }
        };
        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mCancel.setOnClickListener(view -> {
            confirmationDialog.show();
        });

        cancel.setOnClickListener(view ->
        {
            confirmationDialog.show();
        });
    }

    private void startTracking()
    {
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));

    }

    private void stopTracking(){
        mService.removeLocationUpdates();
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                float[] results = new float[1];
                Location.distanceBetween(targetLatLng.latitude,targetLatLng.longitude,location.getLatitude(),location.getLongitude(),results);
                moveCamera(targetLatLng,new LatLng(location.getLatitude(),location.getLongitude()));
                Log.d(TAG, "onReceive: "+results[0]+","+targetDetails.getRadius());

                if(results[0]<=targetDetails.getRadius())
                {
                    Log.d(TAG, "onReceive Reached: "+results[0]+","+targetDetails.getRadius());
                    stopTracking();
                    Utils.sendNotificationOnComplete(context);
                    Utils.playRing(context);
                    updateReached();
                    finish();
                }
            }
        }
    }

    private void uploadData(boolean mail,String email,String name,String targetName,String targetAddress,LatLng targetlatLng,int date,int month,int year,int hour,int minute) {
        UserLatestData user = new UserLatestData(email,name,targetName,targetAddress,targetlatLng,date,month,year,hour,minute);
        if (mail){
        databaseReference.child("Last Used").child(name).setValue(user);
        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()!=null)
        databaseReference.child("Last Used").child(name).child("Phone").setValue(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        databaseReference.child("Last Used").child(name).child("Status").child("Reached").setValue(false);
        databaseReference.child("Last Used").child(name).child("Status").child("Cancelled").setValue(false);
        databaseReference.child("Last Used").child(name).child("Status").child("Running").setValue(true);
        databaseReference.child("Last Used").child(name).child("Status").child("Stopped Ring").setValue(false);
        }
        else
        {
            databaseReference.child("Last Used").child(email).setValue(user);
            if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()!=null)
            databaseReference.child("Last Used").child(email).child("Phone").setValue(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
            databaseReference.child("Last Used").child(email).child("Status").child("Reached").setValue(false);
            databaseReference.child("Last Used").child(email).child("Status").child("Cancelled").setValue(false);
            databaseReference.child("Last Used").child(email).child("Status").child("Running").setValue(true);
            databaseReference.child("Last Used").child(email).child("Status").child("Stopped Ring").setValue(false);

        }
    }

    private void updateReached()
    {
        SharedPreferences recents = getSharedPreferences("recent", MODE_PRIVATE);
        SharedPreferences.Editor editor = recents.edit();
        editor.putBoolean("Reached Location?",true);
        editor.apply();
        databaseReference.child("Last Used").child(userName).child("Status").child("Reached").setValue(true);
        databaseReference.child("Last Used").child(userName).child("Status").child("Running").setValue(false);
    }

    private void updateCancelled()
    {
        databaseReference.child("Last Used").child(userName).child("Status").child("Reached").setValue(true);
        databaseReference.child("Last Used").child(userName).child("Status").child("Cancelled").setValue(true);
        databaseReference.child("Last Used").child(userName).child("Status").child("Running").setValue(false);
    }

    private void updateRecents(String locationName,LatLng latLng,String placeId)
    {
        SharedPreferences recents = getSharedPreferences("recent", MODE_PRIVATE);
        SharedPreferences.Editor editor = recents.edit();

        if((placeId==null && (!(latLng.latitude == recents.getFloat("recent_one_lat",0) && latLng.longitude == recents.getFloat("recent_one_long",0))
                && !(latLng.latitude == recents.getFloat("recent_two_lat",0) && latLng.longitude == recents.getFloat("recent_two_long",0))
                && !(latLng.latitude == recents.getFloat("recent_three_lat",0) && latLng.longitude == recents.getFloat("recent_three_long",0))))
          || (placeId!=null && (!placeId.equals(recents.getString("recent_one_pid",null)) && !placeId.equals(recents.getString("recent_two_pid",null))
                && !placeId.equals(recents.getString("recent_three_pid",null))))
        )
        {
            Log.d(TAG, "updateRecents: 1:"+latLng.latitude+" : "+recents.getFloat("recent_one_lat",0)+"2:"+latLng.longitude+" : "+recents.getFloat("recent_one_long",0));
            editor.putString("recent_three", recents.getString("recent_two", "Recent Location"));
            editor.putFloat("recent_three_lat", recents.getFloat("recent_two_lat", 0));
            editor.putFloat("recent_three_long", recents.getFloat("recent_two_long", 0));
            editor.putString("recent_three_pid", recents.getString("recent_two_pid", ""));
            editor.putString("recent_two", recents.getString("recent_one", "Recent Location"));
            editor.putFloat("recent_two_lat", recents.getFloat("recent_one_lat", 0));
            editor.putFloat("recent_two_long", recents.getFloat("recent_one_long", 0));
            editor.putString("recent_two_pid", recents.getString("recent_one_pid", ""));
            editor.putString("recent_one", locationName);
            editor.putFloat("recent_one_lat", (float) latLng.latitude);
            editor.putFloat("recent_one_long", (float) latLng.longitude);
            editor.putString("recent_one_pid", placeId);
            editor.apply();
        }
        else if(latLng.latitude == recents.getFloat("recent_two_lat",0) && latLng.longitude == recents.getFloat("recent_two_long",0))
        {
            Log.d(TAG, "updateRecents: 2");;
            editor.putString("recent_two", recents.getString("recent_one", "Recent Location"));
            editor.putFloat("recent_two_lat", recents.getFloat("recent_one_lat", 0));
            editor.putFloat("recent_two_long", recents.getFloat("recent_one_long", 0));
            editor.putString("recent_two_pid", recents.getString("recent_one_pid", ""));
            editor.putString("recent_one", locationName);
            editor.putFloat("recent_one_lat", (float) latLng.latitude);
            editor.putFloat("recent_one_long", (float) latLng.longitude);
            editor.putString("recent_one_pid", placeId);
            editor.apply();
        }
    }

    void scheduleNotification()
    {
        Calendar calendar = Calendar.getInstance();
        PendingIntent notificationPendingIntent;
        Intent notificationIntent = new Intent(this,RemainderReceiver.class);
        notificationIntent.putExtra("text",targetPlaceName.getText().toString());
        notificationIntent.putExtra("placeId",targetDetails.getPlaceId());
        notificationIntent.putExtra("latlng",new double[]{targetLatLng.latitude,targetLatLng.longitude});

        if (targetDetails.getPlaceId()!=null)
            updateRecents(targetPlaceName.getText().toString(),new LatLng(targetLatLng.latitude,targetLatLng.longitude),targetDetails.getPlaceId());
        else
            updateRecents(targetPlaceName.getText().toString(),new LatLng(targetLatLng.latitude,targetLatLng.longitude),"");

        Log.d(TAG, "scheduleNotification: "+targetPlaceName.getText().toString()+" ,"+targetDetails.getPlaceId()+" ,"+targetDetails.getPlaceId());

        if(calendar.get(Calendar.HOUR_OF_DAY)>=0 && calendar.get(Calendar.HOUR_OF_DAY)<12){
            notificationIntent.putExtra("id",3);
        notificationPendingIntent = PendingIntent.getBroadcast(this,3,notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);}
        else if(calendar.get(Calendar.HOUR_OF_DAY)>=12 && calendar.get(Calendar.HOUR_OF_DAY)<17)
        {notificationIntent.putExtra("id",4);
            notificationPendingIntent = PendingIntent.getBroadcast(this,4,notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);}
        else
        {notificationIntent.putExtra("id",5);
            notificationPendingIntent = PendingIntent.getBroadcast(this,5,notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);}

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+AlarmManager.INTERVAL_DAY,AlarmManager.INTERVAL_DAY,notificationPendingIntent);
    }

    @Override
    public void onBackPressed(){
        stopTracking();
        showAd();
    }

    public static void moveCamera(LatLng target, LatLng current){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(target);
        builder.include(current);
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: (Final) map is ready");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setPadding(0,0,0,400);
        initVars();
        init();
        if(dark)
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.mapstyle_night));
    }

    private void getLastLocation() {
        Log.d(TAG, "getLastLocation");
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Location mLocation = task.getResult();
                                Popup popup = new Popup(MapsActivityFinal.this);
                                popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                popup.show();
                                popup.setOnDismissListener(dialogInterface -> {
                                    startTracking();
                                    moveCamera(targetLatLng,new LatLng(mLocation.getLatitude(),mLocation.getLongitude()));
                                });

                            } else {
                                Popup popup = new Popup(MapsActivityFinal.this);
                                popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                popup.show();
                                popup.setOnDismissListener(dialogInterface -> startTracking());
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Popup popup = new Popup(MapsActivityFinal.this);
            popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popup.show();
            popup.setOnDismissListener(dialogInterface -> startTracking());
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    void showAd()
    {
        finish();

        /*FACEBOOK-final Handler handler = new Handler();
        handler.postDelayed(() -> {
            Log.d(TAG, "showAd: 1");
            if (!interstitialAd.isAdLoaded())
            {
                Log.d(TAG, "showAd: 2");
                finish();
            }
        },10000);

        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.d(TAG, "onInterstitialDisplayed: ");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.d(TAG, "onInterstitialDismissed: ");
                finish();
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "onError: ");
                finish();

            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(TAG, "onAdLoaded: ");
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(TAG, "onAdClicked: ");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(TAG, "onLoggingImpression: ");
            }
        });*/

        /*ADCOLONY-AdColonyInterstitialListener listener = new AdColonyInterstitialListener() {
            @Override
            public void onRequestFilled(AdColonyInterstitial ad) {
                ad.show();
            }

            @Override
            public void onOpened(AdColonyInterstitial ad) {
                super.onOpened(ad);
            }

            @Override
            public void onClicked(AdColonyInterstitial ad) {
                super.onClicked(ad);
                finish();
            }

            @Override
            public void onExpiring(AdColonyInterstitial ad) {
                super.onExpiring(ad);
                finish();
            }

            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                super.onRequestNotFilled(zone);
                finish();
            }

            @Override
            public void onClosed(AdColonyInterstitial ad) {
                super.onClosed(ad);
                finish();
            }
        };
        AdColony.requestInterstitial("vze1d0cb71432c4109b3",listener);*/
        /*InterstitialAd mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("CA06D3EADBD276408AD961E110AFC903").build());

        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if(mInterstitialAd.isLoaded()){
                    Log.d(TAG, "onAdLoaded: Showing Ad");
                    mInterstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "onAdFailedToLoad");
                finish();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed");
                finish();
            }
        });*/

    }
}