package org.janssen.scoreboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.janssen.scoreboard.task.ClockCountdownTask;
import org.janssen.scoreboard.task.OnTaskCompleted;
import org.janssen.scoreboard.task.StartGameTask;

import static org.janssen.scoreboard.Constants.AUTH_TOKEN;
import static org.janssen.scoreboard.Constants.COURT;
import static org.janssen.scoreboard.Constants.GAME;
import static org.janssen.scoreboard.Constants.MIRRORED;

/**
 * Count down activity.
 * Created by stephan on 18/08/13.
 */
public class CountDownActivity extends WifiControlActivity implements OnTaskCompleted {

    private Integer selectedCourt;

    private String game;
    private String authToken;
    private boolean mirrored = false;

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.activity_countdown);

        validateWifi();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            authToken = bundle.getString(AUTH_TOKEN);
            mirrored = bundle.getBoolean(MIRRORED);
            game = bundle.getString(GAME);
            selectedCourt = (Integer) bundle.get(COURT);
        } else {
            Toast.makeText(getApplicationContext(), "Game was not selected", Toast.LENGTH_LONG).show();
        }
    }

    public void startNewGame(final View view) {
        // Hide keyboard so we can go into immersive (fullscreen) mode
//        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        IBinder windowToken = findViewById(R.id.minuten).getWindowToken();
//        imm.hideSoftInputFromWindow(windowToken, 0);

        new StartGameTask(this, authToken, game, mirrored).execute();
    }

    public void startClock(final View view) {
        EditText clockMinutesText = findViewById(R.id.minuten);

        String clockMinutes = clockMinutesText.getText().toString();
        new ClockCountdownTask(authToken, mirrored).execute(clockMinutes);
    }

    @Override
    public void onTaskCompleted(String value) {
        Intent intent = new Intent(getApplicationContext(), ScoreActivity.class);
        intent.putExtra(GAME, game);
        intent.putExtra(AUTH_TOKEN, authToken);
        intent.putExtra(COURT, selectedCourt);
        startActivity(intent);
    }
}
