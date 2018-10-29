package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.PositiveOrCeroChecker.isPositiveOrCero;
import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.ArrayList;
import java.util.List;

public class CompositeScore {
    private String uid;
    private String label;
    private String hierarchicalCode;
    private int orderPos;
    private String parentUid;
    private ArrayList<CompositeScore> children =  new ArrayList<>();

    public static final String ROOT_NODE_CODE = "0";


    public CompositeScore(String parentUid, String uid, String label, String hierarchicalCode,
            int orderPos) {
        this.parentUid = parentUid;
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

    public String getParent() {
        return parentUid;
    }

    public List<CompositeScore> getChildren() {
        return new ArrayList<>(children);
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

    public boolean isRoot() {
        return hierarchicalCode.equals(ROOT_NODE_CODE);
    }

    @Override
    public String toString() {
        return "CompositeScore{" +
                "uid='" + uid + '\'' +
                ", label='" + label + '\'' +
                ", hierarchicalCode='" + hierarchicalCode + '\'' +
                ", orderPos=" + orderPos +
                ", parent=" + parentUid +
                ", children=" + children +
                '}';
    }


}
