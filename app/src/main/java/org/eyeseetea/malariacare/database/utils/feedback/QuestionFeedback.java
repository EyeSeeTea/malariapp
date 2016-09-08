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

package org.eyeseetea.malariacare.database.utils.feedback;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Media;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

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
     * Cached media to enhance listview performance
     */
    private List<Media> media;

    /**
     * Flag that indicates if this element has its feedback open or not
     */
    private boolean feedbackShown;

    public QuestionFeedback(Question question, Value value) {
        this.question = question;
        this.value = value;
        this.feedbackShown = false;
        this.media = null;
    }

    @Override
    public String getLabel() {
        return this.question.getForm_name();
    }

    @Override
    public boolean isPassed() {
        if (this.value == null || this.value.getConflict()) {
            return false;
        }
        Option option = this.value.getOption();
        if (option == null) {
            return false;
        }
        return option.getFactor() == 1;
    }

    public Question getQuestion() {
        return this.question;
    }

    public List<Media> getMedia() {

        //First time getting media
        if (media == null) {
            //no media attached
            media = Media.findByQuestion(question);
        }

        //No media (checked before) | real image/video media
        return media;
    }

    public boolean hasConflict() {
        if (this.value != null && this.value.getConflict())
            return true;
        return false;
    }

    /**
     * Returns if this row has its feedback open or not
     *
     * @return
     */
    public boolean isFeedbackShown() {
        return this.feedbackShown;
    }

    /**
     * Toggles the feedbackshown flag
     *
     * @return return the new assigned value
     */
    public boolean toggleFeedbackShown() {
        this.feedbackShown = !this.feedbackShown;
        return this.feedbackShown;
    }

    /**
     * Returns the value of the selected option, in other words the 'answer'
     *
     * @return
     */
    public String getOption() {
        if (this.value == null) {
            return "";
        }
        return value.getValue();
    }

    /**
     * Returns the feedback info associated with this row
     *
     * @return
     */
    public String getFeedback() {
        String questionFeedback = this.question.getFeedback();
        //XXX Temporal hack to show some demo feedback
        if (questionFeedback != null && !questionFeedback.isEmpty()) {
            return questionFeedback;
        }
        return null;
    }

    /**
     * Returns the code of the grade to show for this item:
     * -Amber: Not answered
     * -Green: Pass
     * -Red: Fail
     * -Blank: Special question with neither numerator nor denominator
     *
     * @return
     */
    public int getGrade() {
        int msgId;
        if (value != null && value.getConflict()) {
            msgId = R.string.feedback_info_conflict;
        } else if (this.getOption() == null || this.getOption().isEmpty()) {
            msgId = R.string.feedback_info_not_answered;
        } else {
            msgId = this.isPassed() ? R.string.feedback_info_passed : R.string.feedback_info_failed;
        }
        return msgId;
    }

    /**
     * Returns the textcolor (code) of the grade to show for this item:
     * -Amber: Not answered
     * -Green: Pass
     * -Red: Fail
     * -Blank: Special question with neither numerator nor denominator
     *
     * @return
     */
    public int getColor() {
        int textColor;
        if (this.getOption() == null || this.getOption().isEmpty()) {
            textColor = R.color.amber;
        } else {
            textColor = (this.isPassed() && value != null && !this.value.getConflict()) ? R.color.green : R.color.red;
        }
        return textColor;
    }

    /**
     * Every question has a grade except those with numerator + denominator = 0.
     *
     * @return
     */
    public boolean hasGrade() {
        //Some points -> Some grade
        return this.question.getDenominator_w() != 0 || this.question.getNumerator_w() != 0;
    }


    /**
     * Return if the question is a label
     *
     * @return
     */
    public boolean isLabel() {
        if (question.getAnswer().getName() != null && question.getAnswer().getName().equals(Constants.LABEL))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        return this.question.hashCode();
    }
}
