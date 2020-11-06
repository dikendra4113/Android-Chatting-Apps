package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.chattingapp.model.PaperDb;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignClient;
    private static final int RC_SIGN_IN = 1;
    GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    public static String TAG = "MainActivity";
    DatabaseReference seenRef,RootRef ;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signInButton = findViewById(R.id.sign_in);
        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        Paper.init(this);
        RootRef = FirebaseDatabase.getInstance().getReference();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

         mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        mGoogleSignClient = GoogleSignIn.getClient(this,gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingBar.setTitle("Google Account Authentication");
                loadingBar.setIcon(R.drawable.google);
                loadingBar.setMessage("Please Wait...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                signIn();

            }
        });

    }

    private void signIn() {

            Intent signIntent = mGoogleSignClient.getSignInIntent();
            startActivityForResult(signIntent, RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != RESULT_CANCELED) {
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        }else {
            Toast.makeText(this, "Not Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.i(TAG,""+account);
            //Toast.makeText(this, "Sign in Successfully!!", Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(account);

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
           // FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String uid = mAuth.getCurrentUser().getUid();
                    String userName = acct.getDisplayName();
                    String email = acct.getEmail();
                    Uri ulr = acct.getPhotoUrl();
                    Log.i(TAG,userName + " :"+ email + " :"+ ulr);
                    Intent intent = new Intent(getApplicationContext(),FriendListActivity.class);
                    intent.putExtra("name",userName);
                    intent.putExtra("uid",uid);
                    intent.putExtra("email",email);
                    intent.putExtra("image",ulr.toString() );
                    userSeen();
                    loadingBar.dismiss();
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "Sign In Successful..!", Toast.LENGTH_SHORT).show();
                    finish();

                }else {
                    Toast.makeText(MainActivity.this, "Sign In unSuccessful..!", Toast.LENGTH_SHORT).show();
                }
            }


        });
    }

    private void userSeen() {
        seenRef = FirebaseDatabase.getInstance().getReference();

         seenRef.child("lastseen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String a = snapshot.child("seen").getValue().toString();
                Paper.book().write(PaperDb.lastSeen,a);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        if(mAuth.getUid() != null){
            Intent intent = new Intent(getApplicationContext(),FriendListActivity.class);
            intent.putExtra("name",mAuth.getCurrentUser().getDisplayName());
            intent.putExtra("email",mAuth.getCurrentUser().getEmail());
            intent.putExtra("uid",mAuth.getCurrentUser().getUid());
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}