package org.eyeseetea.malariacare.data.remote.api;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

import java.io.IOException;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.net.Proxy;


public class BasicAuthenticator implements Authenticator {

    public final String AUTHORIZATION_HEADER="Authorization";
    private String credentials;
    private int mCounter = 0;

    org.eyeseetea.malariacare.domain.entity.Credentials userCredentials =
            PreferencesState.getInstance().getCreedentials();

    BasicAuthenticator(org.eyeseetea.malariacare.domain.entity.Credentials userCredentials){
        credentials = Credentials.basic(userCredentials.getUsername(), userCredentials.getPassword());
    }
    BasicAuthenticator(){
        credentials = Credentials.basic(userCredentials.getUsername(), userCredentials.getPassword());
    }

    @Override
    public Request authenticate(Proxy proxy, Response response) throws IOException {

        if (mCounter++ > 0) {
            throw new IOException(response.message());
        }
        return response.request().newBuilder().header(AUTHORIZATION_HEADER, credentials).build();
    }

    @Override
    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
        return null;
    }

    public String getCredentials(){
        return credentials;
    }
}