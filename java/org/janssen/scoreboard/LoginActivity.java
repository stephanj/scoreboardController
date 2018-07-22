package org.janssen.scoreboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.janssen.scoreboard.model.Server;
import org.janssen.scoreboard.model.types.RoomType;
import org.janssen.scoreboard.task.OnTaskListener;
import org.janssen.scoreboard.task.UserLoginTask;

/**
 * Login activity.
 * Created by stephan on 16/06/13.
 */
public class LoginActivity extends WifiControlActivity implements OnTaskListener {

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
    private UserLoginTask loginTask = null;

    private Integer selectedCourt;
    private String username;
    private String password;

    private TextView message;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private ProgressBar progressBar;

    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean requestNewAccount = false;

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.activity_login);

        // Use the Server IP number based on selected court
        Bundle bundle = getIntent().getExtras();
        if (bundle != null &&
            bundle.containsKey(Constants.COURT)) {
            selectedCourt = (Integer) bundle.get(Constants.COURT);
            if (selectedCourt != null) {
                Server.setIp(RoomType.getIpForCourt(selectedCourt));
            } else {
                Server.setIp(RoomType.getIpForCourt(RoomType.ROOM_B.ordinal()));
            }
        } else {
            Toast.makeText(getApplicationContext(), "Court was not selected", Toast.LENGTH_LONG).show();
            return;
        }

        validateWifiAndServerComms();

        final Intent intent = getIntent();
        username = intent.getStringExtra(PARAM_USERNAME);
        requestNewAccount = username == null;

        progressBar = findViewById(R.id.progressBar);

        message = findViewById(R.id.message);
        usernameEdit = findViewById(R.id.username_edit);
        passwordEdit = findViewById(R.id.password_edit);
        if (!TextUtils.isEmpty(username)) {
            usernameEdit.setText(username);
        }
        message.setText(getMessage());
    }

    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication. The button is configured to call
     * handleLogin() in the layout XML.
     *
     * @param view The Submit button for which this method is invoked
     */
    public void handleLogin(View view) {
        if (requestNewAccount) {
            username = usernameEdit.getText().toString();
        }
        password = passwordEdit.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            message.setText(getMessage());
        } else {
            progressBar.setVisibility(View.VISIBLE);
            loginTask = new UserLoginTask(this, username, password);
            loginTask.execute();
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
     * Returns the message to be displayed at the top of the login dialog box.
     */
    private CharSequence getMessage() {
        getString(R.string.label);
        if (TextUtils.isEmpty(username)) {
            // If no username, then we ask the user to log in using an appropriate service.
            return getText(R.string.login_activity_newaccount_text);
        }
        if (TextUtils.isEmpty(password)) {
            // We have an account but no password
            return getText(R.string.login_activity_loginfail_text_pwmissing);
        }
        return null;
    }

    @Override
    public void onTaskCancelled() {
        Log.i(TAG, "onAuthenticationCancel()");

        // Our task is complete, so clear it out
        loginTask = null;
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     *
     * @param result the authentication token returned by the server, or NULL if
     *                  authentication failed.
     */
    @Override
    public void onTaskCompleted(String result) {

        boolean success = (result != null &&
                           result.length() > 0) &&
                           !result.contains("Exception");

        progressBar.setVisibility(View.INVISIBLE);

        // Our task is complete, so clear it out
        loginTask = null;

        if (success) {
            finishLogin(result);
        } else {
            if (requestNewAccount) {
                // "Please enter a valid username/password.
                message.setText(result);
            } else {
                // "Please enter a valid password." (Used when the
                // account is already in the database but the password
                // doesn't work.)
                message.setText(getText(R.string.login_activity_loginfail_text_pwonly));
            }
        }
    }
}

