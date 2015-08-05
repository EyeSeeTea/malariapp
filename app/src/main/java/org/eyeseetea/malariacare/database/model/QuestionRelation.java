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
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

/**
 * Created by Jose on 25/05/2015.
 */
@Table(databaseName = AppDatabase.NAME)
public class QuestionRelation extends BaseModel {
    @Column
    @PrimaryKey(autoincrement = true)
    Long id;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "master",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    Question master;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "relative",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    Question relative;
    @Column
    int operation;

    public QuestionRelation(){};

    public QuestionRelation(Question master, Question relative, int operation) {
        this.master = master;
        this.relative = relative;
        this.operation = operation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Question getMaster() {
        return master;
    }

    public void setMaster(Question master) {
        this.master = master;
    }

    public Question getRelative() {
        return relative;
    }

    public void setRelative(Question relative) {
        this.relative = relative;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionRelation)) return false;

        QuestionRelation that = (QuestionRelation) o;

        if (operation != that.operation) return false;
        if (!id.equals(that.id)) return false;
        if (!master.equals(that.master)) return false;
        return relative.equals(that.relative);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + master.hashCode();
        result = 31 * result + relative.hashCode();
        result = 31 * result + operation;
        return result;
    }

    @Override
    public String toString() {
        return "QuestionRelation{" +
                "id=" + id +
                ", master=" + master +
                ", relative=" + relative +
                ", operation=" + operation +
                '}';
    }
}
