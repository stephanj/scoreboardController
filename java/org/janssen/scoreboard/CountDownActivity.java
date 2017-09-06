package org.janssen.scoreboard;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.janssen.scoreboard.comms.NetworkUtilities;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static org.janssen.scoreboard.Constants.AUTH_TOKEN;
import static org.janssen.scoreboard.Constants.COURT;
import static org.janssen.scoreboard.Constants.GAME;
import static org.janssen.scoreboard.Constants.ID;
import static org.janssen.scoreboard.Constants.MIRRORED;

/**
 * Count down activity.
 * Created by stephan on 18/08/13.
 */
public class CountDownActivity extends WifiControlActivity {
    /**
     * The tag used to log to adb console.
     */
    private static final String TAG = "countDownActivity";

    private String authToken;
    private String game;
    private Integer selectedCourt;
    private boolean mirrored;

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.activity_countdown);

        validateWifi();

        Bundle bundle = getIntent().getExtras();
        authToken = bundle.getString(AUTH_TOKEN);
        mirrored = bundle.getBoolean(MIRRORED);
        game = bundle.getString(GAME);
        selectedCourt = (Integer)bundle.get(COURT);
    }

    public void startNewGame(final View view) {
        // Hide keyboard so we can go into immersive (fullscreen) mode
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.minuten).getWindowToken(), 0);

        new StartGameTask().execute();
    }

    public void startClock(final View view) {
        EditText clockMinutesText = findViewById(R.id.minuten);

        String clockMinutes = clockMinutesText.getText().toString();
        new ClockCountdownTask().execute(clockMinutes);
    }

    public void showScorePage() {
        Intent intent = new Intent(getApplicationContext(), ScoreActivity.class);
        intent.putExtra(GAME, game);
        intent.putExtra(AUTH_TOKEN, authToken);
        intent.putExtra(COURT, selectedCourt);
        startActivity(intent);
    }

    /**
     * An asynchronous clock countdown task
     */
    private class ClockCountdownTask extends AsyncTask<String, String, String> {

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

    /**
     * An asynchronous start game task
     */
    private class StartGameTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            int gameId = 0;

            try {
                JSONObject gameArray = new JSONObject(game);
                JSONObject game = (JSONObject)gameArray.get(GAME);
                gameId = game.getInt(ID);
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
            showScorePage();
        }
    }
}
