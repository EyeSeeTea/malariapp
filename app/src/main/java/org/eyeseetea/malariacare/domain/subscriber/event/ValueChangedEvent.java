//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package org.eyeseetea.malariacare.domain.subscriber.event;

import org.eyeseetea.malariacare.domain.entity.Question;

import java.util.ArrayList;
import java.util.List;

public class ValueChangedEvent {

    List<Question> mQuestions;
    public enum Action {INSERT, DELETE, TOGGLE}
    private Action action;
    private long idSurvey;

    public ValueChangedEvent(long idSurvey, Question question, Action action) {
        this.action = action;
        this.idSurvey = idSurvey;
        mQuestions = new ArrayList<>();
        mQuestions.add(question);
    }

    public ValueChangedEvent(long idSurvey, List<Question> questions, Action action) {
        this.action = action;
        this.idSurvey = idSurvey;
        mQuestions = questions;
    }

    public Action getAction() {
        return action;
    }

    public long getIdSurvey() {
        return idSurvey;
    }

    public List<Question> getQuestions() {
        return mQuestions;
    }
}
