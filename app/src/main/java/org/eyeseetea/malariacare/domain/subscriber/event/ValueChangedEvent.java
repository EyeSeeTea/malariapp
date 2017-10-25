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

public class ValueChangedEvent {

    public enum Action {SAVE, DELETE, TOGGLE}
    private Action action;
    private long idSurvey;
    private boolean isCompulsory;
    private boolean isVisible;

    public ValueChangedEvent() {
    }

    public ValueChangedEvent(long idSurvey, boolean isCompulsory, Action action) {
        this.action = action;
        this.idSurvey = idSurvey;
        this.isCompulsory = isCompulsory;
    }

    public ValueChangedEvent(long idSurvey, boolean isCompulsory, boolean isVisible, Action action) {
        this.action = action;
        this.idSurvey = idSurvey;
        this.isCompulsory = isCompulsory;
        this.isVisible = isVisible;
    }

    public Action getAction() {
        return action;
    }

    public long getIdSurvey() {
        return idSurvey;
    }

    public boolean isCompulsory() {
        return isCompulsory;
    }

    public boolean isVisible(){
        return isVisible;
    };
}
