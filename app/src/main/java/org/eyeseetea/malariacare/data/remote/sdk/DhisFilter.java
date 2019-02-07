package org.eyeseetea.malariacare.data.remote.sdk;

import java.util.ArrayList;
import java.util.List;

public class DhisFilter {
    private List<String> uIds;

    public DhisFilter(List<String> uIds){
        this.uIds = uIds;
    }

    public List<String> getUIds() {
        return uIds;
    }

    public static DhisFilter empty(){
        return new DhisFilter(new ArrayList<String>());
    }
}
