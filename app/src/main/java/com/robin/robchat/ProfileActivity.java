package com.robin.robchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId;

    private CircleImageView userProfileImage;
    private TextView userProfileNAme,userProfileStatus;
    private Button sendMessageRequestButton,declineMessageRequestButton;



    private DatabaseReference databaseReference,ChatRequestRef,ContactRef,NotificationRef;

    private String Current_State,sender_userid;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();

        mAuth=FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        sender_userid=mAuth.getCurrentUser().getUid();


        userProfileImage =(CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileNAme =(TextView) findViewById(R.id.visit_username);
        userProfileStatus=(TextView) findViewById(R.id.visit_user_status);
        sendMessageRequestButton =(Button) findViewById(R.id.send_message_request_button);
        declineMessageRequestButton =(Button) findViewById(R.id.decline_message_request_button);

        Current_State ="new";

        RertriveUserInfo();


    }

    private void RertriveUserInfo() {

        databaseReference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("image")){

                    String userImage=dataSnapshot.child("image").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileNAme.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
                else if(dataSnapshot.exists()){
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();
                    userProfileNAme.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        ChatRequestRef.child(sender_userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(receiverUserId)){

                    String request_type=dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){
                        Current_State="request_sent";
                        sendMessageRequestButton.setText("Cancel Request");
                    }
                    else if(request_type.equals("received"))
                    {

                        Current_State="request_received";
                        sendMessageRequestButton.setText("Accept Request");
                        declineMessageRequestButton.setText("Reject Request");
                        declineMessageRequestButton.setVisibility(View.VISIBLE);
                        declineMessageRequestButton.setEnabled(true);

                        declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    ContactRef.child(sender_userid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receiverUserId)){
                                        Current_State="friends";
                                        sendMessageRequestButton.setText("Remove Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(!sender_userid.equals(receiverUserId)){

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if(Current_State.equals("new")){
                        SendChatRequest();
                    }
                    if(Current_State.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if(Current_State.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if(Current_State.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });

        }
        else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void RemoveSpecificContact() {

        ContactRef.child(sender_userid).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            ContactRef.child(receiverUserId).child(sender_userid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State="new";
                                                sendMessageRequestButton.setText("Send Chat Request");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);


                                            }
                                        }
                                    });
                        }
                    }
                });


    }

    private void AcceptChatRequest() {

        ContactRef.child(sender_userid).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            ContactRef.child(receiverUserId).child(sender_userid)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                ChatRequestRef.child(sender_userid).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful()){
                                                                    ChatRequestRef.child(receiverUserId).child(sender_userid)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    sendMessageRequestButton.setEnabled(true);
                                                                                    Current_State="friends";
                                                                                    sendMessageRequestButton.setText("Remove Contact");
                                                                                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    declineMessageRequestButton.setEnabled(false);

                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(sender_userid).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            ChatRequestRef.child(receiverUserId).child(sender_userid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State="new";
                                                sendMessageRequestButton.setText("Send Chat Request");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });


    }

    private void SendChatRequest() {

        ChatRequestRef.child(sender_userid).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            ChatRequestRef.child(receiverUserId).child(sender_userid)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){


                                                HashMap<String,String> chatNotificationMap =new HashMap<>();
                                                chatNotificationMap.put("from",sender_userid);
                                                chatNotificationMap.put("type","request");


                                                NotificationRef.child(receiverUserId).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful()){

                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    Current_State="request_sent";
                                                                    sendMessageRequestButton.setText("Cancel Request");
                                                                }
                                                            }
                                                        });



                                            }
                                        }
                                    });
                        }

                    }
                });
    }
}
