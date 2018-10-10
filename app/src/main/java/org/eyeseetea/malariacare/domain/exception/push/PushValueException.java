package org.eyeseetea.malariacare.domain.exception.push;

import android.util.Log;

import org.eyeseetea.malariacare.R;

public class PushValueException extends Exception {
    private static String messagePattern =
            "The event with UID: %s. was uploaded with conflict in the questionUID: %s. "
                    + "Message: %s.";

    private String surveyUid;
    private String questionUid;
    private String conflictMessage;

    public PushValueException(String surveyUid, String questionUid, String conflictMessage) {
        super(String.format(messagePattern, surveyUid, questionUid, conflictMessage));

        this.surveyUid = surveyUid;
        this.questionUid = questionUid;
        this.conflictMessage = conflictMessage;
    }

    public PushValueException(String errorMessage) {
        super(errorMessage);
    }

    public String getSurveyUid() {
        return surveyUid;
    }

    public String getQuestionUid() {
        return questionUid;
    }

    public String getConflictMessage() {
        return conflictMessage;
    }
}
