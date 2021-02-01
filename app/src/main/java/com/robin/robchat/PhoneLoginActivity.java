package com.robin.robchat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button SendVerificationButton,VerifyButton;
    private EditText InputPhoneNumber,InputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken ;

    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth=FirebaseAuth.getInstance();

        loadingBar=new ProgressDialog(this);

        SendVerificationButton=(Button) findViewById(R.id.send_verification_code);
        VerifyButton=(Button) findViewById(R.id.verify_button);
        InputPhoneNumber =(EditText) findViewById(R.id.phn_number_input);
        InputVerificationCode =(EditText) findViewById(R.id.verification_code_input);

        SendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String phoneNumber = "+88"+InputPhoneNumber.getText().toString();

                if(phoneNumber.length()<14){

                    InputPhoneNumber.setError("Please Enter Your Phone Number");
                    InputPhoneNumber.requestFocus();
                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please Wait,until the verification code send to your Phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();;

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                             phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks

                }
            }
        });


        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVerificationButton.setVisibility(View.GONE);
                InputPhoneNumber.setVisibility(View.GONE);

                String verificationCode=InputVerificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode)){
                    InputVerificationCode.setError("Please Enter The Code");
                    InputVerificationCode.requestFocus();
                }
                else {

                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please Wait,we are comparing your given code with the verification code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();;

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                loadingBar.dismiss();

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number,Please Enter Correct Phone Number.", Toast.LENGTH_LONG).show();
                System.out.print(e.getMessage());


                SendVerificationButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.GONE);
                InputVerificationCode.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent( String verificationId,
                                    PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Verification Code Has Been Sent.", Toast.LENGTH_SHORT).show();


                // Save verification ID and resending token so we can use them later
                 mVerificationId = verificationId;
                 mResendToken = token;




                 SendVerificationButton.setVisibility(View.GONE);
                InputPhoneNumber.setVisibility(View.GONE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);


            }
        };




    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            loadingBar.dismiss();

                            Toast.makeText(PhoneLoginActivity.this, "You are Successfully Logged In", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();


                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            loadingBar.dismiss();

                            Toast.makeText(PhoneLoginActivity.this, "Error"+task.getException().toString(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent= new Intent(getApplicationContext(),MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
