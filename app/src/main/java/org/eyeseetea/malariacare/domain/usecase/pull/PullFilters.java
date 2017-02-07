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

public class PullFilters {

    Date startDate;
    Date endDate;
    boolean fullHierarchy;
    boolean downloadOnlyLastEvents;
    int maxEvents;

    public PullFilters(Date startDate, Date endDate, boolean fullHierarchy, boolean downloadOnlyLastEvents,
            int maxEvents) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.fullHierarchy = fullHierarchy;
        this.downloadOnlyLastEvents = downloadOnlyLastEvents;
        this.maxEvents = maxEvents;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public boolean isFullHierarchy() {
        return fullHierarchy;
    }

    public void setFullHierarchy(boolean fullHierarchy) {
        this.fullHierarchy = fullHierarchy;
    }

    public boolean isDownloadOnlyLastEvents() {
        return downloadOnlyLastEvents;
    }

    public void setDownloadOnlyLastEvents(boolean downloadOnlyLastEvents) {
        this.downloadOnlyLastEvents = downloadOnlyLastEvents;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
