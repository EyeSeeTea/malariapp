/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
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
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ignac on 05/01/2016.
 */
public class CreateSurveyFragment extends Fragment {

    private static String TAG = ".CreateSurvey";

    // UI references.
    private Spinner orgUnitView;
    private View orgUnitContainerItems;

    static class ViewHolder {
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
    LinearLayout        llLayout;
    private SurveyLocationListener locationListener;

    public CreateSurveyFragment() {
    }

    public static CreateSurveyFragment newInstance(int index) {
        CreateSurveyFragment f = new CreateSurveyFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }


    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (container == null) {
            return null;
        }
        //FragmentActivity    faActivity  = (FragmentActivity)    super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        llLayout    = (LinearLayout)    inflater.inflate(R.layout.activity_create_survey, container, false);
        // Of course you will want to faActivity and llLayout in the class and not this method to access them in the rest of
        // the class, just initialize them here

        // Content of activity onCreate() here
        create();
        // Don't use this method, it's handled by inflater.inflate() above :
        // setContentView(R.layout.activity_layout);

        // The FragmentActivity doesn't contain the layout directly so we must use our instance of     LinearLayout :
        // Instead of :
        // findViewById(R.id.someGuiElement);
        return llLayout; // We must return the loaded Layout
    }
    public void create(){

        this.lInflater = LayoutInflater.from(getActivity());

        //Create default options
        orgUnitDefaultOption = new OrgUnit(Constants.DEFAULT_SELECT_OPTION);
        programDefaultOption = new Program(Constants.DEFAULT_SELECT_OPTION);
        tabGroupDefaultOption = new TabGroup(Constants.DEFAULT_SELECT_OPTION);

        //Populate Organization Unit DDL
        ViewHolder viewHolder = new ViewHolder();
        List<OrgUnit> orgUnitList = new Select().all().from(OrgUnit.class).where(Condition.column(OrgUnit$Table.ID_PARENT).isNull()).queryList();
        orgUnitList.add(0, orgUnitDefaultOption);
        viewHolder.component = llLayout.findViewById(R.id.org_unit);
        orgUnitView = (Spinner) viewHolder.component;
        orgUnitView.setTag(orgUnitList.get(1).getOrgUnitLevel());
        orgUnitView.setAdapter(new OrgUnitArrayAdapter( getActivity(), orgUnitList));
        orgUnitView.setOnItemSelectedListener(new OrgUnitSpinnerListener(viewHolder));

        View childView =  llLayout.findViewById(R.id.org_unit_container);
        CustomTextView childViewTextView = (CustomTextView) childView.findViewById(R.id.textView2);
        childViewTextView.setText(orgUnitList.get(1).getOrgUnitLevel().getName());


        //Put in org unit hierarchy map
        orgUnitHierarchyView = new LinkedHashMap<>();
        orgUnitHierarchyView.put(orgUnitList.get(1).getOrgUnitLevel(), childView);

        //Prepare Organization Unit Item DDL
        orgUnitContainerItems =  llLayout.findViewById(R.id.org_unit_container_items);

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
        ;
        programList.add(0, programDefaultOption);
        programView = (Spinner)  llLayout.findViewById(R.id.program);
        programView.setAdapter(new ProgramArrayAdapter( getActivity(), programList));
        programView.setOnItemSelectedListener(new ProgramSpinnerListener());

        //Create Tab Group View DDL. Not populated and not visible.
        tabGroupContainer =  llLayout.findViewById(R.id.tab_group_container);
        tabGroupView = (Spinner)  llLayout.findViewById(R.id.tab_group);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

    }

    private boolean isEverythingFilled() {
        try {
            boolean isEverythingFilled = (!realOrgUnitView.getSelectedItem().equals(orgUnitDefaultOption) && !programView.getSelectedItem().equals(programDefaultOption));
            boolean isTabGroupFilled = !tabGroupView.getSelectedItem().equals(tabGroupDefaultOption);
            return isEverythingFilled && isTabGroupFilled;
        } catch (NullPointerException ex) {
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

    private boolean validateForm() {
        if (!isEverythingFilled()) {
            new AlertDialog.Builder( getActivity())
                    .setTitle( getActivity().getApplicationContext().getString(R.string.dialog_title_missing_selection))
                    .setMessage( getActivity().getApplicationContext().getString(R.string.dialog_content_missing_selection))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else if ((((OrgUnit) realOrgUnitView.getSelectedItem()).getChildren() != null && ((OrgUnit) realOrgUnitView.getSelectedItem()).getChildren().size() > 0)) {
            new AlertDialog.Builder( getActivity())
                    .setTitle( getActivity().getApplicationContext().getString(R.string.dialog_title_incorrect_org_unit))
                    .setMessage( getActivity().getApplicationContext().getString(R.string.dialog_content_incorrect_org_unit))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else if (doesSurveyExist()) {
            new AlertDialog.Builder( getActivity())
                    .setTitle( getActivity().getApplicationContext().getString(R.string.dialog_title_existing_survey))
                    .setMessage( getActivity().getApplicationContext().getString(R.string.dialog_content_existing_survey))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else {
            return true;
        }

        return false;
    }

    /**
     * Called when the user clicks the Send button
     */
    public boolean createSurvey(View view) {
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
            //finishAndGo(SurveyActivity.class);
            return true;
        }
        return false;
    }

    private void prepareLocationListener(Survey survey) {


        locationListener = new SurveyLocationListener(survey.getId_survey());
        LocationManager locationManager = (LocationManager) LocationMemory.getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "requestLocationUpdates via GPS");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.d(TAG, "requestLocationUpdates via NETWORK");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } else {
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "location not available via GPS|NETWORK, last know: " + lastLocation);
            locationListener.saveLocation(lastLocation);
        }
    }

    //@Subscribe
    //public void onLogoutFinished(UiEvent uiEvent) {
        //super.onLogoutFinished(uiEvent);
    //}

    private class ProgramSpinnerListener implements AdapterView.OnItemSelectedListener {

        public ProgramSpinnerListener() {
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Program selectedProgram = (Program) programView.getSelectedItem();
            List<TabGroup> tabGroupList = selectedProgram.getTabGroups();
            if (tabGroupList.size() > 1) {
                // Populate tab group spinner
                tabGroupList.add(0, tabGroupDefaultOption);
                tabGroupView.setAdapter(new TabGroupArrayAdapter( getActivity().getApplicationContext(), tabGroupList));
                //Show tab group select
                tabGroupContainer.setVisibility(View.VISIBLE);
            } else {
                if (tabGroupList.size() == 1) {
                    tabGroupList.add(0, tabGroupDefaultOption);
                    tabGroupView.setAdapter(new TabGroupArrayAdapter( getActivity().getApplicationContext(), tabGroupList));
                    tabGroupView.setSelection(1);
                } else {
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

            OrgUnit selectedOrgUnit = (OrgUnit) ((Spinner) viewHolder.component).getItemAtPosition(pos);
            realOrgUnitView = ((Spinner) viewHolder.component);

            // Populate child view. If it exists in org unit map, grab it; otherwise inflate it
            List<OrgUnit> orgUnitList = selectedOrgUnit.getChildren();

            // If there are children create spinner or populate it otherwise hide existing one
            if (orgUnitList.size() > 0) {
                View childView = orgUnitHierarchyView.get(orgUnitList.get(0).getOrgUnitLevel());
                ViewHolder subViewHolder = new ViewHolder();
                subViewHolder.component = childView.findViewById(R.id.org_unit_item_spinner);

                //Show tab group select and populate tab group spinner
                orgUnitList.add(0, orgUnitDefaultOption);
                ((Spinner) subViewHolder.component).setAdapter(new OrgUnitArrayAdapter( getActivity(), orgUnitList));
                ((Spinner) subViewHolder.component).setOnItemSelectedListener(new OrgUnitSpinnerListener(subViewHolder));

                //Hide org unit selector
                childView.setVisibility(View.VISIBLE);
            } else {
                //If there is not any children, iterate over the org units spinners and hide non needed
                //FIXME This code is horrible. We need a more elegant way
                Boolean setInvisible = false;
                for (Map.Entry<OrgUnitLevel, View> entry : orgUnitHierarchyView.entrySet()) {
                    if (setInvisible) {
                        View childView = entry.getValue();
                        // Select single tab group
                        ((Spinner) childView.findViewById(R.id.org_unit_item_spinner)).setSelection(0, true);
                        // Hide tab group tab selector
                        childView.setVisibility(View.GONE);
                    }
                    if (entry.getKey().equals((viewHolder.component).getTag())) {
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