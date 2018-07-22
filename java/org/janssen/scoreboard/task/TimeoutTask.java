package org.janssen.scoreboard.task;

import android.os.AsyncTask;
import android.util.Log;

import org.janssen.scoreboard.comms.NetworkUtilities;

/**
 * Represents an asynchronous task used to set the quarters
 */
public class TimeoutTask extends AsyncTask<Void, Void, String> {

    /**
     * The tag used to log to adb console.
     */
    private static final String TAG = "TimeoutTask";

    private int teamId;
    private String authToken;

    public TimeoutTask(final int teamId,
                       final String authToken) {
        this.teamId = teamId;
        this.authToken = authToken;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return NetworkUtilities.incrementTimeout(authToken, teamId);
        } catch (Exception ex) {
            Log.e(TAG, "TimeoutTask.doInBackground: failed to set score");
            Log.i(TAG, ex.toString());
            return null;
        }
    }
}