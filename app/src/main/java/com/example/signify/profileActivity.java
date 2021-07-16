package com.example.signify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class profileActivity extends AppCompatActivity {


    EditText mviewusername;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    TextView mmovetoupdateprofile;

    FirebaseFirestore firebaseFirestore;

    ImageView mviewuserimageinimageview;

    StorageReference storageReference;

    private String ImageURIaccessToken;

    androidx.appcompat.widget.Toolbar mtoolbarofviewprofile;
    ImageButton mbackbuttonofviewprofile;

    FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mviewuserimageinimageview=findViewById(R.id.viewuserimageinimageview);
        mviewusername=findViewById(R.id.viewusername);
        mmovetoupdateprofile=findViewById(R.id.movetoupdateprofile);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mtoolbarofviewprofile=findViewById(R.id.toolbarofviewprofile);
        mbackbuttonofviewprofile=findViewById(R.id.backbuttonofviewprofile);
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();


        setSupportActionBar(mtoolbarofviewprofile);

        mbackbuttonofviewprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        storageReference=firebaseStorage.getReference();
        storageReference.child("Images").child(FirebaseAuth.getInstance().getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageURIaccessToken=uri.toString();
                Picasso.get().load(uri).into(mviewuserimageinimageview);
            }
        });

        DatabaseReference databaseReference= firebaseDatabase.getReference(FirebaseAuth.getInstance().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile muserprofile=dataSnapshot.getValue(userProfile.class);
                mviewusername.setText(muserprofile.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed To Fetch", Toast.LENGTH_SHORT).show();
            }
        });

        mmovetoupdateprofile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(profileActivity.this, updateProfile.class);
                intent.putExtra("nameofuser", mviewusername.getText().toString());
                startActivity(intent);
            }
        });


    }


    @Override
    protected void onStop() {
        super.onStop();
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status", "Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Now User is Offline", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status", "Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Now User is Online", Toast.LENGTH_SHORT).show();
            }
        });
    }
}