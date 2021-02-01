package com.robin.robchat;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class ContextFragment extends Fragment {

    private View ContactView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;



    public ContextFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactView= inflater.inflate(R.layout.fragment_context, container, false);

        myContactsList =(RecyclerView) ContactView.findViewById(R.id.contacts_recyclerview);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();

        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UsersRef =FirebaseDatabase.getInstance().getReference().child("Users");



        return  ContactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactRef,Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts) {

                final String usersIDS=getRef(i).getKey();

                contactsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileintent=new Intent(getContext(),ProfileActivity.class);
                        profileintent.putExtra("visit_user_id",usersIDS);
                        startActivity(profileintent);
                    }
                });

                UsersRef.child(usersIDS).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){



                            if(dataSnapshot.hasChild("image")){

                                String profileImage =dataSnapshot.child("image").getValue().toString();
                                String profileName =dataSnapshot.child("name").getValue().toString();
                                String profileStatus =dataSnapshot.child("status").getValue().toString();

                                contactsViewHolder.userName.setText(profileName);
                                contactsViewHolder.userStatus.setText(profileStatus);

                                Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(contactsViewHolder.userProfilePicture);
                            }
                            else {
                                String profileName =dataSnapshot.child("name").getValue().toString();
                                String profileStatus =dataSnapshot.child("status").getValue().toString();

                                contactsViewHolder.userName.setText(profileName);
                                contactsViewHolder.userStatus.setText(profileStatus);
                            }

                            if(dataSnapshot.child("userState").hasChild("state")){
                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online")){

                                    contactsViewHolder.onlineIcone.setVisibility(View.VISIBLE);

                                }
                                else if(state.equals("offline")){
                                    contactsViewHolder.onlineIcone.setVisibility(View.INVISIBLE);

                                    contactsViewHolder.userStatus.setText("Last Seen "+date+"  "+time);

                                }
                            }
                            else{

                                contactsViewHolder.onlineIcone.setVisibility(View.INVISIBLE);
                                contactsViewHolder.userStatus.setText("offline");

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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);

                ContactsViewHolder viewHolder= new ContactsViewHolder(view);

                return viewHolder;
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView userProfilePicture;
        ImageView onlineIcone;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name_ff);
            userStatus=itemView.findViewById(R.id.user_status_ff);
            userProfilePicture=itemView.findViewById(R.id.user_profile_pic_ff);
            onlineIcone=itemView.findViewById(R.id.users_online_status);

        }
    }
}
