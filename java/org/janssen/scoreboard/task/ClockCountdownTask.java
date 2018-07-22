package org.janssen.scoreboard.task;

import android.os.AsyncTask;
import android.util.Log;

import org.janssen.scoreboard.comms.NetworkUtilities;

/**
 * An asynchronous clock countdown task
 */
public class ClockCountdownTask extends AsyncTask<String, String, String> {

    private static final String TAG = "countDownActivity";

    final String authToken;
    final boolean mirrored;

    public ClockCountdownTask(final String authToken,
                              final boolean mirrored) {
        this.authToken = authToken;
        this.mirrored = mirrored;
    }

    @Override
    protected String doInBackground(String... args) {
        try {
            if (args[0] != null && args[0].length() > 0) {
                int minuten = Integer.parseInt(args[0].trim());
                NetworkUtilities.countDown(minuten * 60, mirrored, authToken);
            }
            return "";
        } catch (Exception ex) {
            Log.e(TAG, "Clock countdown task");
            Log.i(TAG, ex.toString());
            return null;
        }
    }
}