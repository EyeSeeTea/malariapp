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
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Tab extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_tab;
    @Column
    String name;
    @Column
    Integer order_pos;
    @Column
    Integer type;
    @Column
    Long id_tab_group;

    /**
     * Reference to parent tabgroup (loaded lazily)
     */
    TabGroup tabGroup;
    
    @Column
    Long id_program;

    /**
     * Reference to parent tabgroup (loaded lazily)
     */
    Program program;
    /**
     * List of headers that belongs to this tab
     */
    List<Header> headers;

    public Tab() {
    }

    public Tab(String name, Integer order_pos, Program program, Integer type) {
        this.name = name;
        this.order_pos = order_pos;
        setProgram(program);
        this.type = type;
    }
    public Tab(String name, Integer order_pos, Integer type, TabGroup tabGroup) {
        this.name = name;
        this.order_pos = order_pos;
        this.type = type;
        setTabGroup(tabGroup);
    }
    public Tab(String name, Integer order_pos, Integer type, TabGroup tabGroup,Program program) {
        this.name = name;
        this.order_pos = order_pos;
        this.type = type;
        setTabGroup(tabGroup);
        setProgram(program);
    }
    public Long getId_tab() {
        return id_tab;
    }

    public void setId_tab(Long id_tab) {
        this.id_tab = id_tab;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrder_pos() {
        return order_pos;
    }

    public void setOrder_pos(Integer order_pos) {
        this.order_pos = order_pos;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public TabGroup getTabGroup() {
        if(tabGroup==null){
            if (id_tab_group == null) return null;

            tabGroup= new Select()
                    .from(TabGroup.class)
                    .where(Condition.column(TabGroup$Table.ID_TAB_GROUP)
                            .is(id_tab_group)).querySingle();
        }
        return tabGroup;
    }

    public void setTabGroup(Long id_tab_group){
        this.id_tab_group=id_tab_group;
        this.tabGroup=null;
    }

    public void setTabGroup(TabGroup tabGroup) {
        this.tabGroup = tabGroup;
        this.id_tab_group = (tabGroup!=null)?tabGroup.getId_tab_group():null;
    }

    public Program getProgram() {
        if(program==null){
            if (id_program == null) return null;

            program= new Select()
                    .from(Program.class)
                    .where(Condition.column(Program$Table.ID_PROGRAM)
                            .is(id_program)).querySingle();
        }
        return program;
    }

    public void setProgram(Long id_tab_group){
        this.id_program=id_program;
        this.program=null;
    }

    public void setProgram(Program program) {
        this.program = program;
        this.id_program = (program!=null)?program.getId_program() :null;
    }

    public List<Header> getHeaders(){
        if(headers==null){
            headers =new Select().from(Header.class)
                    .where(Condition.column(Header$Table.ID_TAB).eq(this.getId_tab()))
                    .orderBy(Header$Table.ORDER_POS).queryList();
        }
        return headers;
    }

    public List<Score> getScores(){
        return new Select().from(Score.class)
                .where(Condition.column(Score$Table.ID_TAB).eq(this.getId_tab())).queryList();
    }

    /*
     * Return tabs filter by program and order by orderpos field
     */
    public static List<Tab> getTabsBySession(){
        return new Select().from(Tab.class)
                .where(Condition.column(Tab$Table.ID_TAB_GROUP).eq(Session.getSurvey().getTabGroup().getId_tab_group()))
                .orderBy(Tab$Table.ORDER_POS).queryList();
    }
    /*
 * Return tabs filter by program and order by orderpos field
 */
    public static List<Tab> getPictureTabsBySession() {
        return new Select().from(Tab.class)
                .where(Condition.column(Tab$Table.ID_TAB_GROUP).eq(Session.getSurvey().getTabGroup().getId_tab_group()))
                .orderBy(Tab$Table.ORDER_POS).queryList();
    }


    /**
     * Checks if this tab is the adherence tab
     * @return
     */
    public boolean isAdherenceTab(){
        return getType() == Constants.TAB_ADHERENCE;
    }

    /**
     * Checks if this tab is the IQA tab
     * @return
     */
    public boolean isIQATab(){
        return getType() == Constants.TAB_IQATAB;
    }

    /**
     * Checks if this tab is a dynamic tab (sort of a wizard)
     * @return
     */
    public boolean isDynamicTab(){
        return getType() == Constants.TAB_DYNAMIC_AUTOMATIC_TAB;
    }

    /**
     * Checks if this tab is a general score tab.
     * @return
     */
    public boolean isGeneralScore(){
        return getType() == Constants.TAB_SCORE_SUMMARY && !isCompositeScore();
    }

    /**
     * Checks if this tab is the composite score tab
     * @return
     */
    public boolean isCompositeScore(){
        return getType().equals(Constants.TAB_COMPOSITE_SCORE);
    }

    /**
     * Checks if this tab is the composite score tab
     * @return
     */
    public boolean isPictureCompositeScore(){
        return getType().equals(Constants.COMPOSITE_SCORE_TAB_NAME);
    }

    /**
     * Checks if this tab is a custom tab
     * @return
     */
    public boolean isACustomTab() {
        return getType().equals(Constants.TAB_ADHERENCE) || getType().equals(Constants.TAB_IQATAB) ||
        getType().equals(Constants.TAB_REPORTING);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tab tab = (Tab) o;

        if (id_tab != tab.id_tab) return false;
        if (name != null ? !name.equals(tab.name) : tab.name != null) return false;
        if (order_pos != null ? !order_pos.equals(tab.order_pos) : tab.order_pos != null)
            return false;
        if (type != null ? !type.equals(tab.type) : tab.type != null) return false;
        if(id_program !=null ? !id_program.equals(tab.id_program): tab.id_program!=null) return false;
        return !(id_tab_group != null ? !id_tab_group.equals(tab.id_tab_group) : tab.id_tab_group != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_tab ^ (id_tab >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (order_pos != null ? order_pos.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (id_tab_group != null ? id_tab_group.hashCode() : 0);
        result = 31 * result + (id_program != null ? id_program.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tab{" +
                "id_tab=" + id_tab +
                ", name='" + name + '\'' +
                ", order_pos=" + order_pos +
                ", type=" + type +
                ", id_tab_group=" + id_tab_group +
                ", id_program=" + id_program +
                '}';
    }
}
