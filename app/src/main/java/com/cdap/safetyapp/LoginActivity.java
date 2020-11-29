package com.cdap.safetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private final FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        final TextInputLayout textInputLayoutEmail=findViewById(R.id.email_layout);
        final TextInputEditText textInputEditTextEmail=findViewById(R.id.email);
        final TextInputLayout textInputLayoutPassword=findViewById(R.id.password_layout);
        final TextInputEditText textInputEditTextPassword=findViewById(R.id.password);

        Button buttonLogin=findViewById(R.id.btnLogin);

        buttonLogin.setOnClickListener(v -> {// input values fetch
            String email=textInputEditTextEmail.getText().toString();
            String password=textInputEditTextPassword.getText().toString();

            //Input Validation
            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) {
                    textInputLayoutEmail.setError("Please Enter Your Email.");
                }else {
                    textInputLayoutEmail.setError(null);
                }
                if (password.isEmpty()) {
                    textInputLayoutPassword.setError("Please Enter Your Password.");
                }else {
                    textInputLayoutPassword.setError(null);
                }
            } else {
                textInputLayoutEmail.setError(null);
                textInputLayoutPassword.setError(null);

                // check for the login credentioals
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login Error, Please Login Again", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        Button buttonRegister=findViewById(R.id.button_register);

        buttonRegister.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
            startActivity(intent);
        });
    }
}