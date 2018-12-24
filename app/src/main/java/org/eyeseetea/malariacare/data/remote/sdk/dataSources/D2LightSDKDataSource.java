package org.eyeseetea.malariacare.data.remote.sdk.dataSources;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.eyeseetea.dhis2.lightsdk.D2Api;
import org.eyeseetea.dhis2.lightsdk.D2Response;
import org.eyeseetea.dhis2.lightsdk.systeminfo.SystemInfo;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.ServerException;

public class D2LightSDKDataSource {

    protected D2Api d2Api;
    private Context context;

    public D2LightSDKDataSource(Context context) {
        this.context = context;
    }

    protected D2Api getD2Api() throws Exception {
        Credentials credentials = getCredentials();

        String apiVersion = getApiVersion();

        if(apiVersion.isEmpty()){
            apiVersion = getApiVersionFromServer(credentials);
        }

        d2Api = new D2Api.Builder()
                .url(credentials.getServerURL())
                .credentials(credentials.getUsername(), credentials.getPassword())
                .apiVersion(apiVersion)
                .build();

        return d2Api;
    }

    protected void handleError(D2Response.Error errorResponse) throws Exception {
        //TODO: for the moment throw exceptions here
        //on the future we will return Algebraic data type object (Result = Success | Error)
        if (errorResponse instanceof D2Response.Error.NetworkConnection) {
            throw new NetworkException();
        } else if (errorResponse instanceof D2Response.Error.HttpError){
            D2Response.Error.HttpError httpError = (D2Response.Error.HttpError) errorResponse;

            String message = "";

            if (httpError.getErrorBody() != null)
                message = httpError.getErrorBody().getMessage();

            throw new ServerException(httpError.getHttpStatusCode(), message);
        }
    }


    private Credentials getCredentials() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);

        String serverURL = sharedPreferences.getString(context.getString(R.string.dhis_url), "");
        String username = sharedPreferences.getString(context.getString(R.string.dhis_user), "");
        String password = sharedPreferences.getString(context.getString(R.string.dhis_password),
                "");

        return new Credentials(serverURL, username, password);
    }

    private String getApiVersion() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);

        String apiVersion = sharedPreferences.getString(context.getString(R.string.dhis_api_version), "");

        return apiVersion;
    }

    private String getApiVersionFromServer(Credentials credentials) throws Exception {
        String apiVersion = "";

        D2Api d2Api = new D2Api.Builder()
                .url(credentials.getServerURL())
                .credentials(credentials.getUsername(), credentials.getPassword())
                .build();

        D2Response<SystemInfo> systemInfoResponse = d2Api.systemInfo().get().execute();

        if (systemInfoResponse.isSuccess()) {
            D2Response.Success<SystemInfo> success =
                    (D2Response.Success<SystemInfo>) systemInfoResponse;

            apiVersion = success.getValue().getVersion().substring(2,4);
        } else {
            D2Response.Error errorResponse = (D2Response.Error) systemInfoResponse;

            handleError(errorResponse);
        }

        saveApiVersion(apiVersion);

        return apiVersion;
    }

    private void saveApiVersion(String apiVersion) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.dhis_api_version),apiVersion);
        editor.commit();
    }
}
