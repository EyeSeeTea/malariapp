package org.eyeseetea.malariacare;

import android.app.ActionBar;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.layout.Layout;
import org.eyeseetea.malariacare.utils.PopulateDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(".MainActivity", "App started");
        setContentView(R.layout.main_layout);
        final ActionBar actionBar = getActionBar();

//        adb pull /data/data/org.eyeseetea.malariacare/databases/malariacare.db ~/malariacare.db

        // We import the initial data in case it has been done yet
        if (Tab.count(Tab.class, null, null)==0) {
            AssetManager assetManager = getAssets();
            PopulateDB.populateDB(assetManager);
        }

        // We get all tabs and insert their content in their layout
        Integer tabsLayouts [] = {new Integer(R.id.tab1),
                new Integer(R.id.tab2),
                new Integer(R.id.tab3),
                new Integer(R.id.tab4),
                new Integer(R.id.tab5),
                new Integer(R.id.tab6),
                new Integer(R.id.tab7),
                new Integer(R.id.tab8),
                new Integer(R.id.tab9),
                new Integer(R.id.tab10),
                new Integer(R.id.tab11),
                new Integer(R.id.tab12),
                new Integer(R.id.tab13),
                new Integer(R.id.tab14),
                new Integer(R.id.tab15),
                new Integer(R.id.tab16)};
        List<Integer> tabLayoutList = Arrays.asList(tabsLayouts);
        List<Tab> tabList2 = Tab.listAll(Tab.class);
        int tabLayout;
        for (int i = 0; i< tabLayoutList.size(); i++){
            Tab tabItem = tabList2.get(i);
            Log.i(".MainActivity", tabItem.toString());

            Layout.insertTab(this, tabItem, tabLayoutList.get(i));
        }
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
            Log.i(".MainActivity", "Button save pressed");
        }
        else if (view.getId() == R.id.clear) {
            Log.i(".MainActivity", "Button clear pressed");
        }
        ArrayList<View> allViewsWithinMyTopView = getAllChildren(view);
        for (View child : allViewsWithinMyTopView) {
            if (child instanceof TextView) {
                TextView childTextView = (TextView) child;
                System.out.println(childTextView.getText().toString());
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
}
