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
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.Condition.In;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Question extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_question;
    @Column
    String code;
    @Column
    String de_name;
    @Column
    String short_name;
    @Column
    String form_name;
    @Column
    String uid;
    @Column
    Integer order_pos;
    @Column
    Float numerator_w;
    @Column
    Float denominator_w;

    @Column
    String feedback;

    @Column
    Long id_header;

    /**
     * Reference to the parent header (loaded lazily)
     */
    Header header;

    @Column
    Long id_answer;

    /**
     * Reference to the associated answer (loaded lazily)
     */
    Answer answer;

    @Column
    Integer output;

    @Column
    Long id_parent;

    /**
     * Reference to parent question (loaded lazily, DEPRECATED??)
     */
    Question question;

    @Column
    Long id_composite_score;

    /**
     * Reference to associated compositeScore for this question (loaded lazily)
     */
    CompositeScore compositeScore;

    /**
     * List of children questions associated to this question
     */
    List<Question> children;

    /**
     * List of values for this question
     */
    List<Value> values;

    /**
     * List of questionRelations of this question
     */
    List<QuestionRelation> questionRelations;

    /**
     * List of matches of this question
     */
    List<Match> matches;

    Boolean parent;

    List<Question> relatives;

    List<Question> master;
    public Question() {
    }

    public Question(String code, String de_name, String short_name, String form_name, String uid, Integer order_pos, Float numerator_w, Float denominator_w, String feedback, Integer output,Header header, Answer answer, Question question, CompositeScore compositeScore) {
        this.code = code;
        this.de_name = de_name;
        this.short_name = short_name;
        this.form_name = form_name;
        this.uid = uid;
        this.order_pos = order_pos;
        this.numerator_w = numerator_w;
        this.denominator_w = denominator_w;
        this.feedback = feedback;
        this.output = output;
        this.parent = null;

        this.setHeader(header);
        this.setAnswer(answer);
        this.setCompositeScore(compositeScore);
        this.setQuestion(question);
    }
   public Question(String code, String de_name, String short_name, String form_name, String uid, Integer order_pos, Float numerator_w, Float denominator_w, Header header, Answer answer, Question question, CompositeScore compositeScore) {
       this.code = code;
       this.de_name = de_name;
       this.short_name = short_name;
       this.form_name = form_name;
       this.uid = uid;
       this.order_pos = order_pos;
       this.numerator_w = numerator_w;
       this.denominator_w = denominator_w;
       
       this.setHeader(header);
       this.setAnswer(answer);
       this.setCompositeScore(compositeScore);
       this.setQuestion(question);
    }

    public Long getId_question() {
        return id_question;
    }

    public void setId_question(Long id_question) {
        this.id_question = id_question;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDe_name() {
        return de_name;
    }

    public void setDe_name(String de_name) {
        this.de_name = de_name;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getForm_name() {
        return form_name;
    }

    public void setForm_name(String form_name) {
        this.form_name = form_name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getOrder_pos() {
        return order_pos;
    }

    public void setOrder_pos(Integer order_pos) {
        this.order_pos = order_pos;
    }

    public Float getNumerator_w() {
        return numerator_w;
    }

    public void setNumerator_w(Float numerator_w) {
        this.numerator_w = numerator_w;
    }

    public Float getDenominator_w() {
        return denominator_w;
    }

    public void setDenominator_w(Float denominator_w) {
        this.denominator_w = denominator_w;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Header getHeader() {
        if(header==null){
            if(id_header==null) return null;
            header = new Select()
                    .from(Header.class)
                    .where(Condition.column(Header$Table.ID_HEADER)
                            .is(id_header)).querySingle();
        }
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
        this.id_header = (header!=null)?header.getId_header():null;
    }

    public void setHeader(Long id_header){
        this.id_header = id_header;
        this.header = null;
    }

    public void setOutput(Integer output){
        this.output = output;
    }

    public Integer getOutput(){
        return output;
    }

    public Answer getAnswer() {
        if(answer==null){
            if(id_answer==null) return  null;
            answer = new Select()
                    .from(Answer.class)
                    .where(Condition.column(Answer$Table.ID_ANSWER)
                            .is(id_answer)).querySingle();
        }
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
        this.id_answer = (answer!=null)?answer.getId_answer():null;
    }

    public void setAnswer(Long id_answer){
        this.id_answer = id_answer;
        this.answer = null;
    }

    public Question getQuestion() {
        if(question ==null){
            question = new Select()
                    .from(Question.class)
                    .where(Condition.column(Question$Table.ID_QUESTION)
                            .is(id_parent)).querySingle();
        }
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
        this.id_parent = (question!=null)?question.getId_question():null;
    }

    public void setQuestion(Long id_parent){
        this.id_parent = id_parent;
        this.question = null;
    }

    public CompositeScore getCompositeScore() {
        if(compositeScore ==null){
            if(id_composite_score==null) return null;
            compositeScore = new Select()
                    .from(CompositeScore.class)
                    .where(Condition.column(CompositeScore$Table.ID_COMPOSITE_SCORE)
                            .is(id_composite_score)).querySingle();
        }
        return compositeScore;
    }

    public void setCompositeScore(CompositeScore compositeScore) {
        this.compositeScore = compositeScore;
        this.id_composite_score = (compositeScore!=null)?compositeScore.getId_composite_score():null;
    }

    public void setCompositeScore(Long id_composite_score){
        this.id_composite_score = id_composite_score;
        this.compositeScore = null;
    }

    public boolean hasParent() {
        if(Utils.isPictureQuestion()){
            return getQuestion() != null;}
        else{
        if (parent == null) {
            long countChildQuestionRelations = new Select().count().from(QuestionRelation.class)
                    .indexedBy("QuestionRelation_operation")
                    .where(Condition.column(QuestionRelation$Table.ID_QUESTION).eq(this.getId_question()))
                    .and(Condition.column(QuestionRelation$Table.OPERATION).eq(QuestionRelation.PARENT_CHILD))
                    .count();
            parent = countChildQuestionRelations > 0;
        }
        return parent;
        }
    }

   public List<Question> getQuestionChildren() {
       if(Utils.isPictureQuestion()) {
           if (this.children == null) {
               this.children = new Select().from(Question.class)
                       .where(Condition.column(Question$Table.ID_QUESTION)
                               .eq(this.getId_question()))
                       .orderBy(true, Question$Table.ORDER_POS).queryList();
           }
           return this.children;
       }
       else{
           return getChildren();
       }
    }
    //@OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "questionRelations")
    public List<QuestionRelation> getQuestionRelations() {
        if(questionRelations ==null){
            this.questionRelations = new Select()
                    .from(QuestionRelation.class)
                    .indexedBy("QuestionRelation_id_question")
                    .where(Condition.column(QuestionRelation$Table.ID_QUESTION)
                            .eq(this.getId_question()))
                    .queryList();
        }
        return this.questionRelations;
    }

    public boolean hasQuestionRelations() {
        return !this.getQuestionRelations().isEmpty();
    }

    public List<QuestionOption> getQuestionOption() {
        //if (this.children == null){
        return new Select().from(QuestionOption.class)
                .indexedBy("QuestionOption_id_question")
                .where(Condition.column(QuestionOption$Table.ID_QUESTION).eq(this.getId_question()))
                .queryList();
        //}
    }

    public boolean hasQuestionOption() {
        return !this.getQuestionOption().isEmpty();
    }

    public List<Match> getMatches() {
        if (matches == null) {
            matches = new Select().from(Match.class).as("m")
                    .join(QuestionOption.class, Join.JoinType.LEFT).as("qo")
                    .on(
                            Condition.column(ColumnAlias.columnWithTable("m", Match$Table.ID_MATCH))
                                    .eq(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_MATCH)))
                    .where(Condition.column(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_QUESTION)).eq(this.getId_question())).queryList();
        }
        return matches;
    }

    public List<Question> getRelatives() {
        if (this.relatives == null) {
            List<QuestionRelation> questionRelations = new Select().from(QuestionRelation.class)
                    .where(Condition.column(QuestionRelation$Table.ID_QUESTION)
                    .eq(this.getId_question())).queryList();
            if (questionRelations.size() == 0) return null;
            Iterator<QuestionRelation> iterator = questionRelations.iterator();
            In in = Condition.column(Question$Table.ID_QUESTION).in(Long.toString(iterator.next().getId_question_relation()));
            while(iterator.hasNext()){
                in.and(Long.toString(iterator.next().getId_question_relation()));
            }
         this.relatives = new Select().from(Question.class)
                    .where(in).queryList();
       }
        return this.relatives;
    }


    //@OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "children")
    public List<Question> getChildren() {
        if(!Utils.isPictureQuestion()){

        if (this.children == null) {

            //No matches no children
            if (getId_question() == 74) {
                Log.d("Question", "testing");
            }
            List<Match> matches = getMatches();
            if (matches.size() == 0) {
                this.children = new ArrayList<>();
                return this.children;
            }


            Iterator<Match> matchesIterator = matches.iterator();
            In in = Condition.column(ColumnAlias.columnWithTable("m", Match$Table.ID_MATCH)).in(Long.toString(matchesIterator.next().getId_match()));
            while (matchesIterator.hasNext()){
                in.and(Long.toString(matchesIterator.next().getId_match()));
            }

            //Select question from questionrelation where operator=1 and id_match in (..)
            return new Select().from(Question.class).as("q")
                    //Question + QuestioRelation
                    .join(QuestionRelation.class, Join.JoinType.LEFT).as("qr")
                    .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_QUESTION))
                            .eq(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.ID_QUESTION)))
                            //+Match
                    .join(Match.class, Join.JoinType.LEFT).as("m")
                    .on(Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.ID_QUESTION_RELATION))
                                    .eq(ColumnAlias.columnWithTable("m", Match$Table.ID_QUESTION_RELATION)))
                            //Parent child relationship
                    .where(in)
                            //In clause
                    .and(Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.OPERATION)).eq(QuestionRelation.PARENT_CHILD)).queryList();
        }
        return this.children;

        }
        else{ return getQuestionChildren();}
    }

    public boolean hasRelatives() {return !getRelatives().isEmpty(); }

    public boolean hasChildren(){
        if(Utils.isPictureQuestion()){
            return !getQuestionChildren().isEmpty();
        }
            return !getChildren().isEmpty();
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "values")
    public List<Value> getValues() {
        if(values==null){
            values = new Select()
                    .from(Value.class)
                    .where(Condition.column(Value$Table.ID_QUESTION)
                            .eq(this.getId_question())).queryList();
        }
        return values;
    }

    /**
     * Gets the value of this question in the current survey in session
     *
     * @return
     */
    public Value getValueBySession() {
        return this.getValueBySurvey(Session.getSurvey());
    }

    /**
     * Gets the value of this question in the given Survey
     *
     * @param survey
     * @return
     */
    public Value getValueBySurvey(Survey survey) {
        if (survey == null) {
            return null;
        }
        List<Value> returnValues = new Select().from(Value.class)
                .indexedBy("Value_id_survey")
                .where(Condition.column(Value$Table.ID_QUESTION).eq(this.getId_question()))
                .and(Condition.column(Value$Table.ID_SURVEY).eq(survey.getId_survey())).queryList();

        if (returnValues.size() == 0) {
            return null;
        } else {
            return returnValues.get(0);
        }
    }

    /**
     * Gets the option of this question in the current survey in session
     *
     * @return
     */
    public Option getOptionBySession() {
        return this.getOptionBySurvey(Session.getSurvey());
    }

    /**
     * Gets the option of this question in the given survey
     *
     * @param survey
     * @return
     */
    public Option getOptionBySurvey(Survey survey) {
        if (survey == null) {
            return null;
        }

        Value value = this.getValueBySurvey(survey);
        if (value == null) {
            return null;
        }

        return value.getOption();
    }


    /*Returns true if the question belongs to a Custom Tab*/
    public boolean belongsToCustomTab() {

        return getHeader().getTab().isACustomTab();
    }

    /**
     * Checks if this question is shown according to the values of the given survey
     *
     * @param survey
     * @return
     */
    public boolean isHiddenBySurvey(Survey survey) {
        //No question relations
        if(Utils.isPictureQuestion()){

            Question parent=this.getQuestion();
            //There is a parent question and it is not answered
            if (parent!= null && parent.getValueBySurvey(survey)==null) {
                return true;
            }

            return false;
        }
        else{
        if (!hasParent()) {
            return false;
        }
        long hasParentOptionActivated = new Select().count().from(Value.class).as("v")
                .join(QuestionOption.class, Join.JoinType.LEFT).as("qo")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_QUESTION))
                                .eq(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_QUESTION)),
                        Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_OPTION))
                                .eq(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_OPTION)))
                .join(Match.class, Join.JoinType.LEFT).as("m")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_MATCH))
                                .eq(ColumnAlias.columnWithTable("m", Match$Table.ID_MATCH)))
                .join(QuestionRelation.class, Join.JoinType.LEFT).as("qr")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("m", Match$Table.ID_QUESTION_RELATION))
                                .eq(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.ID_QUESTION_RELATION)))
                        //Parent child relationship
                .where(Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.OPERATION)).eq(1))
                        //For the given survey
                .and(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_SURVEY)).eq(survey.getId_survey()))
                        //The child question in the relationship is 'this'
                .and(Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.ID_QUESTION)).eq(this.getId_question()))
                .count();

        //Parent with the right value -> not hidden
        return hasParentOptionActivated > 0 ? false : true;
        }
    }

    /**
     * Add register to ScoreRegister if this is an scored question
     *
     * @return List</Float> {num, den}
     */
    public List<Float> initScore(Survey survey) {
        if (!this.isScored()) {
            return null;
        }

        Float num = ScoreRegister.calcNum(this, survey);
        Float denum = ScoreRegister.calcDenum(this, survey);
        ScoreRegister.addRecord(this, num, denum);
        return Arrays.asList(num, denum);
    }

    /**
     * Counts the number of required questions (without a parent question).
     * @param program
     * @return
     */
    public static int countRequiredByProgram(Program program) {
        if (program == null || program.getId_program() == null) {
            return 0;
        }
        /**
         * Sql query that counts required questions in a program (required for % stats)
         */
        List<Question> questionsByProgram = new Select().all().from(Question.class).as("q")
                .join(Answer.class, Join.JoinType.LEFT).as("a")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_ANSWER))
                        .eq(ColumnAlias.columnWithTable("a", Answer$Table.ID_ANSWER)))
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID_HEADER)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)))
                .join(Program.class, Join.JoinType.LEFT).as("p")
                .on(Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.ID_PROGRAM))
                        .eq(ColumnAlias.columnWithTable("p", Program$Table.ID_PROGRAM)))
                .where(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_PARENT)).is(0))
                .and(Condition.column(ColumnAlias.columnWithTable("a", Question$Table.OUTPUT)).isNot(Constants.NO_ANSWER))
                .and(Condition.column(ColumnAlias.columnWithTable("p", Program$Table.ID_PROGRAM)).is(program.getId_program())).queryList();

        return questionsByProgram.size();
    }

    /**
     * Counts the number of required questions (without a parent question).
     *
     * @param tabGroup
     * @return
     */
    public static int countRequiredByProgram(TabGroup tabGroup) {
        if (tabGroup == null || tabGroup.getId_tab_group() == null) {
            return 0;
        }

        // Count all the quesions that may have an answer
        long totalAnswerableQuestions = new Select().count()
                .from(Question.class).as("q")
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID_HEADER)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)))
                .where(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.OUTPUT)).isNot(Constants.NO_ANSWER))
                .and(Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB_GROUP)).eq(tabGroup.getId_tab_group())).count();

        // Count children questions from the given taggroup
        long numChildrenQuestion = new Select().count()
                .from(QuestionRelation.class).as("qr")
                .join(Question.class, Join.JoinType.LEFT).as("q")
                .on(Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.ID_QUESTION))
                        .eq(ColumnAlias.columnWithTable("q", Question$Table.ID_QUESTION)))
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID_HEADER)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)))
                .where(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.OUTPUT)).isNot(Constants.NO_ANSWER))
                .and(Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB_GROUP)).eq(tabGroup.getId_tab_group()))
                .and(Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.OPERATION)).eq(Constants.OPERATION_TYPE_PARENT)).count();

        // Return number of parents (total - children)
        return (int) (totalAnswerableQuestions-numChildrenQuestion);
    }

    /**
     * Checks if this question is triggered according to the current values of the given survey.
     * Only applies to question with answers DROPDOWN_DISABLED
     *
     * @param survey
     * @return
     */
    public boolean isTriggered(Survey survey){

        //No survey no party
        if(survey==null || survey.getId_survey()==null){
            return false;
        }

        //Only disabled dropdowns
        if(this.getOutput()!=Constants.DROPDOWN_LIST_DISABLED){
            return false;
        }

        //Find questionoptions for q1 and q2 and check same match
        List<QuestionOption> questionOptions = new Select().from(QuestionOption.class).as("qo")
                .join(Match.class, Join.JoinType.LEFT).as("m")
                .on(Condition.column(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_MATCH)).eq(ColumnAlias.columnWithTable("m", Match$Table.ID_MATCH)))

                .join(QuestionRelation.class, Join.JoinType.LEFT).as("qr")
                .on(Condition.column(ColumnAlias.columnWithTable("m", Match$Table.ID_QUESTION_RELATION)).eq(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.ID_QUESTION_RELATION)))

                .join(Value.class, Join.JoinType.LEFT).as("v")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_QUESTION))
                                .eq(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_QUESTION)),
                        Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_OPTION))
                                .eq(ColumnAlias.columnWithTable("qo", QuestionOption$Table.ID_OPTION)))
                .where(Condition.column(ColumnAlias.columnWithTable("v", Value$Table.ID_SURVEY)).eq(survey.getId_survey()))
                .and(Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.ID_QUESTION)).eq(this.getId_question()))
                .and(Condition.column(ColumnAlias.columnWithTable("qr", QuestionRelation$Table.OPERATION)).eq(Constants.OPERATION_TYPE_MATCH))
                .queryList();

        //No values no match
        if(questionOptions.size()!=2){
            return false;
        }

        //Match is triggered if questionoptions have same matchid
        long idmatchQ1=questionOptions.get(0).getMatch().getId_match();
        long idmatchQ2=questionOptions.get(1).getMatch().getId_match();
        return idmatchQ1==idmatchQ2;

    }

    /**
     * Returns all the questions that belongs to a program
     *
     * @param tabGroup
     * @return
     */
    public static List<Question> listByTabGroup(TabGroup tabGroup) {
        if (tabGroup == null || tabGroup.getId_tab_group() == null) {
            return new ArrayList();
        }

        //return Question.findWithQuery(Question.class, LIST_ALL_BY_PROGRAM, program.getId().toString());


        return new Select().all().from(Question.class).as("q")
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID_HEADER)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)))
                .join(TabGroup.class, Join.JoinType.LEFT).as("tg")
                .on(Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB_GROUP))
                        .eq(ColumnAlias.columnWithTable("tg", TabGroup$Table.ID_TAB_GROUP)))
                .where(Condition.column(ColumnAlias.columnWithTable("tg", TabGroup$Table.ID_TAB_GROUP))
                        .eq(tabGroup.getId_tab_group()))
                .orderBy(Tab$Table.ORDER_POS)
                .orderBy(Question$Table.ORDER_POS).queryList();

    }

    public static List<Question> listAllByTabs(List<Tab> tabs) {

        if (tabs == null || tabs.size() == 0) {
            return new ArrayList();
        }

        Iterator<Tab> iterator = tabs.iterator();
        In in = Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)).in(Long.toString(iterator.next().getId_tab()));
        while (iterator.hasNext()) {
            in.and(Long.toString(iterator.next().getId_tab()));
        }

        return new Select().from(Question.class).as("q")
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID_HEADER)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)))
                .where(in)
                .orderBy(Tab$Table.ORDER_POS)
                .orderBy(Question$Table.ORDER_POS).queryList();
    }

    /**
     * Returns all the questions that belongs to a program
     * @param program
     * @return
     */
    public static List<Question> listAllByProgram(Program program){
        if(program==null || program.getId_program()==null){
            return new ArrayList();
        }

        return new Select().all().from(Question.class).as("q")
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID_HEADER)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)))
                .join(Program.class, Join.JoinType.LEFT).as("p")
                .on(Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.ID_PROGRAM))
                        .eq(ColumnAlias.columnWithTable("p", Program$Table.ID_PROGRAM)))
                .where(Condition.column(ColumnAlias.columnWithTable("p", Program$Table.ID_PROGRAM))
                        .eq(program.getId_program()))
                .orderBy(Tab$Table.ORDER_POS)
                .orderBy(Question$Table.ORDER_POS).queryList();
    }


    /**
     * Checks if this question is scored or not.
     *
     * @return true|false
     */
    public boolean isScored() {
        Integer output = getOutput();
        return output == Constants.DROPDOWN_LIST ||
                output == Constants.DROPDOWN_LIST_DISABLED ||
                output == Constants.RADIO_GROUP_HORIZONTAL ||
                output == Constants.RADIO_GROUP_VERTICAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (id_question != question.id_question) return false;
        if (code != null ? !code.equals(question.code) : question.code != null) return false;
        if (de_name != null ? !de_name.equals(question.de_name) : question.de_name != null)
            return false;
        if (short_name != null ? !short_name.equals(question.short_name) : question.short_name != null)
            return false;
        if (form_name != null ? !form_name.equals(question.form_name) : question.form_name != null)
            return false;
        if (uid != null ? !uid.equals(question.uid) : question.uid != null) return false;
        if (order_pos != null ? !order_pos.equals(question.order_pos) : question.order_pos != null)
            return false;
        if (numerator_w != null ? !numerator_w.equals(question.numerator_w) : question.numerator_w != null)
            return false;
        if (denominator_w != null ? !denominator_w.equals(question.denominator_w) : question.denominator_w != null)
            return false;
        if (feedback != null ? !feedback.equals(question.feedback) : question.feedback != null)
            return false;
        if (id_header != null ? !id_header.equals(question.id_header) : question.id_header != null)
            return false;
        if (id_answer != null ? !id_answer.equals(question.id_answer) : question.id_answer != null)
            return false;
        if (output != null ? !output.equals(question.output) : question.output != null)
            return false;
        if (id_parent != null ? !id_parent.equals(question.id_parent) : question.id_parent != null)
            return false;
        return !(id_composite_score != null ? !id_composite_score.equals(question.id_composite_score) : question.id_composite_score != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_question ^ (id_question >>> 32));
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (de_name != null ? de_name.hashCode() : 0);
        result = 31 * result + (short_name != null ? short_name.hashCode() : 0);
        result = 31 * result + (form_name != null ? form_name.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (order_pos != null ? order_pos.hashCode() : 0);
        result = 31 * result + (numerator_w != null ? numerator_w.hashCode() : 0);
        result = 31 * result + (denominator_w != null ? denominator_w.hashCode() : 0);
        result = 31 * result + (feedback != null ? feedback.hashCode() : 0);
        result = 31 * result + (id_header != null ? id_header.hashCode() : 0);
        result = 31 * result + (id_answer != null ? id_answer.hashCode() : 0);
        result = 31 * result + (output != null ? output.hashCode() : 0);
        result = 31 * result + (id_parent != null ? id_parent.hashCode() : 0);
        result = 31 * result + (id_composite_score != null ? id_composite_score.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id_question=" + id_question +
                ", code='" + code + '\'' +
                ", de_name='" + de_name + '\'' +
                ", short_name='" + short_name + '\'' +
                ", form_name='" + form_name + '\'' +
                ", uid='" + uid + '\'' +
                ", order_pos=" + order_pos +
                ", numerator_w=" + numerator_w +
                ", feedback='" + feedback + '\'' +
                ", denominator_w=" + denominator_w +
                ", id_header=" + id_header +
                ", id_answer=" + id_answer +
                ", output=" + output +
                ", id_parent=" + id_parent +
                ", id_composite_score=" + id_composite_score +
                '}';
    }
}
