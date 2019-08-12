package org.eyeseetea.malariacare.data.database.model;

import static org.eyeseetea.malariacare.utils.Constants.SURVEY_SENT;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.VisitableToSDK;
import org.eyeseetea.malariacare.data.sync.IData;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.exception.ConversionException;

import java.util.List;

@Table(database = AppDatabase.class, name = "Observation")
public class ObservationDB extends BaseModel  implements VisitableToSDK, IData {

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
        if (valuesDB == null) {
            valuesDB = new Select()
                    .from(ObservationValueDB.class)
                    .where(ObservationValueDB_Table.id_observation_fk
                            .eq(this.getId_observation())).queryList();
        }
        return valuesDB;
    }

    public void setValuesDB(
            List<ObservationValueDB> valuesDB) {
        this.valuesDB = valuesDB;
    }

    @Override
    public Long getSurveyId() {
        return getId_survey_observation_fk();
    }

    @Override
    public void changeStatusToSending() {
        setStatus_observation(ObservationStatus.SENDING.getCode());
        save();
    }

    @Override
    public void changeStatusToSyncConversionError() {
        setStatus_observation(ObservationStatus.ERROR_CONVERSION_SYNC.getCode());
        save();
    }

    @Override
    public void changeStatusToQuarantine() {
        //Quarantine to observations is not necessary because generate duplicates are not possible,
        //This type of element only overwritte the server survey.

        setStatus_observation(ObservationStatus.COMPLETED.getCode());
        save();
    }

    @Override
    public void changeStatusToConflict() {
        setStatus_observation(ObservationStatus.CONFLICT.getCode());
        save();
    }

    @Override
    public void changeStatusToSent() {
        setStatus_observation(ObservationStatus.SENT.getCode());
        save();
    }

    @Override
    public void saveConflict(String questionUid) {
        //for now observationValue does not save conflict in values
    }


    public static List<ObservationDB> getAllCompletedObservationsInSentSurveys() {
        return new Select().from(ObservationDB.class)
                .leftOuterJoin(SurveyDB.class)
                .on(SurveyDB_Table.id_survey.eq(ObservationDB_Table.id_survey_observation_fk))
                .where(SurveyDB_Table.status.eq(SURVEY_SENT))
                .and(ObservationDB_Table.status_observation.eq(ObservationStatus.COMPLETED.getCode()))
                .queryList();
    }

    public static List<ObservationDB> getAllSendingObservations() {
        return new Select().from(ObservationDB.class)
                .where(ObservationDB_Table.status_observation.eq(ObservationStatus.SENDING.getCode()))
                .queryList();
    }

    public static List<SurveyDB> getAllSentSurveysWithSendingObservations() {
        return new Select().from(SurveyDB.class)
                .leftOuterJoin(ObservationDB.class)
                .on(SurveyDB_Table.id_survey.eq(ObservationDB_Table.id_survey_observation_fk))
                .where(SurveyDB_Table.status.eq(SURVEY_SENT))
                .and(ObservationDB_Table.status_observation.eq(ObservationStatus.SENDING.getCode()))
                .queryList();
    }

    public static ObservationDB getBySurveyId(Long surveyId) {
        return new Select().from(ObservationDB.class)
                .where(ObservationDB_Table.id_survey_observation_fk.eq(surveyId)).querySingle();
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

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) throws ConversionException {
        IConvertToSDKVisitor.visit(this);
    }
}