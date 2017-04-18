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

import static org.eyeseetea.malariacare.data.database.AppDatabase.compositeScoreAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.compositeScoreName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.compositeScoreTwoAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.compositeScoreTwoName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.headerAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.questionName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.tabAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.tabName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.headerName;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.VisitableToSDK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(database = AppDatabase.class)
public class CompositeScore extends BaseModel implements VisitableToSDK {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_composite_score;
    @Column
    String hierarchical_code;
    @Column
    String label;
    @Column
    String uid_composite_score;
    @Column
    Integer order_pos;
    @Column
    Long id_composite_score_parent;

    /**
     * Reference to parent compositeScore (loaded lazily)
     */
    CompositeScore compositeScore;

    /**
     * List of compositeScores that belongs to this one
     */
    List<CompositeScore> compositeScoreChildren;

    /**
     * List of questions associated to this compositeScore
     */
    List<Question> questions;

    public CompositeScore() {
    }

    public CompositeScore(String hierarchical_code, String label, CompositeScore compositeScore, Integer order_pos) {
        this.hierarchical_code = hierarchical_code;
        this.label = label;
        this.order_pos = order_pos;
        this.setCompositeScore(compositeScore);
    }

    public CompositeScore(String hierarchical_code, String label, String uid, CompositeScore compositeScore, Integer order_pos) {
        this.hierarchical_code = hierarchical_code;
        this.label = label;
        this.uid_composite_score = uid;
        this.order_pos = order_pos;
        this.setCompositeScore(compositeScore);
    }

    public Long getId_composite_score() {
        return id_composite_score;
    }

    public void setId_composite_score(Long id_composite_score) {
        this.id_composite_score = id_composite_score;
    }

    public String getHierarchical_code() { return hierarchical_code; }

    public void setHierarchical_code(String hierarchical_code) { this.hierarchical_code = hierarchical_code; }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CompositeScore getComposite_score() {
        if(compositeScore==null){
            if (id_composite_score_parent==null) return null;
            compositeScore = new Select()
                    .from(CompositeScore.class)
                    .where(CompositeScore_Table.id_composite_score
                            .is(id_composite_score_parent)).querySingle();
        }
        return compositeScore;
    }

    public void setCompositeScore(CompositeScore compositeScore) {
        this.compositeScore = compositeScore;
        this.id_composite_score_parent = (compositeScore!=null)?compositeScore.getId_composite_score():null;
    }

    public void setCompositeScore(Long id_parent){
        this.id_composite_score_parent = id_parent;
        this.compositeScore = null;
    }

    public String getUid() {
        return uid_composite_score;
    }

    public void setUid(String uid) {
        this.uid_composite_score = uid;
    }

    public Integer getOrder_pos() {
        return order_pos;
    }

    public void setOrder_pos(Integer order_pos) {
        this.order_pos = order_pos;
    }

    public boolean hasParent(){
        return getComposite_score() != null;
    }

    public List<CompositeScore> getCompositeScoreChildren() {
        if (this.compositeScoreChildren == null){
            this.compositeScoreChildren = new Select()
                    .from(CompositeScore.class)
                    .where(CompositeScore_Table.id_composite_score_parent.eq(this.getId_composite_score()))
                    .orderBy(OrderBy.fromProperty(CompositeScore_Table.order_pos))
                    .queryList();
        }
        return this.compositeScoreChildren;
    }

    public List<Question> getQuestions(){
        if(questions==null){
            questions = new Select()
                    .from(Question.class)
                    .where(Question_Table.id_composite_score_fk.eq(this.getId_composite_score()))
                    .orderBy(Question_Table.order_pos ,true)
                    .queryList();
        }
        return questions;
    }

    /**
     * Select all composite score that belongs to a program
     * @param program Program whose composite scores are searched.
     * @return
     */
    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    public static List<CompositeScore> listByProgram(Program program){
        if(program==null || program.getId_program()==null){
            return new ArrayList<>();
        }

        //FIXME: Apparently there is a bug in DBFlow joins that affects here. Question has a column 'uid', and so do CompositeScore, so results are having Questions one, and should keep CompositeScore one. To solve it, we've introduced a last join with CompositeScore again and a HashSet to remove resulting duplicates
        //Take scores associated to questions of the program ('leaves')
        List<CompositeScore> compositeScoresByProgram = new Select().distinct().from(CompositeScore.class).as(compositeScoreName)
                .join(Question.class, Join.JoinType.LEFT_OUTER).as(questionName)
                .on(CompositeScore_Table.id_composite_score.withTable(compositeScoreAlias)
                        .eq(Question_Table.id_composite_score_fk.withTable(questionAlias)))
                .join(Header.class, Join.JoinType.LEFT_OUTER).as(headerName)
                .on(Question_Table.id_header_fk.withTable(questionAlias)
                        .eq(Header_Table.id_header.withTable(headerAlias)))
                .join(Tab.class, Join.JoinType.LEFT_OUTER).as(tabName)
                .on(Header_Table.id_tab_fk.withTable(headerAlias)
                        .eq(Tab_Table.id_tab.withTable(tabAlias)))
                .join(Program.class, Join.JoinType.LEFT_OUTER).as(programName)
                .on(Tab_Table.id_program_fk.withTable(tabAlias)
                        .eq(Program_Table.id_program.withTable(programAlias)))
                .join(CompositeScore.class, Join.JoinType.LEFT_OUTER).as(compositeScoreTwoName)
                .on(CompositeScore_Table.id_composite_score.withTable(compositeScoreAlias)
                        .eq(CompositeScore_Table.id_composite_score.withTable(compositeScoreTwoAlias)))
                .where(Program_Table.id_program.withTable(programAlias)
                        .eq(program.getId_program()))
                .orderBy(CompositeScore_Table.order_pos, true)
                .queryList();


        //Find parent scores from 'leaves'
        Set<CompositeScore> parentCompositeScores = new HashSet<>();
        for(CompositeScore compositeScore: compositeScoresByProgram){
            parentCompositeScores.addAll(listParentCompositeScores(compositeScore));
        }
        compositeScoresByProgram.addAll(parentCompositeScores);

        // remove duplicates
        Set<CompositeScore> uniqueCompositeScoresByProgram = new HashSet<>();
        uniqueCompositeScoresByProgram.addAll(compositeScoresByProgram);
        compositeScoresByProgram.clear();
        compositeScoresByProgram.addAll(uniqueCompositeScoresByProgram);

        Collections.sort(compositeScoresByProgram, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {

                CompositeScore cs1 = (CompositeScore) o1;
                CompositeScore cs2 = (CompositeScore) o2;

                return new Integer(cs1.getOrder_pos().compareTo(new Integer(cs2.getOrder_pos())));
            }
        });


        //return all scores
        return compositeScoresByProgram;
    }

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    public static List<CompositeScore> listParentCompositeScores(CompositeScore compositeScore){
        ArrayList<CompositeScore> parentScores= new ArrayList<>();
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
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) {
        IConvertToSDKVisitor.visit(this);
    }

    /**
     * List of compositeScores
     * @return
     */
    public static List<CompositeScore> list() {
        return new Select().from(CompositeScore.class).queryList();
    }

    public boolean isEmptyCS(){
        return !hasChildren() && (getQuestions()==null || getQuestions().size()==0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositeScore that = (CompositeScore) o;

        if (id_composite_score != that.id_composite_score) return false;
        if (hierarchical_code != null ? !hierarchical_code.equals(that.hierarchical_code) : that.hierarchical_code != null)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (uid_composite_score != null ? !uid_composite_score.equals(that.uid_composite_score) : that.uid_composite_score != null) return false;
        if (order_pos != null ? !order_pos.equals(that.order_pos) : that.order_pos != null)
            return false;
        return !(id_composite_score_parent != null ? !id_composite_score_parent.equals(that.id_composite_score_parent) : that.id_composite_score_parent != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_composite_score ^ (id_composite_score >>> 32));
        result = 31 * result + (hierarchical_code != null ? hierarchical_code.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (uid_composite_score != null ? uid_composite_score.hashCode() : 0);
        result = 31 * result + (order_pos != null ? order_pos.hashCode() : 0);
        result = 31 * result + (id_composite_score_parent != null ? id_composite_score_parent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompositeScore{" +
                "id_composite_score=" + id_composite_score +
                ", hierarchical_code='" + hierarchical_code + '\'' +
                ", label='" + label + '\'' +
                ", uid_composite_score='" + uid_composite_score + '\'' +
                ", order_pos=" + order_pos +
                ", id_composite_score_parent=" + id_composite_score_parent +
                '}';
    }

}
