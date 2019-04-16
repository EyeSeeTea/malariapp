package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.VisitableToSDK;
import org.eyeseetea.malariacare.domain.exception.ConversionException;

@Table(database = AppDatabase.class, name = "ObservationValue")
public class ObservationValueDB extends BaseModel implements VisitableToSDK {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_observation_value;

    @Column
    Long id_observation_fk;

    @Column
    String value;

    @Column
    String uid_observation_value;

    public long getId_observation_value() {
        return id_observation_value;
    }

    public void setId_observation_value(long id_observation_value) {
        this.id_observation_value = id_observation_value;
    }

    public Long getId_observation_fk() {
        return id_observation_fk;
    }

    public void setId_observation_fk(Long id_observation_fk) {
        this.id_observation_fk = id_observation_fk;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUid_observation_value() {
        return uid_observation_value;
    }

    public void setUid_observation_value(String uid_observation_value) {
        this.uid_observation_value = uid_observation_value;
    }

    @Override
    public void accept(IConvertToSDKVisitor convertToSDKVisitor) throws ConversionException {
        convertToSDKVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObservationValueDB that = (ObservationValueDB) o;

        if (id_observation_value != that.id_observation_value) return false;
        if (!id_observation_fk.equals(that.id_observation_fk)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        return uid_observation_value.equals(that.uid_observation_value);
    }

    @Override
    public int hashCode() {
        int result = (int) (id_observation_value ^ (id_observation_value >>> 32));
        result = 31 * result + id_observation_fk.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + uid_observation_value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ObservationValueDB{" +
                "id_observation_value=" + id_observation_value +
                ", id_observation_fk=" + id_observation_fk +
                ", value='" + value + '\'' +
                ", uid_observation_value='" + uid_observation_value + '\'' +
                '}';
    }
}
