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
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(databaseName = AppDatabase.NAME)
public class CompositeScore extends BaseModel {

    /*private static final String LIST_BY_PROGRAM_SQL=
            "select distinct cs.* from composite_score cs "+
            "left join question q on q.composite_score=cs.id "+
            "left join header h on q.header=h.id "+
            "left join tab t on h.tab=t.id "+
            "left join program p on t.program=p.id where p.id=?";*/

    @Column
    @PrimaryKey(autoincrement = true)
    Long id;
    @Column
    String code;
    @Column
    String label;
    @Column
    String uid;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_composite_score",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    CompositeScore compositeScore;

    List<CompositeScore> compositeScoreChildren;

    List<Question> questions;

    public CompositeScore() {
    }

    public CompositeScore(String code, String label, CompositeScore compositeScore) {
        this.code = code;
        this.label = label;
        this.compositeScore = compositeScore;
    }

    public CompositeScore(String code, String label, String uid, CompositeScore compositeScore) {
        this.code = code;
        this.label = label;
        this.uid = uid;
        this.compositeScore = compositeScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean hasParent(){
        return getComposite_score() != null;
    }

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    public List<CompositeScore> getCompositeScoreChildren() {
        if (this.compositeScoreChildren == null){
            this.compositeScoreChildren = new Select()
                    .from(CompositeScore.class)
                    .where(Condition.column(CompositeScore$Table.COMPOSITESCORE_ID_COMPOSITE_SCORE).is(this.getId()))
                    .queryList();
            //this.compositeScoreChildren = CompositeScore.find(CompositeScore.class, "composite_score = ?", String.valueOf(this.getId()));
        }
        return this.compositeScoreChildren;
    }

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "questions")
    public List<Question> getQuestions(){
        //if (questions == null) {
            questions = new Select()
                    .from(Question.class)
                    .where(Condition.column(Question$Table.COMPOSITESCORE_ID_COMPOSITE_SCORE).is(this.getId()))
                    .queryList();
            /*questions = Select.from(Question.class)
                    .where(Condition.prop("composite_score")
                    .eq(String.valueOf(this.getId()))).list();*/
        //}
        return questions;
    }

    /**
     * Select all composite score that belongs to a program
     * @param program Program whose composite scores are searched.
     * @return
     */
    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    public static List<CompositeScore> listAllByProgram(Program program){
        if(program==null || program.getId()==null){
            return new ArrayList<>();
        }
        //TODO: Implement join
        //Take scores associated to questions of the program ('leaves')
        List<CompositeScore> compositeScoresByProgram = new Select().distinct().all().from(CompositeScore.class).as("cs")
                .join(Question.class, Join.JoinType.LEFT).as("q")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.COMPOSITESCORE_ID_COMPOSITE_SCORE))
                        .eq(ColumnAlias.columnWithTable("cs", CompositeScore$Table.ID)))
                .join(Header.class, Join.JoinType.LEFT).as("h")
                .on(Condition.column(ColumnAlias.columnWithTable("q", Question$Table.HEADER_ID_HEADER))
                        .eq(ColumnAlias.columnWithTable("h", Header$Table.ID)))
                .join(Tab.class, Join.JoinType.LEFT).as("t")
                .on(Condition.column(ColumnAlias.columnWithTable("h", Header$Table.TAB_ID_TAB))
                        .eq(ColumnAlias.columnWithTable("t", Tab$Table.ID)))
                .join(Program.class, Join.JoinType.LEFT).as("p")
                .on(Condition.column(ColumnAlias.columnWithTable("t", Tab$Table.PROGRAM_ID_PROGRAM))
                        .eq(ColumnAlias.columnWithTable("p", Program$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("p", Program$Table.ID))
                .eq(program.getId())).queryList();

        //List<CompositeScore> compositeScoresByProgram = CompositeScore.findWithQuery(CompositeScore.class, LIST_BY_PROGRAM_SQL, program.getId().toString());

        //Find parent scores from 'leaves'
        Set<CompositeScore> parentCompositeScores = new HashSet<CompositeScore>();
        for(CompositeScore compositeScore: compositeScoresByProgram){
            parentCompositeScores.addAll(listParentCompositeScores(compositeScore));
        }
        compositeScoresByProgram.addAll(parentCompositeScores);

        //return all scores
        return compositeScoresByProgram;
    }

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    public static List<CompositeScore> listParentCompositeScores(CompositeScore compositeScore){
        ArrayList<CompositeScore> parentScores= new ArrayList<CompositeScore>();
        if(compositeScore==null || !compositeScore.hasParent()){
            return parentScores;
        }
        CompositeScore currentScore=compositeScore;
        while(currentScore!=null && currentScore.hasParent()){
            currentScore=currentScore.getComposite_score();
            parentScores.add(currentScore);
        }
        return parentScores;
    }

    public boolean hasChildren(){
        return !getCompositeScoreChildren().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompositeScore)) return false;

        CompositeScore that = (CompositeScore) o;

        if (!id.equals(that.id)) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;
        return compositeScore.equals(that.compositeScore);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + compositeScore.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CompositeScore{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", label='" + label + '\'' +
                ", uid='" + uid + '\'' +
                ", compositeScore=" + compositeScore +
                '}';
    }
}
