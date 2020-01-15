package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

public class Server {

    private String url;
    private String name;
    private byte[] logo;
    private boolean connected;

    public Server(String url){
        this.url = required(url,"url is required");
    }

    public Server(String url, String name, byte[] logo, boolean connected) {
        this.url = url;
        this.name = name;
        this.logo = logo;
        this.connected = connected;
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

    public boolean isConnected() {
        return connected;
    }

    public void changeToConnected(){
        connected = true;
    }
}
