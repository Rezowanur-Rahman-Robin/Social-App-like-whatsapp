package com.robin.robchat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View RequestFragmentView;

    private RecyclerView myRequestsList;

    private DatabaseReference ChatReequestRef,UserRef,ContactRef;

    private FirebaseAuth mAuth;

    private String currentUserid;




    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView= inflater.inflate(R.layout.fragment_request, container, false);

        ChatReequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactRef =FirebaseDatabase.getInstance().getReference().child("Contacts");

        UserRef =FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        currentUserid=mAuth.getCurrentUser().getUid();

        myRequestsList=(RecyclerView) RequestFragmentView.findViewById(R.id.chat_request_list_recyclerView);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options
                =new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatReequestRef.child(currentUserid),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter
                =new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull Contacts contacts) {


          requestViewHolder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
          requestViewHolder.itemView.findViewById(R.id.request_Reject_button).setVisibility(View.VISIBLE);

          final  String list_user_id = getRef(i).getKey();

          final DatabaseReference getTypeRef =getRef(i).child("request_type").getRef();

          getTypeRef.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {

                  if(dataSnapshot.exists()){

                      String type=dataSnapshot.getValue().toString();

                      if(type.equals("received")){

                          UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                              @Override
                              public void onDataChange(DataSnapshot dataSnapshot) {

                                  if(dataSnapshot.hasChild("image")){


                                      final String requestUserImage=dataSnapshot.child("image").getValue().toString();


                                      Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(requestViewHolder.userImage);



                                  }


                                      final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                      final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                      requestViewHolder.userName.setText(requestUserName);
                                      requestViewHolder.userStatus.setText("wants to connect with you.");



                                  requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {

                                          CharSequence options[]=new CharSequence[]
                                                  {
                                                          "Accept",
                                                          "Reject"

                                                   };

                                          AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
                                          builder.setTitle(requestUserName+"   Chat Request");

                                          builder.setItems(options, new DialogInterface.OnClickListener() {
                                              @Override
                                              public void onClick(DialogInterface dialog, int which) {

                                                  if(which==0){

                                                      ContactRef.child(currentUserid).child(list_user_id).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                          @Override
                                                          public void onComplete(@NonNull Task<Void> task) {
                                                              if(task.isSuccessful()){
                                                                  ContactRef.child(list_user_id).child(currentUserid).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                      @Override
                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                          if(task.isSuccessful()){

                                                                              ChatReequestRef.child(currentUserid).child(list_user_id)
                                                                                      .removeValue()
                                                                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                          @Override
                                                                                          public void onComplete(@NonNull Task<Void> task) {

                                                                                              if (task.isSuccessful()){
                                                                                                  ChatReequestRef.child(list_user_id).child(currentUserid)
                                                                                                          .removeValue()
                                                                                                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                              @Override
                                                                                                              public void onComplete(@NonNull Task<Void> task) {

                                                                                                                  if (task.isSuccessful()){

                                                                                                                      Toast.makeText(getContext(), requestUserName+" is Added.", Toast.LENGTH_SHORT).show();
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
                                                      });
                                                  }
                                                  if(which==1){



                                                      ChatReequestRef.child(currentUserid).child(list_user_id)
                                                              .removeValue()
                                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                  @Override
                                                                  public void onComplete(@NonNull Task<Void> task)
                                                                  {
                                                                      if (task.isSuccessful())
                                                                      {
                                                                          ChatReequestRef.child(list_user_id).child(currentUserid)
                                                                                  .removeValue()
                                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task)
                                                                                      {
                                                                                          if (task.isSuccessful())
                                                                                          {
                                                                                              Toast.makeText(getContext(), "You have rejected the Request.", Toast.LENGTH_SHORT).show();
                                                                                          }
                                                                                      }
                                                                                  });
                                                                      }
                                                                  }
                                                              });

                                                  }
                                              }
                                          });

                                          builder.show();

                                      }
                                  });
                              }

                              @Override
                              public void onCancelled(DatabaseError databaseError) {

                              }
                          });
                      }

                      else if(type.equals("sent")){

                          Button request_sent_button=requestViewHolder.itemView.findViewById(R.id.request_accept_button);
                          request_sent_button.setText("Req Sent");
                          requestViewHolder.itemView.findViewById(R.id.request_Reject_button).setVisibility(View.INVISIBLE);



                          UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                              @Override
                              public void onDataChange(DataSnapshot dataSnapshot) {

                                  if(dataSnapshot.hasChild("image")){


                                      final String requestUserImage=dataSnapshot.child("image").getValue().toString();


                                      Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(requestViewHolder.userImage);



                                  }


                                  final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                  final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                  requestViewHolder.userName.setText(requestUserName);
                                  requestViewHolder.userStatus.setText("You sent a request to "+requestUserName);



                                  requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {

                                          CharSequence options[]=new CharSequence[]
                                                  {
                                                          "Cancel Chat Request"

                                                  };

                                          AlertDialog.Builder builder =new AlertDialog.Builder(getContext());
                                          builder.setTitle("Already Sent Request");

                                          builder.setItems(options, new DialogInterface.OnClickListener() {
                                              @Override
                                              public void onClick(DialogInterface dialog, int which) {


                                                  if(which==0){


                                                      ChatReequestRef.child(currentUserid).child(list_user_id)
                                                              .removeValue()
                                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                  @Override
                                                                  public void onComplete(@NonNull Task<Void> task)
                                                                  {
                                                                      if (task.isSuccessful())
                                                                      {
                                                                          ChatReequestRef.child(list_user_id).child(currentUserid)
                                                                                  .removeValue()
                                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task)
                                                                                      {
                                                                                          if (task.isSuccessful())
                                                                                          {
                                                                                              Toast.makeText(getContext(), "You have canceled the chat Request.", Toast.LENGTH_SHORT).show();
                                                                                          }
                                                                                      }
                                                                                  });
                                                                      }
                                                                  }
                                                              });

                                                  }
                                              }
                                          });

                                          builder.show();

                                      }
                                  });
                              }

                              @Override
                              public void onCancelled(DatabaseError databaseError) {

                              }
                          });
                      }
                  }
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });


            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);

                RequestViewHolder holder =new RequestViewHolder(view);

                return holder;
            }
        };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView userImage;
        Button acceptButton,cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);


            userName=itemView.findViewById(R.id.user_profile_name_ff);
            userStatus=itemView.findViewById(R.id.user_status_ff);
            userImage=itemView.findViewById(R.id.user_profile_pic_ff);
            acceptButton=itemView.findViewById(R.id.request_accept_button);
            cancelButton=itemView.findViewById(R.id.request_Reject_button);



        }
    }
}
