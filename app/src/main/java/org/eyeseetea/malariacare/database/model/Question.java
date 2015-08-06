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
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Question extends BaseModel{

    /**
     * Sql query that counts required questions in a program (required for % stats)
     */
    private static final String LIST_REQUIRED_BY_PROGRAM ="select q.* from question q"+
            " left join answer a on q.answer=a.id"+
            " left join header h on q.header=h.id"+
            " left join tab t on h.tab=t.id"+
            " left join program p on t.program=p.id"+
            " where q.question=0"+
            " and a.output<>"+ Constants.NO_ANSWER+
            " and p.id=?";

    private static final String LIST_ALL_BY_PROGRAM ="select q.* from question q"+
            " left join header h on q.header=h.id"+
            " left join tab t on h.tab=t.id"+
            " left join program p on t.program=p.id"+
            " and p.id=? order by t.orderpos, q.orderpos";

    private static final String LIST_ALL_BY_TABS ="select q.* from question q"+
            " left join header h on q.header=h.id"+
            " left join tab t on h.tab=t.id"+
            " and t.id in (?) order by t.orderpos, q.orderpos";

    @Column
    @PrimaryKey(autoincrement = true)
    Long id;
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
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    Header header;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_answer",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    Answer answer;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_parent",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    Question question;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_composite_score",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    CompositeScore compositeScore;

    List<Question> children;

    List<Question> relatives;

    List<Question> master;

    List<Value> values;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "children")
    public List<Question> getChildren() {
        if (this.children == null){
            this.children = new Select().from(Question.class)
                    .where(Condition.column(Question$Table.QUESTION_ID_PARENT).is(this.getId()))
                    .orderBy(Question$Table.ORDER_POS).queryList();
        }
        return this.children;
    }

    public List<Question> getRelatives() {
        if (this.relatives == null) {
            this.relatives = new Select().from(Question.class).where(Condition.column(Question$Table.ID)
                    .in(new Select(QuestionRelation$Table.RELATIVE_RELATIVE).from(QuestionRelation.class)
                            .where(Condition.column(QuestionRelation$Table.MASTER_MASTER).is(this.getId())))).queryList();
            //this.relatives = Question.findWithQuery(Question.class, "Select * from Question" +
            //        " where id in (Select relative from Question_Relation where master =" + this.getId() + ")");
       }
        return this.relatives;
    }


    public List<Question> getMasters() {
        if (this.master == null) {
            this.master = new Select().from(Question.class).where(Condition.column(Question$Table.ID)
                    .in(new Select(QuestionRelation$Table.MASTER_MASTER).from(QuestionRelation.class)
                    .where(Condition.column(QuestionRelation$Table.RELATIVE_RELATIVE).is(this.getId())))).queryList();
            //this.master = Question.findWithQuery(Question.class, "Select * from Question" +
            //        " where id in (Select master from Question_Relation where relative =" + this.getId() + ")");
        }
        return this.master;
    }

    public boolean belongsToMasterQuestions() {return !getMasters().isEmpty();}

    public boolean hasRelatives() {return !getRelatives().isEmpty(); }

    public boolean hasChildren(){
        return !getChildren().isEmpty();
    }

    @OneToMany(methods = OneToMany.Method.ALL, variableName = "values")
    public List<Value> getValues(){
        return new Select().from(Value.class).where(Condition.column(Value$Table.QUESTION_ID_QUESTION).is(this.getId())).queryList();
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
        String surveyId = String.valueOf(survey.getId());
        String questionId = String.valueOf(this.getId());
        List<Value> returnValues = new Select().from(Value.class)
                .where(Condition.column(Value$Table.QUESTION_ID_QUESTION).is(this.getId()))
                .and(Condition.column(Survey$Table.ID).is(survey.getId())).queryList();
        //List<Value> returnValues = Select.from(Value.class).
        //        where(Condition.prop("question").eq(questionId), Condition.prop("survey").eq(surveyId)).list();

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

    /**
     * Checks if this question is shown according to the values of the given survey
     * @param survey
     * @return
     */
    public boolean isHiddenBySurvey(Survey survey){
        Question parent=this.getQuestion();
        //There is a parent question and it is not answered
        if (parent!= null && parent.getValueBySurvey(survey)==null) {
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

        Float num = ScoreRegister.calcNum(this,survey);
        Float denum = ScoreRegister.calcDenum(this, survey);
        ScoreRegister.addRecord(this, num, denum);
        return Arrays.asList(num, denum);
    }

    /**
     * Counts the number of required questions (without a parent question).
     * @param program
     * @return
     */
    public static int countRequiredByProgram(Program program){
        if(program==null || program.getId()==null){
            return 0;
        }

        //TODO: Implement join
        List<Question> questionsByProgram = Question.findWithQuery(Question.class, LIST_REQUIRED_BY_PROGRAM, program.getId().toString());
        return questionsByProgram.size();
    }

    /**
     * Returns all the questions that belongs to a program
     * @param program
     * @return
     */
    public static List<Question> listAllByProgram(Program program){
        if(program==null || program.getId()==null){
            return new ArrayList<Question>();
        }

        //TODO: Implement join
        return Question.findWithQuery(Question.class, LIST_ALL_BY_PROGRAM, program.getId().toString());
    }

    public static List<Question> listAllByTabs(List<Tab> tabs){

        if(tabs==null || tabs.size()==0){
            return new ArrayList<Question>();
        }
        String tabsAsString="";
        Iterator<Tab> iterator=tabs.iterator();
        while(iterator.hasNext()){
            tabsAsString+="'"+iterator.next().getId().toString()+"'";
            if(iterator.hasNext()){
                tabsAsString+=",";
            }
        }
        //TODO: Implement join
        return Question.findWithQuery(Question.class,LIST_ALL_BY_TABS, tabsAsString);

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
        if (!(o instanceof Question)) return false;

        Question question1 = (Question) o;

        if (!id.equals(question1.id)) return false;
        if (code != null ? !code.equals(question1.code) : question1.code != null) return false;
        if (de_name != null ? !de_name.equals(question1.de_name) : question1.de_name != null)
            return false;
        if (short_name != null ? !short_name.equals(question1.short_name) : question1.short_name != null)
            return false;
        if (form_name != null ? !form_name.equals(question1.form_name) : question1.form_name != null)
            return false;
        if (uid != null ? !uid.equals(question1.uid) : question1.uid != null) return false;
        if (!order_pos.equals(question1.order_pos)) return false;
        if (numerator_w != null ? !numerator_w.equals(question1.numerator_w) : question1.numerator_w != null)
            return false;
        if (denominator_w != null ? !denominator_w.equals(question1.denominator_w) : question1.denominator_w != null)
            return false;
        if (!header.equals(question1.header)) return false;
        if (!answer.equals(question1.answer)) return false;
        if (question != null ? !question.equals(question1.question) : question1.question != null)
            return false;
        return compositeScore.equals(question1.compositeScore);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (de_name != null ? de_name.hashCode() : 0);
        result = 31 * result + (short_name != null ? short_name.hashCode() : 0);
        result = 31 * result + (form_name != null ? form_name.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + order_pos.hashCode();
        result = 31 * result + (numerator_w != null ? numerator_w.hashCode() : 0);
        result = 31 * result + (denominator_w != null ? denominator_w.hashCode() : 0);
        result = 31 * result + header.hashCode();
        result = 31 * result + answer.hashCode();
        result = 31 * result + (question != null ? question.hashCode() : 0);
        result = 31 * result + compositeScore.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
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
