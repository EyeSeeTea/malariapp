package org.eyeseetea.malariacare.domain.exception;

import org.eyeseetea.malariacare.domain.entity.IData;

public class ConversionException extends Exception {
    private IData mFailedSyncData;

    public ConversionException(IData failedSyncData, String message) {
        super("Error in conversion" + message);
        mFailedSyncData = failedSyncData;
    }

    //TODO: jsanchez remove this constructor
    public ConversionException(Exception e) {
        super("Error in conversion" + e.getMessage());
        e.printStackTrace();
    }

    //TODO: jsanchez remove this constructor
    public ConversionException(String message) {
        super("Error in conversion"+ message);
        System.out.println(ConversionException.class.getName() + message);
    }

    //TODO: jsanchez remove this constructor
    public ConversionException() {
        super("Error in conversion");
    }

    public IData getFailedSyncData() {
        return mFailedSyncData;
    }
}
