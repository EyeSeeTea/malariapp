/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.layout.utils;

import org.eyeseetea.malariacare.database.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO that holds the info related to a row in a customTab set of questions
 * Created by arrizabalaga on 24/03/16.
 */
public class QuestionRow {
    private List<Question> questions;

    public QuestionRow(){
        this.questions = new ArrayList<>();
    }

    /**
     * Adds a question to the row
     * @param question
     */
    public void addQuestion(Question question){
        this.questions.add(question);
    }

    public List<Question> getQuestions(){
        return this.questions;
    }

    public int sizeColumns(){
        return questions.size();
    }

    /**
     * Tells if this row is a tableHeader or not
     * @return
     */
    public boolean isCustomTabTableHeader(){
        if (questions.size()==0){
            return false;
        }

        return questions.get(0).isCustomTabTableHeader();
    }

    /**
     * Returns the row number for this questionRow
     * @return
     */
    public int getRow(){
        if (questions.size()==0){
            return 0;
        }

        return questions.get(0).getRow();
    }

    /**
     * Returns the first question of the row.
     * Useful for visibility issues since all of them are shown/hidden at once
     * @return
     */
    public Question getFirstQuestion(){
        if (questions.size()==0){
            return null;
        }

        return questions.get(0);
    }

}
