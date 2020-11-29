package com.cdap.safetyapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cdap.safetyapp.models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SOSMessage extends FragmentActivity implements OnMapReadyCallback {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private final DocumentReference documentReferenceUser=db.collection("users")
            .document(firebaseAuth.getCurrentUser().getUid());

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;


    private final long MIN_TIME = 20000;
    private final long MIN_DIST = 6;



    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_o_s_message);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},PackageManager.PERMISSION_GRANTED);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ImageButton ImageSOS = findViewById(R.id.imgbtn);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        documentReferenceUser.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.e("User", "DocumentSnapshot data: " + document.getData());

                                User user = document.toObject(User.class);

                                locationListener = new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        try {
                                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                            mMap.addMarker(new MarkerOptions().position(latLng).title("Your Position"));
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                                            String number = user.getTelephoneNumber();
                                            String myLatitude = String.valueOf(location.getAltitude());
                                            String myLongitude = String.valueOf(location.getAltitude());

                                            String message = "https://www.google.com/maps/@?api=1&map_action=map&AIzaSyCpSyKWT1z1W0tglZOnGWKc1DfHCJL9ZsM";
                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(number, null, message, null, null);


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) {

                                    }

                                    @Override
                                    public void onProviderEnabled(String provider) {

                                    }

                                    @Override
                                    public void onProviderDisabled(String provider) {

                                    }


                                };

                                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                                try {
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                Log.e("User", "No such document");
                            }
                        } else {
                            Log.e("User", "get failed with ", task.getException());
                            Toast.makeText(SOSMessage.this, "No Internet Connection. Try Again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });




    }
}



