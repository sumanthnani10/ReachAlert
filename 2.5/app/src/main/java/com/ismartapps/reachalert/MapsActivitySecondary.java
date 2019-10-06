package com.ismartapps.reachalert;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

public class MapsActivitySecondary extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String TAG = "Maps Activity Secondary";
    private TextView targetPlaceName,targetPlaceType,targetPlaceAddress,confirm;
    private CardView searchCard, radiusControlCard;
    private SeekBar radiusController;
    private Circle circle = null;
    private Marker marker = null;
    private ImageView mCurrLoc;
    private ImageView mRadiusTick,zoomIn,zoomOut;
    private LatLng targetLatLng;
    private InterstitialAd mInterstitialAd;
    private TargetDetails targetDetails;
    private int clickCount=0;
    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d(TAG, "initMap: (Secondary) initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_primary);
        mapFragment.getMapAsync(this);
        Log.d(TAG, "onCreate: Secondary");
    }

    private void initVars(){
        targetPlaceName = (TextView) findViewById(R.id.place_name);
        targetPlaceType = (TextView) findViewById(R.id.place_type);
        targetPlaceAddress = (TextView) findViewById(R.id.place_address);
        confirm = findViewById(R.id.confirm);
        mCurrLoc = (ImageView) findViewById(R.id.location_btn_img);
        searchCard = (CardView) findViewById(R.id.searchbar_layout_card);;
        mRadiusTick = (ImageView) findViewById(R.id.place_tick_image);
        radiusControlCard = (CardView) findViewById(R.id.radius_controller_container_card);
        radiusController = (SeekBar) findViewById(R.id.radius_controller);
        zoomIn = (ImageView) findViewById(R.id.zoom_in);
        zoomOut = (ImageView) findViewById(R.id.zoom_ot);
        drawerLayout = findViewById(R.id.drawer_layout);
    }

    private void init() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Intent intent = getIntent();
        targetDetails = intent.getExtras().getParcelable("targetDetails");
        marker=null;
        circle=null;
        confirm.setText("Confirm Radius");
        mRadiusTick.setImageResource(R.mipmap.ic_launcher_confirm_radius);
        mRadiusTick.setVisibility(View.VISIBLE);
        targetPlaceName.setText(targetDetails.getName());
        targetPlaceType.setText(targetDetails.getType());
        targetPlaceAddress.setText(targetDetails.getAddress());
        mCurrLoc.setVisibility(View.INVISIBLE);
        searchCard.setVisibility(View.INVISIBLE);
        radiusControlCard.setVisibility(View.VISIBLE);
        targetLatLng = targetDetails.getTarget();
        LatLng currentLatLng = targetDetails.getCurrent();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng,15f));
        marker = mMap.addMarker(new MarkerOptions().position(targetLatLng).title(targetPlaceName.getText().toString()).draggable(false));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(targetLatLng);
        builder.include(currentLatLng);
        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,1000,1000,0));

        int minRadius=50;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            minRadius=1000;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            radiusController.setMin(minRadius);
        }
        radiusController.setProgress(minRadius);
        radiusController.setMax(5000);
        circle = mMap.addCircle(new CircleOptions()
                .center(targetLatLng)
                .strokeWidth(3)
                .radius(minRadius)
                .strokeColor(R.color.imageColor3));

        int finalMinRadius = minRadius;
        radiusController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (radiusController.getProgress() < finalMinRadius) {
                    radiusController.setProgress(finalMinRadius);
                }
                circle.setRadius(radiusController.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mRadiusTick.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mRadiusTick.setVisibility(View.VISIBLE);
                Toast.makeText(MapsActivitySecondary.this, "Radius set to : "+circle.getRadius(), Toast.LENGTH_SHORT).show();
            }
        });

        mRadiusTick.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivitySecondary.this, "Confirm Radius", Toast.LENGTH_SHORT).show();
            return true;
        });

        zoomOut.setOnClickListener(view -> {
            CameraPosition zoomOutPosition = new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom((float) (mMap.getCameraPosition().zoom-(0.75))).build();
            Log.d(TAG, "onClick: Zoom Out");
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(zoomOutPosition));
        });

        zoomIn.setOnClickListener(view -> {
            CameraPosition zoomInPosition = new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom((float) (mMap.getCameraPosition().zoom+(0.75))).build();
            Log.d(TAG, "onClick: Zoom In");
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(zoomInPosition));
        });

        zoomIn.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivitySecondary.this, "Zoom In", Toast.LENGTH_SHORT).show();
            return true;
        });

        zoomOut.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivitySecondary.this, "Zoom Out", Toast.LENGTH_SHORT).show();
            return false;
        });

        mRadiusTick.setOnClickListener(view -> {
            Toast.makeText(this, "Radius Confirmed", Toast.LENGTH_SHORT).show();
            clickCount++;

            if (clickCount==1){
            //Show Ad
            InterstitialAd mInterstitialAd = new InterstitialAd(this);
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
                    Toast.makeText(MapsActivitySecondary.this, "Don't Remove App From Recents!", Toast.LENGTH_LONG).show();
                    goToFinal();
                    finish();
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    Log.d(TAG, "onAdClosed");
                    goToFinal();
                    finish();
                }
            });}
        });

    }

    @Override
    public void onBackPressed() {
        //Show Ad
        InterstitialAd mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6999236909954331/5210656040");
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
                Log.d(TAG, "onAdFailedToLoad: going to primary");
                finish();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed: going to primary");
                finish();
            }
        });
    }

    private void goToFinal() {
        Intent intent = new Intent(this,MapsActivityFinal.class);
        targetDetails.setRadius(circle.getRadius());
        intent.putExtra("targetDetails", (Parcelable) targetDetails);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: (Secondary) map is ready");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setPadding(0,0,0,400);
        initVars();
        init();
    }


}
