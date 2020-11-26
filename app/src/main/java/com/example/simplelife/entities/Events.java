package com.example.simplelife.entities;

public class Events {
    String EVENT, PLACE, TIME, DATE, MONTH, YEAR, BRING;

    public String getEVENT() {
        return EVENT;
    }

    public String getPLACE() {
        return PLACE;
    }

    public String getTIME() {
        return TIME;
    }

    public String getDATE() {
        return DATE;
    }

    public String getMONTH() {
        return MONTH;
    }

    public String getYEAR() {
        return YEAR;
    }

    public String getBRING() {
        return BRING;
    }

    public void setEVENT(String EVENT) {
        this.EVENT = EVENT;
    }

    public void setPLACE(String PLACE) {
        this.PLACE = PLACE;
    }

    public void setTIME(String TIME) {
        this.TIME = TIME;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }

    public void setMONTH(String MONTH) {
        this.MONTH = MONTH;
    }

    public void setYEAR(String YEAR) {
        this.YEAR = YEAR;
    }

    public void setBRING(String BRING) {
        this.BRING = BRING;
    }

    public Events(String EVENT, String PLACE, String TIME, String DATE, String MONTH, String YEAR, String BRING) {
        this.EVENT = EVENT;
        this.PLACE = PLACE;
        this.TIME = TIME;
        this.DATE = DATE;
        this.MONTH = MONTH;
        this.YEAR = YEAR;
        this.BRING = BRING;
    }

}
