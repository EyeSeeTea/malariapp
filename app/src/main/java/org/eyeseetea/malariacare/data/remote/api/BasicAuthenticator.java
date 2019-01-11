package org.eyeseetea.malariacare.data.remote.api;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

import java.io.IOException;

import javax.annotation.Nullable;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class BasicAuthenticator implements Authenticator {

    public final String AUTHORIZATION_HEADER = "Authorization";
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

    public String getCredentials() {
        return credentials;
    }

    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (mCounter++ > 0) {
            throw new IOException(response.message());
        }
        return response.request().newBuilder().header(AUTHORIZATION_HEADER,
                credentials).build();
    }
}
