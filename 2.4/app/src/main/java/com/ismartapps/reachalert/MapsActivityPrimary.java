package com.ismartapps.reachalert;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivityPrimary extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private ImageView mCurrLoc, mLocationTick,zoomIn,zoomOut;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String TAG = "MapsActivityPrimary";
    private FusedLocationProviderClient mFusedLocationClient;
    private static float zoom = 20f;
    private TextView targetPlaceName, targetPlaceType, targetPlaceAddress;
    private ImageView[] targetPlaceImages = new ImageView[4];
    private LinearLayout targetPlacePhotosScrollViewLinearLayout;
    public LatLng currentLocationLatlng = null,targetLatLng;
    private static Marker marker = null;
    private Bitmap[] targetPlacebitmaps = new Bitmap[4];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d(TAG, "onCreate");
            initVars();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (!checkPermissions()) {
            Log.d(TAG, "onResume: Permission asking");
            requestPermissions();
        } else {
            checkLocation();
        }
    }

    @Override
    public void onBackPressed(){
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    private void checkLocation(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "checkLocation: checking....");
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Log.d(TAG, "checkLocation: Not On");
            new AlertDialog.Builder(this)
                    .setTitle(R.string.gps_not_found_title)  // GPS not found
                    .setMessage(R.string.gps_not_found_message) // Want to enable?
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .show();
        }
        else{
            Log.d(TAG, "checkLocation: On Initing Map");
            initMap();
        }
    }

    private void init(){

        Log.d(TAG, "init:(Primary) initializing");

        mCurrLoc.setOnClickListener(v -> {
            Log.d(TAG, "onClick:(Primary) Location Button is Clicked");
            getDeviceLocation();
        });

        mMap.setOnMapLongClickListener(latLng -> {
                Log.d(TAG, "onMapLongClick: Long Clicked On Map Moving Camera to " + latLng);
                String address = getAddressFromMarker(latLng);
                movecamera(latLng, mMap.getCameraPosition().zoom, "Dropped Pin", address, "Point of interest");
        });

        mMap.setOnPoiClickListener(pointOfInterest -> {
                Log.d(TAG, "onPoiClick: (Primary) Clicked on " + pointOfInterest.name);
                fetchPlaceDetails(pointOfInterest.placeId);
        });

        mMap.setOnMapClickListener(latLng -> {
            if (marker != null) {
                Log.d(TAG, "onMapClick: (Primary) Single Click On Map");
                getDeviceLocation();
            }
        });

        autocomplete();

        mLocationTick.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivityPrimary.this, "Confirm Location", Toast.LENGTH_SHORT).show();
            return true;
        });

        mLocationTick.setOnClickListener(v -> {
            Log.d(TAG, "onClick: (Primary) Target Location Confirmed : (Primary) " + targetLatLng);
            goToSecondary();
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
    }

    private void goToSecondary(){
        Intent intent = new Intent(this,MapsActivitySecondary.class);
        intent.putExtra("targetPlaceName",targetPlaceName.getText());
        intent.putExtra("targetPlaceType",targetPlaceType.getText());
        intent.putExtra("targetPlaceAddress",targetPlaceAddress.getText());
        intent.putExtra("targetLatLng",targetLatLng);
        intent.putExtra("currentLatLng",currentLocationLatlng);
        intent.putExtra("zoom",mMap.getCameraPosition().zoom);
        startActivity(intent);
    }

    private void autocomplete(){
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //autocompleteFragment.setCountry("IN");

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place Selected From Search: (Primary) " + place.getName());
                fetchPlaceDetails(place.getId());
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: (Primary) " + status);
            }
        });
    }

    private String getAddressFromMarker(LatLng latLng) {
        Geocoder geocoder = new Geocoder(MapsActivityPrimary.this, Locale.getDefault());
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

        CameraPosition movingPlace = new CameraPosition.Builder()
                .target(latLng)
                .tilt(32f).zoom(zoom).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(movingPlace));

        if (!title.equals("Current Location")){
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(title).draggable(true));
            marker.setDraggable(true);
            mLocationTick.setVisibility(View.VISIBLE);
            targetLatLng=latLng;
        }

        if (title.equals("Current Location") || title.equals("Dropped Pin")) {

            targetPlaceImages[0].setImageResource(R.color.imageColor1);
            targetPlaceImages[1].setImageResource(R.color.imageColor2);
            targetPlaceImages[2].setImageResource(R.color.imageColor3);
            targetPlaceImages[3].setImageResource(R.color.imageColor4);

            targetPlacePhotosScrollViewLinearLayout.getLayoutParams().height = (int) getResources().getDimension(R.dimen.dp0);
        }

        DecimalFormat decimalFormat = new DecimalFormat("#0.00000");
        String latlong = decimalFormat.format(latLng.latitude)+","+decimalFormat.format(latLng.longitude);

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

    private void initVars(){

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        mCurrLoc = (ImageView) findViewById(R.id.location_btn_img);
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
    }

    private void getDeviceLocation()
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
                        String address = getAddressFromMarker(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));
                        movecamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),15f,"Current Location",address,"Point of interest");
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

            getDeviceLocation();
            init();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }
    }

    private boolean checkPermissions() {
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
}
