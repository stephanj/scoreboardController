package org.janssen.scoreboard.comms;

/**
 * The valid REST URI's which communicate with the server side score board.
 *
 * Created by stephan on 16/06/13.
 */
public enum RestURI {

    // Ping
    PING("http://%s"),

    // Auth
    LOGIN("http://%s/api/auth/login?username=%s&password=%s"),

    // Games
    CREATE_GAME("http://%s/api/game?teamA=%s&teamB=%s&type=%d&age=%d&court=%s&mirrored=%s&token=%s"),
    START_GAME("http://%s/api/game/start?token=%s&gameId=%d&mirrored=%s"),

    // Score
    INC_SCORE("http://%s/api/score/inc/%d?points=%d&token=%s"),
    DEC_SCORE("http://%s/api/score/dec/%d?points=%d&token=%s"),

    // Quarter
    INC_QUARTERS("http://%s/api/quarter/inc/%d?token=%s"),
    DEC_QUARTERS("http://%s/api/quarter/dec/%d?token=%s"),

    // Fouls
    INC_FOULS("http://%s/api/foul/inc/%d/%d?token=%s"),
    DEC_FOULS("http://%s/api/foul/dec/%d?token=%s"),

    // Timeouts
    INC_TIMEOUT("http://%s/api/timeout/inc/%d?token=%s"),

    ATTENTION("http://%s/api/attention?token=%s"),

    // Clock
    START("http://%s/api/clock/start/%d?token=%s"),
    STOP("http://%s/api/clock/stop/%d?token=%s"),
    INC_CLOCK("http://%s/api/clock/inc/%d?token=%s&seconds=%d"),
    DEC_CLOCK("http://%s/api/clock/dec/%d?token=%s&seconds=%d"),

    // Clock countdown
    COUNTDOWN("http://%s/api/clock/countdown/%d?mirrored=%s&token=%s");

    String value;

    RestURI(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
