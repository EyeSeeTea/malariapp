package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.domain.common.RequiredChecker;

public class ObservationValue {
    private String value;
    private String observationValueUid;

    public ObservationValue(String value, String observationValueUid) {
        this.value = RequiredChecker.required(value, "value is required");
        this.observationValueUid = RequiredChecker.required(observationValueUid, "observationValueUid is required");
    }

    public String getValue() {
        return value;
    }

    public String getObservationValueUid() {
        return observationValueUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObservationValue that = (ObservationValue) o;

        if (!value.equals(that.value)) return false;
        return observationValueUid.equals(that.observationValueUid);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + observationValueUid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ObservationValue{" +
                "value='" + value + '\'' +
                ", observationValueUid='" + observationValueUid + '\'' +
                '}';
    }
}
