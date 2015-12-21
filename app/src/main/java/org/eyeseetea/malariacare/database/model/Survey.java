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

package org.eyeseetea.malariacare.database.model;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatioCache;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Table(databaseName = AppDatabase.NAME)
public class Survey extends BaseModel implements VisitableToSDK {
    public static final float MAX_AMBER = 80f;
    public static final float MAX_RED = 50f;

    @Column
    @PrimaryKey(autoincrement = true)
    long id_survey;

    @Column
    Long id_tab_group;
    /**
     * Reference to the tabgroup associated to this survey (loaded lazily)
     */
    TabGroup tabGroup;

    @Column
    Long id_org_unit;
    /**
     * Reference to the org unit associated to this survey (loaded lazily)
     */
    OrgUnit orgUnit;

    @Column
    Long id_user;
    /**
     * Reference to the user that has created this survey (loaded lazily)
     */
    User user;
    
    @Column
    Long id_program;
    /**
     * Reference to the program associated to this survey (loaded lazily)
     */
    Program program;

    @Column
    Date creationDate;

    @Column
    Date completionDate;

    @Column
    Date eventDate;

    @Column
    Date scheduledDate;

    @Column
    Integer status;

    /**
     * List of values for this survey
     */
    List<Value> values;

    /**
     * Calculated answered ratio for this survey according to its values
     */
    SurveyAnsweredRatio answeredQuestionRatio;

    /**
     * Calculated main Score for this survey, is not persisted, just calculated on runtime
     */
    Float mainScore;

    public Survey() {
        //Set dates
        this.creationDate = new Date();
        this.completionDate = this.creationDate;
        this.eventDate = null;
        this.scheduledDate = null;
    }

    public Survey(OrgUnit orgUnit, Program program, User user) {
        this();
        this.setProgram(program);
        this.eventDate = new Date();
        this.status = Constants.SURVEY_IN_PROGRESS; // Possibilities [ In progress | Completed | Sent ]
        this.completionDate= this.eventDate;

        this.setOrgUnit(orgUnit);
        this.setTabGroup(program.getTabGroups().get(0));
        this.setUser(user);
        Log.i(".Survey", Long.valueOf(this.completionDate.getTime()).toString());
    }

    public Survey(OrgUnit orgUnit, TabGroup tabGroup, User user) {
        this();

        // Possibilities [ In progress | Completed | Sent ]
        this.status = Constants.SURVEY_IN_PROGRESS;

        //Set context of the survey
        this.setOrgUnit(orgUnit);
        this.setTabGroup(tabGroup);
        this.setUser(user);
    }

    public Long getId_survey() {
        return id_survey;
    }

    public void setId_survey(Long id_survey) {
        this.id_survey = id_survey;
    }

    public OrgUnit getOrgUnit() {
        if(orgUnit==null){
            if (id_org_unit==null) return null;
            orgUnit = new Select()
                    .from(OrgUnit.class)
                    .where(Condition.column(OrgUnit$Table.ID_ORG_UNIT)
                            .is(id_org_unit)).querySingle();
        }
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit = orgUnit;
        this.id_org_unit = (orgUnit!=null)?orgUnit.getId_org_unit():null;
    }

    public void setOrgUnit(Long id_org_unit){
        this.id_org_unit = id_org_unit;
        this.orgUnit = null;
    }

    public TabGroup getTabGroup() {
        if(tabGroup==null){
            if (id_tab_group==null) return null;
            tabGroup = new Select()
                    .from(TabGroup.class)
                    .where(Condition.column(TabGroup$Table.ID_TAB_GROUP)
                            .is(id_tab_group)).querySingle();
        }
        return tabGroup;
    }

    public void setTabGroup(TabGroup tabGroup) {
        this.tabGroup = tabGroup;
        this.id_tab_group = (tabGroup!=null)?tabGroup.getId_tab_group():null;
    }

    public void setTabGroup(Long id_tab_group){
        this.id_tab_group = id_tab_group;
        this.tabGroup = null;
    }

    public User getUser() {
        if(user==null){
            if(id_user==null) return null;
            user= new Select()
                    .from(User.class)
                    .where(Condition.column(User$Table.ID_USER)
                            .is(id_user)).querySingle();
        }
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.id_user = (user!=null)?user.getId_user():null;
    }

    public void setUser(Long id_user){
        this.id_user = id_user;
        this.user = null;
    }

    public Program getProgram() {
        if(program==null){
            if(id_program==null) return null;
            program= new Select()
                    .from(Program.class)
                    .where(Condition.column(org.eyeseetea.malariacare.database.model.Program$Table.ID_PROGRAM)
                            .is(id_program)).querySingle();
        }
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
        this.id_program = (program!=null)?program.getId_program():null;
    }

    public void setProgram(Long id_program){
        this.id_program = id_program;
        this.program = null;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Date getEventDate(){
        return eventDate;
    }
    
    public Date getCompletionDate(){
        return completionDate;
    }
    
    public void setCompletionDate(Date completionDate){
        this.completionDate=completionDate;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * Checks if the survey has been sent or not
     * @return true|false
     */
    public boolean isSent(){
        return Constants.SURVEY_SENT==this.status;
    }

    /**
     * Checks if the survey has been hidden or not
     * @return true|false
     */
    public boolean isHide(){
        return Constants.SURVEY_HIDE==this.status;
    }
    
    /**
     * Checks if the survey is in progress
     * @return true|false
     */
    public boolean isInProgress(){
        return !isSent() && !isCompleted()&& !isHide();
    }

    /**
     * Checks if the survey has been completed or not
     * @return true|false
     */
    public boolean isCompleted(){
        return Constants.SURVEY_COMPLETED==this.status;
    }

    public Float getMainScore() {
        //The main score is only return from a query 1 time
        if(this.mainScore==null){
            Score score=getScore();
            this.mainScore=(score==null)?0f:score.getScore();
        }
        return mainScore;
    }

    public void setMainScore(Float mainScore) {
        this.mainScore = mainScore;
    }

    public void saveMainScore(){
        Float valScore=0f;
        if(mainScore!=null){
            valScore=mainScore;
        }
        Score score=new Score(this,"",valScore);
        score.save();
    }

    private Score getScore(){
        return new Select()
                .from(Score.class)
                .where(Condition.column(Score$Table.ID_SURVEY).eq(this.getId_survey())).querySingle();
    }

    @Override
    public void delete(){
        Score score=getScore();
        if(score!=null){
            score.delete();
        }
        super.delete();
    }

    public String getType(){
        String type = "";
        if (isTypeA()) type = "A";
        else if (isTypeB()) type = "B";
        else if (isTypeC()) type = "C";
        return type;
    }
    /**
     * Returns this survey is type A (green)
     * @return
     */
    public boolean isTypeA(){
        return this.mainScore>= MAX_AMBER;
    }

    /**
     * Returns this survey is type B (amber)
     * @return
     */
    public boolean isTypeB(){
        return this.mainScore>= MAX_RED && !isTypeA();
    }

    /**
     * Returns this survey is type C (red)
     * @return
     */
    public boolean isTypeC(){
        return !isTypeA() && !isTypeB();
    }

    /**
     * Returns the list of answered values from this survey
     * @return
     */
    public List<Value> getValues(){
        if(values==null){
            values = new Select()
                    .from(Value.class)
                    .where(Condition.column(Value$Table.ID_SURVEY)
                            .eq(this.getId_survey())).queryList();
        }
        return values;
    }

    /**
     * Returns the list of answered values from this survey that belong to a parent question
     * @return
     */
    public List<Value> getValuesFromParentQuestions(){
        List<Value> values = new Select().all().from(Value.class).as("v")
                .join(Question.class, Join.JoinType.LEFT).as("q")
                .on(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_QUESTION))
                        .eq(ColumnAlias.columnWithTable("q", Question$Table.ID_QUESTION)))
                .where(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_SURVEY))
                        .eq(this.getId_survey()))
                .and(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_PARENT)).isNull())
                .and(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.VALUE)).isNotNull())
                .and(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.VALUE)).isNot("")).queryList();
        //List<Value> values = Value.findWithQuery(Value.class, LIST_VALUES_PARENT_QUESTION, this.getId().toString());
        return values;
    }
    
    /**
     * Since there are three possible values first question (RDT):'Positive','Negative','Not Tested'
     * @return String
     */
    public String getRDT() {
        String rdtValue = "";
        if (values == null) {
            values = Value.listAllBySurvey(this);
        }

        if (values.size() > 0) {
            Value firstValue = values.get(0);
            rdtValue = firstValue.getOption().getName();
        }
        return rdtValue;
    }

    /**
     * Turns all values from a survey into a string with values separated by commas
     * @return String
     */
    public String getValuesToString(){
        if(values==null || values.size()==0){
            return "";
        }

        Iterator<Value> iterator=values.iterator();

        String valuesStr="";
        boolean valid = true;

        //Define a filter to select which values will be turned into string by code_question
        List<String> codeQuestionFilter = new ArrayList<String>() {{
            add("Specie");  //4
            add("Sex");     //2
            add("Age");     //3
        }};

        Map mapa = new HashMap();
        while(iterator.hasNext() && valid){
            Value value = iterator.next();
            String qCode = value.getQuestion().getCode();

            // RDT is the first field: if it is not Positive no values are shown
            if(("RDT").equals(qCode) && !value.isAPositive()){
                valid = false;
            }else{
                if(codeQuestionFilter.contains(qCode)) {
                    String val = (value.getOption()!=null)?value.getOption().getCode():value.getValue();
                        mapa.put(qCode, val);
                }
            }
        }

        if(valid) {
            //Sort values
            for(int i=0; i<codeQuestionFilter.size(); i++){
                valuesStr += mapa.get(codeQuestionFilter.get(i));
                if (i < codeQuestionFilter.size()-1) {
                    valuesStr += ", ";
                }
            }
        }

        return valuesStr;
    }

    /**
     * Ratio of completion is cached into answeredQuestionRatio in order to speed up loading
     * @return
     */
    public SurveyAnsweredRatio getAnsweredQuestionRatio(){
        if (answeredQuestionRatio == null) {
            answeredQuestionRatio = SurveyAnsweredRatioCache.get(this.getId_survey());
            if(answeredQuestionRatio == null) {
                answeredQuestionRatio = reloadSurveyAnsweredRatio();
            }
        }
        return answeredQuestionRatio;
    }

    /**
     * Calculates the current ratio of completion for this survey
     * @return SurveyAnsweredRatio that hold the total & answered questions.
     */
    private SurveyAnsweredRatio reloadSurveyAnsweredRatio(){
        if(PreferencesState.isPictureQuestion()){

            int numRequired= Question.countRequiredByProgram(this.getTabGroup().getProgram());
            int numOptional=0;
            int numAnswered = Value.countBySurvey(this);

            for (Value value : this.getValuesFromParentQuestions()) {
                if (value.isAPositive()) {
                    //There might be children no answer questions that should be skipped
                    for(Question childQuestion:value.getQuestion().getQuestionChildren()){
                        numOptional+=(childQuestion.getOutput()==Constants.NO_ANSWER)?0:1;
                    }
                }

            }
            SurveyAnsweredRatio surveyAnsweredRatio=new SurveyAnsweredRatio(numRequired+numOptional, numAnswered);
            SurveyAnsweredRatioCache.put(this.getId_survey(), surveyAnsweredRatio);
            return surveyAnsweredRatio;
        }
        else {
            int numRequired = Question.countRequiredByProgram(this.getTabGroup());
            int numOptional = (int) countNumOptionalQuestionsToAnswer();
            int numAnswered = Value.countBySurvey(this);
            SurveyAnsweredRatio surveyAnsweredRatio = new SurveyAnsweredRatio(numRequired + numOptional, numAnswered);
            SurveyAnsweredRatioCache.put(this.id_survey, surveyAnsweredRatio);
            return surveyAnsweredRatio;
        }
    }

    /**
     * Return the number of child questions that should be answered according to the values of the parent questions.
     * @return
     */
    private long countNumOptionalQuestionsToAnswer(){
        long numOptionalQuestions = new Select().count().from(Question.class).as("q")
                .join(QuestionRelation.class, Join.JoinType.LEFT).as("qr")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_QUESTION))
                                .eq(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.ID_QUESTION)))
                .join(Match.class, Join.JoinType.LEFT).as("m")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.ID_QUESTION_RELATION))
                                .eq(ColumnAlias.columnWithTable("m", Match$Table.ID_QUESTION_RELATION)))
                .join(QuestionOption.class, Join.JoinType.LEFT).as("qo")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("m", Match$Table.ID_MATCH))
                                .eq(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_MATCH)))
                .join(Value.class, Join.JoinType.LEFT).as("v")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_QUESTION))
                                .eq(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_QUESTION)),
                        Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_OPTION))
                                .eq(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_OPTION)))
                    //Parent Child relationship
                .where(Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.OPERATION)).eq(1))
                        //For the given survey
                .and(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_SURVEY)).eq(this.getId_survey()))
                        //The child question requires an answer
                .and(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.OUTPUT)).isNot(Constants.NO_ANSWER))
                .count();

        //Parent with the right value -> not hidden
        return numOptionalQuestions;
    }

    /**
     * Updates ratios, status and completion date depending on the question and answer (text)
     */
    public void updateSurveyStatus(){

        //Sent surveys are not updated
        if(this.isSent() || this.isHide()){
            return;
        }

        SurveyAnsweredRatio answeredRatio=this.reloadSurveyAnsweredRatio();
        if(PreferencesState.isPictureQuestion()){
            if((answeredRatio.getAnswered()==7) || (answeredRatio.getTotal()==0 && answeredRatio.getAnswered()==1)) {
                this.setStatus(Constants.SURVEY_COMPLETED);
                this.setCompletionDate(new Date());
            }else {
                this.setStatus(Constants.SURVEY_IN_PROGRESS);
                this.setCompletionDate(this.eventDate);
            }
        }
        else {
            //Update status & completionDate
            if (answeredRatio.isCompleted()) {
                this.setStatus(Constants.SURVEY_COMPLETED);
                this.setCompletionDate(new Date());
            } else {
                this.setStatus(Constants.SURVEY_IN_PROGRESS);
                this.setCompletionDate(this.eventDate);
            }
        }

        if(!PreferencesState.isPictureQuestion()) {
            //Update status
            this.setStatus(answeredRatio.isCompleted() ? Constants.SURVEY_COMPLETED : Constants.SURVEY_IN_PROGRESS);

            //CompletionDate
            this.setCompletionDate(new Date());

            //it is needed for calculate the score in the completed surveys but not sent.
            saveScore();

        }
        //Saves new status & completionDate
        this.save();
    }

    private void saveScore() {        //Prepare scores info
        List<CompositeScore> compositeScoreList= ScoreRegister.loadCompositeScores(this);

        //Calculate main score to push later
        this.setMainScore(ScoreRegister.calculateMainScore(compositeScoreList));
        this.saveMainScore();
    }

    /**
     * Returns a concrete survey, if it exists
     * @param orgUnit
     * @param tabGroup
     * @return
     */
    public static List<Survey> getUnsentSurveys(OrgUnit orgUnit, TabGroup tabGroup) {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.ID_ORG_UNIT).eq(orgUnit.getId_org_unit()))
                .and(Condition.column(Survey$Table.ID_TAB_GROUP).eq(tabGroup.getId_tab_group()))
                .and(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_SENT))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }

    // Returns a concrete survey, if it exists
    public static List<Survey> getUnsentSurveys(OrgUnit orgUnit, Program program) {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.ID_ORG_UNIT).eq(orgUnit.getId_org_unit()))
                .and(Condition.column(Survey$Table.ID_PROGRAM).eq(program.getId_program()))
                .and(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_SENT))
                .and(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_HIDE))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }

    /**
     * Returns all the surveys with status yet not put to "Sent"
     * @return
     */
    public static List<Survey> getAllUnsentSurveys() {
        if(PreferencesState.isPictureQuestion()){
            return new Select().from(Survey.class)
                    .where(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_SENT))
                    .and(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_HIDE))
                    .orderBy(Survey$Table.EVENTDATE)
                    .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
        }
        else {
            return new Select().from(Survey.class)
                    .where(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_SENT))
                    .orderBy(Survey$Table.EVENTDATE)
                    .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
        }
    }

    /**
     * Returns the last surveys (by date) with status yet not put to "Sent"
     * @param limit
     * @return
     */
    public static List<Survey> getUnsentSurveys(int limit) {
        if(PreferencesState.isPictureQuestion()){        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_SENT))
                .and(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_HIDE))
                .limit(String.valueOf(limit))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
        }
        else {
            return new Select().from(Survey.class)
                    .where(Condition.column(Survey$Table.STATUS).isNot(Constants.SURVEY_SENT))
                    .limit(String.valueOf(limit))
                    .orderBy(Survey$Table.EVENTDATE)
                    .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
        }
    }

    /**
     * Returns all the surveys with status put to "Sent"
     * @return
     */
    public static List<Survey> getAllSentSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_SENT))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }

    /**
     * Returns all the surveys with status put to "Sent" or completed
     * @return
     */
    public static List<Survey> getAllSentOrCompletedSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_SENT))
                .or(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_COMPLETED))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }
    /**
     * Returns the last surveys (by date) with status put to "Sent"
     * @param limit
     * @return
     */
    public static List<Survey> getSentSurveys(int limit) {
        if(PreferencesState.isPictureQuestion()){
            return new Select().from(Survey.class)
                    .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_SENT))
                    .or(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_HIDE))
                    .orderBy(Survey$Table.EVENTDATE)
                    .orderBy(Survey$Table.ID_ORG_UNIT)
                    .queryList();
        }
        else {
            return new Select().from(Survey.class)
                    .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_SENT))
                    .limit(String.valueOf(limit))
                    .orderBy(Survey$Table.EVENTDATE)
                    .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
        }
    }
   // Returns all the surveys with status put to "Hide"
    public static List<Survey> getAllHideAndSentSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_SENT))
                .or(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_HIDE))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }

    /**
     * Returns all the surveys with status put to "Completed"
     * @return
     */
    public static List<Survey> getAllCompletedSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_COMPLETED))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }

    /**
     * Returns the last surveys (by date) with status put to "Completed"
     * @param limit
     * @return
     */
    public static List<Survey> getCompletedSurveys(int limit) {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_COMPLETED))
                .limit(String.valueOf(limit))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }

    /**
     * Returns all the surveys with status put to "In progress"
     * @return
     */
    public static List<Survey> getAllUncompletedSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_IN_PROGRESS))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }

    /**
     * Returns the last surveys (by date) with status put to "In progress"
     * @param limit
     * @return
     */
    public static List<Survey> getUncompletedSurveys(int limit) {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_IN_PROGRESS))
                .limit(String.valueOf(limit))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }

    /**
     * Returns the last surveys (by date) with status Completed or sent
     * @return
     */
    public static List<Survey> getAllUncompletedUnsentSurveys() {
        return new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_COMPLETED))
                .or(Condition.column(Survey$Table.STATUS).eq(Constants.SURVEY_SENT))
                .orderBy(Survey$Table.EVENTDATE)
                .orderBy(Survey$Table.ID_ORG_UNIT).queryList();
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) throws Exception{
        IConvertToSDKVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Survey survey = (Survey) o;

        if (id_survey != survey.id_survey) return false;
        if (id_tab_group != null ? !id_tab_group.equals(survey.id_tab_group) : survey.id_tab_group != null)
            return false;   
        if (id_program != null ? !id_program.equals(survey.id_program) : survey.id_program != null)
            return false;
        if (id_org_unit != null ? !id_org_unit.equals(survey.id_org_unit) : survey.id_org_unit != null)
            return false;
        if (id_user != null ? !id_user.equals(survey.id_user) : survey.id_user != null)
            return false;
        if (creationDate != null ? !creationDate.equals(survey.creationDate) : survey.creationDate != null)
            return false;
        if (completionDate != null ? !completionDate.equals(survey.completionDate) : survey.completionDate != null)
            return false;
        if (eventDate != null ? !eventDate.equals(survey.eventDate) : survey.eventDate != null)
            return false;
        if (scheduledDate != null ? !scheduledDate.equals(survey.scheduledDate) : survey.scheduledDate != null)
            return false;
        return !(status != null ? !status.equals(survey.status) : survey.status != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_survey ^ (id_survey >>> 32));
        result = 31 * result + (id_tab_group != null ? id_tab_group.hashCode() : 0);
        result = 31 * result + (id_program != null ? id_program.hashCode() : 0);
        result = 31 * result + (id_org_unit != null ? id_org_unit.hashCode() : 0);
        result = 31 * result + (id_user != null ? id_user.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (completionDate != null ? completionDate.hashCode() : 0);
        result = 31 * result + (eventDate != null ? eventDate.hashCode() : 0);
        result = 31 * result + (scheduledDate != null ? scheduledDate.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "id_survey=" + id_survey +
                ", id_tab_group=" + id_tab_group +
                ", id_org_unit=" + id_org_unit +
                ", id_user=" + id_user +
                ", creationDate=" + creationDate +
                ", completionDate=" + completionDate +
                ", eventDate=" + eventDate +
                ", scheduledDate=" + scheduledDate +
                ", status=" + status +
                '}';
    }

    public static void removeInProgress() {
        List<Survey> inProgressSurvey= getAllUncompletedSurveys();
        for(int i=inProgressSurvey.size()-1;i>=0;i--){
            inProgressSurvey.get(i).delete();
        }
    }
}
