package org.janssen.scoreboard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import static org.janssen.scoreboard.Constants.*;
import org.janssen.scoreboard.comms.NetworkUtilities;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
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
        new ClockCountdownTask().execute();
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
    public class ClockCountdownTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                EditText clockMinutes = (EditText) findViewById(R.id.minuten);

                if (clockMinutes.getText() != null &&
                    clockMinutes.getText().length() > 0) {
                    String value = clockMinutes.getText().toString();
                    if (value != null) {
                        int minuten = Integer.parseInt(value.trim());
                        NetworkUtilities.countDown(minuten * 60, mirrored, authToken);
                    }
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
    public class StartGameTask extends AsyncTask<Void, Void, String> {

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
