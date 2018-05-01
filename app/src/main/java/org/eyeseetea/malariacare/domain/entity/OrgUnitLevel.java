package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrgUnitLevel {
    private final String uid;
    private final String name;

    public OrgUnitLevel(String uid, String name){
        required(uid,"uid is required");
        required(name,"name is required");

        this.uid = uid;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }
}
