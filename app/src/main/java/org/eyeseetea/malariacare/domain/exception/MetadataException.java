package org.eyeseetea.malariacare.domain.exception;

public class MetadataException extends Exception {
    public MetadataException(Exception e) {
        super("Error in metadata " + e.getMessage());
        e.printStackTrace();
    }
    public MetadataException() {
        super("All necessary mandatory metadata tables are not populated");
    }
}
