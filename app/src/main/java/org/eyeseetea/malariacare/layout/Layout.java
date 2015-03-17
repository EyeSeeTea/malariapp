package org.eyeseetea.malariacare.layout;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.layout.configuration.LayoutConfiguration;
import org.eyeseetea.malariacare.layout.configuration.TabConfiguration;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.List;

/**
 * Created by adrian on 19/02/15.
 */
public class Layout {

    // This method fill in a tab layout
    public static void insertTab(MainActivity mainActivity, Tab tab) {
        // We reset backgrounds counter
        TabConfiguration tabConfiguration = LayoutConfiguration.getTabsConfiguration().get(tab);
        Log.i(".Layout", "Generating Tab " + tab.getName());

        // This layout inflater is for joining other layouts
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // This layout is for the tab content (questions)
        LinearLayout layoutGrandParent = (LinearLayout) mainActivity.findViewById(tabConfiguration.getTabId());
        layoutGrandParent.setTag(tab);
        ViewGroup layoutParentScroll = null;
        GridLayout layoutParent = null;
        if (tabConfiguration.isAutomaticTab()) layoutParentScroll = (ScrollView) layoutGrandParent.getChildAt(0);
        else layoutParentScroll = (ViewGroup) layoutGrandParent.getChildAt(0);
        layoutParent = (GridLayout) layoutParentScroll.getChildAt(0);

        //Initialize numerator and denominator record map
        ScoreRegister.registerScore(tab);

        // We do this to have a default value in the ddl
        Option defaultOption = new Option(Constants.DEFAULT_SELECT_OPTION);

        Log.i(".Layout", "Get View For Tab");
        TabHost tabHost = (TabHost)mainActivity.findViewById(R.id.tabHost);
        tabHost.setup();

        Log.i(".Layout", "Generate Tab");
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(Long.toString(tab.getId())); // Here we set the tag, we'll use later to move between tabs
        tabSpec.setIndicator(tab.getName());
        tabSpec.setContent(tabConfiguration.getTabId());
        tabHost.addTab(tabSpec);

        if (!tabConfiguration.isAutomaticTab() && tabConfiguration.getLayoutId() != null){
            CustomTabLayout.generateCustomTab(mainActivity, tab, inflater, layoutParent);
        }else {
            AutomaticTabLayout.generateAutomaticTab(mainActivity, tab, inflater, layoutParent, defaultOption);
        }
        if (tabConfiguration.getScoreFieldId() != null) generateScore(tab, inflater, layoutGrandParent);
    }


    private static void generateScore(Tab tab, LayoutInflater inflater, LinearLayout layoutGrandParent) {
        // This layout is for showing the accumulated score
        GridLayout layoutParentScore = (GridLayout) layoutGrandParent.getChildAt(1);
        View subtotalView = null;
        TextView totalNumText = null;
        TextView totalDenText = null;
        if (LayoutConfiguration.getTabsConfiguration().get(tab).isAutomaticTab()) {
            subtotalView = inflater.inflate(R.layout.subtotal_num_dem, layoutParentScore, false);
            totalNumText = (TextView) subtotalView.findViewById(R.id.totalNum);
            totalDenText = (TextView) subtotalView.findViewById(R.id.totalDen);
            totalNumText.setText("0.0");
            List<Float> numDenSubTotal = ScoreRegister.calculateGeneralScore(tab);
            totalDenText.setText(Utils.round(numDenSubTotal.get(1)));
        } else {
            subtotalView = inflater.inflate(R.layout.subtotal_custom, layoutParentScore, false);
        }
        TextView tabName = (TextView) subtotalView.findViewById(R.id.tabName);
        tabName.setText(tab.getName());


        Integer generalScoreId = LayoutConfiguration.getTabsConfiguration().get(tab).getScoreFieldId();
        // Now, for being able to write Score in the score tab and score averages in its place (in score tab), we use setTag() to include a pointer to
        // the score View id, and in that id, we include a pointer to the average view id. This way, we can do the calculus here and represent there
        TextView subscoreView = (TextView) subtotalView.findViewById(R.id.score);
        subscoreView.setTag(generalScoreId);

        layoutParentScore.addView(subtotalView);
    }

}


