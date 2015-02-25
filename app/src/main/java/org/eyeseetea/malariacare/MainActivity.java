package org.eyeseetea.malariacare;

import android.app.ActionBar;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView;


import org.eyeseetea.malariacare.data.Option;
import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.layout.Layout;
import org.eyeseetea.malariacare.utils.PopulateDB;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    // layouts array configuration
    //      { Tab parent Layout ID     || add num/den layout || Child layout to include (-1 Integer for generate it programmaticaly)}
    private static Object tabsLayouts [][] = {
            {new Integer(R.id.tab1parent), new Boolean(true), new Integer(-1)},     // PROFILE
            {new Integer(R.id.tab2parent), new Boolean(true), new Integer(-1)},     // C1-GENERAL
            {new Integer(R.id.tab3parent), new Boolean(true), new Integer(-1)},     // C1-RDT
            {new Integer(R.id.tab4parent), new Boolean(true), new Integer(-1)},     // C1-MICROSCOPY
            {new Integer(R.id.tab5parent), new Boolean(true), new Integer(-1)},     // C2-GENERAL
            {new Integer(R.id.tab6parent), new Boolean(true), new Integer(-1)},     // C2-RDT
            {new Integer(R.id.tab7parent), new Boolean(true), new Integer(-1)},     // C2-MICROSCOPY
            {new Integer(R.id.tab8parent), new Boolean(true), new Integer(-1)},     // C3-GENERAL
            {new Integer(R.id.tab9parent), new Boolean(true), new Integer(-1)},     // C3-RDT
            {new Integer(R.id.tab10parent), new Boolean(true), new Integer(-1)},    // C3-MICROSCOPY
            {new Integer(R.id.tab11parent), new Boolean(false), new Integer(-1)},   // ADHERENCE
            {new Integer(R.id.tab12parent), new Boolean(false), new Integer(-1)},   // FEEDBACK
            {new Integer(R.id.tab13parent), new Boolean(true), new Integer(-1)},    // ENVIRONMENT & MATERIALS
            {new Integer(R.id.tab14parent), new Boolean(false), new Integer(-1)},   // REPORTING
            {new Integer(R.id.tab15parent), new Boolean(false), new Integer(-1)},   // IQA EQA
            {new Integer(R.id.tab16parent), new Boolean(false), R.layout.scoretab}  // SCORE (Score tab layout is fixed)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(".MainActivity", "App started");
        setContentView(R.layout.main_layout);
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        actionBar.setLogo(R.drawable.malariapp_mosquito);
        actionBar.setDisplayUseLogoEnabled(true);


//        adb pull /data/data/org.eyeseetea.malariacare/databases/malariacare.db ~/malariacare.db

        // We import the initial data in case it has been done yet
        if (Tab.count(Tab.class, null, null)==0) {
            AssetManager assetManager = getAssets();
            PopulateDB.populateDB(assetManager);
        }



        // We get the tab selector spinner and add the different options
        Spinner tabSpinner = (Spinner) this.findViewById(R.id.tabSpinner);
        ArrayList<String> spinnerArray = new ArrayList<String>();
        // We get all tabs and insert their content in their layout
        final List<Object[]> tabLayoutList = Arrays.asList(tabsLayouts);
        final List<Tab> tabList2 = Tab.listAll(Tab.class);
        int tabLayout;
        for (int i = 0; i< tabLayoutList.size(); i++){
            Tab tabItem = tabList2.get(i);
            spinnerArray.add(tabItem.getName());
            Log.d(".MainActivity", tabItem.toString());
            Layout.insertTab(this, tabItem, ((Integer)tabLayoutList.get(i)[0]).intValue(), ((Boolean)tabLayoutList.get(i)[1]).booleanValue(), ((Integer)tabLayoutList.get(i)[2]).intValue());
        }
        // We
        tabSpinner.setTag(tabsLayouts);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, spinnerArray);
        tabSpinner.setAdapter(spinnerArrayAdapter);

        // Now we manage the selection event, to launch a tab change
        tabSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner spinner = (Spinner) parentView;
                Tab triggeredTab = (Tab)((View)Utils.findParentRecursively(spinner, R.id.Grid).findViewById(((Integer)tabsLayouts[position][0]).intValue())).getTag(); // Just in case in the future we need to have the tab captured
                TabHost tabHost = (TabHost) Utils.findParentRecursively(spinner, R.id.tabHost);
                tabHost.setCurrentTab(position);
                Log.i(".MainActivity", "Tab selected: " +triggeredTab.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        // We manage the event for changing between tabs
        // from http://stackoverflow.com/questions/8311236/how-to-call-tab-onclick-and-ontabchange-for-same-tab
        // I hope it works
        final TabHost tabHost = (TabHost)this.findViewById(R.id.tabHost);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener()
        {
            @Override
            public void onTabChanged(String tabId) {
                for (int i = 0; i < tabLayoutList.size(); i++){
                    final String idLong = Long.toString(tabList2.get(i).getId());
                    if (tabHost.getCurrentTabTag().equals(idLong)) {
                        tabHost.getCurrentTabView().setOnTouchListener(
                            new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                        if (tabHost.getCurrentTabTag().equals(idLong)) {
                                            for (int k = 0; k < tabLayoutList.size(); k++) {
                                                String idLong2 = Long.toString(tabList2.get(k).getId());
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

        // Score tab is a little bit special, we don't need to add it
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

    public void saveClearResults(View view) {

        if (view.getId() == R.id.save) {
            Log.d(".MainActivity", "Button save pressed");
        }
        else if (view.getId() == R.id.clear) {
            Log.d(".MainActivity", "Button clear pressed");
        }
        ArrayList<View> allViewsWithinMyTopView = getAllChildren(view);
        for (View child : allViewsWithinMyTopView) {
            if (child instanceof TextView) {
                TextView childTextView = (TextView) child;
                Log.d(".MainActivity", childTextView.getText().toString());
            }
            else if(child instanceof Spinner){

            }
        }

    }

    private ArrayList<View> getAllChildren(View v) {

        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<View>();

        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    public static List<Integer> getLayoutIds(){
        List<Integer> ids = new ArrayList<Integer>();
        for(int i=0; i<tabsLayouts.length; i++){
            ids.add((Integer) tabsLayouts[i][0]);
        }
        return ids;
    }
}
