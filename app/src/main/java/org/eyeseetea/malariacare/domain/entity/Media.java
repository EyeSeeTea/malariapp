package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import org.eyeseetea.malariacare.utils.Constants;

public class Media {
    private int mediaType;
    private String filename;

    public Media(int mediaType, String filename) {
        this.mediaType = checkMediaCorrectType(mediaType);
        this.filename = required(filename, "filename is required");
    }

    public int getMediaType() {
        return mediaType;
    }

    public String getFilename() {
        return filename;
    }

    /**
     * Returns if is a picture
     */
    public boolean isPicture() {
        return (mediaType == Constants.MEDIA_TYPE_IMAGE);
    }

    /**
     * Returns if is video
     */
    public boolean isVideo() {
        return (mediaType == Constants.MEDIA_TYPE_VIDEO);
    }

    private int checkMediaCorrectType(int mediaType) {
        if (mediaType != Constants.MEDIA_TYPE_IMAGE && mediaType != Constants.MEDIA_TYPE_VIDEO) {
            throw new IllegalArgumentException("mediaType is not correct");
        }
        return mediaType;
    }
}
