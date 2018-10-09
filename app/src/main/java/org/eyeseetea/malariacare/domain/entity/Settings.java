package org.eyeseetea.malariacare.domain.entity;

public class Settings {
    Server server;

    public Settings(Server server){
        this.server = server;
    }

    public Server getServer(){
        return server;
    }
}
