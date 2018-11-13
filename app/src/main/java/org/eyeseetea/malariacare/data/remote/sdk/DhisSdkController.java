package org.eyeseetea.malariacare.data.remote.sdk;

import android.app.Application;

import org.eyeseetea.malariacare.domain.controllers.IApiSdkController;
import org.hisp.dhis.client.sdk.android.api.D2;

public class DhisSdkController implements IApiSdkController {

    Application mApplication;

    public DhisSdkController(Application application) {
        mApplication = application;
        D2.init(mApplication);
    }
}
