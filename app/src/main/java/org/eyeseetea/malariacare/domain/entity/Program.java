package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Program {

    private final String uid;
    private final String name;
    private final List<String> assignedOrgUnits;

    public Program(String uid, String name) {
        required(uid, "uid is required");
        required(name, "name is required");

        this.uid = uid;
        this.name = name;
        this.assignedOrgUnits = new ArrayList<>();
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public void addAssignedOrgUnits(List<String> relatedPrograms) {
        this.assignedOrgUnits.addAll(relatedPrograms);
    }

    public List<String> getAssignedOrgUnits() {
        return Collections.unmodifiableList(assignedOrgUnits);
    }
}