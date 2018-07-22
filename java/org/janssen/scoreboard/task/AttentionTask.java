package org.janssen.scoreboard.task;

/**
 * Created by stephan on 10/12/2017.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.janssen.scoreboard.comms.NetworkUtilities;

/**
 * Represents an asynchronous task used to set the quarters
 */
public class AttentionTask extends AsyncTask<Void, Void, String> {

    /**
     * The tag used to log to adb console.
     */
    private static final String TAG = "AttentionTask";

    private String authToken;

    public AttentionTask(String authToken) {
        this.authToken = authToken;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return NetworkUtilities.attention(authToken);
        } catch (Exception ex) {
            Log.e(TAG, "AttentionTask.doInBackground: failed to call attention");
            Log.i(TAG, ex.toString());
            return null;
        }
    }
}