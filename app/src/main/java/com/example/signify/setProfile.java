package com.example.signify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.usage.NetworkStats;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.graphics.Bitmap.CompressFormat.*;

public class setProfile extends AppCompatActivity {


    private CardView mgetuserimage;
    private ImageView mgetuserimageinimageview;
    private static final int PICK_IMAGE=123;
    private Uri imagepath;

    private EditText mgetusername;

    private android.widget.Button msaveprofile;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private String ImageUriAccessToken;

    private FirebaseFirestore firebaseFirestore;

    ProgressBar mprogressbarofsetprofile;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        Toast.makeText(this," Object " + firebaseAuth,Toast.LENGTH_SHORT).show();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        mgetusername=findViewById(R.id.getusername);
        mgetuserimage=findViewById(R.id.getuserimage);
        mgetuserimageinimageview = findViewById(R.id.getuserimageinimageview);
        msaveprofile=findViewById(R.id.saveProfile);
        mprogressbarofsetprofile=findViewById(R.id.progressbarofsetprofile);

        mgetuserimage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });


        msaveprofile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String name = mgetusername.getText().toString();
                if(name.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();
                }
                else if(imagepath==null)
                {
                    Toast.makeText(getApplicationContext(), "Image is Empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mprogressbarofsetprofile.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(setProfile.this, chatActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        });
        
    }


    private void sendDataForNewUser()
    {
        sendDataToRealTimeDatabase();
    }

    private void sendDataToRealTimeDatabase()
    {
        String name = mgetusername.getText().toString().trim();
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        NetworkStats.Bucket firebaseAuth;
        DatabaseReference databaseReference=firebaseDatabase.getReference(FirebaseAuth.getInstance().getUid());

        userProfile muserprofile=new userProfile(name, FirebaseAuth.getInstance().getUid());
        databaseReference.setValue(muserprofile);
        Toast.makeText(getApplicationContext(), "User Profile Added Successfully", Toast.LENGTH_SHORT).show();
        sendImagetoStorage();

    }

    private void sendImagetoStorage() {
        StorageReference imageref = storageReference.child("Images").child(FirebaseAuth.getInstance().getUid()).child("Profile Pic");

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        /*try{
            Bitmap original = BitmapFactory.decodeStream(getAssets().open("1024x768.jpg"));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.PNG, 100, out);
            Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        }catch (Exception e){
            Toast.makeText(this,"Exception : " + e.getMessage(),Toast.LENGTH_SHORT).show();
        }*/


       // ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //Bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
      //  byte[] data = byteArrayOutputStream.toByteArray();

        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] data = byteBuffer.array();

        UploadTask uploadTask = imageref.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAccessToken=uri.toString();
                        Toast.makeText(getApplicationContext(),"URI get sucess",Toast.LENGTH_SHORT).show();
                        sendDataTocloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"URI get Failed",Toast.LENGTH_SHORT).show();
                    }

                });
                Toast.makeText(getApplicationContext(),"Image is uploaded",Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Image Not UPdloaded",Toast.LENGTH_SHORT).show();
            }
        });





    }

    private void sendDataTocloudFirestore() {


        DocumentReference documentReference=firebaseFirestore.collection("Users").document(FirebaseAuth.getInstance().getUid());
        Map<String , Object> userdata=new HashMap<>();
        String name = mgetusername.getText().toString();
        userdata.put("name", name);
        userdata.put("image",ImageUriAccessToken);
        userdata.put("uid",FirebaseAuth.getInstance().getUid());
        userdata.put("status","Online");

        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"Data on Cloud Firestore send success",Toast.LENGTH_SHORT).show();

            }
        });



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK)
        {
            imagepath=data.getData();
            mgetuserimageinimageview.setImageURI(imagepath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}