package org.eyeseetea.malariacare.domain.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum CompetencyScoreClassification {
    NOT_AVAILABLE(0, "NA"),
    COMPETENT(1, "C"),
    COMPETENT_NEEDS_IMPROVEMENT(2, "CNI"),
    NOT_COMPETENT(3, "NC");

    private static final Map<Integer, CompetencyScoreClassification> itemsMap = new HashMap<>();

    static {
        for (CompetencyScoreClassification s : EnumSet.allOf(CompetencyScoreClassification.class)) {
            itemsMap.put(s.getId(), s);
        }
    }

    private final int id;
    private final String code;

    CompetencyScoreClassification(int id, String code) {
        this.id = id;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static CompetencyScoreClassification get(int id) {
        return itemsMap.get(id);
    }

    public static CompetencyScoreClassification getByCode(String code) {
        CompetencyScoreClassification competencyScoreClassification = null;

        for (Map.Entry<Integer, CompetencyScoreClassification> entry : itemsMap.entrySet()) {
            if (entry.getValue().getCode().equals(code)) {
                competencyScoreClassification = entry.getValue();
            }
        }

        if (competencyScoreClassification == null) {
            throw new IllegalArgumentException(
                    "Does not exists any competency score classification with code " + code);
        }

        return competencyScoreClassification;
    }
}
