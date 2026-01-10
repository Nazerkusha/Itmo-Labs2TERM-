package com.lab4.geometry.model;

public class PointResponse {
    private double x;
    private double y;
    private double r;
    private boolean hit;
    private long scriptTime;
    private String serverTime;
    private String username;

    public PointResponse() {}

    public PointResponse(double x, double y, double r, boolean hit, long scriptTime, String serverTime, String username) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
        this.scriptTime = scriptTime;
        this.serverTime = serverTime;
        this.username = username;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public long getScriptTime() {
        return scriptTime;
    }

    public void setScriptTime(long scriptTime) {
        this.scriptTime = scriptTime;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
