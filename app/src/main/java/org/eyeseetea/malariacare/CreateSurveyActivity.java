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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnit$Table;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.OrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.ProgramArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.TabGroupArrayAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreateSurveyActivity extends BaseActivity {

    // UI references.
    private Spinner orgUnitView;
    private View orgUnitContainerItems;
    static class ViewHolder{
        public View component;
    }
    private Map<Integer, View> orgUnitHierarchyView;
    private Spinner realOrgUnitView;


    private Spinner programView;
    private View tabGroupContainer;
    private Spinner tabGroupView;

    private OrgUnit orgUnitDefaultOption;
    private Program programDefaultOption;
    private TabGroup tabGroupDefaultOption;

    private LayoutInflater lInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Manage uncaught exceptions that may occur
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_create_survey);

        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);

        this.lInflater = LayoutInflater.from(this);

        //Create default options
        orgUnitDefaultOption = new OrgUnit(Constants.DEFAULT_SELECT_OPTION);
        programDefaultOption = new Program(Constants.DEFAULT_SELECT_OPTION);
        tabGroupDefaultOption = new TabGroup(Constants.DEFAULT_SELECT_OPTION);

        //Populate Organization Unit DDL
        ViewHolder viewHolder = new ViewHolder();
        List<OrgUnit> orgUnitList = new Select().all().from(OrgUnit.class).where(Condition.column(OrgUnit$Table.ORGUNIT_ID_PARENT).isNull()).queryList();
        orgUnitList.add(0, orgUnitDefaultOption);
        viewHolder.component = findViewById(R.id.org_unit);
        orgUnitView = (Spinner) viewHolder.component;
        orgUnitView.setTag(R.id.OrgUnitLevelTag, 1);
        orgUnitView.setAdapter(new OrgUnitArrayAdapter(this, orgUnitList));
        orgUnitView.setOnItemSelectedListener(new OrgUnitSpinnerListener(viewHolder));
        orgUnitHierarchyView = new HashMap<Integer, View>();
        orgUnitHierarchyView.put(1, findViewById(R.id.org_unit_container));
//        realOrgUnitView = orgUnitView;

        //Prepare Organization Unit Item DDL
        orgUnitContainerItems = findViewById(R.id.org_unit_container_items);

        //Populate Program View DDL
        List<Program> programList = new Select().all().from(Program.class).queryList();;
        programList.add(0, programDefaultOption);
        programView = (Spinner) findViewById(R.id.program);
        programView.setAdapter(new ProgramArrayAdapter(this, programList));
        programView.setOnItemSelectedListener(new ProgramSpinnerListener());

        //Create Tab Group View DDL. Not populated and not visible.
        tabGroupContainer = (LinearLayout) findViewById(R.id.tab_group_container);
        tabGroupView = (Spinner) findViewById(R.id.tab_group);

    }

    private boolean isEverythingFilled() {
        try {
            boolean isEverythingFilled = (!orgUnitView.getSelectedItem().equals(orgUnitDefaultOption) && !programView.getSelectedItem().equals(programDefaultOption));
            return (isEverythingFilled) && !tabGroupView.getSelectedItem().equals(tabGroupDefaultOption);
        } catch (Exception ex) {
            ex.printStackTrace();
            //FIXME: getSelectedItem throws an exception when there is not an item selected. I looked for a while in API but couldn't find anything. It is not a good idea to catch this behaviour as an exception.
            return true;
        }
    }

    private boolean doesSurveyExist() {
        // Read Selected Items
        OrgUnit orgUnit = (OrgUnit) realOrgUnitView.getSelectedItem();

        //FIXME: Once we have the tabs groups this needs to be fix
        TabGroup tabGroup = ((Program) programView.getSelectedItem()).getTabGroups().get(0);

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
        Log.i(".CreateSurveyActivity", "Saving survey and saving in session");

        if (validateForm()){
            // Read Selected Items
            OrgUnit orgUnit = (OrgUnit) realOrgUnitView.getSelectedItem();

            //Read Tab Group
            TabGroup tabGroup = (TabGroup) tabGroupView.getSelectedItem();

            //FIXME: Once we have the tabs groups this needs to be fix
//            TabGroup tabGroup = ((TabGroup) tabGroupView.getSelectedItem()).getTabGroups().get(0);

            // Put new survey in session
            Survey survey = new Survey(orgUnit, tabGroup, Session.getUser());
            survey.save();
            Session.setSurvey(survey);

            //Call Survey Activity
            finishAndGo(SurveyActivity.class);

        }

    }

    private class ProgramSpinnerListener implements AdapterView.OnItemSelectedListener {

        public ProgramSpinnerListener() {
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Program selectedProgram = (Program) programView.getSelectedItem();
            List<TabGroup> tabGroupList = selectedProgram.getTabGroups();
            if (tabGroupList.size() > 1){
                //Show tab group select and populate tab group spinner
                tabGroupList.add(0, tabGroupDefaultOption);
                tabGroupView.setAdapter(new TabGroupArrayAdapter(CreateSurveyActivity.this, tabGroupList));
                tabGroupContainer.setVisibility(View.VISIBLE);
            }
            else{
                //Hide tab group tab selector and select single tab group
                tabGroupView.setAdapter(new TabGroupArrayAdapter(CreateSurveyActivity.this, tabGroupList));
                tabGroupView.setSelection(0);
                tabGroupContainer.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class OrgUnitSpinnerListener implements AdapterView.OnItemSelectedListener {

        private ViewHolder viewHolder;
        //private View childView;

        public OrgUnitSpinnerListener(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            OrgUnit selectedOrgUnit = (OrgUnit) ((Spinner)viewHolder.component).getItemAtPosition(pos);
            realOrgUnitView = ((Spinner) viewHolder.component);
            List<OrgUnit> orgUnitList = selectedOrgUnit.getChildren();
            //Check for child View
            boolean hasOrgUnitChild = orgUnitHierarchyView.containsKey((Integer)((Spinner)viewHolder.component).getTag(R.id.OrgUnitLevelTag)+1);
            View childView = null;
            if (hasOrgUnitChild)
                childView = orgUnitHierarchyView.get((Integer)((Spinner)viewHolder.component).getTag(R.id.OrgUnitLevelTag)+1);

            if (orgUnitList.size() > 1){


                if (!hasOrgUnitChild) {
                    childView = lInflater.inflate(R.layout.activity_create_survey_org_unit_item, (LinearLayout) orgUnitContainerItems, false);
                    ((LinearLayout) orgUnitContainerItems).addView(childView);
                    orgUnitHierarchyView.put((Integer)((Spinner)viewHolder.component).getTag(R.id.OrgUnitLevelTag)+1, childView);
                }


                ViewHolder subViewHolder = new ViewHolder();
                subViewHolder.component = childView.findViewById(R.id.org_unit_item_spinner);

                //Show tab group select and populate tab group spinner
                orgUnitList.add(0, orgUnitDefaultOption);
                ((Spinner) subViewHolder.component).setAdapter(new OrgUnitArrayAdapter(CreateSurveyActivity.this, orgUnitList));
                ((Spinner) subViewHolder.component).setOnItemSelectedListener(new OrgUnitSpinnerListener(subViewHolder));
                ((Spinner) subViewHolder.component).setTag(R.id.OrgUnitLevelTag, (Integer)((Spinner)viewHolder.component).getTag(R.id.OrgUnitLevelTag)+1);


                ((Spinner)childView.findViewById(R.id.org_unit_item_spinner)).setSelection(0);
                childView.setVisibility(View.VISIBLE);
            }
            else{
                if (hasOrgUnitChild) {
                    //Hide tab group tab selector and select single tab group
                    ((Spinner) childView.findViewById(R.id.org_unit_item_spinner)).setSelection(0,true);
                    childView.setVisibility(View.GONE);
                }
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


}
