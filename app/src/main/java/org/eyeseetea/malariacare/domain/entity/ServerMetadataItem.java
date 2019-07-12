package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

public class ServerMetadataItem {
    private final String code;
    private final String UId;

    public ServerMetadataItem(String code, String UId) {
        this.code = required(code, "Code is required");
        this.UId = required(UId, "UId is required");
    }

    public String getCode() {
        return code;
    }

    public String getUId() {
        return UId;
    }
}