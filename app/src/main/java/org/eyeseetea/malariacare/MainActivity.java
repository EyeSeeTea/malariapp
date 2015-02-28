package org.eyeseetea.malariacare;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.layout.Layout;
import org.eyeseetea.malariacare.utils.PopulateDB;
import org.eyeseetea.malariacare.utils.TabConfiguration;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final List<TabConfiguration> tabsLayouts = new ArrayList<TabConfiguration>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(".MainActivity", "Starting App");
        initializeApp();

        Log.i(".MainActivity", "Starting App");
        createTabConfiguration();

        // We import the initial data in case it has been done yet
        if (Tab.count(Tab.class, null, null)==0) {
            Log.i(".MainActivity", "Populating DB");
            AssetManager assetManager = getAssets();
            PopulateDB.populateDB(assetManager);
            Log.i(".MainActivity", "DB populated");
        }

        Log.i(".MainActivity", "Initializing Menu and generating tabs");
        createMenuAndTabs();


        // Score tab is a little bit special, we don't need to add it
    }

    private void createTabConfiguration() {
        tabsLayouts.add(new TabConfiguration(R.id.profile, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.c1General, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.c1RDT, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.c1Microcospy, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.c2General, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.c2RDT, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.c2Microscopy, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.c3General, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.c3RDT, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.c3Microscopy, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.adherence, false, null));
        tabsLayouts.add(new TabConfiguration(R.id.feedback, false, null));
        tabsLayouts.add(new TabConfiguration(R.id.environmentMaterial, true, null));
        tabsLayouts.add(new TabConfiguration(R.id.reporting, false, R.layout.reportingtab));
        tabsLayouts.add(new TabConfiguration(R.id.iqaEQA, false, null));
        tabsLayouts.add(new TabConfiguration(R.id.scoreSummary, false, R.layout.scoretab));
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
                Tab triggeredTab = (Tab) ((View) Utils.findParentRecursively(spinner, R.id.Grid).findViewById(tabsLayouts.get(position).getTabId())).getTag(); // Just in case in the future we need to have the tab captured
                TabHost tabHost = (TabHost) Utils.findParentRecursively(spinner, R.id.tabHost);
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

    public void saveClearResults(View view) {

        if (view.getId() == R.id.save) {
            Log.d(".MainActivity", "Button save pressed");
        }
        else if (view.getId() == R.id.clear) {
            Log.d(".MainActivity", "Button clear pressed");
        }
        List<View> allViewsWithinMyTopView = Utils.getAllChildren(view);
        for (View child : allViewsWithinMyTopView) {
            if (child instanceof TextView) {
                TextView childTextView = (TextView) child;
                Log.d(".MainActivity", childTextView.getText().toString());
            }
            else if(child instanceof Spinner){

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
