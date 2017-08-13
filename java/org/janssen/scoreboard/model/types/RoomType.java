package org.janssen.scoreboard.model.types;

/**
 * Created by stephan on 23/06/13.
 */
public enum RoomType {

    ROOM_A("192.168.1.100:8080"),
    ROOM_B("192.168.1.101:8080"),
//    ROOM_B("10.0.1.82:8080"), // LOCAL TEST IP SERVER
    ROOM_C("192.168.1.102:8080");

    String ip;

    RoomType(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public static String getIpForCourt(int pos) {
        return values()[pos].ip;
    }
}
