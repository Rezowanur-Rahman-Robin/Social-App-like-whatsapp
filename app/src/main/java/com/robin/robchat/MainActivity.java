package com.robin.robchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabAccessAdaptor myTabAccessAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private String currentUserID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            currentUserID=currentUser.getUid();
        }
        databaseReference= FirebaseDatabase.getInstance().getReference();

        mToolbar=(Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("RobChat");

        myViewPager=(ViewPager)findViewById(R.id.main_tab_pager);
        myTabAccessAdapter =new TabAccessAdaptor(getSupportFragmentManager(),0);
        myViewPager.setAdapter(myTabAccessAdapter);

        myTabLayout =(TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();


        if (currentUser==null){

            SendUserToLoginActivity();
        }
        else
        {
            updateUserStatus("online");
            VerifyUserExistance();
        }


    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (currentUser!=null){

            updateUserStatus("offline");
        }
    }



    private void VerifyUserExistance() {

        String currentUserID = mAuth.getCurrentUser().getUid();

        databaseReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){

                }
                else
                {
                    sendUserToSettingFirstTime();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){

            case R.id.main_find_friends:

                sendUserToFindFriendsActivity();
                return true;


            case R.id.main_group_create_option:

                RequestNewGroup();

                return true;

            case R.id.settings:
                sendUserToSetting();
                return true;

            case R.id.logout:
                updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                return true;

            default:
                return false;


        }
    }





    private void RequestNewGroup() {
        AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this,R.style.AlertDialogue);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("EX:Friend Zone");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupName=groupNameField.getText().toString();
                if(groupName.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Enter The Group Name", Toast.LENGTH_LONG).show();
                }
                else {

                    CreateNewGroup(groupName);
                }
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder.show();


    }


    private void CreateNewGroup(final String groupName) {

        databaseReference.child("Group").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupName+" group has been created successfully.", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent=new Intent(getApplicationContext(),LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }
    private void sendUserToFindFriendsActivity() {
        Intent findIntent=new Intent(getApplicationContext(),FindFriendActivity.class);
        startActivity(findIntent);
    }










    private void sendUserToSetting() {
        Intent settingIntent=new Intent(getApplicationContext(),SettingsActivity.class);
        startActivity(settingIntent);
    }




    private void sendUserToSettingFirstTime() {
        Intent settingIntent=new Intent(getApplicationContext(),SettingsActivity.class);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
        finish();
    }





    private void updateUserStatus(String state){

        String saveCurrentTime,saveCurrentDate;

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");

        saveCurrentDate=currentDate.format(calendar.getTime());
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMAp =new HashMap<>();

        onlineStateMAp.put("time",saveCurrentTime);
        onlineStateMAp.put("date",saveCurrentDate);
        onlineStateMAp.put("state",state);



        databaseReference.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMAp);





    }
}
