package com.robin.robchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton,PhoneRegisterButton;
    private TextView AlreadyhasAccount;
    private EditText UserEmail,UserPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        setTitle("Create An Account");

        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        AlreadyhasAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email=UserEmail.getText().toString();
        String password =UserPassword.getText().toString();

        if(email.isEmpty())
        {
            UserEmail.setError("Enter an email address");
            UserEmail.requestFocus();
            return;
        }

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            UserEmail.setError("Enter a valid email address");
            UserEmail.requestFocus();
            return;
        }

        //checking the validity of the password
        if(password.isEmpty())
        {
            UserPassword.setError("Enter a password");
            UserPassword.requestFocus();
            return;
        }

        //check password length
        if(password.length()<6){
            Toast.makeText(this, "Password Length should be 6", Toast.LENGTH_LONG).show();
            UserPassword.requestFocus();
            return;
        }

        loadingbar.setTitle("Creating New Account");
        loadingbar.setMessage("Please Wait Until the Registration is Successfully Completed...");
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.show();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            String deviceToken= FirebaseInstanceId.getInstance().getToken();


                            String currentUserId=mAuth.getCurrentUser().getUid();
                            databaseReference.child("Users").child(currentUserId).setValue("");

                            databaseReference.child("Users").child(currentUserId).child("device_token")
                                    .setValue(deviceToken);


                            Toast.makeText(RegisterActivity.this, "Acoount Created Successfully", Toast.LENGTH_SHORT).show();

                            SendUserToMainActivaity();
                            loadingbar.dismiss();
                        }
                        else {
                            loadingbar.dismiss();

                            if(task.getException() instanceof FirebaseAuthUserCollisionException)
                            {
                                Toast.makeText(RegisterActivity.this, "You have already an Account", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(RegisterActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });


    }

    private void SendUserToMainActivaity() {
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void InitializeFields() {
        CreateAccountButton=(Button)findViewById(R.id.register_button);
        UserEmail=(EditText)findViewById(R.id.register_email);
        UserPassword=(EditText)findViewById(R.id.register_password);
        AlreadyhasAccount =(TextView) findViewById(R.id.already_have_an_account);

        loadingbar = new ProgressDialog(this);

    }



    private void SendUserToLoginActivity() {


        Intent loginIntent=new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(loginIntent);
    }
}
