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

package org.eyeseetea.malariacare.database.feedback;

import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Value;

/**
 * Created by arrizabalaga on 14/09/15.
 */
public class QuestionFeedback implements Feedback {

    /**
     * Question referred by this feedback
     */
    private Question question;

    /**
     * Value associated to question in the current survey
     */
    private Value value;

    /**
     * Flag that indicates if this element has its feedback open or not
     */
    private boolean feedbackShown;

    public QuestionFeedback(Question question, Value value){
        this.question=question;
        this.value=value;
        this.feedbackShown=false;
    }

    @Override
    public String getLabel() {
        return this.question.getForm_name();
    }

    @Override
    public boolean isPassed() {
        if(this.value==null){
            return false;
        }
        return this.value.getOption().getFactor()==1;
    }

    /**
     * Returns if this row has its feedback open or not
     * @return
     */
    public boolean isFeedbackShown(){
        return this.feedbackShown;
    }

    /**
     * Toggles the feedbackshown flag
     * @return return the new assigned value
     */
    public boolean toggleFeedbackShown(){
        this.feedbackShown=!this.feedbackShown;
        return this.feedbackShown;
    }

    /**
     * Returns the value of the selected option, in other words the 'answer'
     * @return
     */
    public String getOption(){
        if(this.value==null){
            return "";
        }
        return value.getValue();
    }

    /**
     * Returns the feedback info associated with this row
     * @return
     */
    public String getFeedback(){

        //Pass->null (cannot return 'No feedback' due to i18N
        if(this.isPassed()){
            return null;
        }

        String questionFeedback=this.question.getFeedback();
        //XXX Temporal hack to show some demo feedback
        if(questionFeedback!=null && !questionFeedback.isEmpty()){
            return questionFeedback;
        }
        return "Some <b>mocked</b> feedback that includes<br/> breaklines and some <a href='http://www.psi.org/'>links</a>";
    }

    @Override
    public int hashCode() {
        return this.question.hashCode();
    }
}
