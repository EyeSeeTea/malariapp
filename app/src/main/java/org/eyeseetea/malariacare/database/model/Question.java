package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

public class Question extends SugarRecord<Question> {

    String code;
    String de_name;
    String short_name;
    String form_name;
    String uid;
    Integer order_pos;
    Float numerator_w;
    Float denominator_w;
    Header header;
    Answer answer;
    Question question;
    CompositiveScore compositiveScore;

    @Ignore
    List<Question> _questionChildren;

    public Question() {
    }

    public Question(String code, String de_name, String short_name, String form_name, String uid, Integer order_pos, Float numerator_w, Float denominator_w, Header header, Answer answer, Question question, CompositiveScore compositiveScore) {
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
        this.compositiveScore = compositiveScore;
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

    public CompositiveScore getCompositiveScore() { return compositiveScore; }

    public void setCompositiveScore(CompositiveScore compositiveScore) { this.compositiveScore = compositiveScore; }

    public boolean hasParent(){
        return getQuestion() != null;
    }

    public List<Question> getQuestionChildren() {
        if (this._questionChildren == null){
            this._questionChildren = Select.from(Question.class)
                    .where(Condition.prop("question")
                            .eq(String.valueOf(this.getId())))
                    .orderBy("orderpos").list();
        }
        return this._questionChildren;
    }

    public boolean hasChildren(){
        return !getQuestionChildren().isEmpty();
    }

    public List<Value> getValues(){
        return Value.find(Value.class, "question = ?", String.valueOf(this.getId()));
    }

    // This method returns a value for this question given a survey. We return the first element because this must be unique
    public Value getValue(Survey survey){
        String surveyId = String.valueOf(survey.getId());
        String questionId = String.valueOf(this.getId());
        List<Value> returnValues = Select.from(Value.class)
                .where(Condition.prop("question").eq(questionId),
                        Condition.prop("survey").eq(surveyId)).list();
        if (returnValues.size() == 0) return null;
        else return returnValues.get(0);
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
                ", compositiveScore=" + compositiveScore +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question1 = (Question) o;

        if (answer != null ? !answer.equals(question1.answer) : question1.answer != null)
            return false;
        if (code != null ? !code.equals(question1.code) : question1.code != null) return false;
        if (compositiveScore != null ? !compositiveScore.equals(question1.compositiveScore) : question1.compositiveScore != null)
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
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (de_name != null ? de_name.hashCode() : 0);
        result = 31 * result + (short_name != null ? short_name.hashCode() : 0);
        result = 31 * result + (form_name != null ? form_name.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (order_pos != null ? order_pos.hashCode() : 0);
        result = 31 * result + (numerator_w != null ? numerator_w.hashCode() : 0);
        result = 31 * result + (denominator_w != null ? denominator_w.hashCode() : 0);
        result = 31 * result + (header != null ? header.hashCode() : 0);
        result = 31 * result + (answer != null ? answer.hashCode() : 0);
        result = 31 * result + (question != null ? question.hashCode() : 0);
        result = 31 * result + (compositiveScore != null ? compositiveScore.hashCode() : 0);
        return result;
    }
}
