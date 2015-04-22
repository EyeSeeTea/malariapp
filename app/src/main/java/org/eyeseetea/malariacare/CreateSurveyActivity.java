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

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;


public class CreateSurveyActivity extends ActionBarActivity {

    // UI references.
    private Spinner orgUnitView;
    private Spinner programView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_survey);

        //Create default options
        OrgUnit orgUnitDefaultOption = new OrgUnit(Constants.DEFAULT_SELECT_OPTION);
        Program programDefaultOption = new Program(Constants.DEFAULT_SELECT_OPTION);

        //Populate Organization Unit DDL
        List<OrgUnit> orgUnitList = OrgUnit.listAll(OrgUnit.class);
        orgUnitList.add(0, orgUnitDefaultOption);
        ArrayAdapter orgUnitAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, orgUnitList);
        orgUnitAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        orgUnitView = (Spinner) findViewById(R.id.org_unit);
        orgUnitView.setAdapter(orgUnitAdapter);

        //Populate Program View DDL
        List<Program> programList = Program.listAll(Program.class);
        programList.add(0, programDefaultOption);
        ArrayAdapter programAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, programList);
        programAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        programView = (Spinner) findViewById(R.id.program);
        programView.setAdapter(programAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_form, menu);
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

    /** Called when the user clicks the Send button */
    public void createSurvey(View view) {

        OrgUnit orgUnit = (OrgUnit) orgUnitView.getSelectedItem();
        Program program = (Program) programView.getSelectedItem();

        Survey survey = new Survey(orgUnit, program, Session.getUser());
        survey.save();

        Session.setSurvey(survey);

        Intent surveyIntent = new Intent(this, MainActivity.class);
        startActivity(surveyIntent);
    }
}
