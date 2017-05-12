package org.eyeseetea.malariacare.domain.utils;

public class RequiredChecker {

    private RequiredChecker() {
        // no instances
    }

    /* this is just convenience which allows
    to reduce amount of boilerplate code */
    public static <T> T required(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }

        return obj;
    }
}
