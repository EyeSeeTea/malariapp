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
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Tab extends BaseModel implements Visitable {

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
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_tab_group",
            columnType = Long.class,
            foreignColumnName = "id_tab_group")},
            saveForeignKeyModel = false)
    TabGroup tabGroup;

    List<Header> headers;

    public Tab() {
    }

    public Tab(String name, Integer order_pos, Integer type, TabGroup tabGroup) {
        this.name = name;
        this.order_pos = order_pos;
        this.type = type;
        this.tabGroup = tabGroup;
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
        return tabGroup;
    }

    public void setTabGroup(TabGroup tabGroup) {
        this.tabGroup = tabGroup;
    }

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "headers")
    public List<Header> getHeaders(){
        return new Select().from(Header.class)
                .where(Condition.column(Header$Table.TAB_ID_TAB).eq(this.getId_tab()))
                .orderBy(Header$Table.ORDER_POS).queryList();
    }

    /*
     * Return tabs filter by program and order by orderpos field
     */
    public static List<Tab> getTabsBySession(){
        return new Select().from(Tab.class)
                .where(Condition.column(Tab$Table.TABGROUP_ID_TAB_GROUP).eq(Session.getSurvey().getTabGroup().getId_tab_group()))
                .orderBy(Tab$Table.ORDER_POS).queryList();
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
     * Checks if this tab is a custom tab
     * @return
     */
    public boolean isACustomTab() {
        return getType().equals(Constants.TAB_ADHERENCE) || getType().equals(Constants.TAB_IQATAB) ||
        getType().equals(Constants.TAB_REPORTING);
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) {
        IConvertToSDKVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tab)) return false;

        Tab tab = (Tab) o;

        if (id_tab != tab.id_tab) return false;
        if (name != null ? !name.equals(tab.name) : tab.name != null) return false;
        if (!order_pos.equals(tab.order_pos)) return false;
        if (!tabGroup.equals(tab.tabGroup)) return false;
        return type.equals(tab.type);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_tab ^ (id_tab >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + order_pos.hashCode();
        result = 31 * result + tabGroup.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Tab{" +
                "id=" + id_tab +
                ", name='" + name + '\'' +
                ", order_pos=" + order_pos +
                ", type=" + type +
                ", tabGroup=" + tabGroup +
                '}';
    }
}
