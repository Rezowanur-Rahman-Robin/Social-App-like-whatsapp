package com.robin.robchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.robin.robchat.R.layout.custom_chat_bar;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId,messageReceiverName,messageReceiverImage,messageSenderId;

    private TextView userName,userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolbar;

    private ImageButton sendMessageButton,sendFilesButton;
    private EditText messageInputText;

    private FirebaseAuth mAuth;

    private DatabaseReference Rootref;

    private final List<Messages> messageList =new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime,saveCurrentDate;
    private String checker="",myUri="";
    private StorageTask uploadTask;

    private ProgressDialog loadingBar;


    private Uri fileUri;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();

        Rootref = FirebaseDatabase.getInstance().getReference();

        messageReceiverId=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage=getIntent().getExtras().get("visit_image").toString();

        InitializeController();


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);



    }

    private void InitializeController() {

        ChatToolbar =(Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView =layoutInflater.inflate(custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage =(CircleImageView) findViewById(R.id.custom_profile_image);
        userName =(TextView)findViewById(R.id.custom_profile_name);
        userLastSeen =(TextView)findViewById(R.id.custom_user_last_seen);

        sendMessageButton=(ImageButton)findViewById(R.id.private_send_message_button);
        sendFilesButton=(ImageButton)findViewById(R.id.private_send_files_button);
        messageInputText=(EditText) findViewById(R.id.private_input_message);

        messageAdapter =new MessageAdapter(messageList);
        userMessagesList =(RecyclerView)findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager =new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        loadingBar=new ProgressDialog(this);

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");

        saveCurrentDate=currentDate.format(calendar.getTime());
        saveCurrentTime=currentTime.format(calendar.getTime());

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendMessage();
            }
        });

        DisplaylastSeen();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{

                        "Images",
                        "PDF Files",
                        "Ms Word Files"
                };
                AlertDialog.Builder builder =new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the file");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which==0){

                            checker="image";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent,438);

                        }
                        if(which==1){

                            checker="pdf";

                        }
                        if(which==2){
                            checker="docx";

                        }
                    }
                });
                builder.show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please Wait.We are sending your file....");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();



            fileUri=data.getData();

            if(!checker.equals("image")){

            }
            else if(checker.equals("image")){

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/"+messageSenderId+"/"+messageReceiverId;
                final String messageReceiverRef = "Messages/"+messageReceiverId+"/"+messageSenderId;

                DatabaseReference userMessageKeyRef=Rootref.child("Messages")
                        .child(messageReceiverId).child(messageReceiverId).push();

                final String messagePushKey =userMessageKeyRef.getKey();

                final StorageReference filepath=storageReference.child(messagePushKey+"."+"jpg");

                uploadTask=filepath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        else
                        {
                            return filepath;
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUri =task.getResult();

                            myUri=downloadUri.toString();

                            Map meaageImageBody = new HashMap();
                            meaageImageBody.put("message",myUri);
                            meaageImageBody.put("name",fileUri.getLastPathSegment());
                            meaageImageBody.put("type",checker);
                            meaageImageBody.put("from",messageSenderId);
                            meaageImageBody.put("to",messageReceiverId);
                            meaageImageBody.put("messageID",messagePushKey);
                            meaageImageBody.put("time",saveCurrentTime);
                            meaageImageBody.put("date",saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            Object meaageTextBody = null;
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushKey,meaageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushKey,meaageTextBody);

                            Rootref.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if(task.isSuccessful()){
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error: "+task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });


            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected.", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void DisplaylastSeen(){

        Rootref.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


              if(dataSnapshot.exists()){
                  if(dataSnapshot.child("userState").hasChild("state")){
                      String state=dataSnapshot.child("userState").child("state").getValue().toString();
                      String date=dataSnapshot.child("userState").child("date").getValue().toString();
                      String time=dataSnapshot.child("userState").child("time").getValue().toString();

                      if(state.equals("online")){
                          userLastSeen.setText("online");

                      }
                      else if(state.equals("offline")){
                          userLastSeen.setText("Last Seen "+date+"  "+time);

                      }
                  }
                  else{

                      userLastSeen.setText("offline");

                  }
              }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Rootref.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Messages messages=dataSnapshot.getValue(Messages.class);

                        messageList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage() {

        String messageText = messageInputText.getText().toString();
        
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please Write your message.", Toast.LENGTH_SHORT).show();
        }
        else{

            String messageSenderRef = "Messages/"+messageSenderId+"/"+messageReceiverId;
            String messageReceiverRef = "Messages/"+messageReceiverId+"/"+messageSenderId;

            DatabaseReference userMessageKeyRef=Rootref.child("Messages")
                    .child(messageReceiverId).child(messageReceiverId).push();

            String messagePushKey =userMessageKeyRef.getKey();

            Map meaageTextBody = new HashMap();
            meaageTextBody.put("message",messageText);
            meaageTextBody.put("type","text");
            meaageTextBody.put("from",messageSenderId);
            meaageTextBody.put("to",messageReceiverId);
            meaageTextBody.put("messageID",messagePushKey);
            meaageTextBody.put("time",saveCurrentTime);
            meaageTextBody.put("date",saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushKey,meaageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushKey,meaageTextBody);

            Rootref.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error: "+task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });

        }
    }
}
