package org.janssen.scoreboard.model;

/**
 * Created by stephan on 15/06/13.
 */
public class Server {

    private static String ip;

    private static String token;

    private static int court;

    private static int gameId;

    private static String[] courts = { "A", "B", "C"};

    public static String getIp() {
        return ip;
    }

    public static void setIp(String newIp) {
        ip = newIp;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        Server.token = token;
    }

    public static String getCourt() {
        return courts[court];
    }

    public static void setCourt(int court) {
        Server.court = court;
    }

    public static int getGameId() {
        return gameId;
    }

    public static void setGameId(int gameId) {
        Server.gameId = gameId;
    }
}
