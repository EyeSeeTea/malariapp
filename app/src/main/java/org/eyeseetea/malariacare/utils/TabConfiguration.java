package org.eyeseetea.malariacare.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adrian on 28/02/15.
 */
public class TabConfiguration {

    Integer tabId;
    boolean isAutomaticTab;
    Integer layoutId;
    Integer scoreFieldId;

    public TabConfiguration(Integer tabId, boolean isAutomaticTab, Integer layoutId, Integer scoreFieldId) {
        this.tabId = tabId;
        this.isAutomaticTab = isAutomaticTab;
        this.layoutId = layoutId;
        this.scoreFieldId = scoreFieldId;
    }

    public Integer getTabId() {
        return tabId;
    }

    public void setTabId(Integer tabId) {
        this.tabId = tabId;
    }

    public boolean isAutomaticTab() {
        return isAutomaticTab;
    }

    public void setAutomaticTab(boolean isAutomaticTab) {
        this.isAutomaticTab = isAutomaticTab;
    }

    public Integer getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(Integer layoutId) {
        this.layoutId = layoutId;
    }

    public Integer getScoreFieldId() { return scoreFieldId; }

    public void setScoreFieldId(Integer scoreFieldId) { this.scoreFieldId = scoreFieldId; }

    @Override
    public String toString() {
        return "TabConfiguration{" +
                "tabId=" + tabId +
                ", isAutomaticTab=" + isAutomaticTab +
                ", layoutId=" + layoutId +
                ", scoreFieldId=" + scoreFieldId +
                '}';
    }
}
