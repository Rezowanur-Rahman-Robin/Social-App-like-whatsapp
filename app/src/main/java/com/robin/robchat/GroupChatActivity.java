package com.robin.robchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference,databaseReferenceGroup,databaseReferenceGroupMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("groupname").toString();

        mAuth=FirebaseAuth.getInstance();
        currentUserId =mAuth.getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReferenceGroup =FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupName);





        InitializeFields();

        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageToDataBase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


        databaseReferenceGroup.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void InitializeFields() {
        mToolbar=(Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton=(ImageButton) findViewById(R.id.send_message_button);
        userMessageInput =(EditText) findViewById(R.id.input_group_message);
        displayTextMessages =(TextView) findViewById(R.id.group_chat_text_display);
        mScrollView =(ScrollView) findViewById(R.id.my_scroll_view_groupChat);


    }
    private void GetUserInfo() {

        databaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SaveMessageToDataBase() {

        String message =userMessageInput.getText().toString();
        String messageKey =databaseReferenceGroup.push().getKey();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please Write Message First", Toast.LENGTH_LONG).show();
            userMessageInput.requestFocus();
        }
        else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
            currentDate =currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime =currentTimeFormat.format(calForTime.getTime());


            HashMap<String,Object> groupMessegeKey =new HashMap<>();
            databaseReferenceGroup.updateChildren(groupMessegeKey);

            databaseReferenceGroupMessage=databaseReferenceGroup.child(messageKey);

            HashMap<String,Object> messageInfoMap =new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);

            databaseReferenceGroupMessage.updateChildren(messageInfoMap);




        }
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {

        Iterator iterator =dataSnapshot.getChildren().iterator();

        while(iterator.hasNext()){
            String chaDate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chaMessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chaName=(String) ((DataSnapshot)iterator.next()).getValue();
            String chaTime=(String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chaName + " :\n"+chaMessage+"\n"+chaTime +"  "+chaDate+"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }

    }
}
