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
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnit$Table;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.OrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.ProgramArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.TabGroupArrayAdapter;
import org.eyeseetea.malariacare.layout.listeners.SurveyLocationListener;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.hisp.dhis.android.sdk.events.UiEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class CreateSurveyActivity extends BaseActivity {

    private static String TAG=".CreateSurvey";

    // UI references.
    private Spinner orgUnitView;
    private View orgUnitContainerItems;
    static class ViewHolder{
        public View component;
    }
    private LinkedHashMap<OrgUnitLevel, View> orgUnitHierarchyView;
    private Spinner realOrgUnitView;


    private Spinner programView;
    private View tabGroupContainer;
    private Spinner tabGroupView;

    private OrgUnit orgUnitDefaultOption;
    private Program programDefaultOption;
    private TabGroup tabGroupDefaultOption;

    private LayoutInflater lInflater;

    private SurveyLocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Manage uncaught exceptions that may occur
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        if(Utils.isPictureQuestion())
            setContentView(R.layout.activity_create_survey_pictureapp);
        else
            setContentView(R.layout.activity_create_survey);

        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);

        //Create default options
        orgUnitDefaultOption = new OrgUnit(Constants.DEFAULT_SELECT_OPTION);
        programDefaultOption = new Program(Constants.DEFAULT_SELECT_OPTION);

        if (Utils.isPictureQuestion()) {


            //Populate Organization Unit DDL
            List<OrgUnit> orgUnitList = new Select().all().from(OrgUnit.class).queryList();
            orgUnitList.add(0, orgUnitDefaultOption);
            orgUnitView = (Spinner) findViewById(R.id.org_unit);
            OrgUnitArrayAdapter orgUnitAdapter=new OrgUnitArrayAdapter(this, orgUnitList);
            orgUnitView.setAdapter(orgUnitAdapter);

            //Populate Program View DDL
            List<Program> programList = new Select().all().from(Program.class).queryList();
            programList.add(0, programDefaultOption);
            programView = (Spinner) findViewById(R.id.program);
            ProgramArrayAdapter programAdapter=new ProgramArrayAdapter(this, programList);
            programView.setAdapter(programAdapter);
        } else {

            this.lInflater = LayoutInflater.from(this);

            tabGroupDefaultOption = new TabGroup(Constants.DEFAULT_SELECT_OPTION);

            //Populate Organization Unit DDL
            ViewHolder viewHolder = new ViewHolder();
            List<OrgUnit> orgUnitList = new Select().all().from(OrgUnit.class).where(Condition.column(OrgUnit$Table.ID_PARENT).isNull()).queryList();
            orgUnitList.add(0, orgUnitDefaultOption);
            viewHolder.component = findViewById(R.id.org_unit);
            orgUnitView = (Spinner) viewHolder.component;
            orgUnitView.setTag(orgUnitList.get(1).getOrgUnitLevel());
            orgUnitView.setAdapter(new OrgUnitArrayAdapter(this, orgUnitList));
            orgUnitView.setOnItemSelectedListener(new OrgUnitSpinnerListener(viewHolder));

            View childView = findViewById(R.id.org_unit_container);
            CustomTextView childViewTextView = (CustomTextView) childView.findViewById(R.id.textView2);
            childViewTextView.setText(orgUnitList.get(1).getOrgUnitLevel().getName());


            //Put in org unit hierarchy map
            orgUnitHierarchyView = new LinkedHashMap<>();
            orgUnitHierarchyView.put(orgUnitList.get(1).getOrgUnitLevel(), childView);

            //Prepare Organization Unit Item DDL
            orgUnitContainerItems = findViewById(R.id.org_unit_container_items);

            List<OrgUnitLevel> orgUnitLevelList = new Select().all().from(OrgUnitLevel.class).queryList();
            for (OrgUnitLevel orgUnitLevel : orgUnitLevelList) {
                if (!orgUnitLevel.equals(orgUnitList.get(1).getOrgUnitLevel())) {
                    childView = lInflater.inflate(R.layout.activity_create_survey_org_unit_item, (LinearLayout) orgUnitContainerItems, false);
                    childViewTextView = (CustomTextView) childView.findViewById(R.id.textView);
                    childViewTextView.setText(orgUnitLevel.getName());

                    Spinner childViewSpinner = (Spinner) childView.findViewById(R.id.org_unit_item_spinner);
                    childViewSpinner.setTag(orgUnitLevel);
                    childView.setVisibility(View.GONE);
                    ((LinearLayout) orgUnitContainerItems).addView(childView);
                    //Put in org unit hierarchy map
                    orgUnitHierarchyView.put(orgUnitLevel, childView);
                }
            }


            //Populate Program View DDL
            List<Program> programList = new Select().all().from(Program.class).queryList();
            programList.add(0, programDefaultOption);
            programView = (Spinner) findViewById(R.id.program);
            programView.setAdapter(new ProgramArrayAdapter(this, programList));
            programView.setOnItemSelectedListener(new ProgramSpinnerListener());

            //Create Tab Group View DDL. Not populated and not visible.
            tabGroupContainer = findViewById(R.id.tab_group_container);
            tabGroupView = (Spinner) findViewById(R.id.tab_group);
        }
    }

    private boolean isEverythingFilled() {
        try {
            boolean isEverythingFilled = (!realOrgUnitView.getSelectedItem().equals(orgUnitDefaultOption) && !programView.getSelectedItem().equals(programDefaultOption));
            boolean isTabGroupFilled = !tabGroupView.getSelectedItem().equals(tabGroupDefaultOption);
            return isEverythingFilled && isTabGroupFilled;
        }catch(NullPointerException ex){
            return false;
        }
    }

    private boolean doesSurveyExist() {
        // Read Selected Items
        OrgUnit orgUnit = (OrgUnit) realOrgUnitView.getSelectedItem();
        TabGroup tabGroup = (TabGroup) tabGroupView.getSelectedItem();
        List<Survey> existing = Survey.getUnsentSurveys(orgUnit, tabGroup);
        return (existing != null && existing.size() != 0);
    }

    private boolean validateForm(){
        if (!isEverythingFilled()) {
            new AlertDialog.Builder(this)
                    .setTitle(getApplicationContext().getString(R.string.dialog_title_missing_selection))
                    .setMessage(getApplicationContext().getString(R.string.dialog_content_missing_selection))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else if ((((OrgUnit) realOrgUnitView.getSelectedItem()).getChildren() != null && ((OrgUnit) realOrgUnitView.getSelectedItem()).getChildren().size() > 0)) {
                new AlertDialog.Builder(this)
                        .setTitle(getApplicationContext().getString(R.string.dialog_title_incorrect_org_unit))
                        .setMessage(getApplicationContext().getString(R.string.dialog_content_incorrect_org_unit))
                        .setPositiveButton(android.R.string.ok, null).create().show();
        } else if (doesSurveyExist()) {
                new AlertDialog.Builder(this)
                        .setTitle(getApplicationContext().getString(R.string.dialog_title_existing_survey))
                        .setMessage(getApplicationContext().getString(R.string.dialog_content_existing_survey))
                        .setPositiveButton(android.R.string.ok, null).create().show();
        }
        else{
            return true;
        }

        return false;
    }

    /**
     * Called when the user clicks the Send button
     */
    public void createSurvey(View view) {
        if(Utils.isPictureQuestion()) {

            Log.i(".CreateSurveyActivity", "Saving survey and saving in session");

            // Read Selected Items
            OrgUnit orgUnit = (OrgUnit) orgUnitView.getSelectedItem();
            Program program = (Program) programView.getSelectedItem();

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
                Survey survey = new Survey(orgUnit, program, Session.getUser());
                survey.save();
                Session.setSurvey(survey);

                //Call Survey Activity
                finishAndGo(SurveyActivity.class);
            }
        }
        else {
            Log.i(".CreateSurveyActivity", "Saving survey and saving in session");

            if (validateForm()) {
                // Read Selected Items
                OrgUnit orgUnit = (OrgUnit) realOrgUnitView.getSelectedItem();
                //Read Tab Group
                TabGroup tabGroup = (TabGroup) tabGroupView.getSelectedItem();

                // Put new survey in session
                Survey survey = new Survey(orgUnit, tabGroup, Session.getUser());
                survey.save();
                Session.setSurvey(survey);

                //Look for coordinates
                prepareLocationListener(survey);


                //Call Survey Activity
                finishAndGo(SurveyActivity.class);
            }
        }

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
        Program program = (Program) programView.getSelectedItem();

        List<Survey> existing = Survey.getUnsentSurveys(orgUnit, program);
        return (existing == null || existing.size() == 0);
    }

    private void prepareLocationListener(Survey survey){


        locationListener=new SurveyLocationListener(survey.getId_survey());
        LocationManager locationManager=(LocationManager) LocationMemory.getContext().getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.d(TAG,"requestLocationUpdates via GPS");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }

        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Log.d(TAG,"requestLocationUpdates via NETWORK");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        }else{
            Location lastLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "location not available via GPS|NETWORK, last know: " + lastLocation);
            locationListener.saveLocation(lastLocation);
        }
    }

    @Subscribe
    public void onLogoutFinished(UiEvent uiEvent){
        super.onLogoutFinished(uiEvent);
    }

    private class ProgramSpinnerListener implements AdapterView.OnItemSelectedListener {

        public ProgramSpinnerListener() {
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Program selectedProgram = (Program) programView.getSelectedItem();
            List<TabGroup> tabGroupList = selectedProgram.getTabGroups();
            if (tabGroupList.size() > 1){
                // Populate tab group spinner
                tabGroupList.add(0, tabGroupDefaultOption);
                tabGroupView.setAdapter(new TabGroupArrayAdapter(getApplicationContext(), tabGroupList));
                //Show tab group select
                tabGroupContainer.setVisibility(View.VISIBLE);
            }
            else{
                if (tabGroupList.size() == 1){
                    tabGroupList.add(0, tabGroupDefaultOption);
                    tabGroupView.setAdapter(new TabGroupArrayAdapter(getApplicationContext(), tabGroupList));
                    tabGroupView.setSelection(1);
                }
                else {
                    // Select single tab group
                    tabGroupView.setSelection(0);
                }
                //Hide tab group selector
               tabGroupContainer.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class OrgUnitSpinnerListener implements AdapterView.OnItemSelectedListener {

        private ViewHolder viewHolder;

        public OrgUnitSpinnerListener(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            OrgUnit selectedOrgUnit = (OrgUnit) ((Spinner)viewHolder.component).getItemAtPosition(pos);
            realOrgUnitView = ((Spinner) viewHolder.component);

            // Populate child view. If it exists in org unit map, grab it; otherwise inflate it
            List<OrgUnit> orgUnitList = selectedOrgUnit.getChildren();

            // If there are children create spinner or populate it otherwise hide existing one
            if (orgUnitList.size() > 0){
                View childView = orgUnitHierarchyView.get(orgUnitList.get(0).getOrgUnitLevel());
                ViewHolder subViewHolder = new ViewHolder();
                subViewHolder.component = childView.findViewById(R.id.org_unit_item_spinner);

                //Show tab group select and populate tab group spinner
                orgUnitList.add(0, orgUnitDefaultOption);
                ((Spinner) subViewHolder.component).setAdapter(new OrgUnitArrayAdapter(CreateSurveyActivity.this, orgUnitList));
                ((Spinner) subViewHolder.component).setOnItemSelectedListener(new OrgUnitSpinnerListener(subViewHolder));

                //Hide org unit selector
                childView.setVisibility(View.VISIBLE);
            }
            else{
                //If there is not any children, iterate over the org units spinners and hide non needed
                //FIXME This code is horrible. We need a more elegant way
                Boolean setInvisible = false;
                for (Map.Entry<OrgUnitLevel, View> entry : orgUnitHierarchyView.entrySet()){
                    if (setInvisible){
                        View childView = entry.getValue();
                        // Select single tab group
                        ((Spinner) childView.findViewById(R.id.org_unit_item_spinner)).setSelection(0, true);
                        // Hide tab group tab selector
                        childView.setVisibility(View.GONE);
                    }
                    if (entry.getKey().equals((viewHolder.component).getTag())){
                        setInvisible = true;
                    }
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


}
