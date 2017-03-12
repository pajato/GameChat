package com.pajato.android.gamechat.chat.fragment;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.ui.TaskFailureLogger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;
import com.pajato.android.gamechat.R;
import com.pajato.android.gamechat.chat.BaseChatFragment;
import com.pajato.android.gamechat.common.DispatchManager;
import com.pajato.android.gamechat.common.FabManager;
import com.pajato.android.gamechat.common.ToolbarManager;
import com.pajato.android.gamechat.credentials.Credentials;
import com.pajato.android.gamechat.database.ProtectedUserManager;
import com.pajato.android.gamechat.event.ClickEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import static com.pajato.android.gamechat.common.FragmentType.groupsForProtectedUser;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.helpAndFeedback;
import static com.pajato.android.gamechat.common.ToolbarManager.MenuItemType.settings;

/**
 * Provide a fragment to create protected users.
 */

public class CreateProtectedUsersFragment extends BaseChatFragment {

    // Private class constants.

    /** The logcat tag. */
    private static final String TAG = CreateProtectedUsersFragment.class.getSimpleName();

    /** Firebase required password to be at least 6 characters */
    private static final int PASSWORD_MIN = 6;

    /** After testing email address, remember if it is new or already in the auth database */
    private boolean accountIsKnown = false;

    // Private inner classes

    /** Handle email address provider check */
    private class EmailOnSuccessListener implements OnSuccessListener<ProviderQueryResult> {
        @Override
        public void onSuccess(ProviderQueryResult result) {
            int id = R.id.email_next_btn_layout;
            RelativeLayout layout = (RelativeLayout) getActivity().findViewById(id);
            layout.setVisibility(View.GONE);
            List<String> providers = result.getProviders();
            if (providers == null || providers.isEmpty()) {
                // TODO: Get name and photo URI from SmartLock ??
                Log.i(TAG, "New user e-mail specified");
                showPasswordLayout(true, false);
                showNameLayout(true, true);
            } else if (EmailAuthProvider.PROVIDER_ID.equalsIgnoreCase(providers.get(0))) {
                Log.i(TAG, "Existing email user");
                accountIsKnown = true;
                showPasswordLayout(true, true);
                showNameLayout(false, false);
            } else {
                // TODO: what to do here?
                Log.i(TAG, "Existing user (some other provider, not e-mail)...");
                accountIsKnown = true;
                showPasswordLayout(true, true);
                showNameLayout(false, false);
            }
        }
    }

    /** Handle changes in the email text field */
    private class EmailTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setEmailError(false);
            RelativeLayout layout =
                    (RelativeLayout) getActivity().findViewById(R.id.email_next_btn_layout);
            layout.setVisibility(View.VISIBLE);
            enableFinishButton(false);
            showPasswordLayout(false, false);
            showNameLayout(false, false);
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    }

    /** Handle changes in the password text field */
    private class PasswordTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (isValidPasswordFormat(getPassword())) {
                setPasswordError(false);
                enableFinishButton(true);
                Button pwdBtn = (Button) getActivity().findViewById(R.id.pwd_next_button);
                pwdBtn.setEnabled(true);
            } else
                setPasswordError(true);
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    }

    // Public instance methods.

    /** Handle click events. */
    @Subscribe public void onClick(final ClickEvent event) {
        FragmentActivity activity = getActivity();
        if (event == null || event.view == null)
            return;
        switch (event.view.getId()) {
            case R.id.create_button_finish:
            case R.id.pwd_next_button:
                TextInputLayout mailLayout =
                        (TextInputLayout) activity.findViewById(R.id.email_layout);
                TextInputLayout passwordLayout =
                        (TextInputLayout) activity.findViewById(R.id.password_layout);
                if (mailLayout.getError() == null && passwordLayout.getError() == null) {
                    ProtectedUserManager.instance.setEMailCredentials(getEmailAddress(),
                            getUserName(), getPassword(), accountIsKnown);
                    // Dismiss the Keyboard and return to the previous fragment.
                    dismissKeyboard();
                    DispatchManager.instance.chainFragment(getActivity(), groupsForProtectedUser);
                }
                break;
            case R.id.email_next_button:
                final TextInputEditText editText =
                        (TextInputEditText) activity.findViewById(R.id.emailEditText);
                if (isValidEmailFormat(editText.getText().toString())) {
                    setEmailError(false);
                    checkEmailAccountExists(editText.getText().toString());
                } else
                    setEmailError(true);

                break;
            default:
                processClickEvent(event.view, "createProtectedUsers");
                break;
        }
    }

    /** When this fragment is no longer visible, make sure to close the keyboard, if necessary */
    @Override public void onPause() {
        super.onPause();
        dismissKeyboard();
    }

    /** Deal with the fragment's lifecycle by managing the progress bar and the FAB. */
    @Override public void onResume() {
        // Set the titles in the toolbar to the app title only; ensure that the FAB is visible, the
        // FAM is not and the FAM is set to the home chat menu.
        super.onResume();
        FabManager.chat.setVisibility(this, View.INVISIBLE);

        final TextInputEditText emailText =
                (TextInputEditText) getActivity().findViewById(R.id.emailEditText);
        final TextInputEditText nameText =
                (TextInputEditText) getActivity().findViewById(R.id.nameEditText);
        final TextInputEditText passwordText =
                (TextInputEditText) getActivity().findViewById(R.id.passwordEditText);
        Button pwdBtn = (Button) getActivity().findViewById(R.id.pwd_next_button);

        Credentials credentials = ProtectedUserManager.instance.getEMailCredentials();
        if (credentials != null) {
            emailText.setText(credentials.email);
            RelativeLayout layout =
                    (RelativeLayout) getActivity().findViewById(R.id.email_next_btn_layout);
            layout.setVisibility(View.GONE);
            if (!credentials.accountIsKnown) {
                nameText.setText(credentials.name);
                showNameLayout(true, false);
            }
            showPasswordLayout(true, false);
            passwordText.setText(credentials.secret);
            enableFinishButton(true);
            pwdBtn.setEnabled(true);
        } else {
            emailText.setText("");
            setEmailError(false);
            passwordText.setText("");
            setPasswordError(false);
            nameText.setText("");
            showNameLayout(false, false);
            showPasswordLayout(false, false);

            RelativeLayout layout =
                    (RelativeLayout) getActivity().findViewById(R.id.email_next_btn_layout);
            layout.setVisibility(View.VISIBLE);
            enableFinishButton(false);
            pwdBtn.setEnabled(true);
        }
    }

    /** Set up toolbar and FAM */
    @Override public void onStart() {
        super.onStart();
        int titleResId = R.string.CreateRestrictedUserTitle;
        ToolbarManager.instance.init(this, titleResId, helpAndFeedback, settings);

        final TextInputEditText editText =
                (TextInputEditText) getActivity().findViewById(R.id.emailEditText);
        editText.addTextChangedListener(new EmailTextWatcher());

        final TextInputEditText passwordText =
                (TextInputEditText) getActivity().findViewById(R.id.passwordEditText);
        passwordText.addTextChangedListener(new PasswordTextWatcher());
    }

    // Private instance methods.

    /** Determine if the email specified already exists */
    private void checkEmailAccountExists(@NonNull final String email) {
        if (!TextUtils.isEmpty(email)) {
            accountIsKnown = false;
            FirebaseAuth.getInstance().fetchProvidersForEmail(email)
                    .addOnFailureListener(
                            new TaskFailureLogger(TAG, "Error fetching providers for email"))
                    .addOnCompleteListener(
                            getActivity(),
                            new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    // we need the onSuccess results to continue so this is a no op
                                }
                            })
                    .addOnSuccessListener(getActivity(), new EmailOnSuccessListener());
        }
    }

    /** Enable (or disable) the 'finish' button in the toolbar */
    private void enableFinishButton(final boolean enable) {
        final TextView finishButton =
                (TextView) getActivity().findViewById(R.id.create_button_finish);
        finishButton.setEnabled(enable);
    }

    /** Retrieve the email address from the edit text field */
    private String getEmailAddress() {
        final TextInputEditText emailText =
                (TextInputEditText) getActivity().findViewById(R.id.emailEditText);
        return emailText.getText().toString();
    }

    /** Retrieve the user name from the edit text field */
    private String getUserName() {
        final TextInputEditText nameText =
                (TextInputEditText) getActivity().findViewById(R.id.nameEditText);
        return nameText.getText().toString();
    }

    /** Retrieve the password from the edit text field */
    private String getPassword() {
        final TextInputEditText passwordText =
                (TextInputEditText) getActivity().findViewById(R.id.passwordEditText);
        return passwordText.getText().toString();
    }

    /** Determine if value is a valid email format. */
    private static boolean isValidEmailFormat(String email) {
        return !TextUtils.isEmpty(email) &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /** Determine if value is a valid password as per Firebase spec */
    private static boolean isValidPasswordFormat(String password) {
        return (password.length() >= PASSWORD_MIN);
    }

    /** Set or clear error for email layout */
    private void setEmailError(boolean hasError) {
        TextInputLayout emailLayout =
                (TextInputLayout) getActivity().findViewById(R.id.email_layout);
        if (hasError) {
            emailLayout.setErrorEnabled(true);
            emailLayout.setError(getActivity().getString(R.string.EmailInvalidMessage));
        }
        else {
            emailLayout.setError(null);
            emailLayout.setErrorEnabled(false);
        }
    }

    /** Set or clear error for password layout */
    private void setPasswordError(boolean hasError) {
        TextInputLayout passwordLayout =
                (TextInputLayout) getActivity().findViewById(R.id.password_layout);
        if (hasError) {
            passwordLayout.setErrorEnabled(true);
            passwordLayout.setError(getActivity().getString(R.string.PasswordInvalidMessage));
        }
        else {
            passwordLayout.setError(null);
            passwordLayout.setErrorEnabled(true);
        }
    }

    /**
     * Set visibility of password layout. If it is not visible, also clear the error (if any). If
     * requested, set focus to EditText field.
     */
    private void showPasswordLayout(boolean visible, boolean setFocus) {
        TextInputLayout layout = (TextInputLayout) getActivity().findViewById(R.id.password_layout);
        layout.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) {
            layout.setError(null);
            layout.setErrorEnabled(false);
        }
        else if (setFocus) {
            EditText editText = (EditText) getActivity().findViewById(R.id.passwordEditText);
            editText.requestFocus();
        }
    }

    /** Set visibility of name layout. If requested, set focus to EditText field. */
    private void showNameLayout(boolean visible, boolean setFocus) {
        TextInputLayout layout = (TextInputLayout) getActivity().findViewById(R.id.name_layout);
        layout.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (setFocus) {
            EditText editText = (EditText) getActivity().findViewById(R.id.nameEditText);
            editText.requestFocus();
        }
    }
}
