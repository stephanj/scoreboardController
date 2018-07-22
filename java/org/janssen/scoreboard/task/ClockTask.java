package org.janssen.scoreboard.task;

import android.os.AsyncTask;

import org.janssen.scoreboard.comms.NetworkUtilities;
import org.janssen.scoreboard.model.types.ClockAction;

import java.io.IOException;

import static org.janssen.scoreboard.ScoreActivity.NETWORK_PROBLEEM_PROBEER_OPNIEUW;

/**
 * Represents an asynchronous task used to set the clock
 */
public class ClockTask extends AsyncTask<Void, Void, String> {

    static final String ERROR_PROBEER_OPNIEUW = "Error probeer opnieuw - ";
    private String token;
    private int gameId;
    private int seconds;
    private ClockAction action;

    public ClockTask(final String token,
                     final int gameId,
                     final ClockAction action,
                     final int seconds) {
        this.token = token;
        this.gameId = gameId;
        this.action = action;
        this.seconds = seconds;
    }

    public ClockTask(final String token,
                     final int gameId,
                     final ClockAction action) {
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
