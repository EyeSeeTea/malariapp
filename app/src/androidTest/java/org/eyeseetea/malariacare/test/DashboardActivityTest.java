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

import android.app.FragmentManager;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.test.TouchUtils;
import android.widget.Button;

import org.eyeseetea.malariacare.CreateSurveyActivity;
import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.fragments.DashboardFragment;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.DashboardAdapter;

import java.util.List;

/**
 * Created by arrizabalaga on 20/05/15.
 */
public class DashboardActivityTest extends MalariaInstrumentationTestCase<DashboardActivity> {

    private static final long TIMEOUT_IN_MS=3000;

    private DashboardActivity activity;
    private DashboardFragment dashboardFragment;
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

        initActivity();
    }

    protected void initActivity(){
        activity=getActivity();
        newSurveyButton =(Button) activity.findViewById(R.id.button);
        FragmentManager fragmentManager=getActivity().getFragmentManager();
        dashboardFragment=(DashboardFragment)waitForFragment(R.id.dashboard_container,10000);
        res = getInstrumentation().getTargetContext().getResources();
    }

    @Override
    protected void tearDown() throws Exception{
        super.tearDown();
        cleanDB();
    }

//    public void test_preconditions(){
//        assertNotNull("button is null", newSurveyButton);
//        assertEquals(res.getString(R.string.dashboard_button_new_survey), newSurveyButton.getText().toString());
//        DashboardAdapter dashboardAdapter=(DashboardAdapter)dashboardFragment.getListAdapter();
//        assertEquals(5, dashboardAdapter.getCount());
//
//        activity.finish();
//    }
//
//    public void test_zero_surveys(){
//        DashboardAdapter dashboardAdapter=(DashboardAdapter)dashboardFragment.getListAdapter();
//        AssessmentAdapter assessmentAdapter=(AssessmentAdapter)dashboardAdapter.getItem(0);
//        assertEquals(0,assessmentAdapter.getCount());
//    }
//
//    public void test_button_starts_intent(){
//        //GIVEN
//        Instrumentation.ActivityMonitor receiverActivityMonitor =getInstrumentation().addMonitor(CreateSurveyActivity.class.getName(),null, false);
//
//        //WHEN
//        TouchUtils.clickView(this, newSurveyButton);
//
//        //THEN
//        CreateSurveyActivity createSurveyActivity = (CreateSurveyActivity) receiverActivityMonitor.waitForActivityWithTimeout(TIMEOUT_IN_MS);
//        assertNotNull("Create survey activity is not started", createSurveyActivity);
//        createSurveyActivity.finish();
//    }

//
//    public void test_1_surveys(){
//        //GIVEN
//        mockSurveys(1);
//
//        //WHEN
//        activity.finish();
//        setActivity(null);
//        initActivity();
//
//        //THEN
//        DashboardAdapter dashboardAdapter=(DashboardAdapter)dashboardFragment.getListAdapter();
//        AssessmentAdapter assessmentAdapter=(AssessmentAdapter)dashboardAdapter.getItem(0);
//        assertEquals(1, assessmentAdapter.getCount());
//    }


    public void test_N_surveys_but_5_shown(){
        //GIVEN
        mockSurveys(7);

        //WHEN
        activity.finish();
        setActivity(null);
        initActivity();

        //THEN
        DashboardAdapter dashboardAdapter=(DashboardAdapter)dashboardFragment.getListAdapter();
        AssessmentAdapter assessmentAdapter=(AssessmentAdapter)dashboardAdapter.getItem(0);
        assertEquals(5, assessmentAdapter.getCount());
    }

    private void mockSurveys(int num){
        populateData(activity);
        List<OrgUnit> orgUnitList=OrgUnit.find(OrgUnit.class, null, null);
        Program program=Program.findById(Program.class,1l);
        User user =getSafeUser();

        for(int i=0;i<num;i++){
            Survey survey=new Survey(orgUnitList.get(i%num),program,user);
            survey.save();
        }

    }

    private User getSafeUser(){
        User user=Session.getUser();
        if(user!=null){
            return user;
        }
        user = new User("user", "user");
        user.save();
        Session.setUser(user);
        return user;
    }

}
