package org.eyeseetea.malariacare;

import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
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

    protected int getLayout(String layout){
        switch(layout){
            case "asked":
                return R.layout.asked;
            case "done":
                return R.layout.done;
            case "yesno":
                return R.layout.yesno;
            case "yesNoNA":
                return R.layout.yesnona;
            case "yesNoAsked":
                return R.layout.yesnonotanswered;
            case "yesNoUnkasked":
                return R.layout.yesnounkasked;
            case "gender":
                return R.layout.gender;
            case "officer":
                return R.layout.officer;
            case "malResults":
                return R.layout.malresults;
            case "malDiagnose":
                return R.layout.maldiagnose;
            case "malSpecies":
                return R.layout.malspecies;
            case "result":
                return R.layout.result;
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
        GridView body = (GridView) findViewById(R.id.Body);

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        // We add the questions
        for(Question question: questions){
            // Each optionSet has its own layout defined by <optionSetString>.xml
            int layoutholder = getLayout(question.getOptionSet());
            assertEquals(-1, layoutholder);
            getLayoutInflater().inflate(layoutholder, body);
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
