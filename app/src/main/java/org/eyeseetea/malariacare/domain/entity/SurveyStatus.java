package org.eyeseetea.malariacare.domain.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum SurveyStatus {
    PLANNED(-1),
    IN_PROGRESS(0),
    COMPLETED (1),
    SENT(2),
    HIDE(3),
    CONFLICT(4),
    QUARANTINE(5),
    SENDING(6),
    ERROR_CONVERSION_SYNC(7);

    private static final Map<Integer, SurveyStatus> lookup
            = new HashMap<Integer, SurveyStatus>();

    static {
        for (SurveyStatus s : EnumSet.allOf(SurveyStatus.class))
            lookup.put(s.getCode(), s);
    }

    private int code;

    SurveyStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static SurveyStatus get(int code) {
        return lookup.get(code);
    }
}