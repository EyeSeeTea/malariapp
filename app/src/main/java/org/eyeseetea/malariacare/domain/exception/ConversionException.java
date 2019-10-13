package org.eyeseetea.malariacare.domain.exception;

import org.eyeseetea.malariacare.data.sync.IData;

public class ConversionException extends Exception {

    private IData conversionFailedData;

    public ConversionException(Exception e) {
        super("Error in conversion: " + e.getMessage());
        e.printStackTrace();
    }
    public ConversionException(String message) {
        super("Error in conversion: "+ message);
        System.out.println(ConversionException.class.getName() + message);
    }
    public ConversionException() {
        super("Error in conversion");
    }

    public ConversionException(IData conversionFailedData, String message) {
        super("Error in survey conversion: " + message);
        this.conversionFailedData = conversionFailedData;
    }

    public IData getConversionFailedData() {
        return conversionFailedData;
    }

}
