package org.eyeseetea.malariacare.sdk;

import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by idelcano on 15/11/2016.
 */

public class SdkPushController extends SdkController {

    /**
     * Push the events in sdk database
     * This method is called before the conversion from surveys to sdk events
     */
    public static void sendEventChanges() {
        Set<String> eventUids= new HashSet<>();
        for(EventFlow eventFlow:SdkQueries.getEvents()){
            eventUids.add(eventFlow.getUId());
        }
        D2.events().push(eventUids);
    }

}
