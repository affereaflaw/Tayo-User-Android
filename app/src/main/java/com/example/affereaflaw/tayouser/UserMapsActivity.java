package com.example.affereaflaw.tayouser;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mgoogleApiClient;
    Location mlocation;
    LocationRequest mlocationRequest;

    private FirebaseAuth auth;
    private DatabaseReference dbProfil;
    private String userKey;

    private Chronometer simpleChronometer;
    private long timeWhenStopped = 0;
    private int h, m, s, tumpang;
    private String t;

    private Button btnLokasi;
    private LatLng userLokasi;
    private Location location1, location2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maps);

        Button btnWaktu = (Button) findViewById(R.id.btn_time);
        Button btnWaktuStop = (Button) findViewById(R.id.btn_timestop);
        auth = FirebaseAuth.getInstance();

        userKey = auth.getCurrentUser().getUid();
        dbProfil = FirebaseDatabase.getInstance().getReference().child("Users");
        dbProfil.child(userKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tumpang = dataSnapshot.child("tumpang").getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnWaktu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tumpang=tumpang+1;
                final DatabaseReference databaseReferenceUpdate = dbProfil.child(userKey);
                databaseReferenceUpdate.child("tumpang").setValue(tumpang);
                startTime();
            }
        });

        btnWaktuStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTime();
            }
        });

        //TIMER
        simpleChronometer = (Chronometer) findViewById(R.id.simpleChronometer); // initiate a chronometer
        simpleChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                h   = (int)(time /3600000);
                m = (int)(time - h*3600000)/60000;
                s= (int)(time - h*3600000- m*60000)/1000 ;
                t = (h < 10 ? "0"+h: h)+":"+(m < 10 ? "0"+m: m)+":"+ (s < 10 ? "0"+s: s);
                chronometer.setText(t);
            }
        });
        simpleChronometer.setBase(SystemClock.elapsedRealtime());
        simpleChronometer.setText("00:00:00");

        btnLokasi = (Button) findViewById(R.id.lokasi_bus);
        btnLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userAvaliable");

                GeoFire geoFire = new GeoFire(reference);
                geoFire.setLocation(userId, new GeoLocation(mlocation.getLatitude(), mlocation.getLongitude()));

                userLokasi = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
                //mMap.addMarker(new MarkerOptions().position(userLokasi).title("Lokasi anda"));

                btnLokasi.setText("Mencari Lokasi Bus...");

                getBusLocation();

                location1 = new Location("");
                location1.setLatitude(mlocation.getLatitude());
                location1.setLongitude(mlocation.getLongitude());


            }
        });

        loadAllDrivers();

    }

    private void loadAllDrivers() {

    }

    private int radius = 100;
    private boolean busFound = false;
    private String busFoundId;
    private void getBusLocation(){
        DatabaseReference driverReference = FirebaseDatabase.getInstance().getReference("driverAvaliable");

        GeoFire geoFire = new GeoFire(driverReference);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(userLokasi.latitude,userLokasi.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!busFound){
                    busFound = true;
                    busFoundId = key;

    //--------
                    final TextView npenumpang = findViewById(R.id.penumpang);
                    DatabaseReference refbus = FirebaseDatabase.getInstance().getReference().child("Users").child(busFoundId).child("penumpang");
                    refbus.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String penum = (String) dataSnapshot.getValue();
                            npenumpang.setText(String.valueOf(penum + "/50"));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                       }
                    });

                    final TextView njalur = findViewById(R.id.jalur);
                    DatabaseReference refbus2 = FirebaseDatabase.getInstance().getReference().child("Users").child(busFoundId).child("jalur");
                    refbus2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String jalurbus = (String) dataSnapshot.getValue();
                            njalur.setText(String.valueOf(jalurbus));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(busFoundId);
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);
                    driverRef.updateChildren(map);

                    getNearestDriverLocation();
                    btnLokasi.setText("Mencari lokasi....");
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

                if(!busFound){
                    radius++;
                    getBusLocation();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    //-----------------
    private Marker mDriverMarker;
    private void getNearestDriverLocation(){
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(busFoundId).child("l");
        driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    btnLokasi.setText("Lokasi bus ditemukan");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng)
                            .title("Bus"));
                    mDriverMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.tayologokecil));

                    location2 = new Location("");
                    location2.setLatitude(locationLat);
                    location2.setLongitude(locationLng);
                    int distanceInMeters = (int) location1.distanceTo(location2);
                    int speedIs10MetersPerMinute = 600;
                    int estimatedDriveTimeInMinutes = distanceInMeters / speedIs10MetersPerMinute;
                    Toast.makeText(getApplicationContext(), estimatedDriveTimeInMinutes + "menit", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
    //------------

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    protected synchronized void buildGoogleApiClient(){
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(1000);
        mlocationRequest.setFastestInterval(1000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, mlocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if(getApplicationContext()!=null) {
            mlocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void startTime (){
        simpleChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        simpleChronometer.start();
    }

    public void stopTime (){
        simpleChronometer.stop();
        Toast.makeText(getApplicationContext(), t + "", Toast.LENGTH_SHORT).show();
    }
}
