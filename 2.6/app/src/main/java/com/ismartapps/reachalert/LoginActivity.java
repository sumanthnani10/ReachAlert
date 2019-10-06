package com.ismartapps.reachalert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    String TAG = "Login Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            startMain();
        }
        else
        {createSignInIntent();}
    }


    @Override
    public void onBackPressed(){
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    /*private void createRemainderNotificatio() {
        Intent intent = new Intent(this , RemainderReceiver.class);
        AlarmManager alarmManagerMorning = (AlarmManager) getSystemService(ALARM_SERVICE);
        AlarmManager alarmManagerEvening = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, 0);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, 7);
        calendar1.set(Calendar.MINUTE, 00);
        calendar1.set(Calendar.SECOND, 00);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, 16);
        calendar2.set(Calendar.MINUTE, 00);
        calendar2.set(Calendar.SECOND, 00);
        alarmManagerMorning.setRepeating(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), 24*60*60*1000, pendingIntent);
        alarmManagerEvening.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), 24*60*60*1000, pendingIntent);
    }*/

    public void createSignInIntent() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                startMain();
            } else {
                Log.d(TAG, "onActivityResult: Sign in failed");
            }
        }
    }

    public void startMain(){
        Intent intent = new Intent(this,MapsActivityPrimary.class);
        startActivity(intent);
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
}
