package org.psi.malariacare.data;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by adrian on 14/02/15.
 */
public class Question extends SugarRecord<Tab> {

    String code;
    String de_name;
    String short_name;
    String form_name;
    String uid;
    Integer order_question;
    Float numerator_w;
    Float denominator_w;
    Header header;
    Answer answer;
    Integer master;
    Question question;

    public Question() {
    }

    public Question(String code, String de_name, String short_name, String form_name, String uid, Integer order_question, Float numerator_w, Float denominator_w, Header header, Answer answer, Integer master, Question question) {
        this.code = code;
        this.de_name = de_name;
        this.short_name = short_name;
        this.form_name = form_name;
        this.uid = uid;
        this.order_question = order_question;
        this.numerator_w = numerator_w;
        this.denominator_w = denominator_w;
        this.header = header;
        this.answer = answer;
        this.master = master;
        this.question = question;
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

    public Integer getOrder_question() {
        return order_question;
    }

    public void setOrder_question(Integer order_question) {
        this.order_question = order_question;
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

    public Integer getMaster() {
        return master;
    }

    public void setMaster(Integer master) {
        this.master = master;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public List<Value> getValues(){
        return Value.find(Value.class, "question = ?", String.valueOf(this.getId()));
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
                ", order_question=" + order_question +
                ", numerator_w=" + numerator_w +
                ", denominator_w=" + denominator_w +
                ", header=" + header +
                ", answer=" + answer +
                ", master=" + master +
                ", question=" + question +
                '}';
    }
}
