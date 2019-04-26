package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class Server {

    private String url;
    private String name;
    private byte[] logo;
    private NextScheduleMonths nextScheduleMatrix;

    public Server(String url){
        this.url = required(url,"url is required");
    }

    public Server(String url, String name, byte[] logo) {
        this.url = url;
        this.name = name;
        this.logo = logo;
    }

    public Server(String url, NextScheduleMonths nextScheduleMatrix){
        this.url = required(url,"url is required");
        this.nextScheduleMatrix = required(nextScheduleMatrix,"nextScheduleMatrix is required");
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public byte[] getLogo() {
        return logo;
    }

    public NextScheduleMonths getNextScheduleMatrix() {
        return nextScheduleMatrix;
    }
}
