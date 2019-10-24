package com.ismartapps.reachalert;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    public String name;
    String TAG = "Login Activity";
    FirebaseAuth mAuth;
    FirebaseUser user;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        sharedPreferences = getSharedPreferences("userdetails",MODE_PRIVATE);
        if(user!=null && ((user.getEmail()!=null && !user.getEmail().equals("")) || !sharedPreferences.getString("name","User Name").equals("User Name"))){
            startMain();
        }
        else if(user==null)
        {
            createSignInIntent();
            scheduleNotification();
            SharedPreferences settings = getSharedPreferences("settings",MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("dark",false);
            editor.apply();
        }
        else if(sharedPreferences.getString("name","User Name").equals("User Name"))
        {
            Intent intent = new Intent(this,NamePrompt.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed(){
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void createSignInIntent() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.mipmap.ic_launcher_icon)
                        .setTheme(R.style.AppTheme)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: Sign in Success");
                if(user.getEmail()==null)
                {
                    Intent intent = new Intent(this,NamePrompt.class);
                    startActivity(intent);
                }
                else{
                    startMain();}
            } else {
                Log.d(TAG, "onActivityResult: Sign in failed");
            }
        }
    }

    public void startMain(){
        Log.d(TAG, "startMain");
        Intent intent = getIntent();
        Intent mainIntent = new Intent(this, MapsActivityPrimary.class);
        if(intent.getStringExtra("from").equals("SAN")){
        String text = intent.getStringExtra("name");
        Log.d(TAG, "onCreate: "+text+" , "+intent.getExtras());
        double[] latLng = intent.getExtras().getDoubleArray("latlng");
        mainIntent.putExtra("name",text);
        mainIntent.putExtra("placeId",intent.getStringExtra("placeId"));
        mainIntent.putExtra("latlng",latLng);}
        startActivity(mainIntent);
        finish();
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Logged Out");
                    }
                });
    }

    public void privacyAndTerms() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTosAndPrivacyPolicyUrls(
                                "https://sites.google.com/view/reach-alert/home",
                                "https://sites.google.com/view/reach-alert/home")
                        .build(),
                RC_SIGN_IN);
    }

    void scheduleNotification()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY,7);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);

        PendingIntent notificationPendingIntent;

        Intent notificationIntent = new Intent(this,RemainderReceiver.class);
        notificationIntent.putExtra("text","Wanna Go Somewhere?");
        notificationIntent.putExtra("New?",true);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        notificationIntent.putExtra("id",3);
        notificationPendingIntent = PendingIntent.getBroadcast(this,3,notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis()+AlarmManager.INTERVAL_DAY,AlarmManager.INTERVAL_DAY,notificationPendingIntent);

        calendar.set(Calendar.HOUR_OF_DAY,16);
        notificationIntent.putExtra("id",4);
        notificationPendingIntent = PendingIntent.getBroadcast(this,4,notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis()+AlarmManager.INTERVAL_DAY,AlarmManager.INTERVAL_DAY,notificationPendingIntent);

        calendar.set(Calendar.HOUR_OF_DAY,20);
        notificationIntent.putExtra("id",5);
        notificationPendingIntent = PendingIntent.getBroadcast(this,5,notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis()+AlarmManager.INTERVAL_DAY,AlarmManager.INTERVAL_DAY,notificationPendingIntent);
    }

}
