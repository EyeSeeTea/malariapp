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

import static org.eyeseetea.malariacare.data.database.AppDatabase.headerAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.headerName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.matchAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.matchName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionOptionAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionOptionName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionRelationAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionRelationName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.tabAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.tabName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.valueAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.valueName;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Condition.In;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.fragments.SurveyFragment;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Table(database = AppDatabase.class)
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
    String uid_question;
    @Column
    Integer order_pos;
    @Column
    Float numerator_w;
    @Column
    Float denominator_w;

    @Column
    String feedback;

    @Column
    Long id_header_fk;

    /**
     * Reference to the parent header (loaded lazily)
     */
    Header header;

    @Column
    Long id_answer_fk;

    /**
     * Reference to the associated answer (loaded lazily)
     */
    Answer answer;

    @Column
    Integer output;

    @Column
    Boolean compulsory;

    @Column
    Long id_composite_score_fk;

    @Column
    Integer row;

    @Column
    Integer column;

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

    /**
     * Cached value that tells if this questions launches a match trigger or not
     */
    Boolean matchTrigger;

    /**
     * Reference to the question triggered with a match by this one
     */
    //Question questionTriggered;
    public Question() {
    }

    public Question(String code, String de_name, String short_name, String form_name, String uid,
            Integer order_pos, Float numerator_w, Float denominator_w, String feedback,
            Integer output, Header header, Answer answer, CompositeScore compositeScore,
            Boolean compulsory, Integer row, Integer column) {
        this.code = code;
        this.de_name = de_name;
        this.short_name = short_name;
        this.form_name = form_name;
        this.uid_question = uid;
        this.order_pos = order_pos;
        this.numerator_w = numerator_w;
        this.denominator_w = denominator_w;
        this.feedback = feedback;
        this.output = output;
        this.parent = null;
        this.matchTrigger = null;
        this.compulsory = compulsory;
        this.row = row;
        this.column = column;

        this.setHeader(header);
        this.setAnswer(answer);
        this.setCompositeScore(compositeScore);
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
        return uid_question;
    }

    public void setUid(String uid) {
        this.uid_question = uid;
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

    public void setCompulsory(Boolean compulsory) {
        this.compulsory = compulsory;
    }

    public Boolean getCompulsory() {
        return compulsory;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getRow() {
        return this.row;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public Integer getColumn() {
        return this.column;
    }

    public Header getHeader() {
        if(header==null){
            if(id_header_fk==null) return null;
            header = new Select()
                    .from(Header.class)
                    .where(Header_Table.id_header
                            .is(id_header_fk)).querySingle();
        }
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
        this.id_header_fk = (header!=null)?header.getId_header():null;
    }

    public void setHeader(Long id_header){
        this.id_header_fk = id_header;
        this.header = null;
    }

    public void setOutput(Integer output) {
        this.output = output;
    }

    public Integer getOutput() {
        return output;
    }

    public Answer getAnswer() {
        if(answer==null){
            if(id_answer_fk==null) return  null;
            answer = new Select()
                    .from(Answer.class)
                    .where(Answer_Table.id_answer
                            .is(id_answer_fk)).querySingle();
        }
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
        this.id_answer_fk = (answer!=null)?answer.getId_answer():null;
    }

    public void setAnswer(Long id_answer){
        this.id_answer_fk = id_answer;
        this.answer = null;
    }

    public CompositeScore getCompositeScore() {
        if(compositeScore ==null){
            if(id_composite_score_fk==null) return null;
            compositeScore = new Select()
                    .from(CompositeScore.class)
                    .where(CompositeScore_Table.id_composite_score
                            .is(id_composite_score_fk)).querySingle();
        }
        return compositeScore;
    }

    public void setCompositeScore(CompositeScore compositeScore) {
        this.compositeScore = compositeScore;
        this.id_composite_score_fk = (compositeScore!=null)?compositeScore.getId_composite_score():null;
    }

    public void setCompositeScore(Long id_composite_score){
        this.id_composite_score_fk = id_composite_score;
        this.compositeScore = null;
    }

    public boolean hasParent() {
        if (parent == null) {
            long countChildQuestionRelations = SQLite.selectCountOf().from(QuestionRelation.class)
                    //// FIXME: 11/11/2016
                    //.indexedBy("QuestionRelation_operation")
                    .where(QuestionRelation_Table.id_question_fk.eq(this.getId_question()))
                    .and(QuestionRelation_Table.operation.eq(QuestionRelation.PARENT_CHILD))
                    .count();
            parent = countChildQuestionRelations > 0;
        }
        return parent;
    }

    /**
     * Tells if this questions triggers a match
     */
    public boolean hasAMatchTrigger() {
        if (this.output != Constants.DROPDOWN_LIST) {
            return false;
        }

        if (matchTrigger == null) {
            for (Match match : getMatches()) {
                QuestionRelation questionRelation = match.getQuestionRelation();
                if (questionRelation.getOperation() == QuestionRelation.MATCH) {
                    //questionTriggered=questionRelation.getQuestion();
                    matchTrigger = true;
                    return matchTrigger;
                }
            }
            matchTrigger = false;
        }

        return matchTrigger;
    }

    public List<QuestionRelation> getQuestionRelations() {
        if (questionRelations == null) {
            this.questionRelations = new Select()
                    .from(QuestionRelation.class)
                    //// FIXME: 11/11/2016 https://github
                    // .com/Raizlabs/DBFlow/blob/f0d9e1710205952815db027cb560dd8868f5af0b/usage2
                    // /Indexing.md
                    //.indexedBy("QuestionRelation_id_question")
                    .where(QuestionRelation_Table.id_question_fk
                            .eq(this.getId_question()))
                    .queryList();
        }
        return this.questionRelations;
    }

    public boolean hasQuestionRelations() {
        return !this.getQuestionRelations().isEmpty();
    }

    public List<Match> getMatches() {
        if (matches == null) {

            matches = new Select().from(Match.class).as(matchName)
                    .join(QuestionOption.class, Join.JoinType.LEFT_OUTER).as(questionOptionName)
                    .on(Match_Table.id_match.withTable(matchAlias)
                                    .eq(QuestionOption_Table.id_match_fk.withTable(questionOptionAlias)))
                    .where(QuestionOption_Table.id_question_fk.withTable(questionOptionAlias).eq(this.getId_question())).queryList();
        }
        return matches;
    }

    public List<Question> getChildren() {
        if (this.children == null) {

            //No matches no children
            List<Match> matches = getMatches();
            if (matches.size() == 0) {
                this.children = new ArrayList<>();
                return this.children;
            }

            Iterator<Match> matchesIterator = matches.iterator();
            In in = Match_Table.id_match.withTable(matchAlias)
                    .in(matchesIterator.next().getId_match());
            while (matchesIterator.hasNext()) {
                in.and(Long.toString(matchesIterator.next().getId_match()));
            }

            //Select question from questionrelation where operator=1 and id_match in (..)
            this.children = new Select().from(Question.class).as(questionName)
                    //Question + QuestioRelation
                    .join(QuestionRelation.class, Join.JoinType.LEFT_OUTER).as(questionRelationName)
                    .on(Question_Table.id_question.withTable(questionAlias)
                            .eq(QuestionRelation_Table.id_question_fk.withTable(questionRelationAlias)))
                            //+Match
                    .join(Match.class, Join.JoinType.LEFT_OUTER).as(matchName)
                    .on(QuestionRelation_Table.id_question_relation.withTable(questionRelationAlias)
                                    .eq(Match_Table.id_question_relation_fk.withTable(matchAlias)))
                            //Parent child relationship
                    .where(in)
                    //In clause
                    .and(QuestionRelation_Table.operation.withTable(questionRelationAlias).eq(
                            QuestionRelation.PARENT_CHILD)).queryList();
        }
        return this.children;
    }

    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "values")
    public List<Value> getValues() {
        if (values == null) {
            values = new Select()
                    .from(Value.class)
                    .where(Value_Table.id_question_fk
                            .eq(this.getId_question())).queryList();
        }
        return values;
    }

    /**
     * Gets the value of this question in the current survey in session
     */
    public Value getValueBySession(String module) {
        return this.getValueBySurvey(Session.getSurveyByModule(module).getId_survey());
    }

    /**
     * Gets the value of this question in the given Survey
     */
    public Value getValueBySurvey(float idSurvey) {
        List<Value> returnValues = new Select().from(Value.class)
                //// FIXME: 11/11/2016
                //.indexedBy("Value_id_survey")
                .where(Value_Table.id_question_fk.eq(this.getId_question()))
                .and(Value_Table.id_survey_fk.eq((long)idSurvey)).queryList();

        if (returnValues.size() == 0) {
            return null;
        } else {
            return returnValues.get(0);
        }
    }

    /**
     * Gets the option of this question in the current survey in session
     */
    public Option getOptionBySession(String module) {
        return this.getOptionBySurvey(Session.getSurveyByModule(module).getId_survey());
    }

    /**
     * Gets the option of this question in the given survey
     */
    public Option getOptionBySurvey(float idSurvey) {

        Value value = this.getValueBySurvey(idSurvey);
        if (value == null) {
            return null;
        }

        return value.getOption();
    }


    /**
     * Returns true if the question belongs to a Custom Tab
     */
    public boolean belongsToCustomTab() {
        return this.row != null || this.column != null;
    }

    /**
     * Returns true if this question is a title of a custom Tab table
     */
    public boolean isCustomTabTableHeader() {
        return this.row != null && this.row == 0;
    }

    /**
     * Returns true if this question starts a new row
     */
    public boolean isCustomTabNewRow() {
        return this.column != null && this.column == 1;
    }

    /**
     * Checks if this question is shown according to the values of the given survey
     */
    public boolean isHiddenBySurvey(float idSurvey) {

        //No question relations
        if (!hasParent()) {
            return false;
        }

        int hasParentOptionActivated = numberOfActiveParents(idSurvey);

        //Parent with the right value -> not hidden
        return hasParentOptionActivated > 0 ? false : true;
    }

    public List<Value> parentSavedValues(Float idSurvey){

        List<Value> values = new Select().from(Value.class).as(valueName)
                .join(QuestionOption.class, Join.JoinType.LEFT_OUTER).as(questionOptionName)
                .on(Value_Table.id_question_fk.withTable(valueAlias)
                                .eq(QuestionOption_Table.id_question_fk.withTable(questionOptionAlias)),
                        Value_Table.id_option_fk.withTable(valueAlias)
                                .eq(QuestionOption_Table.id_option_fk.withTable(questionOptionAlias)))
                .join(Match.class, Join.JoinType.LEFT_OUTER).as(matchName)
                .on(QuestionOption_Table.id_match_fk.withTable(questionOptionAlias)
                                .eq(Match_Table.id_match.withTable(matchAlias)))
                .join(QuestionRelation.class, Join.JoinType.LEFT_OUTER).as(questionRelationName)
                .on(Match_Table.id_question_relation_fk.withTable(matchAlias)
                                .eq(QuestionRelation_Table.id_question_relation.withTable(questionRelationAlias)))
                //Parent child relationship
                .where(QuestionRelation_Table.operation.withTable(questionRelationAlias).eq(1))
                //For the given survey
                .and(Value_Table.id_survey_fk.withTable(valueAlias).eq(idSurvey.longValue()))
                //The child question in the relationship is 'this'
                .and(QuestionRelation_Table.id_question_fk.withTable(questionRelationAlias).eq(this.getId_question()))
                .queryList();
        return values;
    }

    /**
     * Returns the number of parents that shown this questions
     */
    public int numberOfActiveParents(float idSurvey) {
        //No question relations
        if (!hasParent()) {
            return 0;
        }
        long numberOfParentOptionActivated = SQLite.selectCountOf().from(Value.class).as(valueName)
                .join(QuestionOption.class, Join.JoinType.LEFT_OUTER).as(questionOptionName)
                .on(Value_Table.id_question_fk.withTable(valueAlias)
                                .eq(QuestionOption_Table.id_question_fk.withTable(questionOptionAlias))

                        ,Value_Table.id_option_fk.withTable(valueAlias)
                                .eq(QuestionOption_Table.id_option_fk.withTable(questionOptionAlias)))
                .join(Match.class, Join.JoinType.LEFT_OUTER).as(matchName)
                .on(QuestionOption_Table.id_match_fk.withTable(questionOptionAlias)
                                .eq(Match_Table.id_match.withTable(matchAlias)))
                .join(QuestionRelation.class, Join.JoinType.LEFT_OUTER).as(questionRelationName)
                .on(Match_Table.id_question_relation_fk.withTable(matchAlias)
                                .eq(QuestionRelation_Table.id_question_relation.withTable(questionRelationAlias)))
                //Parent child relationship
                .where(QuestionRelation_Table.operation.eq(1))
                //For the given survey
                .and(Value_Table.id_survey_fk.withTable(valueAlias)
                        .eq((long)idSurvey))
                //The child question in the relationship is 'this'
                .and(QuestionRelation_Table.id_question_fk.withTable(questionRelationAlias)
                        .eq(this.getId_question()))
                .count();
        return (int) numberOfParentOptionActivated;
    }

    /**
     * Checks if a unique question UID is shown according to the values of the given survey
     */
    public static boolean isHiddenQuestionByUidAndSurvey(String uid, Long id_survey) {
        //get question by uid
        Question question = getQuestionByUid(uid);
        if (question == null) {
            return false;
        }

        return question.isHiddenBySurvey(id_survey);
    }

    /**
     * Add register to ScoreRegister if this is an scored question
     *
     * @return List</Float> {num, den}
     */
    public List<Float> initScore(float idSurvey, String module) {
        if (!this.isScored()) {
            return null;
        }

        Float num = ScoreRegister.calcNum(this, idSurvey);
        //If the num is null the question is ignored
        if (num == null) {
            return null;
        }
        Float denum = ScoreRegister.calcDenum(this, idSurvey);
        ScoreRegister.addRecord(this, num, denum, idSurvey, module);
        return Arrays.asList(num, denum);
    }

    /**
     * Counts the number of required questions (without a parent question).
     */
    public static int countRequiredByProgram(Program program) {
        if (program == null || program.getId_program() == null) {
            return 0;
        }

        // Count all the quesions that may have an answer
        long totalAnswerableQuestions = SQLite.selectCountOf()
                .from(Question.class).as(questionName)
                .join(Header.class, Join.JoinType.LEFT_OUTER).as(headerName)
                .on(Question_Table.id_header_fk.withTable(questionAlias)
                        .eq(Header_Table.id_header.withTable(headerAlias)))
                .join(Tab.class, Join.JoinType.LEFT_OUTER).as(tabName)
                .on(Header_Table.id_tab_fk.withTable(headerAlias)
                        .eq(Tab_Table.id_tab.withTable(tabAlias)))
                .where(Question_Table.output.withTable(questionAlias).isNot(Constants.NO_ANSWER))
                .and(Tab_Table.id_program_fk.withTable(tabAlias).eq(program.getId_program())).count();

        // Count children questions from the given taggroup
        long numChildrenQuestion = SQLite.selectCountOf()
                .from(QuestionRelation.class).as(questionRelationName)
                .join(Question.class, Join.JoinType.LEFT_OUTER).as(questionName)
                .on(QuestionRelation_Table.id_question_fk.withTable(questionRelationAlias)
                        .eq(Question_Table.id_question.withTable(questionAlias)))
                .join(Header.class, Join.JoinType.LEFT_OUTER).as(headerName)
                .on(Question_Table.id_header_fk.withTable(questionAlias)
                        .eq(Header_Table.id_header.withTable(headerAlias)))
                .join(Tab.class, Join.JoinType.LEFT_OUTER).as(tabName)
                .on(Header_Table.id_tab_fk.withTable(headerAlias)
                        .eq(Tab_Table.id_tab.withTable(tabAlias)))
                .where(Question_Table.output.withTable(questionAlias).isNot(Constants.NO_ANSWER))
                .and(Tab_Table.id_program_fk.withTable(tabAlias).eq(program.getId_program()))
                .and(QuestionRelation_Table.operation.withTable(questionRelationAlias).eq(Constants.OPERATION_TYPE_PARENT)).count();

        // Return number of parents (total - children)
        return (int) (totalAnswerableQuestions - numChildrenQuestion);
    }

    /**
     * Counts the number of compulsory questions (without a parent question).
     */
    public static int countCompulsoryByProgram(Program program) {
        if (program == null || program.getId_program() == null) {
            return 0;
        }

        // Count all the quesions that may have an answer
        long totalAnswerableQuestions = SQLite.selectCountOf()
                .from(Question.class).as(questionName)
                .join(Header.class, Join.JoinType.LEFT_OUTER).as(headerName)
                .on(Question_Table.id_header_fk.withTable(questionAlias)
                        .eq(Header_Table.id_header.withTable(headerAlias)))
                .join(Tab.class, Join.JoinType.LEFT_OUTER).as(tabName)
                .on(Header_Table.id_tab_fk.withTable(headerAlias)
                        .eq(Tab_Table.id_tab.withTable(tabAlias)))
                .where(Question_Table.compulsory.withTable(questionAlias).is(true))
                .and(Tab_Table.id_program_fk.withTable(tabAlias).eq(program.getId_program())).count();

        List<Question> questionsChild = new Select()
                .from(Question.class).as(questionName)
                .join(QuestionRelation.class, Join.JoinType.LEFT_OUTER).as(questionRelationName)
                .on(QuestionRelation_Table.id_question_fk.withTable(questionRelationAlias)
                        .eq(Question_Table.id_question.withTable(questionAlias)))
                .join(Header.class, Join.JoinType.LEFT_OUTER).as(headerName)
                .on(Question_Table.id_header_fk.withTable(questionAlias)
                        .eq(Header_Table.id_header.withTable(headerAlias)))
                .join(Tab.class, Join.JoinType.LEFT_OUTER).as(tabName)
                .on(Header_Table.id_tab_fk.withTable(headerAlias)
                        .eq(Tab_Table.id_tab.withTable(tabAlias)))
                .where(Question_Table.compulsory.withTable(questionAlias).is(true))
                .and(Tab_Table.id_program_fk.withTable(tabAlias).eq(program.getId_program()))
                .and(QuestionRelation_Table.operation.withTable(questionRelationAlias)
                        .eq(Constants.OPERATION_TYPE_PARENT)).groupBy(  Question_Table.uid_question.withTable(questionAlias)).queryList();
        // Return number of parents (total - children)
        return (int) (totalAnswerableQuestions - questionsChild.size());
    }

    /**
     * Gets all the children compulsory questions, and returns the  number of active children
     */
    public static int countChildrenCompulsoryBySurvey(Long id_survey, SurveyFragment.Callback callback) {
        int numActiveChildrens=0;
        //This query returns a list of children compulsory questions
        // But the id_question is not correct, because is the the parent id_questions from the questionOption relation.
        List<Question> questions =new Select().from(Question.class).as(questionName)
                .join(QuestionRelation.class, Join.JoinType.LEFT_OUTER).as(questionRelationName)
                .on(Question_Table.id_question.withTable(questionAlias)
                                .eq(QuestionRelation_Table.id_question_fk.withTable(questionRelationAlias)))
                .join(Match.class, Join.JoinType.LEFT_OUTER).as(matchName)
                .on(QuestionRelation_Table.id_question_relation.withTable(questionRelationAlias)
                                .eq(Match_Table.id_question_relation_fk.withTable(matchAlias)))
                .join(QuestionOption.class, Join.JoinType.LEFT_OUTER).as(questionOptionName)
                .on(Match_Table.id_match.withTable(matchAlias)
                                .eq(QuestionOption_Table.id_match_fk.withTable(questionOptionAlias)))
                .join(Value.class, Join.JoinType.LEFT_OUTER).as(valueName)
                .on(Value_Table.id_question_fk.withTable(valueAlias)
                                .eq(QuestionOption_Table.id_question_fk.withTable(questionOptionAlias)),
                        Value_Table.id_option_fk.withTable(valueAlias)
                                .eq(QuestionOption_Table.id_option_fk.withTable(questionOptionAlias)))
                //Parent Child relationship
                .where(QuestionRelation_Table.operation.withTable(questionRelationAlias).eq(1))
                //For the given survey
                .and(Value_Table.id_survey_fk.withTable(valueAlias).eq(id_survey.longValue()))
                //The child question requires an answer
                .and(Question_Table.output.withTable(questionAlias).isNot(Constants.NO_ANSWER))
                .and(Question_Table.compulsory.withTable(questionAlias).eq(true)).queryList();

        //checks if the children questions are active by UID
        // Note: the question id_question is wrong because dbflow query overwrites the children id_question with the parent id_question.
        for(int i=0; i<questions.size();i++) {
            if(questions.get(i).getCompulsory() && !Question.isHiddenQuestionByUidAndSurvey(questions.get(i).getUid(), id_survey)) {
                numActiveChildrens++;
            }
            if(i == (Math.round((Float.parseFloat(questions.size()+"")*0.25)))
                    || i == (Math.round((Float.parseFloat(questions.size()+"")*0.5)))
                    || i == (Math.round((Float.parseFloat(questions.size()+"")*0.75)))){
                if (callback != null) {
                    callback.nextProgressMessage();
                }
            }
        }
        // Return number of active compulsory children
        return numActiveChildrens;
    }


    /**
     * Returns a Question by a UID
     */
    public static Question getQuestionByUid(String uid) {
        return new Select().from(Question.class).where(Question_Table.uid_question.eq(uid)).querySingle();
    }


    /**
     * Checks if this question is triggered according to the current values of the given survey.
     * Only applies to question with answers DROPDOWN_DISABLED
     */
    public boolean isTriggered(float idSurvey) {

        //Only disabled dropdowns
        if (this.getOutput() != Constants.DROPDOWN_LIST_DISABLED) {
            return false;
        }

        //Find questionoptions for q1 and q2 and check same match
        List<QuestionOption> questionOptions = new Select().from(QuestionOption.class).as(questionOptionName)
                .join(Match.class, Join.JoinType.LEFT_OUTER).as(matchName)
                .on(QuestionOption_Table.id_match_fk.withTable(questionOptionAlias).eq(Match_Table.id_match.withTable(matchAlias)))
                .join(QuestionRelation.class, Join.JoinType.LEFT_OUTER).as(questionRelationName)
                .on(Match_Table.id_question_relation_fk.withTable(matchAlias).eq(QuestionRelation_Table.id_question_relation.withTable(questionRelationAlias)))

                .join(Value.class, Join.JoinType.LEFT_OUTER).as(valueName)
                .on(Value_Table.id_question_fk.withTable(valueAlias)
                                .eq(QuestionOption_Table.id_question_fk.withTable(questionOptionAlias)),
                        Value_Table.id_option_fk.withTable(valueAlias)
                                .eq(QuestionOption_Table.id_option_fk.withTable(questionOptionAlias)))
                .where(Value_Table.id_survey_fk.withTable(valueAlias).eq((long) idSurvey))
                .and(QuestionRelation_Table.id_question_fk.withTable(questionRelationAlias).eq(this.getId_question()))
                .and(QuestionRelation_Table.operation.withTable(questionRelationAlias).eq(Constants.OPERATION_TYPE_MATCH))
                .queryList();

        //No values no match
        if (questionOptions.size() != 2) {
            return false;
        }

        //Match is triggered if questionoptions have same matchid
        long idmatchQ1 = questionOptions.get(0).getMatch().getId_match();
        long idmatchQ2 = questionOptions.get(1).getMatch().getId_match();
        return idmatchQ1 == idmatchQ2;

    }

    /**
     * Returns all the questions that belongs to a program
     */
    public static List<Question> listByProgram(Program program) {
        if (program == null || program.getId_program() == null) {
            return new ArrayList();
        }


        //return Question.findWithQuery(Question.class, LIST_ALL_BY_PROGRAM, program.getId()
        // .toString());


        return new Select().from(Question.class).as(questionName)
                .join(Header.class, Join.JoinType.LEFT_OUTER).as(headerName)
                .on(Question_Table.id_header_fk.withTable(questionAlias)
                        .eq(Header_Table.id_header.withTable(headerAlias)))
                .join(Tab.class, Join.JoinType.LEFT_OUTER).as(tabName)
                .on(Header_Table.id_tab_fk.withTable(headerAlias)
                        .eq(Tab_Table.id_tab.withTable(tabAlias)))
                .where(Tab_Table.id_program_fk.withTable(tabAlias)
                        .eq(program.getId_program()))
                .orderBy(OrderBy.fromProperty(Tab_Table.order_pos.withTable(tabAlias)))
                .orderBy(OrderBy.fromProperty(
                        Question_Table.order_pos.withTable(questionAlias))).queryList();

    }

    public static List<Question> listAllByTabsWithoutCs(List<Tab> tabs) {

        if (tabs == null || tabs.size() == 0) {
            return new ArrayList();
        }

        Iterator<Tab> iterator = tabs.iterator();
        In in = Tab_Table.id_tab.withTable(tabAlias).in(iterator.next().getId_tab());
        while (iterator.hasNext()) {
            in.and(Long.toString(iterator.next().getId_tab()));
        }

        return new Select().from(Question.class).as(questionName)
                .join(Header.class, Join.JoinType.LEFT_OUTER).as(headerName)
                .on(Question_Table.id_header_fk.withTable(questionAlias)
                        .eq(Header_Table.id_header.withTable(headerAlias)))
                .join(Tab.class, Join.JoinType.LEFT_OUTER).as(tabName)
                .on(Header_Table.id_tab_fk.withTable(headerAlias)
                        .eq(Tab_Table.id_tab.withTable(tabAlias)))
                .where(in)
                .and(Question_Table.id_composite_score_fk.withTable(questionAlias).isNull())
                .orderBy(OrderBy.fromProperty(Tab_Table.order_pos.withTable(tabAlias)))
                .orderBy(OrderBy.fromProperty(
                        Question_Table.order_pos.withTable(questionAlias))).queryList();
    }

    public static List<Question> listAllByTabs(List<Tab> tabs) {

        if (tabs == null || tabs.size() == 0) {
            return new ArrayList();
        }

        Iterator<Tab> iterator = tabs.iterator();
        In in = Tab_Table.id_tab.withTable(tabAlias).in(iterator.next().getId_tab());
        while (iterator.hasNext()) {
            in.and(Long.toString(iterator.next().getId_tab()));
        }

        return new Select().from(Question.class).as(questionName)
                .join(Header.class, Join.JoinType.LEFT_OUTER).as(headerName)
                .on(Question_Table.id_header_fk.withTable(headerAlias)
                        .eq(Header_Table.id_header.withTable(headerAlias)))
                .join(Tab.class, Join.JoinType.LEFT_OUTER).as(tabName)
                .on(Header_Table.id_tab_fk.withTable(headerAlias)
                        .eq(Tab_Table.id_tab.withTable(tabAlias)))
                .where(in)
                .orderBy(OrderBy.fromProperty(Tab_Table.order_pos.withTable(tabAlias)))
                .orderBy(Question_Table.order_pos.withTable(questionAlias), true).queryList();
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
        if (de_name != null ? !de_name.equals(question.de_name) : question.de_name != null) {
            return false;
        }
        if (short_name != null ? !short_name.equals(question.short_name)
                : question.short_name != null) {
            return false;
        }
        if (form_name != null ? !form_name.equals(question.form_name)
                : question.form_name != null)
            return false;
        if (uid_question != null ? !uid_question.equals(question.uid_question) : question.uid_question != null) return false;
        if (order_pos != null ? !order_pos.equals(question.order_pos) : question.order_pos != null)
            return false;
        if (numerator_w != null ? !numerator_w.equals(question.numerator_w) : question.numerator_w != null)
            return false;
        if (denominator_w != null ? !denominator_w.equals(question.denominator_w) : question.denominator_w != null)
            return false;
        if (feedback != null ? !feedback.equals(question.feedback) : question.feedback != null)
            return false;
        if (id_header_fk != null ? !id_header_fk.equals(question.id_header_fk) : question.id_header_fk != null)
            return false;
        if (id_answer_fk != null ? !id_answer_fk.equals(question.id_answer_fk) : question.id_answer_fk != null)
            return false;
        if (output != null ? !output.equals(question.output) : question.output != null)
            return false;
        if (compulsory != null ? !compulsory.equals(question.compulsory) : question.compulsory != null)
            return false;
        return !(id_composite_score_fk != null ? !id_composite_score_fk.equals(question.id_composite_score_fk) : question.id_composite_score_fk != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_question ^ (id_question >>> 32));
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (de_name != null ? de_name.hashCode() : 0);
        result = 31 * result + (short_name != null ? short_name.hashCode() : 0);
        result = 31 * result + (form_name != null ? form_name.hashCode() : 0);
        result = 31 * result + (uid_question != null ? uid_question.hashCode() : 0);
        result = 31 * result + (order_pos != null ? order_pos.hashCode() : 0);
        result = 31 * result + (numerator_w != null ? numerator_w.hashCode() : 0);
        result = 31 * result + (denominator_w != null ? denominator_w.hashCode() : 0);
        result = 31 * result + (feedback != null ? feedback.hashCode() : 0);
        result = 31 * result + (id_header_fk != null ? id_header_fk.hashCode() : 0);
        result = 31 * result + (id_answer_fk != null ? id_answer_fk.hashCode() : 0);
        result = 31 * result + (output != null ? output.hashCode() : 0);
        result = 31 * result + (compulsory != null ? compulsory.hashCode() : 0);
        result = 31 * result + (id_composite_score_fk != null ? id_composite_score_fk.hashCode() : 0);
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
                ", uid_question='" + uid_question + '\'' +
                ", order_pos=" + order_pos +
                ", numerator_w=" + numerator_w +
                ", feedback='" + feedback + '\'' +
                ", denominator_w=" + denominator_w +
                ", id_header=" + id_header_fk +
                ", id_answer=" + id_answer_fk +
                ", output=" + output +
                ", compulsory=" + compulsory +
                ", id_composite_score=" + id_composite_score_fk +
                '}';
    }
}
