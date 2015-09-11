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

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
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
public class Question extends BaseModel{

    /**
     * Sql query that counts required questions in a program (required for % stats)
     */
    /*private static final String LIST_REQUIRED_BY_PROGRAM =
            "select q.* from question q"+
            " left join answer a on q.answer=a.id"+
            " left join header h on q.header=h.id"+
            " left join tab t on h.tab=t.id"+
            " left join program p on t.program=p.id"+
            " where q.question=0"+
            " and a.output<>"+ Constants.NO_ANSWER+
            " and p.id=?";*/

    /*private static final String LIST_ALL_BY_PROGRAM =
            "select q.* from question q"+
            " left join header h on q.header=h.id"+
            " left join tab t on h.tab=t.id"+
            " left join program p on t.program=p.id"+
            " and p.id=? order by t.orderpos, q.orderpos";*/

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
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_header",
            columnType = Long.class,
            foreignColumnName = "id_header")},
            saveForeignKeyModel = false)
    Header header;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_answer",
            columnType = Long.class,
            foreignColumnName = "id_answer")},
            saveForeignKeyModel = false)
    Answer answer;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_parent",
            columnType = Long.class,
            foreignColumnName = "id_question")},
            saveForeignKeyModel = false)
    Question question;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_composite_score",
            columnType = Long.class,
            foreignColumnName = "id_composite_score")},
            saveForeignKeyModel = false)
    CompositeScore compositeScore;

    List<Question> children;

    List<Value> values;

    List<QuestionRelation> questionRelations;

    public Question() {
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
        this.header = header;
        this.answer = answer;
        this.question = question;
        this.compositeScore = compositeScore;
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

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public CompositeScore getCompositeScore() { return compositeScore; }

    public void setCompositeScore(CompositeScore compositeScore) { this.compositeScore = compositeScore; }

    public boolean hasParent(){
        return getQuestion() != null;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "questionRelations")
    public List<QuestionRelation> getQuestionRelations() {
        //if (this.children == null){
        this.questionRelations = new Select().from(QuestionRelation.class)
                .where(Condition.column(QuestionRelation$Table.QUESTION_ID_QUESTION).eq(this.getId_question()))
                .queryList();
        //}
        return this.questionRelations;
    }

   public boolean hasQuestionRelations(){
       return !this.getQuestionRelations().isEmpty();
   }

    public List<QuestionOption> getQuestionOption() {
        //if (this.children == null){
        return new Select().from(QuestionOption.class)
                .where(Condition.column(QuestionOption$Table.QUESTION_ID_QUESTION).eq(this.getId_question()))
                .queryList();
        //}
    }

    public boolean hasQuestionOption(){
        return !this.getQuestionOption().isEmpty();
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "children")
    public List<Question> getChildren() {
        if (this.children == null){
            this.children = new Select().from(Question.class)
                    .where(Condition.column(Question$Table.QUESTION_ID_PARENT).eq(this.getId_question()))
                    .orderBy(Question$Table.ORDER_POS).queryList();
        }
        return this.children;
    }

    public boolean hasChildren(){
        return !getChildren().isEmpty();
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "values")
    public List<Value> getValues(){
        return new Select().from(Value.class).where(Condition.column(Value$Table.QUESTION_ID_QUESTION).eq(this.getId_question())).queryList();
    }

    /**
     * Gets the value of this question in the current survey in session
     * @return
     */
    public Value getValueBySession(){
        return this.getValueBySurvey(Session.getSurvey());
    }

    /**
     * Gets the value of this question in the given Survey
     * @param survey
     * @return
     */
    public Value getValueBySurvey(Survey survey){
        if(survey==null){
            return null;
        }
        List<Value> returnValues = new Select().from(Value.class)
                .where(Condition.column(Value$Table.QUESTION_ID_QUESTION).eq(this.getId_question()))
                .and(Condition.column(Value$Table.SURVEY_ID_SURVEY).eq(survey.getId_survey())).queryList();

        if (returnValues.size() == 0){
            return null;
        }else{
            return returnValues.get(0);
        }
    }

    /**
     * Gets the option of this question in the current survey in session
     * @return
     */
    public Option getOptionBySession(){
        return this.getOptionBySurvey(Session.getSurvey());
    }

    /**
     * Gets the option of this question in the given survey
     * @param survey
     * @return
     */
    public Option getOptionBySurvey(Survey survey){
        if(survey==null){
            return null;
        }

        Value value = this.getValueBySurvey(survey);
        if(value==null){
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
     * @param survey
     * @return
     */
    public boolean isHiddenBySurvey(Survey survey){
        Question parent=this.getQuestion();

        //There is a parent question and it is not answered and it is not a phantom question
        if (parent!= null && parent.getValueBySurvey(survey)==null && !parent.belongsToCustomTab()) {
            return true;
        }

        return false;
    }

    /**
     * Add register to ScoreRegister if this is an scored question
     * @return List</Float> {num, den}
     */
    public List<Float> initScore(Survey survey) {
        if (!this.isScored()){
            return null;
        }

        Float num = ScoreRegister.calcNum(this, survey);
        Float denum = ScoreRegister.calcDenum(this, survey);
        ScoreRegister.addRecord(this, num, denum);
        return Arrays.asList(num, denum);
    }

    /**
     * Counts the number of required questions (without a parent question).
     * @param tabGroup
     * @return
     */
    public static int countRequiredByProgram(TabGroup tabGroup){
        if(tabGroup==null || tabGroup.getId_tab_group()==null){
            return 0;
        }

        /**
         * Sql query that counts required questions in a program (required for % stats)
         */
        return (int) new Select().count().from(Question.class).as("q")
                .join(Answer.class, Join.JoinType.LEFT).as("a")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.ANSWER_ID_ANSWER))
                        .eq(ColumnAlias.columnWithTable("a", Answer$Table.ID_ANSWER)))
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.HEADER_ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID_HEADER)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.TAB_ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)))
                .join(TabGroup.class, Join.JoinType.LEFT).as("g")
                .on(Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.TABGROUP_ID_TAB_GROUP))
                        .eq(ColumnAlias.columnWithTable("g", TabGroup$Table.ID_TAB_GROUP)))
                .join(Program.class, Join.JoinType.LEFT).as("p")
                .on(Condition.column(ColumnAlias.columnWithTable("g", TabGroup$Table.PROGRAM_ID_PROGRAM))
                        .eq(ColumnAlias.columnWithTable("p", Program$Table.ID_PROGRAM)))
                .where(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.QUESTION_ID_PARENT)).isNull())
                .and(Condition.column(ColumnAlias.columnWithTable("a", Answer$Table.OUTPUT)).isNot(Constants.NO_ANSWER))
                .and(Condition.column(ColumnAlias.columnWithTable("g", TabGroup$Table.ID_TAB_GROUP)).eq(tabGroup.getId_tab_group())).count();
    }

    /**
     * Returns all the questions that belongs to a program
     * @param tabGroup
     * @return
     */
    public static List<Question> listByTabGroup(TabGroup tabGroup){
        if(tabGroup==null || tabGroup.getId_tab_group()==null){
            return new ArrayList();
        }


        //return Question.findWithQuery(Question.class, LIST_ALL_BY_PROGRAM, program.getId().toString());


                return new Select().all().from(Question.class).as("q")
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.HEADER_ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID_HEADER)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.TAB_ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)))
                .join(TabGroup.class, Join.JoinType.LEFT).as("tg")
                .on(Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.TABGROUP_ID_TAB_GROUP))
                        .eq(ColumnAlias.columnWithTable("tg", TabGroup$Table.ID_TAB_GROUP)))
                .where(Condition.column(ColumnAlias.columnWithTable("tg", TabGroup$Table.ID_TAB_GROUP))
                        .eq(tabGroup.getId_tab_group()))
                .orderBy(Tab$Table.ORDER_POS)
                .orderBy(Question$Table.ORDER_POS).queryList();

    }

    public static List<Question> listAllByTabs(List<Tab> tabs){

        if(tabs==null || tabs.size()==0){
            return new ArrayList();
        }

        Iterator<Tab> iterator=tabs.iterator();
        In in = Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)).in(Long.toString(iterator.next().getId_tab()));
        while (iterator.hasNext()) {
            in.and(Long.toString(iterator.next().getId_tab()));
        }

        return new Select().from(Question.class).as("q")
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.HEADER_ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID_HEADER)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.TAB_ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID_TAB)))
                .where(in)
                .orderBy(Tab$Table.ORDER_POS)
                .orderBy(Question$Table.ORDER_POS).queryList();
    }



    /**
     * Checks if this question is scored or not.
     * @return true|false
     */
    public boolean isScored(){
        try {
            Integer output=getAnswer().getOutput();
            return  output == Constants.DROPDOWN_LIST ||
                    output == Constants.RADIO_GROUP_HORIZONTAL ||
                    output == Constants.RADIO_GROUP_VERTICAL;
        }catch(Exception e){
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question1 = (Question) o;

        if (id_question != question1.id_question) return false;
        if (answer != null ? !answer.equals(question1.answer) : question1.answer != null)
            return false;
        if (code != null ? !code.equals(question1.code) : question1.code != null) return false;
        if (compositeScore != null ? !compositeScore.equals(question1.compositeScore) : question1.compositeScore != null)
            return false;
        if (de_name != null ? !de_name.equals(question1.de_name) : question1.de_name != null)
            return false;
        if (denominator_w != null ? !denominator_w.equals(question1.denominator_w) : question1.denominator_w != null)
            return false;
        if (form_name != null ? !form_name.equals(question1.form_name) : question1.form_name != null)
            return false;
        if (header != null ? !header.equals(question1.header) : question1.header != null)
            return false;
        if (numerator_w != null ? !numerator_w.equals(question1.numerator_w) : question1.numerator_w != null)
            return false;
        if (order_pos != null ? !order_pos.equals(question1.order_pos) : question1.order_pos != null)
            return false;
        if (question != null ? !question.equals(question1.question) : question1.question != null)
            return false;
        if (short_name != null ? !short_name.equals(question1.short_name) : question1.short_name != null)
            return false;
        if (uid != null ? !uid.equals(question1.uid) : question1.uid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id_question ^ (id_question >>> 32));
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (de_name != null ? de_name.hashCode() : 0);
        result = 31 * result + (short_name != null ? short_name.hashCode() : 0);
        result = 31 * result + (form_name != null ? form_name.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + order_pos.hashCode();
        result = 31 * result + (numerator_w != null ? numerator_w.hashCode() : 0);
        result = 31 * result + (denominator_w != null ? denominator_w.hashCode() : 0);
        result = 31 * result + header.hashCode();
        result = 31 * result + (answer != null ? answer.hashCode() : 0);
        result = 31 * result + (question != null ? question.hashCode() : 0);
        result = 31 * result + (compositeScore != null ? compositeScore.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id_question + '\'' +
                ", code='" + code + '\'' +
                ", de_name='" + de_name + '\'' +
                ", short_name='" + short_name + '\'' +
                ", form_name='" + form_name + '\'' +
                ", uid='" + uid + '\'' +
                ", order_pos=" + order_pos +
                ", numerator_w=" + numerator_w +
                ", denominator_w=" + denominator_w +
                ", header=" + header +
                ", answer=" + answer +
                ", question=" + question +
                ", compositeScore=" + compositeScore +
                '}';
    }
}
