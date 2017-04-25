package org.eyeseetea.malariacare.domain.entity.pushsummary;

public class PushedValuesCount {

    private int imported;

    private int updated;

    private int ignored;

    private int deleted;

    public PushedValuesCount(int imported, int updated, int ignored, int deleted) {
        this.imported = imported;
        this.updated = updated;
        this.ignored = ignored;
        this.deleted = deleted;
    }

    public int getImported() {
        return imported;
    }

    public int getUpdated() {
        return updated;
    }

    public int getIgnored() {
        return ignored;
    }

    public int getDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        return "PushedValuesCount{" +
                "imported=" + imported +
                ", updated=" + updated +
                ", ignored=" + ignored +
                ", deleted=" + deleted +
                '}';
    }
}
