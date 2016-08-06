package com.pajato.android.gamechat.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends AppCompatActivity
    implements OnConnectionFailedListener, OnClickListener, AuthStateListener,
        OnCompleteListener<AuthResult> {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    // Private instance variables.

    /** Show progress during sign in. */
    private ProgressDialog mProgressDialog;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    // UI Status fields
    private TextView mStatusTextView;
    private TextView mDetailTextView;

    // Public instance methods.

    /** On a destroy Activity lifecycle event lose the progress dialog. */
    @Override public void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }

    /** Use the Activity lifecycle to start the Firebase authentication listener. */
    @Override public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    /** Use the Activity lifecycle to stop the Firebase authentication listener. */
    @Override public void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    /** Deal with the button clicks. */
    @Override public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.google_provider_button) {
            googleSignIn();

        }
    }

    /** Implement the FirebaseAuth AuthStatelistener to update the UI. */
    @Override public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
        updateUI(user);
    }

    /** Process the result returned from the Google sign in activity. */
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Determine if the result is valid.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The result is valid.  Determine if the sign in succeeded.
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Deal with a sign in failure by logging a message and updating the UI.
                Log.e(TAG, "Google Sign In failed.");
                updateUI(null);
            }
        }
    }

    /** Implement the Firebase signin OnCompleteListener by ... */
    @Override public void onComplete(@NonNull Task<AuthResult> task) {
        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

        // If sign in fails, display a message to the user. If sign in succeeds
        // the auth state listener will be notified and logic to handle the
        // signed in user can be handled in the listener.
        if (!task.isSuccessful()) {
            Log.w(TAG, "signInWithCredential", task.getException());
            Toast.makeText(SignInActivity.this, "Authentication failed.",
                           Toast.LENGTH_SHORT).show();
        } else {
            // Deal with a completed and successful sign in by hiding the progress diealog and
            // wrapping up this acivity.
            hideProgressDialog();
            finish();
        }
    }

    /** ... */
    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    // Protected instance methods.

    /** Main activity setup code. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        // Establish the main layout, status and detail views and setup the click listeners on the buttons.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        findViewById(R.id.google_provider_button).setOnClickListener(this);
    }

    // Private instance methods.

    /** Handle a Google sign in by kicking of the Google API sign in activity. */
    private void googleSignIn() {
        // Configure a Google sign in, setup the Google API client and start the Google sign in
        // activity.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /** Deal with Firebase authentication given a Google account. */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        // Update the progess dialog while signing into Firebase with the Google token.
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(this, this);
    }

    /** Update the UI with authentication data. */
    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            findViewById(R.id.google_provider_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.google_provider_button).setVisibility(View.VISIBLE);
        }
    }

    /** Initialize and display the sign in progress dialog. */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    /** Stop showing the progress dialog. */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

}
