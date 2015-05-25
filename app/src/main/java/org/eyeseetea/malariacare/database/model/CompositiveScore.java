package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

public class CompositiveScore extends SugarRecord<CompositiveScore> {

    String code;
    String label;
    CompositiveScore compositive_score;

    @Ignore
    List<CompositiveScore> _compositiveScoreChildren;

    @Ignore
    List<Question> _questions;

    public CompositiveScore() {
    }

    public CompositiveScore(String code, String label, CompositiveScore compositive_score) {
        this.code = code;
        this.label = label;
        this.compositive_score = compositive_score;
    }

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CompositiveScore getCompositive_score() {
        return compositive_score;
    }

    public void setCompositive_score(CompositiveScore compositive_score) {
        this.compositive_score = compositive_score;
    }

    public boolean hasParent(){
        return getCompositive_score() != null;
    }

    public List<CompositiveScore> getCompositiveScoreChildren() {
        if (this._compositiveScoreChildren == null){
            this._compositiveScoreChildren = CompositiveScore.find(CompositiveScore.class, "compositivescore = ?", String.valueOf(this.getId()));
        }
        return this._compositiveScoreChildren;
    }

    public List<Question> getQuestions(){
        if (_questions == null) {
            _questions = Select.from(Question.class)
                    .where(Condition.prop("compositive_score")
                    .eq(String.valueOf(this.getId()))).list();
        }
        return _questions;
    }

    public boolean hasChildren(){
        return !getCompositiveScoreChildren().isEmpty();
    }

    @Override
    public String toString() {
        return "CompositiveScore{" +
                "code='" + code + '\'' +
                ",label='" + label + '\'' +
                ", compositiveScore=" + compositive_score +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositiveScore that = (CompositiveScore) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (compositive_score != null ? !compositive_score.equals(that.compositive_score) : that.compositive_score != null)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (compositive_score != null ? compositive_score.hashCode() : 0);
        return result;
    }
}
