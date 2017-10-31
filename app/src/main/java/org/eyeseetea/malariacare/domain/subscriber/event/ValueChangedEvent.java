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

import java.util.ArrayList;
import java.util.List;

public class ValueChangedEvent {

    public static class ValueChangesContainer{
        private Boolean isCompulsory;
        private Boolean isVisible;

        public ValueChangesContainer(boolean isCompulsory){
            this.isCompulsory = isCompulsory;
        }

        public ValueChangesContainer(boolean isCompulsory, boolean isVisible){
            this.isCompulsory = isCompulsory;
            this.isVisible = isVisible;
        }

        public Boolean isCompulsory() {
            return isCompulsory;
        }

        public Boolean isVisible() {
            return isVisible;
        }
    }
    private List<ValueChangesContainer> mValueChangesContainers;
    public enum Action {INSERT, DELETE, TOGGLE}
    private Action action;
    private long idSurvey;

    public ValueChangedEvent() {
    }

    public ValueChangedEvent(long idSurvey, boolean isCompulsory, Action action) {
        this.action = action;
        this.idSurvey = idSurvey;
        mValueChangesContainers = new ArrayList<>();
        mValueChangesContainers.add(new ValueChangesContainer(isCompulsory));
    }

    public ValueChangedEvent(long idSurvey, List<ValueChangesContainer> valueChangesContainers, Action action) {
        this.action = action;
        this.idSurvey = idSurvey;
        mValueChangesContainers = valueChangesContainers;
    }

    public Action getAction() {
        return action;
    }

    public long getIdSurvey() {
        return idSurvey;
    }

    public List<ValueChangesContainer> getValueChangesContainers() {
        return mValueChangesContainers;
    }
}
