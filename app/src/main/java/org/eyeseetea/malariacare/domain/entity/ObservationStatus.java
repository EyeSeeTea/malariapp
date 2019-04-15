package org.eyeseetea.malariacare.domain.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ObservationStatus {
    IN_PROGRESS(0),
    COMPLETED (1),
    SENT(2),
    CONFLICT(4),
    QUARANTINE(5),
    SENDING(6);

    private static final Map<Integer, ObservationStatus> lookup
            = new HashMap<Integer, ObservationStatus>();

    static {
        for (ObservationStatus s : EnumSet.allOf(ObservationStatus.class))
            lookup.put(s.getCode(), s);
    }

    private int code;

    ObservationStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ObservationStatus get(int code) {
        return lookup.get(code);
    }
}
