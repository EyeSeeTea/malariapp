package org.eyeseetea.malariacare;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.Persistence;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.Layout;
import org.eyeseetea.malariacare.layout.configuration.LayoutConfiguration;
import org.eyeseetea.malariacare.layout.configuration.TabConfiguration;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.layout.dialog.DialogDispatcher;
import org.eyeseetea.malariacare.database.utils.PopulateDB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends ActionBarActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //long time = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        Log.i(".MainActivity", "Starting App");
        initializeApp();

        //long time3 = System.currentTimeMillis();
        //Log.d(".MainActivity", "Time Configuring tab: " + (time3 -time2) + " ms");

        Log.i(".MainActivity", "Initializing Layout Configuration");
        LayoutConfiguration.initialize(Persistence.getTabs());

        //Intializing compositive score register
        for (CompositiveScore compositiveScore : CompositiveScore.listAll(CompositiveScore.class)) {
            ScoreRegister.registerScore(compositiveScore);
        }

        //long time4 = System.currentTimeMillis();
        //Log.d(".MainActivity", "Time populating DB: " + (time4 -time3) + " ms");

        Log.i(".MainActivity", "Initializing Menu and generating tabs");
        createMenuAndTabs();

        //long time5 = System.currentTimeMillis();
        //Log.d(".MainActivity", "Time creating menu and tabs: " + (time5 -time4) + " ms");

        createBreadCrumb();


    }

    private void createBreadCrumb() {
        LinearLayout breadCrumbsView = (LinearLayout) findViewById(R.id.breadCrumbs);

        TextView dashboardBreadCrumbsView = new TextView(this);
        dashboardBreadCrumbsView.setText("Dashboard");
        dashboardBreadCrumbsView.setTextColor(Color.parseColor("#1e506c"));
        dashboardBreadCrumbsView.setTypeface(null, Typeface.BOLD);
        dashboardBreadCrumbsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dashboardIntent = new Intent(view.getContext(), DashboardActivity.class);
                startActivity(dashboardIntent);
            }
        });
        breadCrumbsView.addView(dashboardBreadCrumbsView);

        TextView surveyBreadCrumbsView = new TextView(this);
        surveyBreadCrumbsView.setText(" > Survey");
        breadCrumbsView.addView(surveyBreadCrumbsView);
    }

    private void createMenuAndTabs() {
        // We get all tabs and insert their content in their layout
        //final List<Tab> tabList = Persistence.getTabs();
        // We get the tab selector spinner and add the different options
        List<String> spinnerArray = new ArrayList<String>();
        for (Map.Entry<Tab, TabConfiguration> tabConfigurationEntry : LayoutConfiguration.getTabsConfiguration().entrySet()){
            Log.d(".MainActivity", "Adding tab to menu and generating tab " + tabConfigurationEntry.getKey().toString());
            //Menu
            spinnerArray.add(tabConfigurationEntry.getKey().getName());
            //Insert tab
            Layout.insertTab(this, tabConfigurationEntry.getKey());
            Log.d(".MainActivity", "Tab " + tabConfigurationEntry.getKey().toString() + " created");
        }

        Spinner tabSpinner = (Spinner) this.findViewById(R.id.tabSpinner);
        //tabSpinner.setTag(LayoutConfiguration.getTabsConfiguration());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.main_spinner_item, spinnerArray);
        tabSpinner.setAdapter(spinnerArrayAdapter);

        Log.i(".MainActivity", "Adding tab event");
        // Now we manage the selection event, to launch a tab change
        tabSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                TabHost tabHost = (TabHost) LayoutUtils.findParentRecursively((Spinner) parentView, R.id.tabHost);
                tabHost.setCurrentTab(position);
                Log.i(".MainActivity", "Tab selected: " + position);
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
                for (Map.Entry<Tab, TabConfiguration> tabConfigurationEntry : LayoutConfiguration.getTabsConfiguration().entrySet()){
                    final String idLong = Long.toString(tabConfigurationEntry.getKey().getId());
                    if (tabHost.getCurrentTabTag().equals(idLong)) {
                        tabHost.getCurrentTabView().setOnTouchListener(
                                new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                            if (tabHost.getCurrentTabTag().equals(idLong)) {
                                                for (Map.Entry<Tab, TabConfiguration> tabConfigurationEntry : LayoutConfiguration.getTabsConfiguration().entrySet()){
                                                    String idLong2 = Long.toString(tabConfigurationEntry.getKey().getId());
                                                    if (!idLong2.equals(idLong)) {
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
        actionBar.setLogo(R.drawable.qualityapp_logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.qualityapp_logo);
    }

    // Method for capturing "Save" and "Reset" events
    public void saveClearResults(View view) {
        if (view.getId() == R.id.save) {
            Log.d(".MainActivity", "Button save pressed");
        }
        else if (view.getId() == R.id.clear) {
            Log.d(".MainActivity", "Button clear pressed");
            DialogDispatcher mf = DialogDispatcher.newInstance(view);
            mf.showDialog(getFragmentManager(), DialogDispatcher.CLEAR_DIALOG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.layout.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                return true;// TODO: implement the settings menu
            case R.id.action_pull:
                return true;// TODO: implement the DHIS pull
            case R.id.action_license:
                Log.d(".MainActivity", "User asked for license");
                DialogDispatcher mf = DialogDispatcher.newInstance(this.getCurrentFocus());
                mf.showDialog(getFragmentManager(), DialogDispatcher.LICENSE_DIALOG);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}