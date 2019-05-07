package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class Server {

    String url;

    public Server(String url){
        this.url = required(url,"url is required");
     }

    public String getUrl() {
        return url;
    }
}
