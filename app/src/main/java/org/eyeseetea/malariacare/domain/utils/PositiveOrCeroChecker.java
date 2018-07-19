package org.eyeseetea.malariacare.domain.utils;

public class PositiveOrCeroChecker {

    public static int isPositiveOrCero(int number, String message) {
        if (number < 0) {
            throw new IllegalArgumentException(message);
        }
        return number;
    }
}
