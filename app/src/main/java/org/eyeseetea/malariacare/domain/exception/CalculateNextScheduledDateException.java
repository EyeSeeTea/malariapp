package org.eyeseetea.malariacare.domain.exception;

public class CalculateNextScheduledDateException extends Exception{
    public CalculateNextScheduledDateException() {
        super("It is not possible calculate next schedule date for a non complete survey");
    }
}
