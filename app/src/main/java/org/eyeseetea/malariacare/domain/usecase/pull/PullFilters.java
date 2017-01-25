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

public class PullFilters {

    String mStartDate;
    boolean fullHierarchy;
    boolean downloadOnlyLastEvents;
    int maxEvents;

    public PullFilters(String startDate, boolean fullHierarchy, boolean downloadOnlyLastEvents,
            int maxEvents) {
        mStartDate = startDate;
        this.fullHierarchy = fullHierarchy;
        this.downloadOnlyLastEvents = downloadOnlyLastEvents;
        this.maxEvents = maxEvents;
    }

    public String getStartDate() {
        return mStartDate;
    }

    public void setStartDate(String startDate) {
        mStartDate = startDate;
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
}
