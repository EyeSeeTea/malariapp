package org.eyeseetea.malariacare;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.GridLayout;

import java.util.ArrayList;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import org.eyeseetea.malariacare.data.Header;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.utils.PopulateDB;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import junit.framework.Assert;


import java.util.List;
//import org.eyeseetea.malariacare.database.MalariaCareDbHelper;

public class MainActivity extends ActionBarActivity {
    // Some constants
    public static final int DROPDOWN_LIST = 1,
                            INT = 2,
                            LONG_TEXT = 3,
                            SHORT_TEXT = 4,
                            SHORT_DATE = 5,
                            LONG_DATE = 6;

    protected int insertTab(Tab tab, int parent) {
        GridLayout layoutParent = (GridLayout) this.findViewById(parent);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v;

        int child = -1;

        Log.i(".MainActivity", "before getting tab");
        String name = tab.getName();
        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Question tab");
        tabSpec.setIndicator(name);
        tabSpec.setContent(parent);
        tabHost.addTab(tabSpec);
        Log.i(".MainActivity", "after adding tab ");

        List<Header> headers = tab.getHeaders();
        for (Header header: headers){
            child = R.layout.headers;
            Log.i(".MainActivity", "reading header " + header.toString());
            View headerView = inflater.inflate(child, layoutParent, false);
            TextView headerText = (TextView) headerView.findViewById(R.id.headerName);
            headerText.setText(header.getName());
            layoutParent.addView(headerView);
            Log.i(".MainActivity", "header " + header.toString() + " added");
            List<Question> questionList = header.getQuestions();
            for (Question question : questionList){
                // We first introduce the Header Text

                // The statement is present in every kind of question
                TextView statement;
                switch(question.getAnswer().getOutput()){
                    case DROPDOWN_LIST:
                        break;
                    case INT:
                        break;
                    case LONG_TEXT:
                        break;
                    case SHORT_TEXT:
                        break;
                    case SHORT_DATE: case LONG_DATE:
                        break;
                }
            }
        }
/*      // select the layout and put it in child
        Log.i(".MainActivity", "question statement: " + testQuestion.getStatement());
        v = inflater.inflate(child, layoutParent, false);
        statement = (TextView) v.findViewById(R.id.statement);
        Log.i(".MainActivity", "previous statement: " + statement.getText());
        statement.setText("question statement");
        Log.i(".MainActivity", "later statement: " + statement.getText());
        layoutParent.addView(v);
        // For not found layout, child will be -1*/
        return child;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(".MainActivity", "App started");
        setContentView(R.layout.main_layout);
        final ActionBar actionBar = getActionBar();


//        File dbFile = getDatabasePath("malariacare.db");
//        adb pull /data/data/org.eyeseetea.malariacare/databases/malariacare.db ~/malariacare.db

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


        // We import the initial data in case it has been done yet
        if (Tab.count(Tab.class, null, null)==0) {
            AssetManager assetManager = getAssets();
            PopulateDB.populateDB(assetManager);
        }

        // We get all tabs and insert their content in their layout
        Iterator<Integer> tabLayoutIterator = tabLayoutList.iterator();
        List<Tab> tabList2 = Tab.listAll(Tab.class);
        int tabLayout;
        for (int i = 0; i< tabLayoutList.size(); i++){
            Tab tabItem = tabList2.get(i);
            Log.i(".MainActivity", tabItem.toString());
            insertTab(tabItem, tabLayoutList.get(i));
        }

        /*
        //"Profile"
        Tab currentTab = Tab.findById(Tab.class, 10L);
        List<Header> headerList = currentTab.getHeaders();
        for (Header header : headerList){
            //codigo
            System.out.println(header.toString());
            List<Question> questionList = header.getQuestions();
            for (Question question : questionList){
                //codigo

                System.out.println(question.toString());
                System.out.println("Hijos");
                System.out.println(question.getQuestion());
                // Creating a new TextView
                tv = new TextView(this);
                tv.setText(question.getForm_name());
                tv.setLayoutParams(layoutParams);
                linearLayout.addView(tv);
            }
        }


        // Creating a new EditText
        EditText et=new EditText(this);
        et.setLayoutParams(layoutParams);
        linearLayout.addView(et);

        setContentView(linearLayout, layoutParams);*/
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

    public void sendMessage(View view) {
        Log.i(".MainActivity", "Button pressed");
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
