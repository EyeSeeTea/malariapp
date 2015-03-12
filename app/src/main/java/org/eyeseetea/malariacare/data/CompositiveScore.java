package org.eyeseetea.malariacare.data;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.List;

/**
 * Created by adrian on 14/02/15.
 */
public class CompositiveScore extends SugarRecord<CompositiveScore> {

    String code;
    String label;
    CompositiveScore compositiveScore;

    @Ignore
    List<CompositiveScore> _compositiveScoreChildren;

    public CompositiveScore() {
    }

    public CompositiveScore(String code, String label, CompositiveScore compositiveScore) {
        this.code = code;
        this.label = label;
        this.compositiveScore = compositiveScore;
    }

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CompositiveScore getCompositiveScore() {
        return compositiveScore;
    }

    public void setCompositiveScore(CompositiveScore compositiveScore) {
        this.compositiveScore = compositiveScore;
    }

    public boolean hasParent(){
        return getCompositiveScore() != null;
    }

    public List<CompositiveScore> getCompositiveScoreChildren() {
        if (this._compositiveScoreChildren == null){
            this._compositiveScoreChildren = CompositiveScore.find(CompositiveScore.class, "compositiveScore = ?", String.valueOf(this.getId()));
        }
        return this._compositiveScoreChildren;
    }

    public boolean hasChildren(){
        return !getCompositiveScoreChildren().isEmpty();
    }

    @Override
    public String toString() {
        return "CompositiveScore{" +
                "code='" + code + '\'' +
                "label='" + label + '\'' +
                ", compositiveScore=" + compositiveScore +
                '}';
    }

}
