package org.eyeseetea.malariacare.domain.exception;

public class MetadataException extends Exception {
    public MetadataException() {
        super("All necessary mandatory metadata tables are not populated");
    }
}
