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

package org.eyeseetea.malariacare.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.test.TouchUtils;
import android.widget.Button;
import android.widget.Spinner;

import org.eyeseetea.malariacare.CreateSurveyActivity;
import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.SurveyActivity;
import org.eyeseetea.malariacare.database.utils.PopulateDB;

/**
 * Created by arrizabalaga on 19/05/15.
 */

public class CreateSurveyActivityTest extends MalariaInstrumentationTestCase<CreateSurveyActivity> {

    private static final long TIMEOUT_IN_MS=3000;

    private CreateSurveyActivity createSurveyActivity;

    private Spinner orgUnitSpinner;

    private Spinner surveySpinner;

    private Button createSurveyButton;

    private Resources res;

    public CreateSurveyActivityTest(){
        super(CreateSurveyActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        cleanDB();
        initActivity();
    }

    private void initActivity(){
        setActivityInitialTouchMode(true);
        createSurveyActivity = getActivity();
        orgUnitSpinner =(Spinner) createSurveyActivity.findViewById(R.id.org_unit);
        surveySpinner =(Spinner) createSurveyActivity.findViewById(R.id.program);
        createSurveyButton =(Button) createSurveyActivity.findViewById(R.id.create_form_button);
        res = getInstrumentation().getTargetContext().getResources();
    }

    @Override
    protected void tearDown() throws Exception{
        super.tearDown();
        cleanDB();
    }

    public void test_preconditions() {
        assertNotNull("orgUnitSpinner is null", orgUnitSpinner);
        assertNotNull("surveySpinner is null", surveySpinner);
        assertNotNull("createSurveyButton is null", createSurveyButton);
    }

    public void test_no_selection_no_survey(){
        //GIVEN

        //WHEN
        TouchUtils.clickView(this, createSurveyButton);

        //THEN
        getInstrumentation().waitForIdleSync();

        assertTrue("Alert dialog not showing", createSurveyActivity.isAlertDialogShowing());
    }

//    public void test_selection_creates_survey(){
//        //GIVEN
//        populateData(createSurveyActivity);
//        createSurveyActivity.reloadData();
//
//        Instrumentation.ActivityMonitor receiverActivityMonitor =getInstrumentation().addMonitor(SurveyActivity.class.getName(),null, false);
//        setSelection(orgUnitSpinner, 0);
//        setSelection(surveySpinner, 0);
//
//        //WHEN
//        TouchUtils.clickView(this, createSurveyButton);
//
//        //THEN
//        SurveyActivity surveyActivity = (SurveyActivity) receiverActivityMonitor.waitForActivityWithTimeout(TIMEOUT_IN_MS);
//        assertNotNull("SurveyActivity is not started", surveyActivity);
//
//        //CLEANUP
//        surveyActivity.finish();
//    }

    private void setSelection(final Spinner spinner, final int index){
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(index);
            }
        });
    }

    public void callActivityOnDestroy(final Activity activity){
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                activity.finish();
            }
        });
    }

}
