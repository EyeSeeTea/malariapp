package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.List;

public class CompositeScore extends SugarRecord<CompositeScore> {

    private static final String LIST_BY_PROGRAM_SQL="select distinct cs.* from composite_score cs left join question q on q.composite_score=cs.id "+
            "left join header h on q.header=h.id "+
            "left join tab t on h.tab=t.id "+
            "left join program p on t.program=p.id where p.id=?";

    String code;
    String label;
    CompositeScore compositeScore;

    @Ignore
    List<CompositeScore> _compositeScoreChildren;

    @Ignore
    List<Question> _questions;

    public CompositeScore() {
    }

    public CompositeScore(String code, String label, CompositeScore compositeScore) {
        this.code = code;
        this.label = label;
        this.compositeScore = compositeScore;
    }

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CompositeScore getComposite_score() {
        return compositeScore;
    }

    public void setCompositeScore(CompositeScore compositeScore) {
        this.compositeScore = compositeScore;
    }

    public boolean hasParent(){
        return getComposite_score() != null;
    }

    public List<CompositeScore> getCompositeScoreChildren() {
        if (this._compositeScoreChildren == null){
            this._compositeScoreChildren = CompositeScore.find(CompositeScore.class, "composite_score = ?", String.valueOf(this.getId()));
        }
        return this._compositeScoreChildren;
    }

    public List<Question> getQuestions(){
        if (_questions == null) {
            _questions = Select.from(Question.class)
                    .where(Condition.prop("composite_score")
                    .eq(String.valueOf(this.getId()))).list();
        }
        return _questions;
    }

    /**
     * Select all composite score that belongs to a program
     * @param program Program whose composite scores are searched.
     * @return
     */
    public static List<CompositeScore> listAllByProgram(Program program){
        if(program==null || program.getId()==null){
            return new ArrayList<>();
        }
        List<CompositeScore> compositeScoresByProgram = CompositeScore.findWithQuery(CompositeScore.class, LIST_BY_PROGRAM_SQL, program.getId().toString());
        return compositeScoresByProgram;
    }

    public boolean hasChildren(){
        return !getCompositeScoreChildren().isEmpty();
    }

    @Override
    public String toString() {
        return "CompositeScore{" +
                "code='" + code + '\'' +
                ",label='" + label + '\'' +
                ", compositeScore=" + compositeScore +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositeScore that = (CompositeScore) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (compositeScore != null ? !compositeScore.equals(that.compositeScore) : that.compositeScore != null)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (compositeScore != null ? compositeScore.hashCode() : 0);
        return result;
    }
}
