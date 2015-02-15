package org.eyeseetea.malariacare;

import android.app.ActionBar;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static junit.framework.Assert.*;


public class MainActivity extends ActionBarActivity {

    protected List<Question> getQuestionSet(int size){
        // Just to be able to work, I need a question simulator, in order to emulate the DB entries
        Random r = new Random();
        String optionSets [] = {
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
        Log.i(".MainActivity","optionSet created");
        List<Question> questionSimulator = new ArrayList<>();
        for (int i=0; i<size; i++){
            Log.i(".MainActivity","creating question " + i);
            // The text of the question will be fixed
            String statement = "Question number " + Integer.toString(i);
            // Te OptionSet will be randomized
            String optionSet = optionSets[r.nextInt(optionSets.length)];
            Log.i(".MainActivity","optionSet " + optionSet);
            // We finally add both to the question array
            questionSimulator.add(new Question(statement, optionSet));
            Log.i(".MainActivity","question finished");
        }
        Log.i(".MainActivity","questions simulated");
        return questionSimulator;
    }

    protected int insertLayout(String layout, GridLayout parent){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v;
        int child;
        switch(layout){
            case "asked":
                child = R.layout.asked;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "done":
                child = R.layout.done;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "yesno":
                child = R.layout.yesno;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "yesNoNA":
                child = R.layout.yesnona;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "yesNoAsked":
                child = R.layout.yesnonotanswered;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "yesNoUnkasked":
                child = R.layout.yesnounkasked;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "gender":
                child = R.layout.gender;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "officer":
                child = R.layout.officer;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "malResults":
                child = R.layout.malresults;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "malDiagnose":
                child = R.layout.maldiagnose;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "malSpecies":
                child = R.layout.malspecies;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
            case "result":
                child = R.layout.result;
                v = inflater.inflate(child, parent, false);
                parent.addView(v);
                return child;
        }
        // For not found layout
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(".MainActivity", "App started");
        setContentView(R.layout.main_layout);

        // We get a set of questions for our layout
        List<Question> questions;
        questions = getQuestionSet(10);

        // We take the Layouts for adding the content
        GridLayout body = (GridLayout) this.findViewById(R.id.Body);

        // We add the questions
        for(Question question: questions){
            /* Each optionSet has its own layout defined by <optionSetString>.xml
               With the insertLayout function, we're trying to insert the question layout into the
                parent layout. The function returns the question layout. We assert is always != -1
             */
            assertTrue(insertLayout(question.getOptionSet(), body)!=-1);


            //getLayoutInflater().inflate(layoutholder, placeholder);
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

    public void sendMessage(View view) {
        Log.i(".MainActivity", "Button pressed");
    }
}
