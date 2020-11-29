package com.cdap.safetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.cdap.safetyapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    ImageView button;
    ImageView step;

    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private final DocumentReference documentReferenceUser=db.collection("users")
            .document(firebaseAuth.getCurrentUser().getUid());

    ImageButton ImageNavigation ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        if (!checkCallPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
        }

        if (!checkSmsPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }

        ImageNavigation = findViewById(R.id.imgnavi);
        //Motivation Video
        button=(ImageView) findViewById(R.id.motivation);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,DIDMotivationVideo.class);
                startActivity(intent);
            }
        });
        step=(ImageView) findViewById(R.id.stepcount);
        step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,stepcount.class);
                startActivity(intent);
            }
        });



        ImageButton ImageSOS = findViewById(R.id.imgbtn);

        ImageSOS.setOnClickListener(v -> {
            documentReferenceUser.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.e("User", "DocumentSnapshot data: " + document.getData());

                                    User user=document.toObject(User.class);

                                    String number = user.getTelephoneNumber();
                                    Intent intent = new Intent(Intent.ACTION_CALL);
                                    intent.setData(Uri.parse("tel:" + number));
                                    startActivity(intent);


                                    try {
                                        SmsManager sms = SmsManager.getDefault();
                                        sms.sendTextMessage(user.getTelephoneNumber(), null, "\"https://www.google.com/maps/@?api=1&map_action=map&AIzaSyCpSyKWT1z1W0tglZOnGWKc1DfHCJL9ZsM", null, null);
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, "Sms not Send", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }


                                } else {
                                    Log.e("User", "No such document");
                                }
                            } else {
                                Log.e("User", "get failed with ", task.getException());
                                Toast.makeText(MainActivity.this,"No Internet Connection. Try Again",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });



        Button btnLogout=findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);

            builder.setMessage("Do you want to Log Off?")
                    .setTitle("Log Off")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        firebaseAuth.signOut();
                        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                        startActivity(intent);
                        finish();
                    }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

            builder.show();
        });
    }

    private boolean checkCallPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkSmsPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MAKE_CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission granted for making calls", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission granted for send sms", Toast.LENGTH_SHORT).show();
            }
        }

    }

}