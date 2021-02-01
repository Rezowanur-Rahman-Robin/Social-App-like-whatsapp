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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private Button LoginButton,PhoneLoginButton;
    private TextView NeedNewAccount,ForgetpasswordLink;
    private EditText UserEmail,UserPassword;

    private  ProgressDialog loadingbar;

    private FirebaseAuth mAuth;

    private DatabaseReference Userref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        Userref= FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        NeedNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                      SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent phnloginIntent=new Intent(getApplicationContext(),PhoneLoginActivity.class);
                startActivity(phnloginIntent);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            SendUserToMainActivity();
        }

    }

    private void AllowUserToLogin() {

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

        loadingbar.setTitle("Sign In");
        loadingbar.setMessage("Please Wait Until the Login is Successfully Completed...");
        loadingbar.setCanceledOnTouchOutside(true);
        loadingbar.show();

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            String currentUserId=mAuth.getCurrentUser().getUid();
                            String deviceToken= FirebaseInstanceId.getInstance().getToken();

                            Userref.child(currentUserId).child("device_token")
                                    .setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                             if(task.isSuccessful()){

                                                 SendUserToMainActivity();
                                                 Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                 loadingbar.dismiss();
                                             }
                                        }
                                    });


                        }
                        else {
                            loadingbar.dismiss();


                                Toast.makeText(LoginActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();


                        }
                    }
                });


    }

    private void InitializeFields() {
        LoginButton=(Button)findViewById(R.id.login_button);
        PhoneLoginButton=(Button)findViewById(R.id.login_using_phn_button);
        UserEmail=(EditText)findViewById(R.id.login_email);
        UserPassword=(EditText)findViewById(R.id.login_password);
        NeedNewAccount =(TextView) findViewById(R.id.register_an_account);
        ForgetpasswordLink =(TextView) findViewById(R.id.forget_password);

        loadingbar=new ProgressDialog(this);

    }


    private void SendUserToMainActivity() {
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void SendUserToRegisterActivity() {


        Intent registerIntent=new Intent(getApplicationContext(),RegisterActivity.class);
        startActivity(registerIntent);
    }
}
