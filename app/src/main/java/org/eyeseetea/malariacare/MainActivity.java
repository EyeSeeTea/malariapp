package org.eyeseetea.malariacare;

import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    protected List<Question> getQuestionSet(int size){
        // Just to be able to work, I need a question simulator, in order to emulate the DB entries
        Random r = new Random();
        String optionSets [] = {
                "Asked",
                "Done",
                "Yesno",
                "YesNoNA",
                "YesNoAsked",
                "YesNoUnkasked",
                "Gender",
                "Officer",
                "MAL Results",
                "MAL Diagnose",
                "MAL Species",
                "Result"
        };
        Log.i(".MainActivity","optionSet created");
        List<Question> questionSimulator = new ArrayList<>();
        for (int i=0; i<size; i++){
            // The text of the question will be fixed
            String statement = "Question number " + Integer.toString(i);
            // Te OptionSet will be randomized
            String optionSet = optionSets[r.nextInt(optionSets.length)];
            // We finally add both to the question array
            questionSimulator.add(new Question(statement, optionSet));
        }
        Log.i(".MainActivity","questions simulated");
        return questionSimulator;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(".MainActivity", "App started");
        setContentView(R.layout.main_layout);

        // We get a set of questions for our layout
        //List<Question> questions;
        //questions = getQuestionSet(2);

        // We take the Layouts for adding the content
        ScrollView bodyGNR = (ScrollView) findViewById(R.id.BodyGNR);

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        // We add the questions
        //for(Object[] questions)
        //for(Question question: questions){
        for (int i=0; i<3; i++){
            //Log.i(".MainActivity", question.toString());
            TextView tv = new TextView(this);
            tv.setLayoutParams(layoutParams);
            //tv.setText(questions.toString());
            tv.setText("probando");
            bodyGNR.addView(tv);
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
