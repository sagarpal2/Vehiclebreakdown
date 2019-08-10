package com.example.yash.vba;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MechanicMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout mdrawer;
    private ActionBarDrawerToggle mtoggle;
    private NavigationView mnavi;

    private Button mhistory;

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    public static final int REQUEST_LOCATION_CODE =99;
    private Switch mworkingswitch;

    private Button mOK,mDetails,mbackd,mopen;
    private String TravellerId="";
    private Boolean isLoggingOut=false;
    private int d=0,status=0;

    private LinearLayout mTravellerInfo;

    private ImageView mTravellerProfileImage;

    private TextView mTravellerName,mMechanicNme,mMechanicGarage,mTravellerPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_map);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTravellerName=(TextView) findViewById(R.id.TravellerName);
        mTravellerInfo=(LinearLayout) findViewById(R.id.TravellerInfo);
        mTravellerPhone=(TextView) findViewById(R.id.TravellerPhone);
        mTravellerProfileImage=(ImageView) findViewById(R.id.TravellerProfileImage);

        mMechanicNme=(TextView)findViewById(R.id.Mechname);
        mMechanicGarage=(TextView)findViewById(R.id.Mechgarage);
        mhistory=(Button)findViewById(R.id.history);

        mdrawer=(DrawerLayout)findViewById(R.id.drawer);
        mtoggle= new ActionBarDrawerToggle(this,mdrawer,R.string.open,R.string.close);
        mopen=(Button)findViewById(R.id.bopen);
        mnavi=(NavigationView)findViewById(R.id.navidraw);

        mOK=(Button)findViewById(R.id.ok);
        mbackd=(Button)findViewById(R.id.backd);
        mDetails=(Button)findViewById(R.id.details);
        mworkingswitch=(Switch) findViewById(R.id.workingswitch);

        mdrawer.addDrawerListener(mtoggle);
        mtoggle.syncState();
        mnavi.setNavigationItemSelectedListener(this);

        mworkingswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    connectMechanic();
                }
                else{
                    disconnectmechanic();
                }
            }
        });
        mopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdrawer.openDrawer(Gravity.LEFT);
            }
        });
        mhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MechanicMapActivity.this,HistoryActivity.class);
                intent.putExtra("TravellerOrMechanic","Mechanics");
                startActivity(intent);
                return;
            }
        });
        getAssignedTraveller();
        getMechInfo();
        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    switch (status) {
                        case 1:
                            status = 2;
                            mTravellerInfo.setVisibility(View.GONE);
                            mDetails.setVisibility(View.VISIBLE);
                            mOK.setText("Reached Traveller");
                            break;
                        case 2:
                            status=3;
                            mOK.setText("WorkDone");
                            mbackd.setVisibility(View.GONE);
                            break;
                        case 3:
                            recordework();
                            endWork();
                            break;
                }
            }
        });
        mDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTravellerInfo.setVisibility(View.VISIBLE);
                mbackd.setVisibility(View.VISIBLE);
                mDetails.setVisibility(View.GONE);
            }
        });
        mbackd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTravellerInfo.setVisibility(View.GONE);
                mDetails.setVisibility(View.VISIBLE);
            }
        });
    }
    public String mName,mGarge;
    private void getMechInfo() {
        String MechanicId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference MechInfo = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(MechanicId);
        MechInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();
                        mMechanicNme.setText(mName);
                        mname=mMechanicNme.getText().toString();
                    }
                    if(map.get("garrage")!=null){
                        mMechanicGarage.setText(map.get("garrage").toString());
                        mGarge=mMechanicGarage.getText().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(mdrawer.isDrawerOpen(GravityCompat.START)){
            mdrawer.closeDrawer(Gravity.LEFT);
        }
        else {
            disconnectmechanic();
            super.onBackPressed();
        }
    }

    private void getAssignedTraveller(){

        String MechanicId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedTravellerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(MechanicId).child("TravellerMechID");
        assignedTravellerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    status=1;
                        TravellerId=dataSnapshot.getValue().toString();
                        getAssignedTravellerPickupLocation();
                    getAssignedTravellerInfo();
                }
                else {
                    endWork();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    Marker pickupMarker;
    DatabaseReference assignedMechanicPickupLocationRef;
    private ValueEventListener assignedMechanicPickupLocationRefListener;
    private void getAssignedTravellerPickupLocation() {

        assignedMechanicPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("TravellerRequest").child(TravellerId).child("l");
        assignedMechanicPickupLocationRefListener=assignedMechanicPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !TravellerId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng mechLatLng = new LatLng(locationLat, locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(mechLatLng).title("PickUp Location"));
                    if(pickupMarker!=null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mechLatLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void connectMechanic(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client,locationRequest,this);
            d=0;
        }
    }
    String tnamed;
    public void getAssignedTravellerInfo() {
            mTravellerInfo.setVisibility(View.VISIBLE);
            DatabaseReference mTravallerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Travellers").child(TravellerId);
            mTravallerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        if (map.get("name") != null) {
                            mTravellerName.setText(map.get("name").toString());
                            tnamed=mTravellerName.getText().toString();
                        }
                        if (map.get("phone") != null) {
                            mTravellerPhone.setText(map.get("phone").toString());
                        }
                        if (map.get("profileImageUrl") != null) {
                            Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mTravellerProfileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //PERMISSION GRANTED
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(client==null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this,"PERMISSION DENIED!" ,Toast.LENGTH_LONG).show();
                }
                return;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



    private void endWork(){

        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mechanicRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userid).child("TravellerMechID");
            mechanicRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TravellerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(TravellerId);
        TravellerId="";

        if(pickupMarker!=null){
            if(status!=3){
                //opendialog();
                Toast.makeText(MechanicMapActivity.this, "Traveller Cancelled", Toast.LENGTH_LONG).show();
            }
            pickupMarker.remove();
            mTravellerInfo.setVisibility(View.GONE);
            mDetails.setVisibility(View.GONE);
            d=0;
        }
        mOK.setText("Accept");
        if(assignedMechanicPickupLocationRefListener != null)
            assignedMechanicPickupLocationRef.removeEventListener(assignedMechanicPickupLocationRefListener);
                mTravellerName.setText("");
                mTravellerPhone.setText("");
                mTravellerProfileImage.setImageResource(R.mipmap.ic_default_icon);

    }
    public void recordework(){
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mechanicRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userid).child("history");
        DatabaseReference TravellerRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Travellers").child(TravellerId).child("history");
        DatabaseReference historyRef= FirebaseDatabase.getInstance().getReference().child("history");
        String requestid=historyRef.push().getKey();
        mechanicRef.child(requestid).setValue(true);
        TravellerRef.child(requestid).setValue(true);

        final HashMap map1=new HashMap();
        map1.put("Mechanics",userid);
        map1.put("Travellers",TravellerId);
        map1.put("rating", 0);
        map1.put("timestamp",getCurrentTimestamp());
        map1.put("Tname",tnamed);
        map1.put("Mname",mname);
        map1.put("Mgarage",mGarge);


        historyRef.child(requestid).updateChildren(map1);
    }
    public String mname;

    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }

    public void opendialog(){
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(),"alert dialog");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }


    }
    protected synchronized void buildGoogleApiClient()
    {
        client=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
            lastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if(d == 0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                d=1;
            }
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("MechanicsAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("MechanicWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);
            switch (TravellerId)
            {
                case "":
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailable.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

            }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest=new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }
    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else
            return true;
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void disconnectmechanic()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("MechanicsAvailable");

        GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        if(id==R.id.detai){
            Intent intent = new Intent(MechanicMapActivity.this,MechanicSettingsActivity.class);
            startActivity(intent);
            mdrawer.closeDrawer(Gravity.LEFT);
        }
        else if(id==R.id.settin){

        }
        else if(id==R.id.logo){
            isLoggingOut=true;
            disconnectmechanic();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MechanicMapActivity.this,MainActivity.class);
            startActivity(intent);
            Toast.makeText(MechanicMapActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
            finish();
            super.onBackPressed();
            mdrawer.closeDrawer(Gravity.LEFT);
        }
        return false;
    }
}