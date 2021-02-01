package com.robin.robchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private ProgressDialog loadingBar;

    private static  final int galleryPic = 1;
    private StorageReference UserProfileImageRef;

    private Toolbar SettingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile-Image");



        InitializeFields();


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetriveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPic);
            }
        });


    }




    private void InitializeFields() {
        UpdateAccountSettings=(Button)findViewById(R.id.update_setting_button);
        userName=(EditText) findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage =(CircleImageView)findViewById(R.id.set_profile_image);

        SettingsToolbar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        loadingBar=new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

          if(requestCode==galleryPic && resultCode==RESULT_OK && data!=null){
              Uri ImageUri=data.getData();

              CropImage.activity()
                      .setGuidelines(CropImageView.Guidelines.ON)
                      .setAspectRatio(1,1)
                      .start(this);
          }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){

                loadingBar.setTitle("Set Profile Image.");
                loadingBar.setMessage("Please Wait,Your Profile Image id updating....");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


                Uri resultUri = result.getUri();

                StorageReference filePath=UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully.", Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            databaseReference.child("Users").child(currentUserId).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SettingsActivity.this, "Image Save in DataBase Successfully", Toast.LENGTH_SHORT).show();

                                                loadingBar.dismiss();
                                            }
                                            else 
                                            {
                                                Toast.makeText(SettingsActivity.this, "Error :"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                        }
                        else {
                            Toast.makeText(SettingsActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }

                    }
                });
            }


        }
    }

    private void UpdateSettings() {

        String setUserName=userName.getText().toString();
        String setStatus =userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            userName.setError("Please Write Username");
            userName.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(setStatus)){
            userStatus.setError("Please Write Your Status");
            userStatus.requestFocus();
            return;
        }
        loadingBar.setTitle("Updating");
        loadingBar.setMessage("Please Wait.Your Account Settings is Updating....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        HashMap<String,Object> profileMap = new HashMap<>();
        profileMap.put("uid",currentUserId);
        profileMap.put("name",setUserName);
        profileMap.put("status",setStatus);

        databaseReference.child("Users").child(currentUserId).updateChildren(profileMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            loadingBar.dismiss();
                            SendUserToMainActivity();
                            Toast.makeText(SettingsActivity.this, "Profile Updated Successfully.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(SettingsActivity.this, "Error:"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }
    private void SendUserToMainActivity() {
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);

    }


    private void RetriveUserInfo() {


        databaseReference.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) &&  (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){

                            String retriveUserName=dataSnapshot.child("name").getValue().toString();
                            String retriveStatus=dataSnapshot.child("status").getValue().toString();
                            String retriveProfileImage=dataSnapshot.child("image").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);
                            Picasso.get().load(retriveProfileImage).into(userProfileImage);



                        }
                        else if((dataSnapshot.exists()) &&  (dataSnapshot.hasChild("name"))){

                            String retriveUserName=dataSnapshot.child("name").getValue().toString();
                            String retriveStatus=dataSnapshot.child("status").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);
                        }
                        else {
                            Toast.makeText(SettingsActivity.this, "Please Update Your Profile Information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
