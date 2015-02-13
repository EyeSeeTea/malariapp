package org.psi.malariacare;

import android.app.ActionBar;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    protected Object[][] getQuestionSet(int size){
        // Just to be able to work, I need a question simulator, in order to emulate the DB entries
        Random r = new Random();
        int questionNumber = size;
        String optionSet [] = {"Asked", "Done", "Yesno", "YesNoNA", "YesNoAsked", "YesNoUnkasked",
                "Gender", "Officer", "MAL Results", "MAL Diagnose", "MAL Species",
                "Result"};

        Object questionSimulator [][] = new Object [questionNumber][];
        for (int i=0; i<questionNumber; i++){
            // The text of the question will be fixed
            String questionText = "Question number " + new Integer(i).toString();
            // Te OptionSet will be randomized
            String optionSetNum = optionSet[r.nextInt(optionSet.length)];
            // We finally add both to the question array
            questionSimulator[i] = new String[2];
            questionSimulator[i][1] = questionText;
            questionSimulator[i][2] = optionSetNum;
        }
        return questionSimulator;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        /*MalariaCareDbHelper malariaCareDb = new MalariaCareDbHelper(this);

        //Query Database
        SQLiteDatabase db = malariaCareDb.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                MalariaCareDb.DataElements._ID,
                MalariaCareDb.DataElements.COLUMN_NAME_TITLE,
                MalariaCareDb.DataElements.COLUMN_NAME_TAB,
                MalariaCareDb.DataElements.COLUMN_NAME_OPTION_SET
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                MalariaCareDb.DataElements._ID + " DESC";

        Cursor c = db.query(
                MalariaCareDb.DataElements.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        c.moveToFirst();
        String dataElementTitle = c.getString(
                c.getColumnIndexOrThrow(MalariaCareDb.DataElements.COLUMN_NAME_TITLE)
        );*/


        // We get a set of questions for our layout
        Object questions [][] = getQuestionSet(500);

        // We take the Layouts for adding the content
        LinearLayout header = (LinearLayout) findViewById(R.id.Header);
        LinearLayout body = (LinearLayout) findViewById(R.id.Body);
        LinearLayout footer = (LinearLayout) findViewById(R.id.Footer);

        // We add the questions
        for(int i=0;i<questions.length;i++){
            TextView tv = new TextView(this);
            tv.setText(questions[i][0].toString());
            body.addView(tv);
        }

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        //linearLayout.setWeightSum(6f);
        //linearLayout.setLayoutParams(layoutParams);

        // Creating a new TextView
        //TextView tv = new TextView(this);
        //tv.setText(dataElementTitle);
        //tv.setLayoutParams(layoutParams);
        //linearLayout.addView(tv);


        // Creating a new EditText
        //EditText et=new EditText(this);
        //et.setLayoutParams(layoutParams);
        //linearLayout.addView(et);

        //setContentView(linearLayout, layoutParams);
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
        System.out.println("taka");
    }
}
