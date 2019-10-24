package com.ismartapps.reachalert;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NamePrompt extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_prompt);
        String TAG = "NP";
        Log.d(TAG, "onCreate");
        EditText editText = findViewById(R.id.name_text);
        Button button = findViewById(R.id.btn);
        button.setActivated(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals(""))
                {
                    button.setActivated(true);
                }

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editText.getText().toString().isEmpty()){
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String name = editText.getText().toString();
                databaseReference.child("Phone Number Logins").child(user.getPhoneNumber()).child("Name").setValue(name);
                SharedPreferences sharedPreferences = getSharedPreferences("userdetails",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString("name",name);
                editor.apply();
                button.setClickable(false);
                finish();}
                else {
                    editText.setError("Enter Name");
                    editText.requestFocus();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }
}
