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

import com.google.common.primitives.Booleans;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;

import java.util.Arrays;
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

    public boolean initVisibility(Question question, float idSurvey){
        boolean hidden = isHidden(question, idSurvey);
        elementInvisibility.put(question, hidden);
        return !hidden;
    }

    public boolean initVisibility(QuestionRow questionRow, float idSurvey){
        boolean hidden = isHidden(questionRow, idSurvey);
        elementInvisibility.put(questionRow,hidden);
        for(Question question:questionRow.getQuestions()){
            rowsMap.put(question.getId_question(),questionRow);
        }
        return  !hidden;
    }

    /**
     * Checks if given question should be hidden according to the current survey or not.
     * @param question
     * @return
     */
    public boolean isHidden(Question question, float idSurvey) {
        return question.isHiddenBySurvey(idSurvey);
    }

    /**
     * A question row is hidden if the first question is hidden
     * @param questionRow
     * @return
     */
    public boolean isHidden(QuestionRow questionRow, float idSurvey){
        if(questionRow==null || questionRow.sizeColumns()==0){
            return true;
        }

        Question question = questionRow.getFirstQuestion();

        return isHidden(question,idSurvey);
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

    /**
     * Updates header visibility according to current visibility of its questions.
     * @param header
     * @return true: Visible | false: Invisible
     */
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
        return Booleans.countTrue(Booleans.toArray(elementInvisibility.values()));
    }

    public int getRealPosition(int position,List items){
        int hElements = getHiddenCountUpTo(position);
        int diff = 0;

        for (int i = 0; i < hElements; i++) {
            diff++;
            if (elementInvisibility.get(items.get(position + diff))) i--;
        }
        return (position + diff);
    }

    /**
     * Given a question, make visible or invisible their children. In case all children in a header
     * became invisible, that header is also hidden
     */
    public void toggleChildrenVisibility(AutoTabSelectedItem autoTabSelectedItem, float idSurvey) {
        Question question = autoTabSelectedItem.getQuestion();

        List<Question> children = question.getChildren();
        boolean visible;

        for (Question childQuestion : children) {
            Header childHeader = childQuestion.getHeader();
            visible=!childQuestion.isHiddenBySurvey(idSurvey);
            this.updateVisibility(childQuestion,visible);

            //Show child -> Show header, Update scores
            if(visible){
                Float denum = ScoreRegister.calcDenum(childQuestion, idSurvey);
                ScoreRegister.addRecord(childQuestion, 0F, denum, idSurvey);
                this.setInvisible(childHeader,false);
                continue;
            }

            //Hide child ...
            //-> Remove value
            ReadWriteDB.deleteValue(childQuestion);

            //-> Remove score
            if (ScoreRegister.getNumDenum(childQuestion) != null) {
                ScoreRegister.deleteRecord(childQuestion, idSurvey);
            }
            //-> Check header visibility (no header,done)
            if(childHeader==null){
                continue;
            }
            //-> Check header visibility
            this.updateHeaderVisibility(childHeader);
        }
    }


    /**
     * Get the number of elements that are hidden until a given position
     * @param position
     * @return number of elements hidden (true in elementInvisibility Map)
     */
    private int getHiddenCountUpTo(int position) {
        boolean [] upper = Arrays.copyOfRange(Booleans.toArray(elementInvisibility.values()), 0, position + 1);
        return Booleans.countTrue(upper);
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
