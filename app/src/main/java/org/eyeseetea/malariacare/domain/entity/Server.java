package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class Server {

    private String url;
    private String name;
    private byte[] logo;

    public Server(String url){
        this.url = required(url,"url is required");
    }

    public Server(String url, String name, byte[] logo) {
        this.url = url;
        this.name = name;
        this.logo = logo;
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
}
