package com.pajato.android.gamechat.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.pajato.android.gamechat.R;

/**
 * Provide an Activity to present the signin options to the User and deal with the choices made.
 */
public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    // [END onActivityResult]

    // [START handleSignInResult]

    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {

    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
    }
    // [END revokeAccess]

    private void showProgressDialog() {
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
    }

    @Override
    public void onClick(View v) {
    }
}
