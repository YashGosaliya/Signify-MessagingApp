package com.example.signify;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class updateProfile extends AppCompatActivity {


    private EditText mviewusername;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;


    private FirebaseFirestore firebaseFirestore;

    private ImageView mgetnewuserimageinimageview;

    private StorageReference storageReference;

    private String ImageURIaccessToken;

    private androidx.appcompat.widget.Toolbar mtoolbarofupdateprofile;
    private ImageButton mbackbuttonofupdateprofile;

    private FirebaseStorage firebaseStorage;

    ProgressBar mprogressbarofupdateprofile;

    private Uri imagepath;// = Uri.parse("android.resource://my.package.name/"+R.drawable.default_profile);;

    Intent intent;

    private static int PICK_IMAGE=123;

    android.widget.Button mupdateprofilebutton;

    String newname;
    private View mnewusername;
    private View mgetnewimageinimageview;
    private String ImageUriAccessToken;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateprofile);


        mtoolbarofupdateprofile=findViewById(R.id.toolbarofupdateprofile);
        mbackbuttonofupdateprofile=findViewById(R.id.backbuttonofupdateprofile);
        mgetnewimageinimageview = (ImageView) findViewById(R.id.getnewuserimageinimageview);
        mprogressbarofupdateprofile=findViewById(R.id.progressbarofupdateprofile);
        mnewusername=findViewById(R.id.getnewusername);
        mupdateprofilebutton=findViewById(R.id.updateprofilebutton);


        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        intent=getIntent();

        setSupportActionBar(mtoolbarofupdateprofile);

        mbackbuttonofupdateprofile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mnewusername.setTooltipText(intent.getStringExtra("nameofuser"));
        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());

        mupdateprofilebutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                newname=mnewusername.getTooltipText().toString();
                if(newname.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();
                }
                else if(imagepath!=null)
                {
                     mprogressbarofupdateprofile.setVisibility(View.VISIBLE);
                     userProfile muserprofile = new userProfile(newname,firebaseAuth.getUid());
                     databaseReference.setValue(muserprofile);
                     
                     updateimagetostorage();

                     Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                     mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
                     Intent intent = new Intent(updateProfile.this,chatActivity.class);
                     startActivity(intent);
                     finish();
                }
                else
                {
                    mprogressbarofupdateprofile.setVisibility(View.VISIBLE);
                    userProfile muserprofile = new userProfile(newname,firebaseAuth.getUid());
                    databaseReference.setValue(muserprofile);
                    updatenameoncloudfirestore();
                    Toast.makeText(getApplicationContext(), "Updated",Toast.LENGTH_SHORT).show();
                    mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(updateProfile.this,chatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        mgetnewimageinimageview.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent,PICK_IMAGE);
            }
        });

        storageReference=firebaseStorage.getReference();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageURIaccessToken=uri.toString();
                Picasso.get().load(uri).into((ImageView) mgetnewimageinimageview);
            }
        });

    }

    private void updatenameoncloudfirestore() {

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(FirebaseAuth.getInstance().getUid());
        Map<String, Object> userdata = new HashMap<>();
        Object name = new Object();
        userdata.put("name", name);
        userdata.put("image", ImageUriAccessToken);
        userdata.put("uid", FirebaseAuth.getInstance().getUid());
        userdata.put("status", "Online");

        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void updateimagetostorage() {

        StorageReference imageref = storageReference.child("Images").child(FirebaseAuth.getInstance().getUid()).child("Profile Pic");

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();


        UploadTask uploadTask = imageref.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                StorageReference imageref = null;
                imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "URI get success", Toast.LENGTH_SHORT).show();
                        sendDataToCLoudFirestore();
                    }

                    private void sendDataToCLoudFirestore() {

                        DocumentReference documentReference = firebaseFirestore.collection("Users").document(FirebaseAuth.getInstance().getUid());
                        Map<String, Object> userdata = new HashMap<>();
                        Object name = new Object();
                        userdata.put("name", name);
                        userdata.put("image", ImageUriAccessToken);
                        userdata.put("uid", FirebaseAuth.getInstance().getUid());
                        userdata.put("status", "Online");

                        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(), "Data on Cloud firestore send success", Toast.LENGTH_SHORT).show();

                            }
                        });


                    }


                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI get failed", Toast.LENGTH_SHORT).show();
                    }


                });
                Toast.makeText(getApplicationContext(), "Image is Updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Image is not updated", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK)
        {
            imagepath=data.getData();
            //mgetnewimageinimageview.setImageURI(null);
            // = (ImageView) mget
            ImageView imageView = (ImageView) mgetnewimageinimageview;
            imageView.setImageURI(imagepath);
        }
        super.onActivityResult(requestCode, resultCode, data);
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