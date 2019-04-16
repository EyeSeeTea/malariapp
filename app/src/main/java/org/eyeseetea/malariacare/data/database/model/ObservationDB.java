package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

import java.util.List;

@Table(database = AppDatabase.class, name = "Observation")
public class ObservationDB extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_observation;

    @Column
    Long id_survey_observation_fk;

    @Column
    Integer status_observation;

    List<ObservationValueDB> valuesDB;

    public long getId_observation() {
        return id_observation;
    }

    public void setId_observation(long id_observation) {
        this.id_observation = id_observation;
    }

    public Long getId_survey_observation_fk() {
        return id_survey_observation_fk;
    }

    public void setId_survey_observation_fk(Long id_survey_observation_fk) {
        this.id_survey_observation_fk = id_survey_observation_fk;
    }

    public Integer getStatus_observation() {
        return status_observation;
    }

    public void setStatus_observation(Integer status_observation) {
        this.status_observation = status_observation;
    }

    public List<ObservationValueDB> getValuesDB() {
        return valuesDB;
    }

    public void setValuesDB(
            List<ObservationValueDB> valuesDB) {
        this.valuesDB = valuesDB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObservationDB that = (ObservationDB) o;

        if (id_observation != that.id_observation) return false;
        if (!id_survey_observation_fk.equals(that.id_survey_observation_fk)) return false;
        return status_observation.equals(that.status_observation);
    }

    @Override
    public int hashCode() {
        int result = (int) (id_observation ^ (id_observation >>> 32));
        result = 31 * result + id_survey_observation_fk.hashCode();
        result = 31 * result + status_observation.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ObservationDB{" +
                "id_observation=" + id_observation +
                ", id_survey_observation_fk=" + id_survey_observation_fk +
                ", status=" + status_observation +
                '}';
    }
}