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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.OrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.ProgramArrayAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;


public class CreateSurveyActivity extends BaseActivity {

    // UI references.
    private Spinner orgUnitView;
    private Spinner programView;
    private OrgUnit orgUnitDefaultOption;
    private Program programDefaultOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Manage uncaught exceptions that may occur
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_create_survey);

        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);

        //Create default options
        this.orgUnitDefaultOption = new OrgUnit(Constants.DEFAULT_SELECT_OPTION);
        this.programDefaultOption = new Program(Constants.DEFAULT_SELECT_OPTION);

        //Populate Organization Unit DDL
        List<OrgUnit> orgUnitList = new Select().all().from(OrgUnit.class).queryList();
        orgUnitList.add(0, orgUnitDefaultOption);
        orgUnitView = (Spinner) findViewById(R.id.org_unit);
        orgUnitView.setAdapter(new OrgUnitArrayAdapter(this, orgUnitList));

        //Populate Program View DDL
        List<Program> programList = new Select().all().from(Program.class).queryList();;
        programList.add(0, programDefaultOption);
        programView = (Spinner) findViewById(R.id.program);
        programView.setAdapter(new ProgramArrayAdapter(this, programList));

    }

    public boolean checkEverythingFilled() {
        try {
            return (!orgUnitView.getSelectedItem().equals(this.orgUnitDefaultOption) && !programView.getSelectedItem().equals(this.programDefaultOption));
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean checkSurveyDoesntExist() {
        // Read Selected Items
        OrgUnit orgUnit = (OrgUnit) orgUnitView.getSelectedItem();

        //FIXME: Once we have the tabs groups this needs to be fix
        TabGroup tabGroup = ((Program) programView.getSelectedItem()).getTabGroups().get(0);

        List<Survey> existing = Survey.getUnsentSurveys(orgUnit, tabGroup);
        return (existing == null || existing.size() == 0);
    }

    /**
     * Called when the user clicks the Send button
     */
    public void createSurvey(View view) {
        Log.i(".CreateSurveyActivity", "Saving survey and saving in session");

        // Read Selected Items
        OrgUnit orgUnit = (OrgUnit) orgUnitView.getSelectedItem();

        //FIXME: Once we have the tabs groups this needs to be fix
        TabGroup tabGroup = ((Program) programView.getSelectedItem()).getTabGroups().get(0);

        if (!checkEverythingFilled()) {
            new AlertDialog.Builder(this)
                    .setTitle(getApplicationContext().getString(R.string.dialog_title_missing_selection))
                    .setMessage(getApplicationContext().getString(R.string.dialog_content_missing_selection))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else if (!checkSurveyDoesntExist()) {
            new AlertDialog.Builder(this)
                    .setTitle(getApplicationContext().getString(R.string.dialog_title_existing_survey))
                    .setMessage(getApplicationContext().getString(R.string.dialog_content_existing_survey))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else {
            // Put new survey in session
            Survey survey = new Survey(orgUnit, tabGroup, Session.getUser());
            survey.save();
            Session.setSurvey(survey);

            //Call Survey Activity
            finishAndGo(SurveyActivity.class);
        }
    }


}
