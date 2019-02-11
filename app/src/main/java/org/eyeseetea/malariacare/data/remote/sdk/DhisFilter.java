package org.eyeseetea.malariacare.data.remote.sdk;

import java.util.ArrayList;
import java.util.List;

public class DhisFilter {
    private String[] uIds;

    public DhisFilter(List<String> uIds){
        String[] itemsArray = new String[uIds.size()];
        itemsArray = uIds.toArray(itemsArray);

        this.uIds = itemsArray;
    }

    public String[] getUIds() {
        return uIds;
    }

    public static DhisFilter empty(){
        return new DhisFilter(new ArrayList<String>());
    }
}
