package org.eyeseetea.malariacare.domain.exception;

import org.eyeseetea.malariacare.domain.entity.ISyncData;

public class ConversionException extends Exception {
    private ISyncData mFailedSyncData;

    public ConversionException(ISyncData failedSyncData, String message) {
        super("Error in conversion" + message);
        mFailedSyncData = failedSyncData;
    }

    //TODO: remove this constructor
    public ConversionException(Exception e) {
        super("Error in conversion" + e.getMessage());
        e.printStackTrace();
    }

    //TODO: remove this constructor
    public ConversionException(String message) {
        super("Error in conversion"+ message);
        System.out.println(ConversionException.class.getName() + message);
    }

    //TODO: remove this constructor
    public ConversionException() {
        super("Error in conversion");
    }

    public ISyncData getFailedSyncData() {
        return mFailedSyncData;
    }
}
