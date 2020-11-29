package com.cdap.safetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.cdap.safetyapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private final CollectionReference collectionReferenceUser = db.collection("users");

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        final TextInputLayout textInputLayoutEmail = findViewById(R.id.email);
        final TextInputEditText textInputEditTextEmail = findViewById(R.id.txtemail);
        final TextInputLayout textInputLayoutName = findViewById(R.id.name);
        final TextInputEditText textInputEditTextName = findViewById(R.id.txtname);
        final TextInputLayout textInputLayoutNumber = findViewById(R.id.phoneno);
        final TextInputEditText textInputEditTextNumber = findViewById(R.id.txtphoneno);
        final TextInputLayout textInputLayoutPassword = findViewById(R.id.password);
        final TextInputEditText textInputEditTextPassword = findViewById(R.id.txtpassword);

        Button buttonRegister = findViewById(R.id.reg_btn);

        buttonRegister.setOnClickListener(v -> {
            String email = textInputEditTextEmail.getText().toString();
            final String name = textInputEditTextName.getText().toString();
            final String number = textInputEditTextNumber.getText().toString();
            String password = textInputEditTextPassword.getText().toString();

            if (email.isEmpty() || name.isEmpty() || number.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) {
                    textInputLayoutEmail.setError("Enter Your Email Address.");
                } else {
                    textInputLayoutEmail.setError(null);
                }
                if (name.isEmpty()) {
                    textInputLayoutName.setError("Enter Your Name.");
                } else {
                    textInputLayoutName.setError(null);
                }
                if (number.isEmpty()) {
                    textInputLayoutNumber.setError("Enter Your Telephone Number.");
                } else {
                    textInputLayoutNumber.setError(null);
                }
                if (password.isEmpty()) {
                    textInputLayoutPassword.setError("Enter a Valid Password.");
                } else {
                    textInputLayoutPassword.setError(null);
                }
            } else {
                textInputLayoutEmail.setError(null);
                textInputLayoutName.setError(null);
                textInputLayoutNumber.setError(null);
                textInputLayoutPassword.setError(null);

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                                    User user=new User(email,name,number);

                                    collectionReferenceUser
                                            .document(firebaseUser.getUid())
                                            .set(user)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getApplicationContext(), "Registration Successfully.", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Error writing document", e);
                                                Toast.makeText(getApplicationContext(), "Registration Unsuccessful, Please Try Again", Toast.LENGTH_SHORT).show();

                                            });
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Registration Unsuccessful, Please Try Again", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}