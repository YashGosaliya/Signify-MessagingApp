package com.example.signify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.annotations.NotNull;

public class otpAuthentication extends AppCompatActivity {


    TextView mchangenumber;
    EditText mgetotp;
    android.widget.Button mverifyotp;
    String enteredotp;

    FirebaseAuth firebaseAuth;
    ProgressBar mprogressbarofotpauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_authentication);

        mchangenumber=findViewById(R.id.changenumber);
        mverifyotp=findViewById(R.id.verifyotp);
        mgetotp=findViewById(R.id.getotp);
        mprogressbarofotpauth=findViewById(R.id.progressbarofotpauth);

        firebaseAuth=FirebaseAuth.getInstance();

        mchangenumber.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(otpAuthentication.this, MainActivity.class);

                startActivity(intent);
            }
        });

        mverifyotp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                enteredotp=mgetotp.getText().toString();
                if(enteredotp.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Enter Your OTP First",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mprogressbarofotpauth.setVisibility(View.VISIBLE);
                    String codereceived=getIntent().getStringExtra("otp");
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codereceived, enteredotp);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NotNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    mprogressbarofotpauth.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(otpAuthentication.this, setProfile.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        mprogressbarofotpauth.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}