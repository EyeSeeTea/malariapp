package org.eyeseetea.malariacare;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.layout.Layout;
import org.eyeseetea.malariacare.layout.LayoutUtils;
import org.eyeseetea.malariacare.utils.PopulateDB;
import org.eyeseetea.malariacare.utils.TabConfiguration;
import org.eyeseetea.malariacare.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final List<TabConfiguration> tabsLayouts = new ArrayList<TabConfiguration>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        long time = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        Log.i(".MainActivity", "Starting App");
        initializeApp();

        long time2 = System.currentTimeMillis();
        Log.d(".MainActivity", "Time initialing app: " + (time2 -time) + " ms");

        Log.i(".MainActivity", "Starting App");
        createTabConfiguration();

        long time3 = System.currentTimeMillis();
        Log.d(".MainActivity", "Time Configuring tab: " + (time3 -time2) + " ms");

        // We import the initial data in case it has been done yet
        if (Tab.count(Tab.class, null, null)==0) {
            Log.i(".MainActivity", "Populating DB");
            AssetManager assetManager = getAssets();
            try {
                PopulateDB.populateDB(assetManager);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(".MainActivity", "DB populated");
        }

        long time4 = System.currentTimeMillis();
        Log.d(".MainActivity", "Time populating DB: " + (time4 -time3) + " ms");

        Log.i(".MainActivity", "Initializing Menu and generating tabs");
        createMenuAndTabs();

        long time5 = System.currentTimeMillis();
        Log.d(".MainActivity", "Time creating menu and tabs: " + (time5 -time4) + " ms");

        // Score tab is a little bit special, we don't need to add it
    }

    private void createTabConfiguration() {
        tabsLayouts.add(new TabConfiguration(R.id.profile, true, null, R.id.profileScore, null));
        tabsLayouts.add(new TabConfiguration(R.id.c1General, true, null, R.id.generalCase1, R.id.generalAvg));
        tabsLayouts.add(new TabConfiguration(R.id.c1RDT, true, null, R.id.rdtCase1, R.id.rdtAvg));
        tabsLayouts.add(new TabConfiguration(R.id.c1Microcospy, true, null, R.id.microscopyCase1, R.id.microscopyAvg));
        tabsLayouts.add(new TabConfiguration(R.id.c2General, true, null, R.id.generalCase2, R.id.generalAvg));
        tabsLayouts.add(new TabConfiguration(R.id.c2RDT, true, null, R.id.rdtCase2, R.id.rdtAvg));
        tabsLayouts.add(new TabConfiguration(R.id.c2Microscopy, true, null, R.id.microscopyCase2, R.id.microscopyAvg));
        tabsLayouts.add(new TabConfiguration(R.id.c3General, true, null, R.id.generalCase3, R.id.generalAvg));
        tabsLayouts.add(new TabConfiguration(R.id.c3RDT, true, null, R.id.rdtCase3, R.id.rdtAvg));
        tabsLayouts.add(new TabConfiguration(R.id.c3Microscopy, true, null, R.id.microscopyCase3, R.id.microscopyAvg));
        tabsLayouts.add(new TabConfiguration(R.id.adherence, false, R.layout.adherencetab, R.id.adherenceScore, null));
        tabsLayouts.add(new TabConfiguration(R.id.feedback, false, null, R.id.feedbackScore, null));
        tabsLayouts.add(new TabConfiguration(R.id.environmentMaterial, true, null, R.id.envAndMatScore, null));
        tabsLayouts.add(new TabConfiguration(R.id.reporting, false, R.layout.reportingtab, R.id.reportingScore, null));
        tabsLayouts.add(new TabConfiguration(R.id.iqaEQA, false, R.layout.iqatab, R.id.iqaeqaScore, null));
        tabsLayouts.add(new TabConfiguration(R.id.scoreSummary, false, R.layout.scoretab, null, null));
    }

    private void createMenuAndTabs() {
        // We get the tab selector spinner and add the different options
        Spinner tabSpinner = (Spinner) this.findViewById(R.id.tabSpinner);
        ArrayList<String> spinnerArray = new ArrayList<String>();
        // We get all tabs and insert their content in their layout
        final List<Tab> tabList = Tab.listAll(Tab.class);
        for (int i = 0; i< tabsLayouts.size(); i++){
            Tab tabItem = tabList.get(i);
            Log.d(".MainActivity", "Adding tab to menu and generating tab " + tabItem.toString());
            //Menu
            spinnerArray.add(tabItem.getName());
            //Insert tab
            Layout.insertTab(this, tabItem, tabsLayouts.get(i));
            Log.d(".MainActivity", "Tab " + tabItem.toString() + " created");
        }
        tabSpinner.setTag(tabsLayouts);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, spinnerArray);
        tabSpinner.setAdapter(spinnerArrayAdapter);

        Log.i(".MainActivity", "Adding tab event");
        // Now we manage the selection event, to launch a tab change
        tabSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner spinner = (Spinner) parentView;
                Tab triggeredTab = (Tab) ((View) LayoutUtils.findParentRecursively(spinner, R.id.Grid).findViewById(tabsLayouts.get(position).getTabId())).getTag(); // Just in case in the future we need to have the tab captured
                TabHost tabHost = (TabHost) LayoutUtils.findParentRecursively(spinner, R.id.tabHost);
                tabHost.setCurrentTab(position);
                Log.i(".MainActivity", "Tab selected: " + triggeredTab.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        Log.i(".MainActivity", "Creating event for menu");
        final TabHost tabHost = (TabHost)this.findViewById(R.id.tabHost);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener()
        {
            @Override
            public void onTabChanged(String tabId) {
                for (int i = 0; i < tabsLayouts.size(); i++){
                    final String idLong = Long.toString(tabList.get(i).getId());
                    if (tabHost.getCurrentTabTag().equals(idLong)) {
                        tabHost.getCurrentTabView().setOnTouchListener(
                                new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                            if (tabHost.getCurrentTabTag().equals(idLong)) {
                                                for (int k = 0; k < tabsLayouts.size(); k++) {
                                                    String idLong2 = Long.toString(tabList.get(k).getId());
                                                    if (idLong2 != idLong) {
                                                        tabHost.setCurrentTabByTag(idLong2);
                                                    }
                                                }
                                                tabHost.setCurrentTabByTag(idLong);
                                            }
                                            return false;
                                        }
                                        return false;
                                    }
                                });
                    }
                }
            }
        });
    }

    private void initializeApp() {
        setContentView(R.layout.main_layout);
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        actionBar.setLogo(R.drawable.malariapp_mosquito);
        actionBar.setDisplayUseLogoEnabled(true);
    }

    // Method for capturing "Save" and "Reset" events
    public void saveClearResults(View view) {

        if (view.getId() == R.id.save) {
            Log.d(".MainActivity", "Button save pressed");
        }
        else if (view.getId() == R.id.clear) {
            Log.d(".MainActivity", "Button clear pressed");
            ViewGroup root = (LinearLayout) LayoutUtils.findParentRecursively(view, R.id.Grid);
            List<View> viewsToClear = LayoutUtils.getChildrenByTag(root, R.id.QuestionTypeTag, null);
            for (View viewToClear: viewsToClear){
                LayoutUtils.resetComponent(viewToClear);
            }
        }

    }

    public static List<TabConfiguration> getTabsLayouts() {
        return tabsLayouts;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
