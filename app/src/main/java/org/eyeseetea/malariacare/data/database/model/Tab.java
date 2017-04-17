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

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

@Table(database = AppDatabase.class)
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
    Long id_program_fk;

    /**
     * Reference to parent Program (loaded lazily)
     */
    Program program;

    /**
     * List of headers that belongs to this tab
     */
    List<Header> headers;

    public Tab() {
    }

    public Tab(String name, Integer order_pos, Integer type, Program program) {
        this.name = name;
        this.order_pos = order_pos;
        this.type = type;
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

    public Program getProgram() {
        if(program ==null){
            if (id_program_fk == null) return null;

            program = new Select()
                    .from(Program.class)
                    .where(Program_Table.id_program
                            .is(id_program_fk)).querySingle();
        }
        return program;
    }

    public void setProgram(Long id_program){
        this.id_program_fk=id_program;
        this.program=null;
    }

    public void setProgram(Program program) {
        this.program = program;
        this.id_program_fk = (program!=null)?program.getId_program():null;
    }

    public List<Header> getHeaders(){
        if(headers==null){
            headers =new Select().from(Header.class)
                    .where(Header_Table.id_tab_fk.eq(this.getId_tab()))
                    .orderBy(Header_Table.order_pos,true).queryList();
        }
        return headers;
    }

    /*
     * Return tabs filter by program and order by orderpos field
     */
    public static List<Tab> getTabsBySession(String module){
        return new Select().from(Tab.class)
                .where(Tab_Table.id_program_fk.eq(Session.getSurveyByModule(module).getProgramEntity().getId()))
                .orderBy(Tab_Table.order_pos,true).queryList();
    }

    /**
     * Returns the tab with the given id
     * @param tabID
     * @return
     */
    public static Tab findById(Long tabID) {
        return new Select().from(Tab.class).where(Tab_Table.id_tab.eq(tabID)).querySingle();
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
     * Checks if this tab is a dynamic tab (sort of a wizard)
     * @return
     */
    public boolean isDynamicTab(){
        return getType() == Constants.TAB_DYNAMIC_AUTOMATIC_TAB;
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
        return !(id_program_fk != null ? !id_program_fk.equals(tab.id_program_fk) : tab.id_program_fk != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_tab ^ (id_tab >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (order_pos != null ? order_pos.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (id_program_fk != null ? id_program_fk.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "Tab{" +
                "id_tab=" + id_tab +
                ", name='" + name + '\'' +
                ", order_pos=" + order_pos +
                ", type=" + type +
                ", id_program=" + id_program_fk +
                '}';
    }

}
