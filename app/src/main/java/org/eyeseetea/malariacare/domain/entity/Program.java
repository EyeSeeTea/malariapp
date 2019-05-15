package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

public class Program implements IMetadata{

    private final String uid;
    private final String name;
    private final String programStageUid;

    public Program(String uid, String name, String programStageUid) {
        required(uid, "uid is required");
        required(name, "name is required");
        required(programStageUid, "programStageUid is required");

        this.uid = uid;
        this.name = name;
        this.programStageUid = programStageUid;
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
}