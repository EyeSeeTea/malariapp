package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.PositiveOrCeroChecker.isPositiveOrCero;
import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.ArrayList;

public class CompositeScore {
    private String uid;
    private String label;
    private String hierarchicalCode;
    private int orderPos;
    private String parentUid;
    private ArrayList<String> childrenUids;


    public CompositeScore(String uid, String label, String hierarchicalCode,
            int orderPos) {
        this.uid = required(uid, "Uid is required");
        this.label = required(label, "Label is required");
        this.hierarchicalCode = required(hierarchicalCode, "HierarchicalCode is required");
        this.orderPos = isPositiveOrCero(orderPos, "OrderPos has to be higher than 0");
    }

    public CompositeScore(String uid, String label, String hierarchicalCode, int orderPos,
            String parentUid, ArrayList<String> childrenUids) {
        this.uid = required(uid, "Uid is required");
        this.label = required(label, "Label is required");
        this.hierarchicalCode = required(hierarchicalCode, "HierarchicalCode is required");
        this.orderPos = isPositiveOrCero(orderPos, "OrderPos has to be higher than 0");
        this.parentUid = parentUid;
        this.childrenUids = childrenUids;
    }

    public String getUid() {
        return uid;
    }

    public String getLabel() {
        return label;
    }

    public String getHierarchicalCode() {
        return hierarchicalCode;
    }

    public int getOrderPos() {
        return orderPos;
    }

    public String getParentUid() {
        return parentUid;
    }

    public ArrayList<String> getChildrenUids() {
        return childrenUids;
    }
}
