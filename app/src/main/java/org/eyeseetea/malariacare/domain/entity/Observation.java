package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.domain.common.RequiredChecker;

import java.util.ArrayList;
import java.util.List;

public class Observation {
    private String surveyUid;
    private ObservationStatus status;
    private List<ObservationValue> values;

    private Observation(String surveyUid,
            ObservationStatus observationStatus, List<ObservationValue> observationValues){
        this.surveyUid = RequiredChecker.required(surveyUid,"surveyUid is required");
        this.status =
                RequiredChecker.required(observationStatus,"status is required");
        this.values =
                RequiredChecker.required(observationValues,"values is required");
    }

    public static Observation createNewObservation(String surveyUid) {
        Observation observation = new Observation(surveyUid, ObservationStatus.IN_PROGRESS,
                new ArrayList<ObservationValue>());
        return observation;
    }

    public static Observation createStoredObservation(String surveyUid,
            ObservationStatus status, List<ObservationValue> values) {

        Observation observation = new Observation(surveyUid, status, values);
        return observation;
    }

    public void addObservationValue(ObservationValue observationValue){
        if (values.contains(observationValue))
            values.set(values.indexOf(observationValue),observationValue);
        else
            values.add(values.indexOf(observationValue),observationValue);
    }

    public String getSurveyUid() {
        return surveyUid;
    }

    public ObservationStatus getStatus() {
        return status;
    }

    public List<ObservationValue> getValues() {
        return new ArrayList<>(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Observation that = (Observation) o;

        if (!surveyUid.equals(that.surveyUid)) return false;
        if (status != that.status) return false;
        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        int result = surveyUid.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Observation{" +
                "surveyUid='" + surveyUid + '\'' +
                ", observationStatus=" + status +
                ", observationValues=" + values +
                '}';
    }
}
