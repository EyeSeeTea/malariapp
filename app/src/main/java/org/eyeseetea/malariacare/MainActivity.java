package org.eyeseetea.malariacare;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import org.eyeseetea.malariacare.testing.TestQuestion;
import org.eyeseetea.malariacare.testing.TestTab;
import org.eyeseetea.malariacare.utils.PopulateDB;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import junit.framework.Assert;


import java.util.List;
//import org.eyeseetea.malariacare.database.MalariaCareDbHelper;


public class MainActivity extends ActionBarActivity {

    protected List<TestQuestion> getQuestionSet(int size) {
        // Just to be able to work, I need a question simulator, in order to emulate the DB entries
        Random r = new Random();
        String optionSets[] = {
                "asked",
                "done",
                "yesno",
                "yesNoNA",
                "yesNoAsked",
                "yesNoUnkasked",
                "gender",
                "officer",
                "malResults",
                "malDiagnose",
                "malSpecies",
                "result"
        };
        Log.i(".MainActivity", "optionSet created");
        List<TestQuestion> testQuestionSimulator = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Log.i(".MainActivity", "creating question " + i);
            // The text of the question will be fixed
            String statement = "Question number " + Integer.toString(i);
            // Te OptionSet will be randomized
            String optionSet = optionSets[r.nextInt(optionSets.length)];
            Log.i(".MainActivity", "optionSet " + optionSet);
            // We finally add both to the question array
            testQuestionSimulator.add(new TestQuestion(statement, optionSet));
            Log.i(".MainActivity", "question finished");
        }
        Log.i(".MainActivity", "questions simulated");
        return testQuestionSimulator;
    }

    protected List<TestTab> getTabSet(int size){
        // Just to be able to work, I need a question simulator, in order to emulate the DB entries
        Random r = new Random();
        String tabTypes [] = {
                "Score",
                "GNR1",
                "Microscopy1",
                "RDT1",
                "GNR2",
                "Microscopy2",
                "RDT2",
                "GNR3",
                "Microscopy3",
                "GNR3"
        };
        Log.i(".MainActivity","tabSet created");
        List<TestTab> tabSimulator = new ArrayList<>();
        for (int i=0; i<size; i++) {
            Log.i(".MainActivity", "creating tab " + i);
            // Te OptionSet will be randomized
            String tabType = tabTypes[r.nextInt(tabTypes.length)];
            Log.i(".MainActivity", "tabType " + tabType);
            // We finally add both to the question array
            tabSimulator.add(new TestTab(tabType));
            Log.i(".MainActivity", "tab finished");
        }
        Log.i(".MainActivity","tabs simulated");
        return tabSimulator;
    }

    protected int insertTab(Tab tab, int parent) {
        GridLayout layoutParent = (GridLayout) this.findViewById(parent);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v;
        TextView statement;
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

/*        // select the layout and put it in child
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
        for (Tab tabItem : tabList2){
            Log.i(".MainActivity", tabItem.toString());
            Assert.assertTrue(tabLayoutIterator.hasNext());
            tabLayout = tabLayoutIterator.next().intValue();
            insertTab(tabItem, tabLayout);
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
}
