package org.eyeseetea.malariacare.domain.entity;

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
