package org.eyeseetea.malariacare.domain.entity.pushsummary;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class PushConflict {
    private String uid;

    private String value;

    public PushConflict(String uid, String value) {
        this.uid = required(uid, "Uid is required");
        this.value = required(value, "Value is required");
    }

    public String getUid() {
        return uid;
    }

    public String getValue() {
        return value;
    }

}
