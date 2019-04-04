package org.eyeseetea.malariacare.domain.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum CompetencyScoreClassification {
    NOT_AVAILABLE (0),
    COMPETENT (1),
    COMPETENT_NEEDS_IMPROVEMENT (2),
    NOT_COMPETENT (3);


    private static final Map<Integer, CompetencyScoreClassification> itemsMap = new HashMap<>();

    static {
        for (CompetencyScoreClassification s : EnumSet.allOf(CompetencyScoreClassification.class))
            itemsMap.put(s.getCode(), s);
    }

    private int code;

    CompetencyScoreClassification(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CompetencyScoreClassification get(int code) {
        return itemsMap.get(code);
    }
}
