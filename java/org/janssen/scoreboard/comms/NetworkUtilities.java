package org.janssen.scoreboard.comms;

import android.util.Log;


import org.janssen.scoreboard.model.Server;
import org.janssen.scoreboard.model.types.ClockAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.conn.params.ConnManagerParams;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;

/**
 * The network utilities class.
 */
final public class NetworkUtilities {

    /** The tag used to log to adb console. */
    private static final String TAG = "NetworkUtilities";

    /** Timeout (in ms) we specify for each http request */
    public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
    public static final int HTTP_REQUEST_ONE_SEC_TIMEOUT_MS = 1000;
    public static final String OK = "OK";

    private NetworkUtilities() {
    }

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static HttpClient getHttpClient(int timeout) {
        HttpClient httpClient = new DefaultHttpClient();
        final HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
        ConnManagerParams.setTimeout(params, timeout);
        return httpClient;
    }

    public static HttpClient getHttpClient() {
        return getHttpClient(HTTP_REQUEST_TIMEOUT_MS);
    }

    /**
     * Connects to the scoreboard server, authenticates the provided username and password.
     *
     * @param username The server account username
     * @param password The server account password
     *
     * @return String The authentication token returned by the server (or null)
     */
    public static String authenticate(String username, String password) throws IOException {

        String AUTH_URI = String.format(RestURI.LOGIN.getValue(), Server.getIp(), username, password);

        final HttpPost post = new HttpPost(AUTH_URI);

        final HttpResponse resp = getHttpClient().execute(post);
        String authToken = null;
        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            InputStream stream = (resp.getEntity() != null) ? resp.getEntity().getContent() : null;
            if (stream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                authToken = reader.readLine().trim();
            }
        }
        if ((authToken != null) && (authToken.length() > 0)) {
            return authToken;
        } else {
            throw new IOException(resp.getStatusLine().toString());
        }
    }

    /**
     * Connects to the scoreboard server and creates a new game.
     *
     * @return ok
     */
    public static String newGame(final String token,
                                 final String teamA,
                                 final String teamB,
                                 final int gameType,
                                 final int ageCategory,
                                 final int court,
                                 final boolean mirrored) throws UnsupportedEncodingException {

        String NEW_GAME_URI = String.format(RestURI.CREATE_GAME.getValue(), Server.getIp(),
                URLEncoder.encode(teamA, "UTF8"), URLEncoder.encode(teamB, "UTF8"), gameType, ageCategory, court, mirrored, token);

        final HttpPost post = new HttpPost(NEW_GAME_URI);

        try {
            final HttpResponse resp = getHttpClient().execute(post);
            String newGame = null;
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                InputStream stream = (resp.getEntity() != null) ? resp.getEntity().getContent() : null;
                if (stream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    newGame = reader.readLine().trim();
                }
            }
            if ((newGame != null) && (newGame.length() > 0)) {
                Log.v(TAG, "Successfully created game");
                return newGame;
            } else {
                Log.e(TAG, "Error creating new game " + resp.getStatusLine());
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when creating new game", e);
            return null;
        } finally {
            Log.v(TAG, "newGame completing");
        }
    }

    /**
     * Connects to the scoreboard server and starts the new game.
     */
    public static void startGame(final String token,
                                   final int gameId,
                                   final boolean mirrored) throws UnsupportedEncodingException {

        String START_GAME_URI = String.format(RestURI.START_GAME.getValue(), Server.getIp(),
                token, gameId, mirrored);

        final HttpPost post = new HttpPost(START_GAME_URI);

        try {
            getHttpClient().execute(post);
        } catch (final IOException e) {
            Log.e(TAG, "IOException when creating new game", e);
        } finally {
            Log.v(TAG, "newGame completing");
        }
    }

    /**
     * Connects to the scoreboard server and starts a clock countdown.
     *
     */
    public static void countDown(final int seconds,
                                 final boolean mirrored,
                                 final String token) throws UnsupportedEncodingException {

        String COUNTDOWN_URI = String.format(RestURI.COUNTDOWN.getValue(), Server.getIp(), seconds, mirrored, token);

        final HttpPut put = new HttpPut(COUNTDOWN_URI);

        try {
            getHttpClient().execute(put);

        } catch (final IOException e) {
            Log.e(TAG, "IOException when setting countdown clock", e);
        } finally {
            Log.v(TAG, "Count down started");
        }
    }

    /**
     * Connects to the scoreboard server and increments the score
     *
     * @return ok
     */
    public static String updateScore(final String token,
                                     final boolean isPositive,
                                     final int teamId,
                                     final int points) throws IOException {
        String SCORE_URI;

        if (isPositive) {
            SCORE_URI= String.format(RestURI.INC_SCORE.getValue(), Server.getIp(), teamId, points, token);
        } else {
            SCORE_URI= String.format(RestURI.DEC_SCORE.getValue(), Server.getIp(), teamId, points, token);
        }

        final HttpPut put = new HttpPut(SCORE_URI);

        String response = "0";
        final HttpResponse resp = getHttpClient(HTTP_REQUEST_ONE_SEC_TIMEOUT_MS).execute(put);

        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            InputStream stream = (resp.getEntity() != null) ? resp.getEntity().getContent() : null;
            if (stream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                response = reader.readLine().trim();
            }
            return response;
        } else {
            throw new IOException("Network problem");
        }
    }

    /**
     * Connects to the scoreboard server and increments the score
     *
     * @return ok
     */
    public static String updateFouls(String token, boolean isPositive, int teamId, int totalFouls) {
        String SCORE_URI;

        if (isPositive) {
            SCORE_URI= String.format(RestURI.INC_FOULS.getValue(), Server.getIp(), teamId, totalFouls, token);
        } else {
            SCORE_URI= String.format(RestURI.DEC_FOULS.getValue(), Server.getIp(), teamId, token);
        }

        final HttpPut put = new HttpPut(SCORE_URI);

        try {
            final HttpResponse resp = getHttpClient().execute(put);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.v(TAG, "Successful updated fouls");
                return OK;
            } else {
                Log.e(TAG, "Error updating fouls " + resp.getStatusLine());
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when updating fouls", e);
            return null;
        } finally {
            Log.v(TAG, "updateFoul completing");
        }
    }

    /**
     * Connects to the scoreboard server and increments the score
     *
     * @return ok
     */
    public static String updateQuarters(String token, boolean isPositive, int gameId) throws IOException {
        String SCORE_URI;

        if (isPositive) {
            SCORE_URI= String.format(RestURI.INC_QUARTERS.getValue(), Server.getIp(), gameId, token);
        } else {
            SCORE_URI= String.format(RestURI.DEC_QUARTERS.getValue(), Server.getIp(), gameId, token);
        }

        final HttpPut put = new HttpPut(SCORE_URI);

        final HttpResponse resp = getHttpClient().execute(put);

        StatusLine statusLine = resp.getStatusLine();
        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            return OK;
        } else {
            throw new IOException(getResponse(resp));
        }
    }

    /**
     *
     * @param token
     * @param teamId
     * @return
     * @throws IOException
     */
    public static String incrementTimeout(final String token,
                                          final int teamId) throws IOException {

        String TIMEOUT_URI= String.format(RestURI.INC_TIMEOUT.getValue(), Server.getIp(), teamId, token);

        final HttpPut put = new HttpPut(TIMEOUT_URI);

        final HttpResponse resp = getHttpClient().execute(put);
        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return OK;
        } else {
            throw new IOException(resp.toString());
        }
    }

    public static String attention(final String token) {

        String ATTENTION_URI= String.format(RestURI.ATTENTION.getValue(), Server.getIp(), token);

        final HttpGet get = new HttpGet(ATTENTION_URI);

        try {
            final HttpResponse resp = getHttpClient().execute(get);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return OK;
            } else {
                return null;
            }
        } catch (final IOException e) {
            return null;
        }
    }

    public static String pingServer() {

        String PING_URI = String.format(RestURI.PING.getValue(), Server.getIp());

        final HttpGet get = new HttpGet(PING_URI);

        try {
            final HttpResponse resp = getHttpClient().execute(get);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return OK;
            } else {
                return null;
            }
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Connects to the scoreboard server and increments the score
     *
     * @return ok
     */
    public static String triggerClock(final String token,
                                      final int gameId,
                                      final ClockAction action,
                                      final int seconds) throws IOException {
        String CLOCK_URI;

        switch (action) {
            case START: CLOCK_URI= String.format(RestURI.START.getValue(), Server.getIp(), gameId, token);
                        break;

            case STOP:  CLOCK_URI= String.format(RestURI.STOP.getValue(), Server.getIp(), gameId, token);
                        break;

            case INC:   CLOCK_URI= String.format(RestURI.INC_CLOCK.getValue(), Server.getIp(), gameId, token, seconds);
                        break;

            default:    CLOCK_URI= String.format(RestURI.DEC_CLOCK.getValue(), Server.getIp(), gameId, token, seconds);
                        break;
        }

        final HttpPut put = new HttpPut(CLOCK_URI);

        final HttpResponse resp = getHttpClient(HTTP_REQUEST_ONE_SEC_TIMEOUT_MS).execute(put);

        StatusLine statusLine = resp.getStatusLine();

        if (statusLine != null) {
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                return OK;
            } else {
                throw new IOException(statusLine.getStatusCode() + ":" + getResponse(resp));
            }
        } else {
            throw new IOException(resp.toString());
        }
    }

    /**
     * Connects to the scoreboard server and increments the score
     *
     * @return ok
     */
    public static String redraw(String authToken, int gameId) {
        String REDRAW_URI = String.format(RestURI.REDRAW.getValue(), Server.getIp(), gameId, authToken);

        final HttpPut put = new HttpPut(REDRAW_URI);

        try {
            final HttpResponse resp = getHttpClient().execute(put);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.v(TAG, "Successful redraw");
                return OK;
            } else {
                Log.e(TAG, "Error updating clock " + resp.getStatusLine());
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException when redraw score board", e);
            return null;
        } finally {
            Log.v(TAG, "redraw completing");
        }
    }

    private static String getResponse(HttpResponse resp) throws IOException {
        String response = null;
        InputStream stream = (resp.getEntity() != null) ? resp.getEntity().getContent() : null;
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            response = reader.readLine().trim();
        }
        return response;
    }
}
