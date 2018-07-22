package org.janssen.scoreboard.task;

/**
 * Created by stephan on 10/12/2017.
 */

import android.os.AsyncTask;

import org.janssen.scoreboard.Constants;
import org.janssen.scoreboard.comms.NetworkUtilities;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * An asynchronous start game task
 */
public class StartGameTask extends AsyncTask<Void, Void, String> {

    private OnTaskCompleted listener;
    private String authToken;
    private String game;
    private boolean mirrored;

    public StartGameTask(final OnTaskCompleted listener,
                         final String authToken,
                         final String game,
                         final boolean mirrored) {
        this.listener = listener;
        this.authToken = authToken;
        this.mirrored = mirrored;
        this.game = game;
    }
    @Override
    protected String doInBackground(Void... params) {
        int gameId = 0;

        try {
            JSONObject gameArray = new JSONObject(game);
            JSONObject game = (JSONObject)gameArray.get(Constants.GAME);
            gameId = game.getInt(Constants.ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            NetworkUtilities.startGame(authToken, gameId, mirrored);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    protected void onPostExecute(final String games) {

        listener.onTaskCompleted(null);
    }
}