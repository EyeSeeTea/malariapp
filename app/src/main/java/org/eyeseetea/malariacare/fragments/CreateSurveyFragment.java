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

package org.eyeseetea.malariacare.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.layout.adapters.general.OrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.ProgramArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.TabGroupArrayAdapter;
import org.eyeseetea.malariacare.layout.listeners.SurveyLocationListener;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomButton;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.hisp.dhis.android.sdk.persistence.models.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ignac on 05/01/2016.
 */
public class CreateSurveyFragment extends Fragment {

    private static String TAG = ".CreateSurveyFragment";

    // UI references.
    private Spinner orgUnitView;
    private View orgUnitContainerItems;
    private String TOKEN = ";";
    private String orgUnitStorage = "";

    static class ViewHolder {
        public View component;
    }

    private LinkedHashMap<OrgUnitLevel, View> orgUnitHierarchyView;
    private Spinner realOrgUnitView;

    private SurveyReceiver surveyReceiver;

    private Spinner programView;
    private View tabGroupContainer;
    private Spinner tabGroupView;

    //Loaded one time from service
    List<Program> allProgramList;
    List<OrgUnit> orgUnitList;
    List<OrgUnitLevel> orgUnitLevelList;

    private OrgUnit orgUnitDefaultOption;
    private Program programDefaultOption;
    private TabGroup tabGroupDefaultOption;

    private OrgUnit lastSelectedOrgUnit;
    private String lastOrgUnits = TOKEN;

    private LayoutInflater lInflater;
    LinearLayout llLayout;
    private SurveyLocationListener locationListener;

    OnCreatedSurveyListener mCallback;

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
    public void onStop(){
        Log.d(TAG, "onStop");
        unregisterReceiver();

        super.onStop();
    }
    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        unregisterReceiver();

        super.onPause();
    }
    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        registerReceiver();
        super.onResume();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (container == null) {
            return null;
        }
        llLayout = (LinearLayout) inflater.inflate(R.layout.create_survey_fragment, container, false);
        registerReceiver();
        getData();
        return llLayout; // We must return the loaded Layout
    }


    public void getData(){
        //get data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.ALL_CREATE_SURVEY_DATA_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
    }

    public void create(){
        CustomButton createButton = (CustomButton) llLayout.findViewById(R.id.create_form_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If the survey is validate, it send the order of create survey fragment from this fragment to the activity.
                if(validateForm()) {
                    createSurvey();
                    mCallback.onCreateSurvey();
                }
            }
        });
        this.lInflater = LayoutInflater.from(getActivity());

        //Create default options
        orgUnitDefaultOption = new OrgUnit(Constants.DEFAULT_SELECT_OPTION);
        programDefaultOption = new Program(Constants.DEFAULT_SELECT_OPTION);
        tabGroupDefaultOption = new TabGroup(Constants.DEFAULT_SELECT_OPTION);

        //Populate Organization Unit DDL
        ViewHolder viewHolder = new ViewHolder();
        orgUnitList.add(0, orgUnitDefaultOption);
        viewHolder.component = llLayout.findViewById(R.id.org_unit);
        orgUnitView = (Spinner) viewHolder.component;
        orgUnitView.setTag(orgUnitList.get(1).getOrgUnitLevel());
        orgUnitView.setAdapter(new OrgUnitArrayAdapter( getActivity(), orgUnitList));
        orgUnitView.setOnItemSelectedListener(new OrgUnitSpinnerListener(viewHolder));

        View childView = llLayout.findViewById(R.id.org_unit_container);
        CustomTextView childViewTextView = (CustomTextView) childView.findViewById(R.id.textView2);
        childViewTextView.setText(orgUnitList.get(1).getOrgUnitLevel().getName());


        //Put in org unit hierarchy map
        orgUnitHierarchyView = new LinkedHashMap<>();
        orgUnitHierarchyView.put(orgUnitList.get(1).getOrgUnitLevel(), childView);

        //Prepare Organization Unit Item DDL
        orgUnitContainerItems = llLayout.findViewById(R.id.org_unit_container_items);

        for (OrgUnitLevel orgUnitLevel : orgUnitLevelList) {
            if (!orgUnitLevel.equals(orgUnitList.get(1).getOrgUnitLevel())) {
                childView = lInflater.inflate(R.layout.create_survey_org_unit_item_fragment, (LinearLayout) orgUnitContainerItems, false);
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
        //get all the programs from a DB query only one time.
        List<Program> initProgram=new ArrayList<>();
        initProgram.add(0, programDefaultOption);
        programView = (Spinner)  llLayout.findViewById(R.id.program);
        programView.setAdapter(new ProgramArrayAdapter( getActivity(), initProgram));
        programView.setOnItemSelectedListener(new ProgramSpinnerListener());

        //Create Tab Group View DDL. Not populated and not visible.
        tabGroupContainer =  llLayout.findViewById(R.id.tab_group_container);
        tabGroupView = (Spinner)  llLayout.findViewById(R.id.tab_group);

        //init the lastOrgUnits
        lastOrgUnits= TOKEN;

        //get the lastSelectedOrgUnit
        setDefaultOrgUnit();

        //get the list of org units for be pulled in the spinner.
        orgUnitStorage =getListOrgUnits();

        // If the list is empty(with TOKEN), The list is overwritte by the lastSelectedOrgUnit(for saved orgunits without tree).
        // In the first time, the application not have lastSelectedOrgUnit, it only need be saved if exist.
        if(orgUnitStorage.startsWith(TOKEN))
            if(lastSelectedOrgUnit !=null)
                orgUnitStorage = lastSelectedOrgUnit.getUid();

        //Load the root lastorgUnit/firstOrgUnit(if we have orgUnitLevels).
        if(!orgUnitStorage.equals("")){
            String[] list= orgUnitStorage.split(TOKEN);
            if(list.length>0){
                orgUnitView.setSelection(getIndex(orgUnitView, OrgUnit.getOrgUnit(list[0]).getName()));
                orgUnitStorage = removeLastOrgUnits(orgUnitStorage, list[0] + TOKEN);
            }
        }
        else if(lastSelectedOrgUnit !=null)
            orgUnitView.setSelection(getIndex(orgUnitView, lastSelectedOrgUnit.getName()));
    }


    // Container Activity must implement this interface
    public interface OnCreatedSurveyListener {
        public void onCreateSurvey();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnCreatedSurveyListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCreatedSurveyListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

    }

    //select the default item.
    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;
        for (int i=0;i<spinner.getCount();i++){
            Object objectRow= spinner.getItemAtPosition(i);
            String value="";
            if(objectRow instanceof Program) {
                value= ((Program) objectRow).getName();
            }
            if(objectRow instanceof OrgUnit) {
                value= ((OrgUnit) objectRow).getName();
            }
            if(objectRow instanceof TabGroup) {
                value= ((TabGroup) objectRow).getName();
            }
            if (value.equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
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

    private boolean doesSurveyInProgressExist() {
        // Read Selected Items
        OrgUnit orgUnit = (OrgUnit) realOrgUnitView.getSelectedItem();
        TabGroup tabGroup = (TabGroup) tabGroupView.getSelectedItem();
        Survey survey = Survey.getInProgressSurveys(orgUnit, tabGroup);
        return (survey != null);
    }

    private boolean validateForm(){
        if (!isEverythingFilled()) {
            new AlertDialog.Builder( getActivity())
                    .setTitle( getActivity().getApplicationContext().getString(R.string.dialog_title_missing_selection))
                    .setMessage( getActivity().getApplicationContext().getString(R.string.dialog_content_missing_selection))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else if ((((OrgUnit) realOrgUnitView.getSelectedItem()).getChildren() != null && ((OrgUnit) realOrgUnitView.getSelectedItem()).getChildren().size() > 0)) {
            new AlertDialog.Builder( getActivity())
                    .setTitle(getActivity().getApplicationContext().getString(R.string.dialog_title_incorrect_org_unit))
                    .setMessage(getActivity().getApplicationContext().getString(R.string.dialog_content_incorrect_org_unit))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else if (doesSurveyInProgressExist()) {
            new AlertDialog.Builder( getActivity())
                    .setTitle(getActivity().getApplicationContext().getString(R.string.dialog_title_existing_survey))
                    .setMessage(getActivity().getApplicationContext().getString(R.string.dialog_content_existing_survey))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else {
            return true;
        }

        return false;
    }

    /**
     * Called when the user clicks the Send button
     * Gets the survey with the SURVEY_PLANNED state and set the createdate, user, SURVEY_IN_PROGRESS, and reset main score, and save the survey in session
     */
    public void createSurvey() {
        Log.i(".CreateSurveyActivity", "Saving survey and saving in session");

        // Read Selected Items
        OrgUnit orgUnit = (OrgUnit) realOrgUnitView.getSelectedItem();
        //Read Tab Group
        TabGroup tabGroup = (TabGroup) tabGroupView.getSelectedItem();

        // Put new survey in session
        Survey survey = SurveyPlanner.getInstance().startSurvey(orgUnit,tabGroup);
        Session.setSurveyByModule(survey, Constants.FRAGMENT_SURVEY_KEY);

        //Look for coordinates
        prepareLocationListener(survey);

        //save the lastSelectedOrgUnit and the list of orgUnits
        saveOrgUnit();
        //if the list not cointain the selected orgUnit(if it is root withoutchilds)
        // set the list of orgUnitsLevels to "SEPARECHAR"
        if(!lastOrgUnits.contains(orgUnit.getUid())) {
            saveOrgUnitList(TOKEN);
        }
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

    private class ProgramSpinnerListener implements AdapterView.OnItemSelectedListener {

        public ProgramSpinnerListener() {
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Program selectedProgram = (Program) programView.getSelectedItem();
            List<TabGroup> tabGroupList = selectedProgram.getTabGroups();
            //remove null tabgrouplist
            if (tabGroupList.size() > 1) {
                for (int i = tabGroupList.size() - 1; i >= 0; i--) {
                    if (tabGroupList.get(i).getUid() == null) {
                        tabGroupList.remove(i);
                    }
                }
            }
            if (tabGroupList.size() > 1){
                // Populate tab group spinner
                tabGroupList.add(0, tabGroupDefaultOption);
                tabGroupView.setAdapter(new TabGroupArrayAdapter( getActivity().getApplicationContext(), tabGroupList));
                //Show tab group select
                tabGroupContainer.setVisibility(View.VISIBLE);
            }
            else{
                if (tabGroupList.size() == 1){
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

            OrgUnit selectedOrgUnit = (OrgUnit) ((Spinner)viewHolder.component).getItemAtPosition(pos);
            realOrgUnitView = ((Spinner) viewHolder.component);

            if(selectedOrgUnit!=null && selectedOrgUnit.getUid()!=null) {
                filterPrograms(selectedOrgUnit);
                if (selectedOrgUnit.getUid() != null && selectedOrgUnit.getChildren().isEmpty() && selectedOrgUnit.getOrgUnit() == null) {
                    //without parent without childs
                    lastSelectedOrgUnit = selectedOrgUnit;
                    lastOrgUnits = TOKEN;
                } else if (selectedOrgUnit.getOrgUnit() == null && selectedOrgUnit.getUid() != null) {
                    //parent
                    lastSelectedOrgUnit = selectedOrgUnit;
                    lastOrgUnits = selectedOrgUnit.getUid();
                } else if (selectedOrgUnit.getUid() != null) {
                    //it is a child
                    if (lastOrgUnits == null) {
                        lastOrgUnits = selectedOrgUnit.getUid();
                    } else if (!lastOrgUnits.contains(selectedOrgUnit.getUid())) {
                        lastOrgUnits = lastOrgUnits + TOKEN + selectedOrgUnit.getUid();
                    }
                }
            }
            // Populate child view. If it exists in org unit map, grab it; otherwise inflate it
            List<OrgUnit> orgUnitList = selectedOrgUnit.getChildren();

            // If there are children create spinner or populate it otherwise hide existing one
            if (orgUnitList.size() > 0){
                View childView = orgUnitHierarchyView.get(orgUnitList.get(0).getOrgUnitLevel());
                ViewHolder subViewHolder = new ViewHolder();
                subViewHolder.component = childView.findViewById(R.id.org_unit_item_spinner);

                //Show tab group select and populate tab group spinner
                orgUnitList.add(0, orgUnitDefaultOption);
                Spinner spinner=((Spinner) subViewHolder.component);
                spinner.setAdapter(new OrgUnitArrayAdapter(getActivity(), orgUnitList));
                spinner.setOnItemSelectedListener(new OrgUnitSpinnerListener(subViewHolder));

                //If the orgUnit had OrgUnit levels, it should be load one - to -one.
                //Select the saved orgUnitTree. It gets the first orgUnit(by uid), and remove it from templistorgunits (the next loop gets the next).
                if(!orgUnitStorage.equals("")){
                    String[] list= orgUnitStorage.split(TOKEN);
                    for(int i=0;i<list.length;i++){
                        if(!list[i].equals("") && !list[i].equals(TOKEN)) {
                            try {
                                spinner.setSelection(getIndex(spinner, OrgUnit.getOrgUnit(list[i]).getName()));
                            } catch (Exception e) {
                            }
                            orgUnitStorage = orgUnitStorage.replaceFirst(TOKEN,"");
                            orgUnitStorage = removeLastOrgUnits(orgUnitStorage, list[i]);
                            break;
                        }
                    }
                }

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
    //filter programs by orgUnit
    private void filterPrograms(OrgUnit selectedOrgUnit) {

        List<Program> initProgram= new ArrayList<>();
        for(Program orgUnitProgram: selectedOrgUnit.getPrograms()){
            for(Program program:allProgramList ){
                if(orgUnitProgram!=null && orgUnitProgram.equals(program))
                    initProgram.add(orgUnitProgram);
            }
        }
        initProgram.add(0, programDefaultOption);
        programView = (Spinner)  llLayout.findViewById(R.id.program);
        programView.setAdapter(new ProgramArrayAdapter( getActivity(), initProgram));
        programView.setOnItemSelectedListener(new ProgramSpinnerListener());
    }

    private void saveOrgUnitList(String list){
        SharedPreferences.Editor editor = getEditor();
        editor.putString(getString(R.string.default_orgUnits), list);
        editor.commit();
    }

    /**
     * Saves the orgUnit/Program/TabGroup
     */
    private void saveOrgUnit(){
        SharedPreferences.Editor editor = getEditor();
        if(lastSelectedOrgUnit !=null) {
            editor.putString(getString(R.string.default_orgUnit), this.lastSelectedOrgUnit.getUid());
        }
        editor.commit();
        changeOrgUnitList();
    }

    private void changeOrgUnitList() {
        //if the lastorgUnits is "" o Separechar is not saved in sharedpreferences.
        if (!lastOrgUnits.equals("") && !lastOrgUnits.equals(TOKEN)) {
            lastOrgUnits=lastOrgUnits+ TOKEN + lastSelectedOrgUnit.getUid();
            //remove the repeat root.
            lastOrgUnits=lastOrgUnits.replace(lastSelectedOrgUnit.getUid()+ TOKEN + lastSelectedOrgUnit.getUid(), lastSelectedOrgUnit.getUid());
            saveOrgUnitList(lastOrgUnits);
        }
    }

    //Remove the populate orgUnit in order.(the snippers always get the first position).
    private String removeLastOrgUnits(String list,String orgUnit){
        if(!orgUnit.equals("") && !orgUnit.equals(TOKEN)) {
            list = list.replace(orgUnit, "");
        }
        return list;
    }

    //Get the default orgUnit/program/tab
    private void setDefaultOrgUnit() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        this.lastSelectedOrgUnit = OrgUnit.getOrgUnit(sharedPreferences.getString(getActivity().getApplicationContext().getResources().getString(R.string.default_orgUnit), ""));
    }

    //Get the default orgUnitLevels
    private String getListOrgUnits(){
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getString(getActivity().getApplicationContext().getResources().getString(R.string.default_orgUnits), "");
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    private SharedPreferences.Editor getEditor() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.edit();
    }
    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    public void registerReceiver() {
        Log.d(TAG, "registerReceiver");

        if(surveyReceiver==null){
            surveyReceiver=new SurveyReceiver();
            LocalBroadcastManager.getInstance( getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.ALL_CREATE_SURVEY_DATA_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void  unregisterReceiver(){
        Log.d(TAG, "unregisterReceiver");
        if(surveyReceiver!=null){
            LocalBroadcastManager.getInstance( getActivity()).unregisterReceiver(surveyReceiver);
            surveyReceiver=null;
        }
    }
    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            //Listening only intents from this method
            if (SurveyService.ALL_CREATE_SURVEY_DATA_ACTION.equals(intent.getAction())) {
                HashMap<String,List> data=(HashMap<String,List>) Session.popServiceValue(SurveyService.ALL_CREATE_SURVEY_DATA_ACTION);
                orgUnitList=data.get(SurveyService.PREPARE_ORG_UNIT);
                orgUnitLevelList=data.get(SurveyService.PREPARE_ORG_UNIT_LEVEL);
                allProgramList=data.get(SurveyService.PREPARE_PROGRAMS);
                create();
            }
        }
    }

}
