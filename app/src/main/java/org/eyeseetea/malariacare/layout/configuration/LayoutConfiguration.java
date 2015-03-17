package org.eyeseetea.malariacare.layout.configuration;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Tab;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LayoutConfiguration {

    private static final Map<Tab, TabConfiguration> tabsConfiguration = new LinkedHashMap<Tab, TabConfiguration>();

    public static void initialize(List<Tab> tabList) {

        tabsConfiguration.put(tabList.get(0), new TabConfiguration(R.id.healthFacilityInfo, true, null, R.id.healthScore, null));
        tabsConfiguration.put(tabList.get(1), new TabConfiguration(R.id.profile, true, null, R.id.profileScore, null));
        tabsConfiguration.put(tabList.get(2), new TabConfiguration(R.id.c1Clinical, true, null, R.id.clinicalCase1, R.id.clinicalAvg));
        tabsConfiguration.put(tabList.get(3), new TabConfiguration(R.id.c1RDT, true, null, R.id.rdtCase1, R.id.rdtAvg));
        tabsConfiguration.put(tabList.get(4), new TabConfiguration(R.id.c1Microcospy, true, null, R.id.microscopyCase1, R.id.microscopyAvg));
        tabsConfiguration.put(tabList.get(5), new TabConfiguration(R.id.c2Clinical, true, null, R.id.clinicalCase2, R.id.clinicalAvg));
        tabsConfiguration.put(tabList.get(6), new TabConfiguration(R.id.c2RDT, true, null, R.id.rdtCase2, R.id.rdtAvg));
        tabsConfiguration.put(tabList.get(7), new TabConfiguration(R.id.c2Microscopy, true, null, R.id.microscopyCase2, R.id.microscopyAvg));
        tabsConfiguration.put(tabList.get(8), new TabConfiguration(R.id.c3Clinical, true, null, R.id.clinicalCase3, R.id.clinicalAvg));
        tabsConfiguration.put(tabList.get(9), new TabConfiguration(R.id.c3RDT, true, null, R.id.rdtCase3, R.id.rdtAvg));
        tabsConfiguration.put(tabList.get(10), new TabConfiguration(R.id.c3Microscopy, true, null, R.id.microscopyCase3, R.id.microscopyAvg));

        tabsConfiguration.put(tabList.get(11), new TabConfiguration(R.id.adherence, false, R.layout.adherencetab, R.id.adherenceScore, null));
        tabsConfiguration.put(tabList.get(12), new TabConfiguration(R.id.feedback, true, null, R.id.feedbackScore, null));
        tabsConfiguration.put(tabList.get(13), new TabConfiguration(R.id.environmentMaterial, true, null, R.id.envAndMatScore, null));
        tabsConfiguration.put(tabList.get(14), new TabConfiguration(R.id.reporting, false, R.layout.reportingtab, R.id.reportingScore, null));
        tabsConfiguration.put(tabList.get(15), new TabConfiguration(R.id.iqaEQA, false, R.layout.iqatab, R.id.iqaeqaScore, null));
        tabsConfiguration.put(tabList.get(16), new TabConfiguration(R.id.scoreSummary, false, R.layout.scoretab, null, null));

        tabsConfiguration.put(tabList.get(17), new TabConfiguration(R.id.compositiveScores, false, R.layout.compositivescoretab, null, null));
    }

    public static Map<Tab, TabConfiguration> getTabsConfiguration() {
        return tabsConfiguration;
    }

    public static List<Integer> getTabsConfigurationIds() {
        List<Integer> layouts = new ArrayList<Integer>();
        for (TabConfiguration tab: tabsConfiguration.values()){
            layouts.add(new Integer(tab.getTabId()));
        }
        return layouts;
    }
}
