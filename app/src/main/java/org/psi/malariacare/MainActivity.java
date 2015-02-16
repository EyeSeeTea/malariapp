package org.psi.malariacare;

import android.app.ActionBar;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orm.SugarRecord;

import org.psi.malariacare.data.Header;
import org.psi.malariacare.data.Question;
import org.psi.malariacare.data.Tab;
import org.psi.malariacare.database.MalariaCareTables;
import org.psi.malariacare.utils.PopulateDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.orm.SugarApp.getSugarContext;
//import org.psi.malariacare.database.MalariaCareDbHelper;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        File dbFile = getDatabasePath("malariacare.db");
//        adb pull /data/data/org.psi.malariacare/databases/malariacare.db ~/malariacare.db

        if (Tab.count(Tab.class, null, null)==0) {
            AssetManager assetManager = getAssets();
            PopulateDB.populateDB(assetManager);
        }

        List<Tab> tabList2 = Tab.listAll(Tab.class);
        for (Tab tabItem : tabList2){
            //codigo
            System.out.println(tabItem.toString());
        }

        // Creating a new LinearLayout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        linearLayout.setWeightSum(6f);
        linearLayout.setLayoutParams(layoutParams);


        TextView tv;

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

        setContentView(linearLayout, layoutParams);
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
