package org.janssen.scoreboard;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.janssen.scoreboard.comms.NetworkUtilities;
import org.janssen.scoreboard.model.Server;
import org.janssen.scoreboard.model.types.RoomType;

/**
 * Login activity.
 * Created by stephan on 16/06/13.
 */
public class LoginActivity extends WifiControlActivity {

    /**
     * The Intent extra to store username.
     */
    public static final String PARAM_USERNAME = "username";

    /**
     * The tag used to log to adb console.
     */
    private static final String TAG = "LoginActivity";

    /**
     * Keep track of the login task so can cancel it if requested
     */
    private UserLoginTask mAuthTask = null;

    private TextView mMessage;

    private String mPassword;

    private EditText mPasswordEdit;

    private ProgressBar progressBar;

    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean mRequestNewAccount = false;

    private String mUsername;

    private EditText mUsernameEdit;

    private Integer selectedCourt;

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.activity_login);

        // Use the Server IP number based on selected court
        Bundle bundle = getIntent().getExtras();
        selectedCourt = (Integer)bundle.get(Constants.COURT);
        if (selectedCourt != null) {
            Server.setIp(RoomType.getIpForCourt(selectedCourt));
        } else {
            Server.setIp(RoomType.getIpForCourt(RoomType.ROOM_B.ordinal()));
        }

        validateWifiAndServerComms();

        final Intent intent = getIntent();
        mUsername = intent.getStringExtra(PARAM_USERNAME);
        mRequestNewAccount = mUsername == null;

        progressBar = findViewById(R.id.progressBar);

        mMessage = findViewById(R.id.message);
        mUsernameEdit = findViewById(R.id.username_edit);
        mPasswordEdit = findViewById(R.id.password_edit);
        if (!TextUtils.isEmpty(mUsername)) {
            mUsernameEdit.setText(mUsername);
        }
        mMessage.setText(getMessage());
    }

    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication. The button is configured to call
     * handleLogin() in the layout XML.
     *
     * @param view The Submit button for which this method is invoked
     */
    public void handleLogin(View view) {
        if (mRequestNewAccount) {
            mUsername = mUsernameEdit.getText().toString();
        }
        mPassword = mPasswordEdit.getText().toString();

        if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
            mMessage.setText(getMessage());
        } else {
            progressBar.setVisibility(View.VISIBLE);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute();
        }
    }

    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. We store the
     * authToken that's returned from the server as the 'password' for this
     * account - so we're never storing the user's actual password locally.
     *
     */
    private void finishLogin(String authToken) {

        Server.setToken(authToken);

        Intent intent = new Intent(getApplicationContext(), NewGameActivity.class);
        intent.putExtra(Constants.AUTH_TOKEN, authToken);
        intent.putExtra(Constants.COURT, selectedCourt);
        startActivity(intent);

    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     *
     * @param result the authentication token returned by the server, or NULL if
     *                  authentication failed.
     */
    public void onAuthenticationResult(String result) {

        boolean success = (result != null &&
                           result.length() > 0) &&
                           !result.contains("Exception");

        progressBar.setVisibility(View.INVISIBLE);

        // Our task is complete, so clear it out
        mAuthTask = null;

        if (success) {
            finishLogin(result);
        } else {
            if (mRequestNewAccount) {
                // "Please enter a valid username/password.
                mMessage.setText(result);
            } else {
                // "Please enter a valid password." (Used when the
                // account is already in the database but the password
                // doesn't work.)
                mMessage.setText(getText(R.string.login_activity_loginfail_text_pwonly));
            }
        }
    }

    public void onAuthenticationCancel() {
        Log.i(TAG, "onAuthenticationCancel()");

        // Our task is complete, so clear it out
        mAuthTask = null;
    }

    /**
     * Returns the message to be displayed at the top of the login dialog box.
     */
    private CharSequence getMessage() {
        getString(R.string.label);
        if (TextUtils.isEmpty(mUsername)) {
            // If no username, then we ask the user to log in using an appropriate service.
            return getText(R.string.login_activity_newaccount_text);
        }
        if (TextUtils.isEmpty(mPassword)) {
            // We have an account but no password
            return getText(R.string.login_activity_loginfail_text_pwmissing);
        }
        return null;
    }


    /**
     * Represents an asynchronous task used to authenticate a user against the
     * SampleSync Service
     */
    private class UserLoginTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // We do the actual work of authenticating the user
            // in the NetworkUtilities class.
            try {
                return NetworkUtilities.authenticate(mUsername, mPassword);
            } catch (Exception ex) {
                return ex.toString();
            }
        }

        @Override
        protected void onPostExecute(final String authToken) {
            // On a successful authentication, call back into the Activity to
            // communicate the authToken (or null for an error).
            onAuthenticationResult(authToken);
        }

        @Override
        protected void onCancelled() {
            // If the action was canceled (by the user clicking the cancel
            // button in the progress dialog), then call back into the
            // activity to let it know.
            onAuthenticationCancel();
        }
    }
}

