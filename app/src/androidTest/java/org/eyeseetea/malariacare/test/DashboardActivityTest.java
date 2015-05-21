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

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;

import org.eyeseetea.malariacare.CreateSurveyActivity;
import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentAdapter;

/**
 * Created by arrizabalaga on 20/05/15.
 */
public class DashboardActivityTest extends ActivityInstrumentationTestCase2<DashboardActivity> {

    private static final long TIMEOUT_IN_MS=3000;

    private DashboardActivity activity;
    private TableLayout assessmentTable;
    private Button newSurveyButton;
    private Resources res;

    public DashboardActivityTest(){
        super(DashboardActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        cleanDB();
        setActivityInitialTouchMode(true);

        activity=getActivity();
        newSurveyButton =(Button) activity.findViewById(R.id.button);
        assessmentTable =(TableLayout)activity.findViewById(R.id.assessment_table);
        res = getInstrumentation().getTargetContext().getResources();
    }

    @Override
    protected void tearDown() throws Exception{
        super.tearDown();
        cleanDB();
    }

    public void test_preconditions(){
        assertNotNull("button is null", newSurveyButton);
        assertNotNull("button is null", assessmentTable);
        assertEquals(res.getString(R.string.dashboard_info_new_assessment), newSurveyButton.getText().toString());
    }

    public void test_button_starts_intent(){
        //GIVEN
        Instrumentation.ActivityMonitor receiverActivityMonitor =getInstrumentation().addMonitor(CreateSurveyActivity.class.getName(),null, false);

        //WHEN
        TouchUtils.clickView(this, newSurveyButton);

        //THEN
        CreateSurveyActivity createSurveyActivity = (CreateSurveyActivity) receiverActivityMonitor.waitForActivityWithTimeout(TIMEOUT_IN_MS);
        assertNotNull("Create survey activity is not started", createSurveyActivity);
        createSurveyActivity.finish();
    }

    public void test_assessment_filled(){
        populateData();
        ListView assessmentListView=(ListView) assessmentTable.getChildAt(0);
        AssessmentAdapter assessmentAdapter= (AssessmentAdapter)((HeaderViewListAdapter)assessmentListView.getAdapter()).getWrappedAdapter();
        int currentDBSurveys= (int) Survey.count(Survey.class, null, null);
        int currentViewSurveys=assessmentAdapter.getCount();
        assertEquals(currentDBSurveys, currentViewSurveys);
    }

    private void cleanDB(){
        Question.deleteAll(Question.class);
        CompositiveScore.deleteAll(CompositiveScore.class);
        Option.deleteAll(Option.class);
        Answer.deleteAll(Answer.class);
        Header.deleteAll(Header.class);
        Tab.deleteAll(Tab.class);
        Program.deleteAll(Program.class);
        OrgUnit.deleteAll(OrgUnit.class);
    }

    private void populateData(){
        //prerequisite
        cleanDB();

        try {
            PopulateDB.populateDummyData();
            PopulateDB.populateDB(activity.getAssets());
        }catch(Exception ex){
            Log.e(".DashboardActivityTest",ex.getMessage());
        }

    }

}
