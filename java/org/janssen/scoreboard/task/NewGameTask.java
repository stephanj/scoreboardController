package org.janssen.scoreboard.task;

/**
 * Created by stephan on 10/12/2017.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.janssen.scoreboard.comms.RestURI;
import org.janssen.scoreboard.model.Server;

import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Represents an asynchronous task used to authenticate a user against the
 * SampleSync Service
 */
public class NewGameTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "NewGameTask";

    private OnTaskListener listener;
    private String teamA;
    private String teamB;
    private int gameType;
    private int ageCategory;
    private int selectedCourt;
    private boolean mirroring;
    private String authToken;

    public NewGameTask(final OnTaskListener listener,
                final String teamA,
                final String teamB,
                final int gameType,
                final int ageCategory,
                final int selectedCourt,
                final boolean mirroring,
                final String authToken) {
        this.listener = listener;
        this.teamA = teamA;
        this.teamB = teamB;
        this.gameType = gameType;
        this.ageCategory = ageCategory;
        this.selectedCourt = selectedCourt;
        this.mirroring = mirroring;
        this.authToken = authToken;
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            String NEW_GAME_URL = String.format(RestURI.CREATE_GAME.getValue(), Server.getIp(),
                    URLEncoder.encode(teamA, "UTF8"),
                    URLEncoder.encode(teamB, "UTF8"),
                    gameType, ageCategory, selectedCourt, mirroring, authToken);

            Request request = new Request.Builder()
                    .url(NEW_GAME_URL)
                    .post(null)
                    .build();

            OkHttpClient httpClient = new OkHttpClient();
            try (Response resp = httpClient.newCall(request).execute()) {
                if (resp.isSuccessful()) {
                    return resp.message();
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "New game task");
            Log.i(TAG, ex.toString());
        }

        return null;
    }

    @Override
    protected void onPostExecute(final String games) {
        // On a successful authentication, call back into the Activity to
        // communicate the games (or null for an error).
        listener.onTaskCompleted(games);
    }

    @Override
    protected void onCancelled() {
        // If the action was canceled (by the user clicking the cancel
        // button in the progress dialog), then call back into the
        // activity to let it know.
        listener.onTaskCancelled();
    }
}

