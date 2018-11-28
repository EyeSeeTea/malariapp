package org.eyeseetea.malariacare.data.remote.api;

import java.util.List;

public class ApiEndpointMapper {

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?event=%s";

    public static String getFilteredEventPath(List<String> uids) {
        String endpoint = getUidFilter(uids);
        String url = String.format(DHIS_CHECK_EVENT_API, endpoint);
        return url;
    }

    private static String getUidFilter(List<String> uids) {
        String endpoint = "";
        for(int i=0; i<uids.size();i++){
            endpoint+=uids.get(i);
            if(i!=uids.size()-1) {
                endpoint += ";";
            }
        }
        return endpoint;
    }
}
