package com.ismartapps.reachalert;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdSize;
import com.adcolony.sdk.AdColonyAdView;
import com.adcolony.sdk.AdColonyAdViewListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivityPrimary extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private ImageView mCurrLoc, mLocationTick,zoomIn,zoomOut,userPic;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String TAG = "MapsActivityPrimary";
    private FusedLocationProviderClient mFusedLocationClient;
    private static float zoom = 20f;
    private TextView targetPlaceName, targetPlaceType, targetPlaceAddress,userName,userEmail,confirm;
    private ImageView[] targetPlaceImages = new ImageView[4];
    private LinearLayout targetPlacePhotosScrollViewLinearLayout;
    private CardView searchContainer;
    private RelativeLayout placeDetailsContainer;
    public LatLng currentLocationLatlng = null,targetLatLng;
    private static Marker marker = null;
    private Bitmap[] targetPlacebitmaps = new Bitmap[4];
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View header;
    private LinearLayout adContainer;
    private TargetDetails targetDetails;
    private boolean fromNotification=false;
    private int created=0;
    private Intent mainIntent;
    private String targetPlaceId=null;
    private Menu menu;
    private SharedPreferences searched,recents,settings;
    private boolean dark;
    private AutocompleteSupportFragment autocompleteFragment;
   //ADCOLONY- private AdColonyAdView drawerAd;
   //FACEBOOK-private AdView adView;


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
        Log.d(TAG, "onCreate");
        mainIntent = getIntent();
        if (mainIntent!=null && mainIntent.getStringExtra("name")!=null) {
            fromNotification=true;
        }
        initVars();
        //ADCOLONY-AdColony.configure(this, "app72332c41df0a460897", "vz1c3ebb9bc632449cad");
        AudienceNetworkAds.initialize(this);
        AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE);
        searched = getSharedPreferences("searched",MODE_PRIVATE);
        recents = getSharedPreferences("recent",MODE_PRIVATE);
        updateSearchMenu();
    }

    private void updateSearched(String locationName,String placeId)
    {
        SharedPreferences.Editor editor = searched.edit();
        if(!placeId.equals(searched.getString("searched_one_pid","Searched Location"))
                && !placeId.equals(searched.getString("searched_two_pid","Searched Location"))
                && !placeId.equals(searched.getString("searched_three_pid","Searched Location"))
        )
        {
        editor.putString("searched_three",searched.getString("searched_two","Searched Location"));
        editor.putString("searched_three_pid",searched.getString("searched_two_pid",""));
        editor.putString("searched_two",searched.getString("searched_one","Searched Location"));
        editor.putString("searched_two_pid",searched.getString("searched_one_pid",""));
        editor.putString("searched_one",locationName);
        editor.putString("searched_one_pid",placeId);
        editor.apply();
        }
        else if (placeId.equals(searched.getString("searched_two_pid","Searched Location")))
        {
            editor.putString("searched_two",searched.getString("searched_one","Searched Location"));
            editor.putString("searched_two_pid",searched.getString("searched_one_pid",""));
            editor.putString("searched_one",locationName);
            editor.putString("searched_one_pid",placeId);
            editor.apply();
        }
        updateSearchMenu();
    }

    private void updateSearchMenu()
    {
        MenuItem searchList[] = {menu.findItem(R.id.searched_one),menu.findItem(R.id.searched_two),menu.findItem(R.id.searched_three)};
        MenuItem clear = menu.findItem(R.id.clear_search);
        searchList[0].setTitle(searched.getString("searched_one","Searched Location"));
        searchList[1].setTitle(searched.getString("searched_two","Searched Location"));
        searchList[2].setTitle(searched.getString("searched_three","Searched Location"));
        for (int i =0;i<3;i++)
        {
            if (searchList[i].getTitle().equals("Searched Location")) {
                if(i!=0)
                    searchList[i].setVisible(false);
                else {
                    searchList[i].setTitle("No Recent Searches");
                    clear.setVisible(false);
                }
            }
            else {
                searchList[i].setVisible(true);
                clear.setVisible(true);
            }
        }
    }

    private void updateRecentMenu()
    {
        MenuItem recentList[] = {menu.findItem(R.id.recent_one),menu.findItem(R.id.recent_two),menu.findItem(R.id.recent_three)};
        MenuItem clear = menu.findItem(R.id.clear_reached);
        recentList[0].setTitle(recents.getString("recent_one","Recent Location"));
        recentList[1].setTitle(recents.getString("recent_two","Recent Location"));
        recentList[2].setTitle(recents.getString("recent_three","Recent Location"));
        for (int i =0;i<3;i++)
        {
            if (recentList[i].getTitle().equals("Recent Location")){
                if(i!=0)
                    recentList[i].setVisible(false);
                else{
                    recentList[i].setTitle("No Recent Locations");
                    clear.setVisible(false);}
            }
            else{
                recentList[i].setVisible(true);
                clear.setVisible(true);
            }
        }
        if(recents.getBoolean("Reached Location?",false)){
            SharedPreferences.Editor editor = recents.edit();
            editor.putBoolean("Reached Location?",false);
            editor.apply();
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.icon_round)
                    .setTitle("Reached Successfully")
                    .setMessage("Reach Alert successfully alerted you on reaching your location")
                    .setPositiveButton("Share", (dialogInterface, i) -> {
                        Toast.makeText(MapsActivityPrimary.this, "Share", Toast.LENGTH_SHORT).show();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey,\n\nReach Alert alerts you on reaching a location you desire when you are not aware.\nBest app for people who often tend to fall asleep during travelling.\nJust Set and Sleep\n\nDownload from Play Store - http://bit.ly/reachAlert");
                        sendIntent.setType("text/plain");
                        Intent chooser = Intent.createChooser(sendIntent, "Share");
                        startActivity(chooser);
                    })
                    .setNegativeButton("Dismiss", (dialogInterface, i) -> {
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        updateRecentMenu();

        if (!checkPermissions()) {
            Log.d(TAG, "onResume: Permission asking");
            requestPermissions();
        } else {
            checkLocation();
        }
    }

    @Override
    public void onBackPressed(){
        if(marker==null){
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);}
        else
            getDeviceLocation(1);
    }

    private void init(){

        Log.d(TAG, "init:(Primary) initializing");

        navigationView.setVisibility(View.VISIBLE);

        mCurrLoc.setOnClickListener(v -> {
            Log.d(TAG, "onClick:(Primary) Location Button is Clicked");
            getDeviceLocation(1);
        });

        SharedPreferences sharedPreferences = getSharedPreferences("userdetails",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String dbname = "User Name";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getEmail()!=null && !user.getEmail().equals(""))
        {
            Log.d(TAG, "init email"+user.getEmail());
            userName.setText(user.getDisplayName());
            userEmail.setText(user.getEmail());
            dbname = user.getDisplayName();
            if (dbname != null) {
                dbname = dbname.replaceAll("\\.","_").replaceAll("#","_").replaceAll("\\$","_").replaceAll("\\[","_").replaceAll("]","_");
            }
            editor.putString("dbname",dbname);
        }
        else
        {
            Log.d(TAG, "init phone number ");
            userEmail.setText(user.getPhoneNumber());
            sharedPreferences = getSharedPreferences("userdetails",MODE_PRIVATE);
            userName.setText(sharedPreferences.getString("name","User Name"));
            dbname = user.getPhoneNumber();
            editor.putString("dbname",dbname);
        }

        editor.apply();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Last Used").child(dbname).child("Last Opened").setValue(new SimpleDateFormat("dd-MMM-yyyy,E hh:mm:ss a zzzz",new Locale("EN")).format(new Date()));
        Log.d(TAG, "init: 123456789012345678901234567890"+new SimpleDateFormat("dd-MMM-yy hh:mm:ss a zzzz",new Locale("EN")).format(new Date()));
        Picasso.get()
                .load(user.getPhotoUrl())
                .error(R.mipmap.ic_user_image)
                .placeholder(R.mipmap.ic_user_image)
                .into(userPic);

        mMap.setOnMapLongClickListener(latLng -> {
                Log.d(TAG, "onMapLongClick: Long Clicked On Map Moving Camera to " + latLng);
                targetPlaceId=null;
                String address = getAddressFromMarker(latLng);
                movecamera(latLng, mMap.getCameraPosition().zoom, "Dropped Pin", address, "Point of interest");
        });

        mMap.setOnPoiClickListener(pointOfInterest -> {
                Log.d(TAG, "onPoiClick: (Primary) Clicked on " + pointOfInterest.name);
                targetPlaceId = pointOfInterest.placeId;
                fetchPlaceDetails(pointOfInterest.placeId);
        });

        mMap.setOnMapClickListener(latLng -> {
            if (marker != null) {
                Log.d(TAG, "onMapClick: (Primary) Single Click On Map");
                getDeviceLocation(1);
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i)
            {
                searchContainer.setVisibility(View.INVISIBLE);
                placeDetailsContainer.setVisibility(View.INVISIBLE);
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                searchContainer.setVisibility(View.VISIBLE);
                placeDetailsContainer.setVisibility(View.VISIBLE);

                if(created==0)
                {
                    created++;
                    drawerLayout.openDrawer(GravityCompat.START,true);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawerLayout.closeDrawer(GravityCompat.START,true);
                        }
                    },500);
                }
                if (mLocationTick.getVisibility()==View.VISIBLE)
                {
                    Log.d(TAG, "onCameraIdle");
                    Animation animation = AnimationUtils.loadAnimation(MapsActivityPrimary.this,R.anim.tick_anim);
                    mLocationTick.startAnimation(animation);
                }
            }
        });

        autocomplete();

        MenuItem theme = menu.findItem(R.id.theme);
        Switch aSwitch = (Switch) theme.getActionView();
        if(dark)
        {
            aSwitch.setChecked(true);
        }
        else
        {
            aSwitch.setChecked(false);
        }

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                dark = b;
                drawerLayout.closeDrawer(GravityCompat.START,true);
                SharedPreferences.Editor themeEditor = settings.edit();
                themeEditor.putBoolean("dark",dark);
                themeEditor.apply();
                changeTheme();
            }
        });

        mLocationTick.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivityPrimary.this, "Confirm Location", Toast.LENGTH_SHORT).show();
            return true;
        });

        mLocationTick.setOnClickListener(v -> {
            Log.d(TAG, "onClick: (Primary) Target Location Confirmed : (Primary) TPID" + targetLatLng+targetPlaceId);
            targetDetails = new TargetDetails(targetPlaceName.getText().toString(),targetPlaceType.getText().toString(),targetPlaceAddress.getText().toString(),new double[]{currentLocationLatlng.latitude,currentLocationLatlng.longitude},new double[]{targetLatLng.latitude,targetLatLng.longitude},targetPlaceId);
            showDialog();
        });

        confirm.setOnClickListener(view -> {
                Log.d(TAG, "onClick: (Primary) Target Location Confirmed : (Primary) TPID" + targetLatLng+targetPlaceId);
                targetDetails = new TargetDetails(targetPlaceName.getText().toString(),targetPlaceType.getText().toString(),targetPlaceAddress.getText().toString(),new double[]{currentLocationLatlng.latitude,currentLocationLatlng.longitude},new double[]{targetLatLng.latitude,targetLatLng.longitude},targetPlaceId);
                showDialog();
        });

        zoomOut.setOnClickListener(view -> {
            CameraPosition zoomOutPosition = new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .tilt(32f).zoom((float) (mMap.getCameraPosition().zoom-(0.75))).build();
            Log.d(TAG, "onClick: Zoom Out");
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(zoomOutPosition));
        });
        
        zoomIn.setOnClickListener(view -> {
            CameraPosition zoomInPosition = new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .tilt(32f).zoom((float) (mMap.getCameraPosition().zoom+(0.75))).build();
            Log.d(TAG, "onClick: Zoom In");
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(zoomInPosition));
        });
        
        zoomIn.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivityPrimary.this, "Zoom In", Toast.LENGTH_SHORT).show();
            return true;
        });

        zoomOut.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivityPrimary.this, "Zoom Out", Toast.LENGTH_SHORT).show();
            return false;
        });
        
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        navigationView.setNavigationItemSelectedListener(menuItem -> {

            switch(menuItem.getItemId())
            {
                case R.id.share:
                    Toast.makeText(MapsActivityPrimary.this, "Share", Toast.LENGTH_SHORT).show();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey,\n\nReach Alert alerts you on reaching a location you desire when you are not aware.\nBest app for people who often tend to fall asleep during travelling.\nJust Set and Sleep\n\nDownload from Play Store - http://bit.ly/reachAlert");
                    sendIntent.setType("text/plain");
                    Intent chooser = Intent.createChooser(sendIntent, "Share");
                    startActivity(chooser);
                    break;

                case R.id.sign_out:
                    FirebaseAuth.getInstance().signOut();
                    clearSharedPreferences(1);
                    Intent signOut = new Intent(this,LoginActivity.class);
                    signOut.putExtra("from","MAP");
                    startActivity(signOut);
                    finish();
                    break;

                case R.id.searched_one:
                    fetchPlaceDetails(searched.getString("searched_one_pid",""));
                    drawerLayout.closeDrawer(GravityCompat.START,true);
                    break;

                case R.id.searched_two:
                    fetchPlaceDetails(searched.getString("searched_two_pid",""));
                    drawerLayout.closeDrawer(GravityCompat.START,true);
                    break;

                case R.id.searched_three:
                    fetchPlaceDetails(searched.getString("searched_three_pid",""));
                    drawerLayout.closeDrawer(GravityCompat.START,true);
                    break;

                case R.id.clear_search:
                    clearSharedPreferences(2);
                    break;

                case R.id.recent_one:
                    if (recents.getString("recent_one","Recent Location").equals("Dropped Pin"))
                    {
                        movecamera(new LatLng(recents.getFloat("recent_one_lat",0),recents.getFloat("recent_one_long",0)),
                                zoom,recents.getString("recent_one","Recent Location"),
                                getAddressFromMarker(new LatLng(recents.getFloat("recent_one_lat",0),recents.getFloat("recent_one_long",0))),
                                "Point of interest");
                    }
                    else
                    {
                        fetchPlaceDetails(recents.getString("recent_one_pid",""));
                    }
                    drawerLayout.closeDrawer(GravityCompat.START,true);
                    break;

                case R.id.recent_two:
                    if (recents.getString("recent_two","Recent Location").equals("Dropped Pin"))
                    {
                        movecamera(new LatLng(recents.getFloat("recent_two_lat",0),recents.getFloat("recent_two_long",0)),
                                zoom,recents.getString("recent_two","Recent Location"),
                                getAddressFromMarker(new LatLng(recents.getFloat("recent_two_lat",0),recents.getFloat("recent_two_long",0))),
                                "Point of interest");
                    }
                    else
                    {
                        fetchPlaceDetails(recents.getString("recent_two_pid",""));
                    }
                    drawerLayout.closeDrawer(GravityCompat.START,true);
                    break;

                case R.id.recent_three:
                    if (recents.getString("recent_three","Recent Location").equals("Dropped Pin"))
                    {
                        movecamera(new LatLng(recents.getFloat("recent_three_lat",0),recents.getFloat("recent_three_long",0)),
                                zoom,recents.getString("recent_three","Recent Location"),
                                getAddressFromMarker(new LatLng(recents.getFloat("recent_three_lat",0),recents.getFloat("recent_three_long",0))),
                                "Point of interest");
                    }
                    else
                    {
                        fetchPlaceDetails(recents.getString("recent_three_pid",""));
                    }
                    drawerLayout.closeDrawer(GravityCompat.START,true);
                    break;

                case R.id.clear_reached:
                    clearSharedPreferences(3);
                    break;

                case R.id.theme:
                    dark = !dark;
                    aSwitch.setChecked(dark);
                    drawerLayout.closeDrawer(GravityCompat.START,true);
                    SharedPreferences.Editor themeEditor = settings.edit();
                    themeEditor.putBoolean("dark",dark);
                    themeEditor.apply();
                    changeTheme();
                    break;
            }

            return true;
        });
    }

    private void changeTheme()
    {
        this.recreate();
    }

    private void showDialog()
    {
        ConfirmationDialog confirmationDialog = new ConfirmationDialog(this,targetDetails.getName(),targetDetails.getAddress(),targetDetails,this) {
            @Override
            public void onClick(View view) {
                switch(view.getId())
                {
                    case R.id.confirm_button:
                        Intent intent = new Intent(MapsActivityPrimary.this,MapsActivitySecondary.class);
                        intent.putExtra("targetDetails", (Parcelable) targetDetails);
                        MapsActivityPrimary.this.startActivity(intent);
                        /*ADCOLONY-if(confirmAd!=null)
                        confirmAd.destroy();*/
                        /*FACEBOOK-if(adView!=null)adView.destroy();*/
                        dismiss();
                        break;

                    case R.id.no_button:
                        /*ADCOLONY-if(confirmAd!=null)
                        confirmAd.destroy();*/
                        /*FACEBOOK-if(adView!=null)adView.destroy();*/
                        dismiss();
                        break;
                }
            }};
        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    private void clearSharedPreferences(int o)
    {

        SharedPreferences.Editor editor;
        if(o==1){
            SharedPreferences sharedPreferences = getSharedPreferences("userdetails",MODE_PRIVATE);
            editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            searched = getSharedPreferences("searched",MODE_PRIVATE);
            editor = searched.edit();
            editor.clear();
            editor.apply();
            SharedPreferences recents = getSharedPreferences("recent", MODE_PRIVATE);
            editor = recents.edit();
            editor.clear();
            editor.apply();
            updateSearchMenu();
            updateRecentMenu();
        }
        else if(o==2)
        {
            searched = getSharedPreferences("searched",MODE_PRIVATE);
            editor = searched.edit();
            editor.clear();
            editor.apply();
            updateSearchMenu();
        }
        else if(o==3)
        {
            SharedPreferences recents = getSharedPreferences("recent", MODE_PRIVATE);
            editor = recents.edit();
            editor.clear();
            editor.apply();
            updateSearchMenu();
            updateRecentMenu();
        }

    }

    private void initVars(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        String qwert = getResources().getString(R.string.google_maps_key);
        Places.initialize(this, qwert);
        mCurrLoc = (ImageView) findViewById(R.id.location_btn_img);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        header = navigationView.getHeaderView(0);
        userName = (TextView) header.findViewById(R.id.user_name);
        userEmail = (TextView) header.findViewById(R.id.user_email);
        confirm = (TextView) findViewById(R.id.confirm);
        userPic = (ImageView) header.findViewById(R.id.user_image);
        targetPlaceName = (TextView) findViewById(R.id.place_name);
        targetPlaceType = (TextView) findViewById(R.id.place_type);
        targetPlaceAddress = (TextView) findViewById(R.id.place_address);
        targetPlaceImages[0] = (ImageView) findViewById(R.id.place_images_1);
        targetPlaceImages[1] = (ImageView) findViewById(R.id.place_images_2);
        targetPlaceImages[2] = (ImageView) findViewById(R.id.place_images_3);
        targetPlaceImages[3] = (ImageView) findViewById(R.id.place_images_4);
        mLocationTick = (ImageView) findViewById(R.id.place_tick_image);
        zoomIn = (ImageView) findViewById(R.id.zoom_in);
        zoomIn.setVisibility(View.VISIBLE);
        zoomOut = (ImageView) findViewById(R.id.zoom_ot);
        zoomOut.setVisibility(View.VISIBLE);
        targetPlacePhotosScrollViewLinearLayout = (LinearLayout) findViewById(R.id.place_images_scroll_linearLayout);
        adContainer = findViewById(R.id.adBannerDrawerView);
        placeDetailsContainer = findViewById(R.id.Place_details_view_relative_container);
        searchContainer = findViewById(R.id.searchbar_layout_card);
        /*ADCOLONY-AdColonyAdViewListener listener = new AdColonyAdViewListener() {
            @Override
            public void onRequestFilled(AdColonyAdView ad) {
                adView.addView(ad);
                drawerAd = ad;
            }
        };

        AdColony.requestAdView("vz1c3ebb9bc632449cad", listener, AdColonyAdSize.BANNER);*/

        /*FACEBOOK-adView = new AdView(this,"478651842722184_479882535932448", AdSize.BANNER_HEIGHT_50);
        adContainer.addView(adView);
        adView.loadAd();*/
        menu = navigationView.getMenu();
    }

    private void checkLocation(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "checkLocation: checking....");
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Log.d(TAG, "checkLocation: Not On");
            new AlertDialog.Builder(this)
                    .setTitle(R.string.gps_not_found_title)  // GPS not found
                    .setMessage(R.string.gps_not_found_message) // Want to enable?
                    .setPositiveButton(R.string.okay, (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .show();
        }
        else{
            Log.d(TAG, "checkLocation: On Initing Map");
            initMap();
        }
    }

    private void autocomplete(){
        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setHint("Search here");

        if(dark){
        EditText editText = (EditText) autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        editText.setTextColor(getResources().getColor(R.color.white));
        }


        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place Selected From Search: (Primary) " + place.getName());
                targetPlaceId = place.getId();
                updateSearched(place.getName(),place.getId());
                fetchPlaceDetails(place.getId());
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: (Primary) " + status);
            }
        });
    }

    @Override
    protected void onDestroy() {
        /*if(drawerAd!=null)
        drawerAd.destroy();*/

        /*FACEBOOK-if(adView!=null)adView.destroy();*/

        super.onDestroy();
    }

    private String getAddressFromMarker(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String address="";
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            Address obj = addresses.get(0);
            address = obj.getAddressLine(0);
            Log.d(TAG, "getAddressFromMarker: (Primary) "+address);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private void fetchPlaceDetails(String placeId){
        targetPlaceImages[0].setImageResource(R.color.imageColor1);
        targetPlaceImages[1].setImageResource(R.color.imageColor2);
        targetPlaceImages[2].setImageResource(R.color.imageColor3);
        targetPlaceImages[3].setImageResource(R.color.imageColor4);
        Log.d(TAG, "fetchPlaceDetails: TPID"+targetPlaceId+" , "+placeId);
        targetPlaceId = placeId;
        targetPlacebitmaps[0]=targetPlacebitmaps[1]=targetPlacebitmaps[2]=targetPlacebitmaps[3]=null;

        targetPlacePhotosScrollViewLinearLayout.getLayoutParams().height = (int) getResources().getDimension(R.dimen.dp0);

        PlacesClient placesClient = Places.createClient(this);

        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS,Place.Field.ADDRESS,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.TYPES);

        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, fields).build();

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            Log.d(TAG, "fetchPlaceDetails: (Primary) "+place.getName()+" ("+place.getLatLng()+") ,"+place.getAddress()+" , "+place.getTypes());
            String placeType="POINT_OF_INTEREST";
            if(!place.getTypes().get(0).name().equals("POINT_OF_INTEREST")){
                String temp="";
                placeType=temp;
                if(!place.getTypes().get(0).name().equals("POLITICAL")) {
                    temp = place.getTypes().get(0).name();
                    placeType = temp.substring(0, 1).toUpperCase() + temp.substring(1).toLowerCase();
                }
                if(place.getTypes().get(place.getTypes().size()-1).name().equals("POLITICAL")){
                    placeType=place.getTypes().get(place.getTypes().size()-2).toString();
                }
                else if (place.getTypes().get(place.getTypes().size()-1).name().equals("ROUTE")){
                    placeType="ROUTE";
                }
                else{
                    for (int j = 1; !place.getTypes().get(j).name().equals("POINT_OF_INTEREST") && !place.getTypes().get(j).name().equals("ESTABLISHMENT"); j++) {
                        Log.d(TAG, "fetchPlaceDetails: "+place.getTypes().get(j).name().equals("POLITICAL")+" "+place.getTypes().get(j).name());
                            temp = place.getTypes().get(j).name();
                            placeType = placeType + " / " + temp.substring(0, 1).toUpperCase() + temp.substring(1).toLowerCase();
                    }
                }
                    placeType = placeType.replace(" ", "_");
                    placeType = placeType.toLowerCase();
                    placeType = placeType.substring(0,1).toUpperCase()+placeType.substring(1);
                    while (placeType.contains("_")) {
                        Log.d(TAG, "fetchPlaceDetails:"+placeType);
                        placeType = placeType.substring(0, placeType.indexOf("_") + 1) + placeType.substring(placeType.indexOf("_") + 1, placeType.indexOf("_") + 2).toUpperCase() + placeType.substring(placeType.indexOf("_") + 2);
                        placeType = placeType.replaceFirst("_", " ");
                    }
                    if(placeType.substring(1,2).equals("/")){
                        placeType=placeType.substring(3);
                    }
            }

            if(placeType.contains("Administrative Area Level 1") || placeType.contains("Locality"))
                zoom=10f;
            else if(placeType.contains("Neighborhood") || placeType.contains("Sublocality"))
                zoom=15f;
            else if(placeType.contains("Natural Feature") || placeType.contains("Country"))
                zoom=5f;
            else zoom=20f;
            movecamera(place.getLatLng(),zoom,place.getName(),place.getAddress(),placeType);


            if (place.getPhotoMetadatas() != null) {
                if (place.getPhotoMetadatas().size() > 0) {
                    targetPlacePhotosScrollViewLinearLayout.getLayoutParams().height = (int) getResources().getDimension(R.dimen.dp100);
                }

                for (int i = 0; i < place.getPhotoMetadatas().size() && i < 4; i++) {
                    // Get the photo metadata.
                    PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(i);

                    // Get the attribution text.
                    String attributions = photoMetadata.getAttributions();
                    // Create a FetchPhotoRequest.
                    FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .build();
                    int finalI = i;
                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
                        targetPlacebitmaps[finalI] = fetchPhotoResponse.getBitmap();
                        targetPlaceImages[finalI].setImageBitmap(targetPlacebitmaps[finalI]);
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();
                            Log.e(TAG, "Place not found: (Primary) " + exception.getMessage()+" , "+statusCode);
                        }
                    });
                }
            }
        });

    }

    private void movecamera(LatLng latLng, float zoom, String title, String placeAddress, String placeType)
    {
        Log.d(TAG, "movecamera: (Primary) moving the camera to lat : "+latLng.latitude+" lang : "+latLng.longitude+" Name: "+title+" Type: "+placeType+" Address: "+placeAddress);

        if (marker != null) {
            marker.remove();
            marker=null;
        }

        mLocationTick.setVisibility(View.INVISIBLE);
        confirm.setText("");

        CameraPosition movingPlace = new CameraPosition.Builder()
                .target(latLng)
                .tilt(32f).zoom(zoom).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(movingPlace));

        if (!title.equals("Current Location")){
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(title).draggable(true));
            marker.setDraggable(true);
            mLocationTick.setVisibility(View.VISIBLE);
            confirm.setText("Confirm Location");
            targetLatLng=latLng;
        }
        else
            currentLocationLatlng = latLng;

        if (title.equals("Current Location") || title.equals("Dropped Pin")) {

            targetPlaceImages[0].setImageResource(R.color.imageColor1);
            targetPlaceImages[1].setImageResource(R.color.imageColor2);
            targetPlaceImages[2].setImageResource(R.color.imageColor3);
            targetPlaceImages[3].setImageResource(R.color.imageColor4);

            targetPlacePhotosScrollViewLinearLayout.getLayoutParams().height = (int) getResources().getDimension(R.dimen.dp0);
        }

        DecimalFormat decimalFormat = new DecimalFormat("#0.00000");
        String latlong = decimalFormat.format(latLng.latitude)+" , "+decimalFormat.format(latLng.longitude);

        targetPlaceName.setText(title);

        if (!placeType.equals("Point of interest")) {
            targetPlaceType.setText(placeType);
        }
        else {
            targetPlaceType.setText(latlong);
        }

        targetPlaceAddress.setText(placeAddress);

        hideSoftKeyboard();

    }

    private void initMap()
    {
        Log.d(TAG, "initMap: (Primary) initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_primary);
        mapFragment.getMapAsync(this);
    }

    private void getDeviceLocation(int t)
    {
        Log.d(TAG, "getDeviceLocation: (Primary) getting device location.");

        try
        {
            if(checkPermissions()) {

                final Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: (Primary) found location");
                        Location currentLocation = (Location) task.getResult();
                        currentLocationLatlng=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(toBounds(currentLocationLatlng,1000000)));
                        String address = getAddressFromMarker(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));
                        if(t==1)movecamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),15f,"Current Location",address,"Point of interest");
                    }
                    else{
                        Log.d(TAG, "onComplete: (Primary) location not found");
                        Toast.makeText(MapsActivityPrimary.this, "Unable to find the location.Please Check the Internet", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }catch (SecurityException e)
        {
            Log.d(TAG, "getDeviceLocation: (Primary) SecurityException"+e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: (Primary) map is ready");
        mMap = googleMap;

        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            init();

            if (fromNotification)
            {
                getDeviceLocation(0);
                fromNotification=false;
                targetPlaceId = mainIntent.getStringExtra("placeId");
                Log.d(TAG, "onMapReady: TPID"+targetPlaceId);
                double[] latLng = mainIntent.getExtras().getDoubleArray("latlng");
                targetLatLng = new LatLng(latLng[0],latLng[1]);
                String text = mainIntent.getStringExtra("text");
                if(targetPlaceId==null)
                {
                    movecamera(targetLatLng,15f,"Dropped Pin",getAddressFromMarker(targetLatLng),"Point of interest");
                }
                else
                    fetchPlaceDetails(targetPlaceId);
            }
            else
                getDeviceLocation(1);

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if(dark)
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.mapstyle_night));


        }
    }

    private boolean checkPermissions()  {
        int fineLocationPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);

        int backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        return (fineLocationPermissionState == PackageManager.PERMISSION_GRANTED)/* &&
                (backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED)*/;
    }

    private void requestPermissions() {

        boolean permissionAccessFineLocationApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean shouldProvideRationale =
                permissionAccessFineLocationApproved/* && backgroundLocationPermissionApproved*/;
        if (shouldProvideRationale) {
            Log.i(TAG, "Permissions Ok");
            checkLocation();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onrequestPermissionsResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");

            } else if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                checkLocation();

            }
        }
    }

    private void hideSoftKeyboard()
    {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

}
