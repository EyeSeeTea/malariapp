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

import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Question;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Holds the invisibility info for an autotabadapter.
 * Created by arrizabalaga on 11/04/16.
 */
public class AutoTabInVisibilityState {

    private final LinkedHashMap<Object, Boolean> elementInvisibility;
    private final HashMap<Long,QuestionRow> rowsMap;

    public AutoTabInVisibilityState(){
        elementInvisibility = new LinkedHashMap<>();
        rowsMap = new HashMap<>();
    }

    public boolean initVisibility(Header header){
        boolean invisible=true;
        elementInvisibility.put(header, invisible);
        return !invisible;
    }

    public boolean initVisibility(Question question){
        boolean hidden = AutoTabLayoutUtils.isHidden(question);
        elementInvisibility.put(question, hidden);
        return !hidden;
    }

    public boolean initVisibility(QuestionRow questionRow){
        boolean hidden = AutoTabLayoutUtils.isHidden(questionRow);
        elementInvisibility.put(questionRow,hidden);
        for(Question question:questionRow.getQuestions()){
            rowsMap.put(question.getId_question(),questionRow);
        }
        return  !hidden;
    }

    public void setInvisible(Object key, Boolean invisible){
        elementInvisibility.put(key,invisible);
    }

    public boolean isVisible(Object key){
        return !elementInvisibility.get(key);
    }

    public void updateHeaderVisibility(Question question){
        Header header = question.getHeader();
        boolean headerInvisible = elementInvisibility.get(header);
        elementInvisibility.put(header, headerInvisible && elementInvisibility.get(question));
    }

    public void updateHeaderVisibility(QuestionRow questionRow){
        Question firstQuestion=questionRow.getFirstQuestion();
        Header header = firstQuestion.getHeader();
        boolean headerVisibility = elementInvisibility.get(header);
        elementInvisibility.put(header, headerVisibility && elementInvisibility.get(questionRow));
    }

    public void updateHeaderVisibility(Header header){
        elementInvisibility.put(header, hasToHideHeader(header));
    }

    public void updateVisibility(Question question, boolean visible){
        Object key=question;
        if(question.belongsToCustomTab()){
            key=rowsMap.get(question.getId_question());
        }
        this.setInvisible(key, !visible);
    }

    public int countInvisible(){
        return AutoTabLayoutUtils.getHiddenCount(elementInvisibility);
    }

    public int getRealPosition(int position,List items){
        return AutoTabLayoutUtils.getRealPosition(position, elementInvisibility, items);
    }

    /**
     * Decide whether we need or not to hide this header (if every question inside is hidden)
     * @param header header that
     * @return true if every header question is hidden, false otherwise
     */
    private boolean hasToHideHeader(Header header) {
        // look in every question to see if every question is hidden. In case one cuestion is not hidden, we return false
        for (Question question : header.getQuestions()) {
            //Find the right visibility key (questionRow | question)
            Object key=question.belongsToCustomTab()?rowsMap.get(question.getId_question()):question;
            if (!elementInvisibility.get(key)) {
                return false;
            }
        }
        return true;
    }

}
