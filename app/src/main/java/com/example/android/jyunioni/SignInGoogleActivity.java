package com.example.android.jyunioni;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * This class handles the sign in process to Google account.
 * User needs to be logged in to a Google account to post or read content in the shoutbox.
 *
 * NOTE:
 * This class is my own implementation from the original code of Google and Firebase.
 * The original code can be found in this course material:
 * https://codelabs.developers.google.com/codelabs/firebase-android/#0
 *
 * @author Jani Suoranta 22.11.2017
 */
public class SignInGoogleActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    /**
     * "Sign in" request code
     */
    private static final int RC_SIGN_IN = 9001;

    /**
     * Google api client object
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Firebase authentication instance variable
     */
    private FirebaseAuth mFirebaseAuth;


    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState Activity's previously frozen state, if there was one.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_screen);


        // Assign sign in button
        SignInButton mSignInButton = findViewById(R.id.sign_in_button);
        // Set a click listener
        mSignInButton.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Access Google's sign in api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize mFirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();
    }


    /**
     * Click listener for the sign in button.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                // Call signIn if the button is clicked.
                signIn();
                break;
        }
    }


    /**
     * Get the Google sign in api and start the intent with the sign in request code.
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    /**
     * Show a toast if connecting to Google APIs fails.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.google_api_error, Toast.LENGTH_SHORT).show();
    }


    /**
     * Get the result from the intent that was started for signing in.
     *
     * @param requestCode Intent's original request code.
     * @param resultCode  The result code from the intent.
     * @param data        The information of the user's logged in account.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google sign in was successful and authentication with Firebase.
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Show a toast if Google sign in failed.
                Toast.makeText(this, R.string.sign_in_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Authenticate users Google account with Firebase.
     *
     * @param acct The logged in Google account.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        // Get the account credentials
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        // Authenticate with the credentials
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // If sign in fails, display a message to the user.
                if (!task.isSuccessful()) {
                    Toast.makeText(SignInGoogleActivity.this, R.string.firebase_auth_failed, Toast.LENGTH_SHORT).show();
                } else {
                    // If sign in succeeds the authentication state listener will be notified and
                    // logic to handle the signed in user can be handled in the listener.
                    startActivity(new Intent(SignInGoogleActivity.this, FragmentActivity.class));
                    finish();
                }
            }
        });
    }

}