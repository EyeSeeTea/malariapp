package org.eyeseetea.malariacare.sdk;

import android.util.Log;

import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by idelcano on 15/11/2016.
 */

public class SdkPushController extends SdkController {

    /**
     * Push the events in sdk database
     * This method is called before the conversion from surveys to sdk events
     */
    public static void sendEventChanges() {
    }

}
