package com.ismartapps.reachalert;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ConfirmationDialog extends Dialog implements View.OnClickListener {

    private String place,address;
    private TextView header,description,confirm,no;
    private Context context;
    private TargetDetails targetDetails;

    public ConfirmationDialog(@NonNull Context context,String place,String address,TargetDetails targetDetails) {
        super(context);
        this.place = place;
        this.address = address;
        this.context = context;
        this.targetDetails = targetDetails;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        //AdView adView = (AdView) findViewById(R.id.adBannerDialog);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //adView.loadAd(adRequest);
        header = (TextView)findViewById(R.id.dialog_header);
        description = (TextView)findViewById(R.id.dialog_description);
        header.setText(place);
        description.setText(address);
        confirm = (TextView) findViewById(R.id.confirm_button);
        no = (TextView) findViewById(R.id.no_button);
        confirm.setOnClickListener(this);
        no.setOnClickListener(this);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void onClick(View view) {

        switch(view.getId())
        {
            case R.id.confirm_button:
                Intent intent = new Intent(context,MapsActivitySecondary.class);
                Log.d("CD", "goToSecondary: "+targetDetails.getName());
                intent.putExtra("targetDetails", (Parcelable) targetDetails);
                context.startActivity(intent);
                dismiss();
                break;

            case R.id.no_button:
                dismiss();
                break;
        }

    }
}
