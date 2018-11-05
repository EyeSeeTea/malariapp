/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.domain.usecase.pull;


import java.util.Date;

public class PullSurveyFilter {

    public enum SurveysToRetrieve {ALL, COMPLETED, UNSENT, SENT}

    private Date startDate;
    private Date endDate;
    private int maxEvents;

    public PullSurveyFilter(Date startDate, Date endDate, int maxEvents) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxEvents = maxEvents;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getMaxSize() {
        return maxEvents;
    }

    public Date getEndDate() {
        return endDate;
    }


    public static class Builder {
        private Date startDate = null;
        private Date endDate = null;
        private int maxSize = 0;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder withStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(Date endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withMaxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public PullSurveyFilter build() {
            return new PullSurveyFilter(startDate, endDate, maxSize);
        }
    }
}
