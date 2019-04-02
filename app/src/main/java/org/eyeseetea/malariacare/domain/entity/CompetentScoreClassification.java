package org.eyeseetea.malariacare.domain.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum CompetentScoreClassification {
    NOT_AVAILABLE (0),
    COMPETENT (1),
    COMPETENT_NEEDS_IMPROVEMENT (2),
    NOT_COMPETENT (3);


    private static final Map<Integer, CompetentScoreClassification> itemsMap = new HashMap<>();

    static {
        for (CompetentScoreClassification s : EnumSet.allOf(CompetentScoreClassification.class))
            itemsMap.put(s.getCode(), s);
    }

    private int code;

    CompetentScoreClassification(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CompetentScoreClassification get(int code) {
        return itemsMap.get(code);
    }
}
