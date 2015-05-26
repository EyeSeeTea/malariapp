/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;

/**
 * Created by Jose on 25/05/2015.
 */
public class Relative extends SugarRecord<Relative> {
    Question master;
    Question relative;
    int operation;

    public Relative(){};

    public Relative(Question master, Question relative, int operation) {
        this.master = master;
        this.relative = relative;
        this.operation = operation;
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
}
