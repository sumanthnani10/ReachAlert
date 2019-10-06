package com.ismartapps.reachalert;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationDrawerActivity extends AppCompatActivity {

    private ImageView userImage;
    private TextView userName,userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        initVars();
        init();
    }

    private void init() {
        userName.setText("Sumanth");
    }

    private void initVars() {
        userImage = findViewById(R.id.user_image);
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
    }


}
