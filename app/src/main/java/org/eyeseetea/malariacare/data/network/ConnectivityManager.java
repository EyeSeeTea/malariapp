package org.eyeseetea.malariacare.data.network;

import android.content.Context;
import android.net.NetworkInfo;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;

public class ConnectivityManager implements IConnectivityManager{
    @Override
    public boolean isDeviceOnline() {
        android.net.ConnectivityManager cm =
                (android.net.ConnectivityManager) PreferencesState.getInstance().getContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
