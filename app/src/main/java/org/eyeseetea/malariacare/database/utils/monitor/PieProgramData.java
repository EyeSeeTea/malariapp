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

package org.eyeseetea.malariacare.database.utils.monitor;

import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.utils.Constants;

/**
 * VO thats hold the info of a single pie chart
 * Created by arrizabalaga on 9/10/15.
 */
public class PieProgramData {

    private static final String JSONFORMAT="{title:'%s',tip:'%s',idTabGroup: %d,valueA:%d,valueB:%d,valueC:%d,uidprogram:'%s',uidorgunit:'%s'}";

    /**
     * Type of program for this chart
     */
    private Program program;

    /**
     * Number of surveys with A score
     */
    private int numA;
    /**
     * Number of surveys with B score
     */
    private int numB;
    /**
     * Number of surveys with C score
     */
    private int numC;


    /**
     * Constructor per program
     * @param program
     */
    public PieProgramData(Program program) {
        this.program = program;
    }

    /**
     * Increments the right counter according to the score
     * @param score
     */
    public void incCounter(float score){
        if(score< Constants.MAX_RED){
            numC++;
            return;
        }

        if(score< Constants.MAX_AMBER){
            numB++;
            return;
        }

        numA++;
        return;
    }

    /**
     * Turns this info into a JSON understandable by the js:
     *  {
     *    title:'First Program',
     *    idProgram: 1,
     *    valueA:14,
     *    valueB:8,
     *    valueC:10
     *  }
     * @return
     */
    public String toJSON(String tipChat){
        String pieTitle=String.format("%s", program.getName());
        String json= String.format(JSONFORMAT, pieTitle, tipChat, program.getId_program(), this.numA, this.numB, this.numC, program.getUid(), program.getUid());
        return json;
    }


}
