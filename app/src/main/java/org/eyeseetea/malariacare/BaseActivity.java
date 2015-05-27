/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Spinner;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.OrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.ProgramArrayAdapter;
import org.eyeseetea.malariacare.layout.dialog.DialogDispatcher;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;


public abstract class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setTheme(R.style.EyeSeeTheme);
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);
        // Manage uncaught exceptions that may occur
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_general, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                finish();
                Intent settingsIntent = new Intent(BaseActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;// TODO: implement the settings menu
            case R.id.action_pull:
                return true;// TODO: implement the DHIS pull
            case R.id.action_license:
                Log.d(".MainActivity", "User asked for license dialog");
                DialogDispatcher mf = DialogDispatcher.newInstance(new View(this)); // FIXME: here we create a View just to be able to show the dialog...this shouldn't be needed
                mf.showDialog(getFragmentManager(), DialogDispatcher.LICENSE_DIALOG);
                break;
            case R.id.action_about:
                Log.d(".MainActivity", "User asked for about dialog");
                DialogDispatcher aboutD = DialogDispatcher.newInstance(new View(this)); // FIXME: here we create a View just to be able to show the dialog...this shouldn't be needed
                aboutD.showDialog(getFragmentManager(), DialogDispatcher.ABOUT_DIALOG);
                break;
            case R.id.action_logout:
                Log.d(".MainActivity", "User asked for logging out");
                new AlertDialog.Builder(this)
                        .setTitle(getApplicationContext().getString(R.string.settings_menu_logout))
                        .setMessage(getApplicationContext().getString(R.string.dialog_content_logout_confirmation))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                List<Survey> surveys = ReadWriteDB.getAllNotSentSurveys();
                                for (Survey survey: surveys){
                                    survey.delete();
                                }
                                Session.getUser().delete();
                                Session.setUser(null);
                                Session.setSurvey(null);
                                Session.setAdapter(null);
                                finish();
                                Intent loginIntent = new Intent(BaseActivity.this, LoginActivity.class);
                                startActivity(loginIntent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    /** Called when the user clicks the New Survey button */
    public void newSurvey(View view) {
        finish();
        Intent createSurveyIntent = new Intent(this, CreateSurveyActivity.class);
        startActivity(createSurveyIntent);
    }

}
