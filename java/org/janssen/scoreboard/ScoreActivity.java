package org.janssen.scoreboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.janssen.scoreboard.comms.NetworkUtilities;
import org.janssen.scoreboard.model.Server;
import org.janssen.scoreboard.model.types.ClockAction;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.janssen.scoreboard.Constants.AUTH_TOKEN;
import static org.janssen.scoreboard.Constants.GAME;
import static org.janssen.scoreboard.Constants.ID;
import static org.janssen.scoreboard.Constants.NAME;
import static org.janssen.scoreboard.Constants.TEAM_A;
import static org.janssen.scoreboard.Constants.TEAM_B;

public class ScoreActivity extends ImmersiveStickyActivity {

    /**
     * The tag used to log to adb console.
     */
    private static final String TAG = "ScoreActivity";

    public static final String NETWORK_PROBLEEM_PROBEER_OPNIEUW = "Communicatie probleem, probeer opnieuw!";
    public static final String ERROR_PROBEER_OPNIEUW = "Error, probeer opnieuw -";
    public static final String OPNIEUW = "opnieuw";
    public static final String OK = "OK";

    private ImageButton scoreA1;
    private ImageButton scoreA2;
    private ImageButton scoreA3;

    private ImageButton scoreB1;
    private ImageButton scoreB2;
    private ImageButton scoreB3;

    private ImageButton foulA;
    private ImageButton foulB;

    private ImageButton quarterBtn;

    private ImageButton homeTimeoutBtn;
    private ImageButton visitorsTimeoutBtn;

    private ImageButton timeBtn;
    private ImageButton timeUpBtn;
    private ImageButton timeDownBtn;

    private ImageButton attentionBtn;

    private TextView homeTeam;
    private TextView visitorsTeam;

    private String teamAName;
    private String teamBName;

    private boolean isPositive = true;
    private boolean isClockRunning = false;

    private String authToken;

    private int gameId;
    private int teamA;
    private int teamB;

    final CharSequence[] personalFouls = {"1", "2", "3", "4", "5"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // validateWifi();

        addListenerOnButton();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                authToken = bundle.getString(AUTH_TOKEN);

                if (bundle.containsKey(GAME)) {
                    String gameString = bundle.getString(GAME);

                    if (gameString != null && gameString.length() > 0) {

                        JSONObject gameArray = new JSONObject(gameString);

                        JSONObject game = (JSONObject)gameArray.get(GAME);
                        JSONObject jsonTeamA = (JSONObject)game.get(TEAM_A);
                        JSONObject jsonTeamB = (JSONObject)game.get(TEAM_B);

                        homeTeam = (TextView) findViewById(R.id.homeTeam);
                        teamAName = (String)jsonTeamA.get(NAME);
                        homeTeam.setText(teamAName + " 0");

                        visitorsTeam = (TextView) findViewById(R.id.visitorsTeam);
                        teamBName = (String)jsonTeamB.get(NAME);
                        visitorsTeam.setText(teamBName + " 0");

                        gameId = game.getInt(ID);
                        Server.setGameId(gameId);

                        teamA = jsonTeamA.getInt(ID);
                        teamB = jsonTeamB.getInt(ID);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Game info available!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        new ClockTask(authToken, gameId, ClockAction.STOP).execute();
    }

    @Override
    public void onBackPressed() {
        // Ignore the back button
    }

    public void addListenerOnButton() {

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                ImageButton btn = (ImageButton) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    // TODO Could probably be replaced by using an /drawable/button.xml file
                    if (btn.getId() == scoreA1.getId()) {
                        btn.setImageResource(R.drawable.press_one);
                        processScoreA(btn.getId());

                    } else if (btn.getId() == scoreA2.getId()) {
                        btn.setImageResource(R.drawable.press_two);
                        processScoreA(btn.getId());

                    } else if (btn.getId() == scoreA3.getId()) {
                        btn.setImageResource(R.drawable.press_three);
                        processScoreA(btn.getId());

                    } else if (btn.getId() == scoreB1.getId()) {
                        btn.setImageResource(R.drawable.press_one);
                        processScoreB(btn.getId());

                    } else if (btn.getId() == scoreB2.getId()) {
                        btn.setImageResource(R.drawable.press_two);
                        processScoreB(btn.getId());

                    } else if (btn.getId() == scoreB3.getId()) {
                        btn.setImageResource(R.drawable.press_three);
                        processScoreB(btn.getId());

                    } else if (btn.getId() == foulA.getId()) {
                        btn.setImageResource(R.drawable.press_foul);
                        showFoulsDialog(v, teamA);

                    } else if (btn.getId() == foulB.getId()) {
                        btn.setImageResource(R.drawable.press_foul);
                        showFoulsDialog(v, teamB);

                    } else if (btn.getId() == quarterBtn.getId()) {
                        btn.setImageResource(R.drawable.press_quarter);
                        executeQuarterTask(gameId);

                        // Would be nice if a websocket push request does this!
                        timeBtn.setImageResource(R.drawable.clock);
                        isClockRunning = false;

                    } else if (btn.getId() == attentionBtn.getId()) {
                        attentionBtn.setImageResource(R.drawable.press_attention);
                        new AttentionTask().execute();

                    } else if (btn.getId() == homeTimeoutBtn.getId()) {
                        homeTimeoutBtn.setImageResource(R.drawable.press_timeout);
                        new TimeoutTask(teamA).execute();

                    } else if (btn.getId() == visitorsTimeoutBtn.getId()) {
                        visitorsTimeoutBtn.setImageResource(R.drawable.press_timeout);
                        new TimeoutTask(teamB).execute();

                    } else if (btn.getId() == timeUpBtn.getId() && !isClockRunning) {
                        if (isPositive) {
                            btn.setImageResource(R.drawable.press_plus_one_sec);
                            executeClockTask(authToken, gameId, ClockAction.INC, 1);
                        } else {
                            executeClockTask(authToken, gameId, ClockAction.INC, 20);
                        }

                    } else if (btn.getId() == timeDownBtn.getId() && !isClockRunning) {
                        if (isPositive) {
                            btn.setImageResource(R.drawable.press_minus_one_sec);
                            executeClockTask(authToken, gameId, ClockAction.DEC, 1);
                        } else {
                            executeClockTask(authToken, gameId, ClockAction.DEC, 20);
                        }

                    } else if (btn.getId() == timeBtn.getId()) {
                        invalidateOptionsMenu();

                        isClockRunning = !isClockRunning;

                        executeClockTask(authToken, gameId, isClockRunning? ClockAction.START : ClockAction.STOP);
                    }

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (btn.getId() == scoreA1.getId() ||
                        btn.getId() == scoreB1.getId()) {
                        btn.setImageResource(isPositive ? R.drawable.one : R.drawable.min_one);
                    } else if (btn.getId() == scoreA2.getId() ||
                               btn.getId() == scoreB2.getId()) {
                        btn.setImageResource(isPositive ? R.drawable.two : R.drawable.min_two);
                    } else if (btn.getId() == scoreA3.getId() ||
                               btn.getId() == scoreB3.getId()) {
                        btn.setImageResource(isPositive? R.drawable.three : R.drawable.min_three);
                    } else if (btn.getId() == foulA.getId() ||
                               btn.getId() == foulB.getId()) {
                        btn.setImageResource(isPositive ? R.drawable.foul : R.drawable.min_foul);
                    } else if (btn.getId() == quarterBtn.getId()) {
                        btn.setImageResource(isPositive ? R.drawable.quarter : R.drawable.min_quarter);
                    } else if (btn.getId() == timeUpBtn.getId()) {
                        btn.setImageResource(isPositive ? R.drawable.plusone : R.drawable.clock_plus_20s);
                    } else if (btn.getId() == timeDownBtn.getId()) {
                        btn.setImageResource(isPositive ? R.drawable.minusone : R.drawable.clock_minus_20s);
                    } else if (btn.getId() == attentionBtn.getId()) {
                        btn.setImageResource(R.drawable.attention);
                    } else if (btn.getId() == homeTimeoutBtn.getId() ||
                               btn.getId() == visitorsTimeoutBtn.getId()) {
                        btn.setImageResource(R.drawable.timeout);
                    }
                }

                return false;
            }
        };

        Button newGameBtn = (Button) findViewById(R.id.newGame);
        newGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewGameDialog(view);
            }
        });

        // Team A
        scoreA1 = (ImageButton) findViewById(R.id.scoreA1);
        scoreA1.setOnTouchListener(onTouchListener);

        scoreA2 = (ImageButton) findViewById(R.id.scoreA2);
        scoreA2.setOnTouchListener(onTouchListener);

        scoreA3 = (ImageButton) findViewById(R.id.scoreA3);
        scoreA3.setOnTouchListener(onTouchListener);

        // Team B
        scoreB1 = (ImageButton) findViewById(R.id.scoreB1);
        scoreB1.setOnTouchListener(onTouchListener);

        scoreB2 = (ImageButton) findViewById(R.id.scoreB2);
        scoreB2.setOnTouchListener(onTouchListener);

        scoreB3 = (ImageButton) findViewById(R.id.scoreB3);
        scoreB3.setOnTouchListener(onTouchListener);

        // Fouls
        foulA = (ImageButton) findViewById(R.id.foulA);
        foulA.setOnTouchListener(onTouchListener);

        foulB = (ImageButton) findViewById(R.id.foulB);
        foulB.setOnTouchListener(onTouchListener);

        // Quarter
        quarterBtn = (ImageButton) findViewById(R.id.quarterBtn);
        quarterBtn.setOnTouchListener(onTouchListener);

        // Timeout buttons
        homeTimeoutBtn = (ImageButton) findViewById(R.id.homeTimeoutButton);
        homeTimeoutBtn.setOnTouchListener(onTouchListener);

        visitorsTimeoutBtn = (ImageButton) findViewById(R.id.visitorsTimeoutButton);
        visitorsTimeoutBtn.setOnTouchListener(onTouchListener);

        // Attention button
        attentionBtn = (ImageButton) findViewById(R.id.attentionBtn);
        attentionBtn.setOnTouchListener(onTouchListener);

        // Time
        timeBtn = (ImageButton) findViewById(R.id.timeButton);
        timeBtn.setOnTouchListener(onTouchListener);

        timeUpBtn = (ImageButton) findViewById(R.id.clockUp);
        timeUpBtn.setOnTouchListener(onTouchListener);

        timeDownBtn = (ImageButton) findViewById(R.id.clockDown);
        timeDownBtn.setOnTouchListener(onTouchListener);

        Switch switchBtn = (Switch) findViewById(R.id.switch1);
        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPositive = !isPositive;

                if (isPositive) {
                    scoreA1.setImageResource(R.drawable.one);
                    scoreA2.setImageResource(R.drawable.two);
                    scoreA3.setImageResource(R.drawable.three);
                    scoreB1.setImageResource(R.drawable.one);
                    scoreB2.setImageResource(R.drawable.two);
                    scoreB3.setImageResource(R.drawable.three);
                    foulA.setImageResource(R.drawable.foul);
                    foulB.setImageResource(R.drawable.foul);
                    quarterBtn.setImageResource(R.drawable.quarter);
                    timeUpBtn.setImageResource(R.drawable.plusone);
                    timeDownBtn.setImageResource(R.drawable.minusone);

                } else {
                    scoreA1.setImageResource(R.drawable.min_one);
                    scoreA2.setImageResource(R.drawable.min_two);
                    scoreA3.setImageResource(R.drawable.min_three);
                    scoreB1.setImageResource(R.drawable.min_one);
                    scoreB2.setImageResource(R.drawable.min_two);
                    scoreB3.setImageResource(R.drawable.min_three);
                    foulA.setImageResource(R.drawable.min_foul);
                    foulB.setImageResource(R.drawable.min_foul);
                    quarterBtn.setImageResource(R.drawable.min_quarter);
                    timeUpBtn.setImageResource(R.drawable.clock_plus_20s);
                    timeDownBtn.setImageResource(R.drawable.clock_minus_20s);
                }
            }
        });
    }

    private void processScoreA(int buttonId) {
        int points = 0;

        switch (buttonId) {
            case R.id.scoreA1:  points = 1; break;
            case R.id.scoreA2:  points = 2; break;
            case R.id.scoreA3:  points = 3; break;
        }

        executeScoreTask(authToken, homeTeam, teamAName, teamA, points);
    }

    private void processScoreB(int buttonId) {
        int points = 0;

        switch (buttonId) {
            case R.id.scoreB1:  points = 1; break;
            case R.id.scoreB2:  points = 2; break;
            case R.id.scoreB3:  points = 3; break;
        }

        executeScoreTask(authToken, visitorsTeam, teamBName, teamB, points);
    }

    private void executeQuarterTask(final int gameId) {

        QuarterTask quarterTask = new QuarterTask(gameId);
        quarterTask.execute();

        try {
            String result = quarterTask.get();
            Toast toast;
            if (result.equalsIgnoreCase(OK)) {
                toast = Toast.makeText(getApplicationContext(), OK, Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
            }
            toast.setGravity(Gravity.BOTTOM, 0, -200);
            toast.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void executeScoreTask(final String token,
                                  final TextView teamView,
                                  final String teamName,
                                  final int teamId,
                                  final int points) {

        final ScoreTask scoreTask = new ScoreTask(token, teamView, teamName, teamId, points);
        scoreTask.execute();
        try {
            String result = scoreTask.get();
            Toast toast;
            if (result.contains(NETWORK_PROBLEEM_PROBEER_OPNIEUW)) {
                toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
            } else {
                toast = Toast.makeText(getApplicationContext(), OK, Toast.LENGTH_SHORT);
            }
            toast.setGravity(Gravity.BOTTOM, 0, -200);
            toast.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void executeClockTask(final String token,
                                  final int gameId,
                                  final ClockAction action) {
        executeClockTask(token, gameId, action, 1);
    }

    private void executeClockTask(final String token,
                                  final int gameId,
                                  final ClockAction action,
                                  final int seconds) {
        final ClockTask task = new ClockTask(token, gameId, action, seconds);
        task.execute();
        try {
            String result = task.get();
            if (result.equals(OK)) {
                if (action == ClockAction.START) {
                    timeUpBtn.setEnabled(false);
                    timeDownBtn.setEnabled(false);
                    timeBtn.setImageResource(R.drawable.clockactive);
                } else if (action == ClockAction.STOP) {
                    timeUpBtn.setEnabled(true);
                    timeDownBtn.setEnabled(true);
                    timeBtn.setImageResource(R.drawable.clock);
                }
            }
            Toast toast= Toast.makeText(getApplicationContext(), result, result.equals(OK) ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, -200);
            toast.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isClockRunning) {
            menu.getItem(0).setEnabled(false);
        }
        return true;
    }

    /**
     * Shows the personal fouls dialog.
     *
     * @param v         the view
     * @param teamID    the team identifier
     */
    private void showFoulsDialog(View v, final int teamID) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

        builder.setTitle(getString(R.string.personal_fouls))
               .setSingleChoiceItems(personalFouls, 0, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialogInterface, int item) {
                       new FoulTask(authToken, teamID, item + 1).execute();
                       dialogInterface.dismiss();
                   }
               })

               .setNegativeButton(getString(R.string.cancel_txt), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
                       dialog.dismiss();
                   }
               })
               .create()
               .show();
    }

    /**
     * Shows the personal fouls dialog.
     *
     * @param v         the view
     */
    private void showNewGameDialog(View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

        builder.setTitle(getString(R.string.new_game_question))

               .setPositiveButton(getString(R.string.ok_txt), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialogInterface, int item) {
                       Intent intent = new Intent(getApplicationContext(), CourtActivity.class);
                       startActivity(intent);
                   }
               })

               .setNegativeButton(getString(R.string.cancel_txt), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
                       dialog.dismiss();
                   }
               })
               .create()
               .show();
    }

    /**
     * Represents an asynchronous task used to set the score
     */
    public class ScoreTask extends AsyncTask<Void, Void, String> {

        private String token;
        private int teamId;
        private int points;
        private TextView textView;
        private String teamName;

        public ScoreTask(final String token,
                         final TextView textView,
                         final String teamName,
                         final int teamId,
                         final int points) {
            this.token = token;
            this.textView = textView;
            this.teamName = teamName;
            this.teamId = teamId;
            this.points = points;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return NetworkUtilities.updateScore(token, isPositive, teamId, points);
            } catch (IOException ex) {
                return NETWORK_PROBLEEM_PROBEER_OPNIEUW;
            } catch (Exception ex) {
                return ERROR_PROBEER_OPNIEUW + ex.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.contains(OPNIEUW)) {
                textView.setText(teamName + " " + result);
            }
        }
    }

    /**
     * Represents an asynchronous task used to set the foul
     */
    public class FoulTask extends AsyncTask<Void, Void, String> {

        private String token;
        private int teamId;
        private int totalFouls;

        public FoulTask(final String token,
                        final int teamId,
                        final int totalFouls) {
            this.token = token;
            this.teamId = teamId;
            this.totalFouls = totalFouls;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return NetworkUtilities.updateFouls(token, isPositive, teamId, totalFouls);
            } catch (Exception ex) {
                Log.e(TAG, "FoulTask.doInBackground: failed to set fouls");
                Log.i(TAG, ex.toString());
                return null;
            }
        }
    }

    /**
     * Represents an asynchronous task used to set the quarters
     */
    public class QuarterTask extends AsyncTask<Void, Void, String> {

        private int gameId;

        public QuarterTask(int gameId) {
            this.gameId = gameId;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return NetworkUtilities.updateQuarters(authToken, isPositive, gameId);
            } catch (Exception ex) {
                return ex.toString();
            }
        }
    }

    /**
     * Represents an asynchronous task used to set the quarters
     */
    public class TimeoutTask extends AsyncTask<Void, Void, String> {

        private int teamId;

        public TimeoutTask(int teamId) {
            this.teamId = teamId;
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

    /**
     * Represents an asynchronous task used to set the quarters
     */
    public class AttentionTask extends AsyncTask<Void, Void, String> {

        public AttentionTask() {
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

    /**
     * Represents an asynchronous task used to set the clock
     */
    public class ClockTask extends AsyncTask<Void, Void, String> {

        public static final String ERROR_PROBEER_OPNIEUW = "Error probeer opnieuw - ";
        private String token;
        private int gameId;
        private int seconds;
        private ClockAction action;

        public ClockTask(final String token, final int gameId, final ClockAction action, final int seconds) {
            this.token = token;
            this.gameId = gameId;
            this.action = action;
            this.seconds = seconds;
        }

        public ClockTask(final String token, final int gameId, final ClockAction action) {
            this(token, gameId, action, 1);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            try {
                result = NetworkUtilities.triggerClock(token, gameId, action, seconds);
            } catch (IOException ex) {
                result = NETWORK_PROBLEEM_PROBEER_OPNIEUW + " \n " + ex.toString();
            } catch (Exception ex) {
                result = ERROR_PROBEER_OPNIEUW + ex.toString();
            }

            return result;
        }
    }

    /**
     * Represents an asynchronous task to redraw the score board for the given game
     */
    public class RedrawTask extends AsyncTask<Void, Void, String> {

        private int gameId;

        public RedrawTask(int gameId) {
            this.gameId = gameId;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return NetworkUtilities.redraw(authToken, gameId);
            } catch (Exception ex) {
                Log.e(TAG, "RedrawTask.doInBackground: failed to set redraw");
                Log.i(TAG, ex.toString());
                return null;
            }
        }
    }
}
