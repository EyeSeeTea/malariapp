package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.PositiveOrCeroChecker.isPositiveOrCero;
import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.ArrayList;

public class CompositeScore {
    private String uid;
    private String label;
    private String hierarchicalCode;
    private int orderPos;
    private CompositeScore parent;
    private ArrayList<CompositeScore> children;


    public CompositeScore(String uid, String label, String hierarchicalCode,
            int orderPos) {
        this.uid = required(uid, "Uid is required");
        this.label = required(label, "Label is required");
        this.hierarchicalCode = required(hierarchicalCode, "HierarchicalCode is required");
        this.orderPos = isPositiveOrCero(orderPos, "OrderPos has to be higher than 0");
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

    public CompositeScore getParent() {
        return parent;
    }

    public ArrayList<CompositeScore> getChildren() {
        return children;
    }

    public void addParent(CompositeScore parentUid) {
        this.parent = required(parentUid, "ParentUid is required and not empty");
    }

    public void addChildren(ArrayList<CompositeScore> children) {
        for (CompositeScore child : children) {
            addChild(child);
        }
    }

    public void addChild(CompositeScore child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(required(child, "ChildUid is required an not empty"));
    }
}
