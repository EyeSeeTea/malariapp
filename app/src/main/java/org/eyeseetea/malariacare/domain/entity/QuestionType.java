package org.eyeseetea.malariacare.domain.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum QuestionType {
    DROPDOWN_LIST(1),
    INT(2),
    LONG_TEXT(3),
    SHORT_TEXT(4),
    DATE(5),
    POSITIVE_INT(6),
    NO_ANSWER(7),
    RADIO_GROUP_HORIZONTAL(8),
    RADIO_GROUP_VERTICAL(9),
    DROPDOWN_LIST_DISABLED(10),
    A(11),
    SWITCH_BUTTON(12);

    private static final Map<Integer, QuestionType> lookup
            = new HashMap<Integer, QuestionType>();

    static {
        for (QuestionType s : EnumSet.allOf(QuestionType.class))
            lookup.put(s.getCode(), s);
    }

    private int code;

    QuestionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static QuestionType get(int code) {
        return lookup.get(code);
    }
}