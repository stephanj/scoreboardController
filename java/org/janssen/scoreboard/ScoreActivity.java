package org.janssen.scoreboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.janssen.scoreboard.model.Server;
import org.janssen.scoreboard.model.types.ClockAction;
import org.janssen.scoreboard.task.AttentionTask;
import org.janssen.scoreboard.task.ClockTask;
import org.janssen.scoreboard.task.FoulTask;
import org.janssen.scoreboard.task.QuarterTask;
import org.janssen.scoreboard.task.ScoreTask;
import org.janssen.scoreboard.task.TimeoutTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import static org.janssen.scoreboard.Constants.AUTH_TOKEN;
import static org.janssen.scoreboard.Constants.GAME;
import static org.janssen.scoreboard.Constants.ID;
import static org.janssen.scoreboard.Constants.NAME;
import static org.janssen.scoreboard.Constants.TEAM_A;
import static org.janssen.scoreboard.Constants.TEAM_B;

public class ScoreActivity extends ImmersiveStickyActivity {

    public static final String NETWORK_PROBLEEM_PROBEER_OPNIEUW = "Communicatie probleem, probeer opnieuw!";
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

    private ImageButton timeBtn;
    private ImageButton timeUpBtn;
    private ImageButton timeDownBtn;

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

        addButtonListeners();

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

                        homeTeam = findViewById(R.id.homeTeam);
                        teamAName = (String)jsonTeamA.get(NAME);
                        homeTeam.setText(String.format("%s 0", teamAName));

                        visitorsTeam = findViewById(R.id.visitorsTeam);
                        teamBName = (String)jsonTeamB.get(NAME);
                        visitorsTeam.setText(String.format("%s 0", teamBName));

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

    public void addButtonListeners() {

        Button newGameBtn = findViewById(R.id.newGame);
        newGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewGameDialog(view);
            }
        });

        // Setup score A and B buttons
        setupScoreATeamButtons();
        setupScoreBTeamButtons();

        // Fouls
        foulA = findViewById(R.id.foulA);
        foulA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFoulsDialog(view, teamA);
            }
        });

        foulB = findViewById(R.id.foulB);
        foulB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFoulsDialog(view, teamB);
            }
        });

        // Quarter
        quarterBtn = findViewById(R.id.quarterBtn);
        quarterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeQuarterTask(gameId);

                timeBtn.setImageResource(R.drawable.clock);
                isClockRunning = false;
            }
        });

        // Timeout buttons
        ImageButton homeTimeoutBtn = findViewById(R.id.homeTimeoutButton);
        homeTimeoutBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new TimeoutTask(teamA, authToken).execute();
            }
        });

        ImageButton visitorsTimeoutBtn = findViewById(R.id.visitorsTimeoutButton);
        visitorsTimeoutBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new TimeoutTask(teamB, authToken).execute();
            }
        });

        // Attention button
        ImageButton attentionBtn = findViewById(R.id.attentionBtn);
        attentionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AttentionTask(authToken).execute();
            }
        });

            // Time
        timeBtn = findViewById(R.id.timeButton);
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invalidateOptionsMenu();

                isClockRunning = !isClockRunning;

                executeClockTask(authToken, gameId, isClockRunning? ClockAction.START : ClockAction.STOP);
            }
        });

        timeUpBtn = findViewById(R.id.clockUp);
        timeUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPositive) {
                    executeClockTask(authToken, gameId, ClockAction.INC, 1);
                } else {
                    executeClockTask(authToken, gameId, ClockAction.INC, 20);
                }
            }
        });

        timeDownBtn = findViewById(R.id.clockDown);
        timeDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPositive) {
                    executeClockTask(authToken, gameId, ClockAction.DEC, 1);
                } else {
                    executeClockTask(authToken, gameId, ClockAction.DEC, 20);
                }
            }
        });

        Switch switchBtn = findViewById(R.id.switch1);
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

    private void setupScoreATeamButtons() {
        scoreA1 = findViewById(R.id.scoreA1);
        scoreA2 = findViewById(R.id.scoreA2);
        scoreA3 = findViewById(R.id.scoreA3);

        View.OnClickListener scoreAListener = new View.OnClickListener() {
            public void onClick(View v) {
                processScoreA(v.getId());
            }
        };

        // Team A
        scoreA1.setOnClickListener(scoreAListener);
        scoreA2.setOnClickListener(scoreAListener);
        scoreA3.setOnClickListener(scoreAListener);
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

    private void setupScoreBTeamButtons() {
        scoreB1 = findViewById(R.id.scoreB1);
        scoreB2 = findViewById(R.id.scoreB2);
        scoreB3 = findViewById(R.id.scoreB3);

        View.OnClickListener scoreBListener = new View.OnClickListener() {
            public void onClick(View v) {
                processScoreB(v.getId());
            }
        };

        // Score B
        scoreB1.setOnClickListener(scoreBListener);
        scoreB2.setOnClickListener(scoreBListener);
        scoreB3.setOnClickListener(scoreBListener);
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

    @SuppressLint("ShowToast")
    private void executeQuarterTask(final int gameId) {

        QuarterTask quarterTask = new QuarterTask(gameId, authToken, isPositive);
        quarterTask.execute();

        try {
            final String result = quarterTask.get();
            Toast toast;
            if (result.equalsIgnoreCase(OK)) {
                toast = Toast.makeText(getApplicationContext(), OK, Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
            }
            toast.setGravity(Gravity.BOTTOM, 0, -200);
            toast.show();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ShowToast")
    private void executeScoreTask(final String token,
                                  final TextView teamView,
                                  final String teamName,
                                  final int teamId,
                                  final int points) {

        final ScoreTask scoreTask = new ScoreTask(token, teamView, teamName, teamId, points, isPositive);
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
        } catch (InterruptedException | ExecutionException e) {
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
        } catch (InterruptedException | ExecutionException e) {
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
                       new FoulTask(authToken, teamID, item + 1, isPositive).execute();
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
}
