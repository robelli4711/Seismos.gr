package com.seismos.pentesigma.seismos;

public class Data_Events {

    private long id;
    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private double magnitude;
    private double depth;
    private String date;
    private String time;

    public String getDate() { return date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public void setDate(String date) { this.date = date; }
    public double getDepth() { return depth; }
    public void setDepth(double depth) { this.depth = depth; }
    public double getMagnitude() { return magnitude; }
    public void setMagnitude(double magnitude) { this.magnitude = magnitude; }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    Data_Events(String title, String Description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public String toString() {
        return title;
    }
}
