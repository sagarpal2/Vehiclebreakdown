package com.example.yash.vba;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravallerMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    public static final int REQUEST_LOCATION_CODE =99;
    private String requestService;
    private Button mopen,mhistory;
    private Button mRequest;
    private LatLng pickuplocation;
    private int d=0;
    private Boolean requestBol = false;

    private DrawerLayout mdrawer;
    private ActionBarDrawerToggle mtoggle;
    private NavigationView mnavi;

    private Marker pickupMarker;
    private TextView mMechanicName,mMechanicPhone,mMechanicGarrage;
    private LinearLayout mMechanicInfo;
    private RadioGroup mRadioGroup;
    private ImageView mMechanicProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travaller_map);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mRequest=(Button)findViewById(R.id.request);

        mMechanicGarrage=(TextView) findViewById(R.id.MechanicGarrage);
        mMechanicName=(TextView) findViewById(R.id.MechanicName);
        mMechanicInfo=(LinearLayout) findViewById(R.id.MechanicInfo);
        mMechanicPhone=(TextView) findViewById(R.id.MechanicPhone);
        mMechanicProfileImage=(ImageView) findViewById(R.id.MechanicProfileImage);
        mRadioGroup =(RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.wheel2);
        mhistory=(Button)findViewById(R.id.history);

        mdrawer=(DrawerLayout)findViewById(R.id.drawer);
        mtoggle= new ActionBarDrawerToggle(this,mdrawer,R.string.open,R.string.close);
        mnavi=(NavigationView)findViewById(R.id.navidraw);

        mdrawer.addDrawerListener(mtoggle);
        mtoggle.syncState();
        mnavi.setNavigationItemSelectedListener(this);
        mopen=(Button)findViewById(R.id.bopen);

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestBol){
                   endWork();
                }else {

                    int  selectId = mRadioGroup.getCheckedRadioButtonId();

                    final RadioButton radioButton= (RadioButton)findViewById(selectId);

                    if(radioButton.getText() == null)
                    {
                        return;
                    }

                    requestService = radioButton.getText().toString();
                    requestBol=true;
                    String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TravellerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userid, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                    pickuplocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickuplocation).title("Pick-Up Point"));


                    Toast.makeText(TravallerMapActivity.this, "Searching Mechanic For You", Toast.LENGTH_SHORT).show();

                    mRequest.setText("Cancel");
                    getClosestMechanic();
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
                Intent intent = new Intent(TravallerMapActivity.this,HistoryActivity.class);
                intent.putExtra("TravellerOrMechanic","Travellers");
                startActivity(intent);
                return;
            }
        });
    }

    public void onBackPressed() {
        if(mdrawer.isDrawerOpen(GravityCompat.START)){
            mdrawer.closeDrawer(Gravity.LEFT);
        }
        else {
            super.onBackPressed();
        }
    }
    private int radius = 1;
    private Boolean mechanicfound= false;
    private String mechanicfoundID;

    GeoQuery geoQuery;
    private void getClosestMechanic()
    {
        DatabaseReference mechaniclocation = FirebaseDatabase.getInstance().getReference().child("MechanicsAvailable");

        GeoFire geoFire = new GeoFire(mechaniclocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickuplocation.latitude, pickuplocation.longitude),radius);
        geoQuery.removeAllListeners();
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (!mechanicfound && requestBol) {

                        DatabaseReference mTravellerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(key);
                        mTravellerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                    Map<String, Object> mechanicmap = (Map<String, Object>) dataSnapshot.getValue();
                                    if (mechanicfound) {
                                        return;
                                    }

                                    if (mechanicmap.get("service").equals(requestService)) {
                                        mechanicfound = true;
                                        mechanicfoundID = dataSnapshot.getKey();

                                        DatabaseReference mechanicRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(mechanicfoundID);
                                        String TravellerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        HashMap map = new HashMap();
                                        map.put("TravellerMechID", TravellerID);
                                        mechanicRef.updateChildren(map);

                                        getMechanicLocation();
                                        getMechanicInfo();
                                        getWorkDone();
                                        Toast.makeText(TravallerMapActivity.this, "Getting Mechanic Location...", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    if (!mechanicfound) {
                        radius++;
                        getClosestMechanic();
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
    }
    private Marker mMechanicMarker;
    private DatabaseReference mechanicLocationRef;
    private ValueEventListener MechanicLocationRefListener;
        private void getMechanicLocation()
        {
            mechanicLocationRef= FirebaseDatabase.getInstance().getReference().child("MechanicWorking").child(mechanicfoundID).child("l");
            MechanicLocationRefListener = mechanicLocationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && requestBol){
                        List<Object> map=(List<Object>) dataSnapshot.getValue();
                        double locationLat = 0;
                        double locationLng = 0;
                        if(map.get(0) != null){
                            locationLat = Double.parseDouble(map.get(0).toString());
                        }
                        if(map.get(1) != null){
                            locationLng = Double.parseDouble(map.get(1).toString());
                        }
                        LatLng mechLatLng = new LatLng(locationLat,locationLng);
                        if(mMechanicMarker!=null) {
                            mMechanicMarker.remove();
                        }

                        Location locl= new Location("");
                        locl.setLatitude(pickuplocation.latitude);
                        locl.setLongitude(pickuplocation.longitude);

                        Location loc2= new Location("");
                        loc2.setLatitude(mechLatLng.latitude);
                        loc2.setLongitude(mechLatLng.longitude);

                        float distance = locl.distanceTo(loc2);
                        if(distance<100){

                        }
                        mMechanicMarker = mMap.addMarker(new MarkerOptions().position(mechLatLng).title("your Mechanic"));
                        if(mMechanicMarker!=null) {
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

    public void getMechanicInfo() {
        mMechanicInfo.setVisibility(View.VISIBLE);
        DatabaseReference mTravallerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(mechanicfoundID);
        mTravallerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mMechanicName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {
                        mMechanicPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("garrage") != null) {
                        mMechanicGarrage.setText(map.get("garrage").toString());
                    }
                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mMechanicProfileImage);
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

    private DatabaseReference workHasEndedRef;
        private ValueEventListener workHasEndedRefListener;

    private void getWorkDone(){

         workHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(mechanicfoundID).child("TravellerMechID");
        workHasEndedRefListener= workHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                } else {
                    endWork();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void endWork(){
        requestBol=false;
        geoQuery.removeAllListeners();
        if(mechanicfound==false && radius>6000)
            Toast.makeText(TravallerMapActivity.this, "No mechanics Available nearby \n Please! try again later", Toast.LENGTH_SHORT).show();
        if(MechanicLocationRefListener!=null)
            mechanicLocationRef.removeEventListener(MechanicLocationRefListener);
        if(workHasEndedRefListener!=null)
            workHasEndedRef.removeEventListener(workHasEndedRefListener);

        if(mechanicfoundID != null){
            DatabaseReference mechanicRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(mechanicfoundID).child("TravellerMechID");
            mechanicRef.removeValue();
            mechanicfoundID=null;
        }
        mechanicfound=false;

        radius=1;

        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TravellerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userid);

        if(pickupMarker!=null){
            pickupMarker.remove();
            mMechanicInfo.setVisibility(View.GONE);
        }
        if(mMechanicMarker!=null){
            mMechanicMarker.remove();
        }
        mRequest.setText("Request Mechanic");
        d=0;
        mMechanicName.setText("");
        mMechanicPhone.setText("");
        mMechanicGarrage.setText("");
        mMechanicProfileImage.setImageResource(R.mipmap.ic_default_icon);
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


        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        if(d==0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            d=1;
        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest=new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client,locationRequest,this);
        }
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
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        if(id==R.id.detai){
            Intent intent=new Intent(TravallerMapActivity.this,TravellerSettingsActivity.class);
            startActivity(intent);
            mdrawer.closeDrawer(Gravity.LEFT);
        }
        else if(id==R.id.settin){

        }
        else if(id==R.id.logo){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(TravallerMapActivity.this,MainActivity.class);
            startActivity(intent);
            Toast.makeText(TravallerMapActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
            finish();
            super.onBackPressed();
            mdrawer.closeDrawer(Gravity.LEFT);
        }
        return false;
    }
}
