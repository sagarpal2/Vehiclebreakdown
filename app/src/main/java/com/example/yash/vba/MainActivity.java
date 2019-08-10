package com.example.yash.vba;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button mTraveller,mMechanic,mGarrage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMechanic=(Button)findViewById(R.id.Mechanic);
        mTraveller=(Button)findViewById(R.id.Traveller);


        startService(new Intent(MainActivity.this, onAppKilled.class));
        mMechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MechanicLoginActivity.class);
                startActivity(intent);
            }
        });
        mTraveller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TravellerLoginActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onBackPressed() {
       Intent a=new Intent(Intent.ACTION_MAIN);
       a.addCategory(Intent.CATEGORY_HOME);
       a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       startActivity(a);
    }
}

