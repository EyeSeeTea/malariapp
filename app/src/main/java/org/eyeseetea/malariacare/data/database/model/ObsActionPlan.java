/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.model;

import static org.eyeseetea.malariacare.data.database.AppDatabase.obsActionPlanAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.obsActionPlanName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.surveyAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.surveyName;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.VisitableToSDK;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

@Table(database = AppDatabase.class)
public class ObsActionPlan extends BaseModel implements VisitableToSDK {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_obs_action_plan;

    @Column
    Long id_survey_obs_action_fk;
    /**
     * Reference to the survey associated to this obs action plan (loaded lazily)
     */
    Survey mSurvey;

    @Column
    String gaps;

    @Column
    String action_plan;

    @Column
    String action1;

    @Column
    String action2;

    @Column
    Integer status;

    public ObsActionPlan() {
    }

    public ObsActionPlan(Long surveyId) {
        this();

        this.status = Constants.SURVEY_IN_PROGRESS;

        setSurvey(surveyId);
    }


    public Survey getSurvey() {
        if (mSurvey == null) {
            if (id_survey_obs_action_fk == null) return null;
            mSurvey = new Select()
                    .from(Survey.class)
                    .where(Survey_Table.id_survey
                            .is(id_survey_obs_action_fk)).querySingle();
        }
        return mSurvey;
    }

    public void setSurvey(Survey survey) {
        this.mSurvey = survey;
        this.id_survey_obs_action_fk = (survey != null) ? survey.getId_survey() : null;
    }

    public void setSurvey(Long id_survey) {
        this.id_survey_obs_action_fk = id_survey;
        this.mSurvey = null;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        Log.d(ObsActionPlan.class.getName() + "B&D ObsActionPlan",
                "Id: " + id_survey_obs_action_fk + " actual status:" + status + " set as:"
                        + status);
        this.status = status;
    }

    /**
     * Checks if the survey has been sent or not
     *
     * @return true|false
     */
    public boolean isSent() {
        return Constants.SURVEY_SENT == this.status;
    }

    /**
     * Checks if the survey is PLANNED
     *
     * @return true|false
     */
    public boolean isPlanned() {
        return Constants.SURVEY_PLANNED == this.status;
    }

    /**
     * Checks if the survey is IN_PROGRESS
     *
     * @return true|false
     */
    public boolean isInProgress() {
        return Constants.SURVEY_IN_PROGRESS == this.status;
    }

    /**
     * Checks if the survey is Quarantine
     *
     * @return true|false
     */
    public boolean isInQuarantine() {
        return Constants.SURVEY_QUARANTINE == this.status;
    }

    /**
     * Checks if the survey has been completed or not
     *
     * @return true|false
     */
    public boolean isCompleted() {
        return Constants.SURVEY_COMPLETED == this.status;
    }

    public boolean isConflict() {
        return Constants.SURVEY_CONFLICT == this.status;
    }

    public boolean isReadOnly() {
        return (isCompleted() || isSent());
    }

    public long getId_obs_action_plan() {
        return id_obs_action_plan;
    }

    public void setId_obs_action_plan(long id_obs_action_plan) {
        this.id_obs_action_plan = id_obs_action_plan;
    }

    public Long getId_survey_obs_action_fk() {
        return id_survey_obs_action_fk;
    }

    public void setId_survey_obs_action_fk(Long id_survey_obs_action_fk) {
        this.id_survey_obs_action_fk = id_survey_obs_action_fk;
    }

    public String getGaps() {
        return gaps;
    }

    public void setGaps(String gaps) {
        this.gaps = gaps;
    }

    public String getAction_plan() {
        return action_plan;
    }

    public void setAction_plan(String action_plan) {
        this.action_plan = action_plan;
    }

    public String getAction1() {
        return action1;
    }

    public void setAction1(String action1) {
        this.action1 = action1;
    }

    public String getAction2() {
        return action2;
    }

    public void setAction2(String action2) {
        this.action2 = action2;
    }

    public static ObsActionPlan findObsActionPlanBySurvey(long id_survey) {
        return new Select().from(ObsActionPlan.class).where(ObsActionPlan_Table.id_survey_obs_action_fk.eq(id_survey)).querySingle();
    }

    public static List<ObsActionPlan> getAllCompletedObsActionPlansInSentSurveys() {
        return new Select().from(ObsActionPlan.class).as(obsActionPlanName)
                .join(Survey.class, Join.JoinType.LEFT_OUTER).as(surveyName)
                .on(Survey_Table.id_survey.withTable(surveyAlias)
                        .eq(ObsActionPlan_Table.id_survey_obs_action_fk.withTable(obsActionPlanAlias)))
                .where(Survey_Table.status.withTable(surveyAlias).eq(Constants.SURVEY_SENT))
                .and(ObsActionPlan_Table.status.withTable(obsActionPlanAlias).eq(Constants.SURVEY_COMPLETED))
                .queryList();
    }


    public static List<Survey> getAllSentSurveysWithSendingObsActionPlans() {
        return new Select().from(Survey.class).as(surveyName)
                .join(ObsActionPlan.class, Join.JoinType.LEFT_OUTER).as(obsActionPlanName)
                .on(Survey_Table.id_survey.withTable(surveyAlias)
                        .eq(ObsActionPlan_Table.id_survey_obs_action_fk.withTable(obsActionPlanAlias)))
                .where(Survey_Table.status.withTable(surveyAlias).eq(Constants.SURVEY_SENT))
                .and(ObsActionPlan_Table.status.withTable(obsActionPlanAlias).eq(Constants.SURVEY_SENDING))
                .queryList();
    }


    public static List<ObsActionPlan> getAllSendingObsActionPlans() {
        return new Select().from(ObsActionPlan.class)
                .where(ObsActionPlan_Table.status.eq(Constants.SURVEY_SENDING))
                .queryList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObsActionPlan that = (ObsActionPlan) o;

        if (id_obs_action_plan != that.id_obs_action_plan) return false;
        if (id_survey_obs_action_fk != null ? !id_survey_obs_action_fk.equals(
                that.id_survey_obs_action_fk) : that.id_survey_obs_action_fk != null) {
            return false;
        }
        if (mSurvey != null ? !mSurvey.equals(that.mSurvey) : that.mSurvey != null) return false;
        if (gaps != null ? !gaps.equals(that.gaps) : that.gaps != null) return false;
        if (action_plan != null ? !action_plan.equals(that.action_plan)
                : that.action_plan != null) {
            return false;
        }
        if (action1 != null ? !action1.equals(that.action1) : that.action1 != null) return false;
        if (action2 != null ? !action2.equals(that.action2) : that.action2 != null) return false;
        return status != null ? status.equals(that.status) : that.status == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id_obs_action_plan ^ (id_obs_action_plan >>> 32));
        result = 31 * result + (id_survey_obs_action_fk != null ? id_survey_obs_action_fk.hashCode()
                : 0);
        result = 31 * result + (mSurvey != null ? mSurvey.hashCode() : 0);
        result = 31 * result + (gaps != null ? gaps.hashCode() : 0);
        result = 31 * result + (action_plan != null ? action_plan.hashCode() : 0);
        result = 31 * result + (action1 != null ? action1.hashCode() : 0);
        result = 31 * result + (action2 != null ? action2.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ObsActionPlan{" +
                "id_obs_action_plan=" + id_obs_action_plan +
                ", id_survey_obs_action_fk=" + id_survey_obs_action_fk +
                ", mSurvey=" + mSurvey +
                ", gaps='" + gaps + '\'' +
                ", action_plan='" + action_plan + '\'' +
                ", action1='" + action1 + '\'' +
                ", action2='" + action2 + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) throws ConversionException {
        IConvertToSDKVisitor.visit(this);
    }
}