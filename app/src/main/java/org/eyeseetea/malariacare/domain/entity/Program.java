package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

import java.util.List;

public class Program implements IMetadata{

    private final String uid;
    private final String name;
    private final String programStageUid;
    private final List<Tab> tabs;

    public Program(String uid, String name, String programStageUid, List<Tab> tabs) {
        required(uid, "uid is required");
        required(name, "name is required");
        required(programStageUid, "programStageUid is required");
        required(tabs, "tabs is required");

        this.uid = uid;
        this.name = name;
        this.programStageUid = programStageUid;
        this.tabs = tabs;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getProgramStageUid() {
        return programStageUid;
    }

    public List<Tab> getTabs() {
        return tabs;
    }
}