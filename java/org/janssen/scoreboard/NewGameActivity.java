package org.janssen.scoreboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.janssen.scoreboard.comms.NetworkUtilities;

import static org.janssen.scoreboard.Constants.AUTH_TOKEN;
import static org.janssen.scoreboard.Constants.COURT;
import static org.janssen.scoreboard.Constants.GAME;
import static org.janssen.scoreboard.Constants.MIRRORED;

/**
 * Info voor nieuwe wedstrijd.
 *
 * Created by stephan on 16/06/13.
 */
public class NewGameActivity extends WifiControlActivity {

    public static final int COURT_B = 1;
    public static final int SENIOREN = 0;

    private String authToken;

    private NewGameTask newGameTask;

    /**
     * The tag used to log to adb console.
     */
    private static final String TAG = "NewGameActivity";

    private ProgressBar progressBar;

    private EditText teamAEdit;
    private EditText teamBEdit;

    private String teamA;
    private String teamB;
    private Integer selectedCourt;
    private int gameType;
    private int ageCategory;

    private boolean mirroring = false;

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);

        setContentView(R.layout.activity_new_game);

        validateWifi();

        Bundle bundle = getIntent().getExtras();
        authToken = bundle.getString(AUTH_TOKEN);
        selectedCourt = (Integer) bundle.get(COURT);

        teamAEdit = findViewById(R.id.teamA);
        teamAEdit.setText(R.string.oostkamp);
        teamBEdit = findViewById(R.id.teamB);
        teamBEdit.setText(R.string.bezoekers);

        progressBar = findViewById(R.id.progressBar3);

        // Game Type
        Spinner gameTypeSpinner = findViewById(R.id.gameType);
        ArrayAdapter<CharSequence> adapterGameTypeSpinner
                = ArrayAdapter.createFromResource(this, R.array.game_types, android.R.layout.simple_spinner_item);
        adapterGameTypeSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        gameTypeSpinner.setAdapter(adapterGameTypeSpinner);
        gameTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                gameType = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Age Category
        Spinner ageCategorySpinner = findViewById(R.id.ageCategory);
        ArrayAdapter<CharSequence> adapterAgeCategorySpinner
                = ArrayAdapter.createFromResource(this, R.array.age_category, android.R.layout.simple_spinner_item);
        adapterAgeCategorySpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        ageCategorySpinner.setAdapter(adapterAgeCategorySpinner);
        ageCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ageCategory = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void createNewGame(View view) {
        Log.i(TAG, "create new game");

        teamA = teamAEdit.getText().toString();
        teamB = teamBEdit.getText().toString();

        if (TextUtils.isEmpty(teamA) || TextUtils.isEmpty(teamB)) {
            Toast.makeText(getApplicationContext(), "Provide team names", Toast.LENGTH_LONG);
        } else {

            if (ageCategory == SENIOREN &&
                selectedCourt == COURT_B) {

                final AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setTitle("Boodschap");
                dialog.setMessage("Wens je 2 scoreborden te gebruiken?");
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        startNewGame(true);
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Nee", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        startNewGame(false);
                    }
                });
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.show();
            } else {
                startNewGame(false);
            }
        }
    }

    private void startNewGame(final boolean isMirrored) {
        mirroring = isMirrored;

        // Show a progress dialog, and kick off a background task to perform the new game attempt
        progressBar.setVisibility(View.VISIBLE);

        newGameTask = new NewGameTask();
        newGameTask.execute();
    }

    /**
     * Called when the new game creation process completes.
     */
    public void onNewGameResult(String result) {
        progressBar.setVisibility(View.INVISIBLE);

        // Show count down
        Intent intent = new Intent(getApplicationContext(), CountDownActivity.class);
        intent.putExtra(MIRRORED, mirroring);
        intent.putExtra(GAME, result);
        intent.putExtra(AUTH_TOKEN, authToken);
        intent.putExtra(COURT, selectedCourt);
        startActivity(intent);
    }


    public void onNewGameCancel() {
        Log.i(TAG, "onGamesListCancel()");

        // Our task is complete, so clear it out
        newGameTask = null;

        // Hide the progress dialog
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * Represents an asynchronous task used to authenticate a user against the
     * SampleSync Service
     */
    private class NewGameTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                return NetworkUtilities.newGame(authToken, teamA, teamB, gameType, ageCategory, selectedCourt, mirroring);
            } catch (Exception ex) {
                Log.e(TAG, "New game task");
                Log.i(TAG, ex.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String games) {
            // On a successful authentication, call back into the Activity to
            // communicate the games (or null for an error).
            onNewGameResult(games);
        }

        @Override
        protected void onCancelled() {
            // If the action was canceled (by the user clicking the cancel
            // button in the progress dialog), then call back into the
            // activity to let it know.
            onNewGameCancel();
        }
    }

}
