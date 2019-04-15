package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

@Table(database = AppDatabase.class, name = "ObservationValue")
public class ObservationValueDB extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_observation_value;

    @Column
    Long id_survey_observation_value_fk;

    @Column
    String value;

    @Column
    String uid_observation;

    @Column
    Integer status;

    public long getId_observation_value() {
        return id_observation_value;
    }

    public void setId_observation_value(long id_observation_value) {
        this.id_observation_value = id_observation_value;
    }

    public Long getId_survey_observation_value_fk() {
        return id_survey_observation_value_fk;
    }

    public void setId_survey_observation_value_fk(Long id_survey_observation_value_fk) {
        this.id_survey_observation_value_fk = id_survey_observation_value_fk;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUid_observation() {
        return uid_observation;
    }

    public void setUid_observation(String uid_observation) {
        this.uid_observation = uid_observation;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObservationValueDB that = (ObservationValueDB) o;

        if (id_observation_value != that.id_observation_value) return false;
        if (id_survey_observation_value_fk != null ? !id_survey_observation_value_fk.equals(
                that.id_survey_observation_value_fk)
                : that.id_survey_observation_value_fk != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (uid_observation != null ? !uid_observation.equals(that.uid_observation)
                : that.uid_observation != null) {
            return false;
        }
        return status != null ? status.equals(that.status) : that.status == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id_observation_value ^ (id_observation_value >>> 32));
        result = 31 * result + (id_survey_observation_value_fk != null
                ? id_survey_observation_value_fk.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (uid_observation != null ? uid_observation.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ObservationValueDB{" +
                "id_observation_value=" + id_observation_value +
                ", id_survey_observation_value_fk=" + id_survey_observation_value_fk +
                ", value='" + value + '\'' +
                ", uid_observation='" + uid_observation + '\'' +
                ", status=" + status +
                '}';
    }
}
