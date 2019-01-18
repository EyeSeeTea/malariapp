package org.eyeseetea.malariacare.domain.entity;

public class ServerInfo {

    int version;
    boolean isServerSupported = true;

    public ServerInfo(int version){
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public boolean isServerSupported(){
        return isServerSupported;
    }

    public void markAsUnsupported(){
        isServerSupported = false;
    }
}
