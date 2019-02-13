package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

public class Tab {

    private final String uid;
    private final String name;
    private final int orderPosition;

    public Tab(String uid, String name, int orderPosition) {
        required(uid, "uid is required");
        required(name, "name is required");

        this.uid = uid;
        this.name = name;
        this.orderPosition = orderPosition;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public int getOrderPosition() {
        return orderPosition;
    }
}