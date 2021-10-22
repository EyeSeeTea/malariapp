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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevelDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.services.BaseServiceBundle;
import org.eyeseetea.malariacare.layout.adapters.general.OrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.general.ProgramArrayAdapter;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomButton;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateSurveyFragment extends Fragment {

    private static String TAG = ".CreateSurveyFragment";

    // UI references.
    private Spinner orgUnitView;
    private View orgUnitContainerItems;
    private String TOKEN = ";";
    private String orgUnitStorage = "";

    public void init() {
        loadHierarchy=true;
    }

    static class ViewHolder {
        public View component;
    }

    private LinkedHashMap<OrgUnitLevelDB, View> orgUnitHierarchyView;

    private SurveyReceiver surveyReceiver;

    private Spinner programView;

    //Loaded one time from service
    List<ProgramDB> allProgramList;
    List<OrgUnitDB> orgUnitList;
    List<OrgUnitLevelDB> orgUnitLevelList;

    private OrgUnitDB orgUnitDefaultOption;
    private ProgramDB programDefaultOption;

    private OrgUnitHierarchy orgUnitHierarchy;

    private LayoutInflater lInflater;
    LinearLayout llLayout;

    OrgUnitProgramFilterView filter;

    //Flag used to control the layout inflating is only in the creation of the fragment.
    private boolean loadHierarchy=true;

    public CreateSurveyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        filter  = (OrgUnitProgramFilterView) DashboardActivity.dashboardActivity.findViewById(
                R.id.assess_org_unit_program_filter_view);
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
        init();
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
        orgUnitHierarchy= new OrgUnitHierarchy();
        CustomButton createButton = (CustomButton) llLayout.findViewById(R.id.create_form_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If the survey is validate, it send the order of create survey fragment from this fragment to the activity.
                if(validateForm()) {
                    createSurvey();
                }
            }
        });
        this.lInflater = LayoutInflater.from(getActivity());

        //Create default options
        orgUnitDefaultOption = new OrgUnitDB(Constants.DEFAULT_SELECT_OPTION);
        programDefaultOption = new ProgramDB(Constants.DEFAULT_SELECT_OPTION);

        //Populate Organization Unit DDL
        ViewHolder viewHolder = new ViewHolder();
        List <OrgUnitDB> orgUnitListFirstLevel=new ArrayList<>();
        for(OrgUnitDB orgUnit:orgUnitList){
            if(orgUnitList.get(0).getOrgUnitLevel().equals(orgUnit.getOrgUnitLevel())) {
                orgUnitListFirstLevel.add(orgUnit);
            }
        }
        orgUnitListFirstLevel.add(0, orgUnitDefaultOption);
        viewHolder.component = llLayout.findViewById(R.id.org_unit);
        orgUnitView = (Spinner) viewHolder.component;
        orgUnitView.setTag(orgUnitListFirstLevel.get(1).getOrgUnitLevel());
        orgUnitView.setAdapter(new OrgUnitArrayAdapter( getActivity(), orgUnitListFirstLevel));
        orgUnitView.setOnItemSelectedListener(new OrgUnitSpinnerListener(viewHolder));

        View childView = llLayout.findViewById(R.id.org_unit_container);

        bindOUFacility(orgUnitListFirstLevel, childView);

        //Put in org unit hierarchy map
        orgUnitHierarchyView = new LinkedHashMap<>();
        orgUnitHierarchyView.put(orgUnitListFirstLevel.get(1).getOrgUnitLevel(), childView);

        //Prepare Organization Unit Item DDL
        orgUnitContainerItems = llLayout.findViewById(R.id.org_unit_container_items);

        for (OrgUnitLevelDB orgUnitLevel : orgUnitLevelList) {
            if (!orgUnitLevel.equals(orgUnitListFirstLevel.get(1).getOrgUnitLevel())) {
                childView = lInflater.inflate(R.layout.create_survey_org_unit_item_fragment, (LinearLayout) orgUnitContainerItems, false);
                CustomTextView childViewTextView = (CustomTextView) childView.findViewById(R.id.textView);
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
        List<ProgramDB> initProgram=new ArrayList<>();
        initProgram.add(0, programDefaultOption);
        programView = (Spinner)  llLayout.findViewById(R.id.program);
        programView.setAdapter(new ProgramArrayAdapter( getActivity(), initProgram));

        //set the first orgUnit saved


        String orgunitUidFilter = filter.getSelectedOrgUnitFilter();

        if(orgunitUidFilter!=null) {
            orgUnitStorage = orgunitUidFilter;
        }else {
            if (orgUnitHierarchy.getSavedUidsList().length() > 1) {
                orgUnitStorage = orgUnitHierarchy.getSavedUidsList().split(TOKEN)[0];
            } else {
                orgUnitStorage = "";
            }
        }

        //Load the root lastorgUnit/firstOrgUnit(if we have orgUnitLevels).
        if(orgUnitStorage!=null && !orgUnitStorage.equals("")){
            orgUnitView.setSelection(getIndex(orgUnitView, OrgUnitDB.getOrgUnit(orgUnitStorage).getName()));
        }
        loadHierarchy=false;
    }

    private void bindOUFacility(List<OrgUnitDB> orgUnitListFirstLevel, View childView) {
        CustomTextView childViewTextView = (CustomTextView) childView.findViewById(
                R.id.textView2);

        if(BuildConfig.OUSelectionHealthFacility) {
            childViewTextView.setText(R.string.health_facility);
        }else{
            childViewTextView.setText(orgUnitListFirstLevel.get(1).getOrgUnitLevel().getName());
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
            if(objectRow instanceof ProgramDB) {
                value= ((ProgramDB) objectRow).getName();
            }
            if(objectRow instanceof OrgUnitDB) {
                value= ((OrgUnitDB) objectRow).getName();
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
            boolean isEverythingFilled = (!programView.getSelectedItem().equals(programDefaultOption));
            boolean isProgramInOrgUnit=orgUnitHierarchy.getLastSelected().getPrograms().contains((ProgramDB)programView.getSelectedItem());
            return isEverythingFilled && isProgramInOrgUnit;
        }catch(NullPointerException ex){
            return false;
        }
    }

    private boolean doesSurveyInProgressExist() {
        // Read Selected Items
        OrgUnitDB orgUnit = orgUnitHierarchy.getLastSelected();
        ProgramDB program = (ProgramDB) programView.getSelectedItem();

        SurveyDB survey = SurveyDB.getInProgressSurveys(orgUnit, program);
        return (survey != null);
    }

    private boolean validateForm(){
        if (!isEverythingFilled()) {
            new AlertDialog.Builder( getActivity())
                    .setTitle( getActivity().getApplicationContext().getString(R.string.dialog_title_missing_selection))
                    .setMessage( getActivity().getApplicationContext().getString(R.string.dialog_content_missing_selection))
                    .setPositiveButton(android.R.string.ok, null).create().show();
        } else if (!orgUnitHierarchy.getLastSelected().getPrograms().contains((ProgramDB) programView.getSelectedItem())) {
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

        //Get selected orgUnit
        OrgUnitDB orgUnit = orgUnitHierarchy.getLastSelected();

        //Get selected program
        ProgramDB program = (ProgramDB)programView.getSelectedItem();


        //save  the list of orgUnits
        orgUnitHierarchy.saveSelectionInPreferences();

        //save the program in the preferents
        setLastSelectedProgram(program.getUid());

        DashboardActivity.dashboardActivity.onCreateSurvey(orgUnit,program);
    }

    private class OrgUnitSpinnerListener implements AdapterView.OnItemSelectedListener {

        private ViewHolder viewHolder;

        public OrgUnitSpinnerListener(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            OrgUnitDB selectedOrgUnit = (OrgUnitDB) ((Spinner)viewHolder.component).getItemAtPosition(1);
            OrgUnitLevelDB selectedOrgUnitLevel=selectedOrgUnit.getOrgUnitLevel();
            selectedOrgUnit = (OrgUnitDB) ((Spinner)viewHolder.component).getItemAtPosition(pos);

            if(selectedOrgUnit.getUid()==null)
                selectedOrgUnit.setOrgUnitLevel(selectedOrgUnitLevel);


            // Populate child view. If it exists in org unit map, grab it; otherwise inflate it
            List<OrgUnitDB> orgUnitList = selectedOrgUnit.getChildrenOrderedByName();

            orgUnitHierarchy.addOrgUnit(selectedOrgUnit);
            if(orgUnitHierarchy.getLastSelected()!=null)
                refreshPrograms(orgUnitHierarchy.getLastSelected());

            // If there are children create actionSpinner or populate it otherwise hide existing one
            if (orgUnitList.size() > 0){
                View childView;
                if(orgUnitList.get(0).getUid()!=null){
                    childView= orgUnitHierarchyView.get(orgUnitList.get(0).getOrgUnitLevel());
                }
                else {
                    childView = orgUnitHierarchyView.get(orgUnitList.get(1).getOrgUnitLevel());
                }
                if(childView==null || childView.findViewById(R.id.org_unit_item_spinner)==null) {
                    hideChild(false);
                }
                else {
                    ViewHolder subViewHolder = new ViewHolder();
                    subViewHolder.component = childView.findViewById(R.id.org_unit_item_spinner);

                    //Show  and populate orgunits actionSpinner
                    if(orgUnitList.get(0).getUid()!=null){
                        orgUnitDefaultOption.setOrgUnitLevel(orgUnitList.get(0).getOrgUnitLevel());
                        orgUnitList.add(0, orgUnitDefaultOption);
                    }
                    Spinner spinner = ((Spinner) subViewHolder.component);
                    spinner.setAdapter(new OrgUnitArrayAdapter(getActivity(), orgUnitList));
                    spinner.setOnItemSelectedListener(new OrgUnitSpinnerListener(subViewHolder));

                    //Loads the saved org units and remove the list when the last was selected
                    orgUnitStorage=orgUnitHierarchy.getSavedUidsList();
                    if (!orgUnitStorage.equals("")) {
                        String[] list = orgUnitStorage.split(TOKEN);
                        String activeUid="";
                        for (int i = 0; i < list.length; i++) {
                            if (!list[i].equals("") && !list[i].equals(TOKEN)) {
                                try {
                                    OrgUnitDB selectOrgUnit= OrgUnitDB.getOrgUnit(list[i]);
                                    int index=getIndex(spinner, selectOrgUnit.getName());
                                    if(index!=0) {
                                        activeUid=selectOrgUnit.getUid();
                                        spinner.setSelection(index, true);
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                        if(!activeUid.equals("")){
                            orgUnitHierarchy.removeUid(activeUid);
                        }
                        Log.d(TAG,orgUnitHierarchy.savedUidsList);
                    }

                    childView.setVisibility(View.VISIBLE);
                }
            } else {
                hideChild(false);
            }
        }

        private void hideChild(boolean click) {
            //If there is not any children, iterate over the org units spinners and hide non needed
            //FIXME This code is horrible. We need a more elegant way
            Boolean setInvisible = false;
            for (Map.Entry<OrgUnitLevelDB, View> entry : orgUnitHierarchyView.entrySet()) {
                if (setInvisible) {
                    View childView = entry.getValue();
                    ((Spinner) childView.findViewById(R.id.org_unit_item_spinner)).setSelection(0, click);
                    childView.setVisibility(View.GONE);
                }
                if (entry.getKey().equals((viewHolder.component).getTag())) {
                    setInvisible = true;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
    //filter programs by orgUnit
    private void refreshPrograms(OrgUnitDB selectedOrgUnit) {
        if(filterPrograms(selectedOrgUnit).size()<=1){
            View view = llLayout.findViewById(R.id.select_survey_view);
            view.setVisibility(View.GONE);
        }
        else{
            View view = llLayout.findViewById(R.id.select_survey_view);
            view.setVisibility(View.VISIBLE);
        }
    }

    //filter programs by orgUnit
    private List<ProgramDB> filterPrograms(OrgUnitDB selectedOrgUnit) {

        List<ProgramDB> initProgram= new ArrayList<>();
        for(ProgramDB orgUnitProgram: selectedOrgUnit.getPrograms()){
            for(ProgramDB program:allProgramList ){
                if(orgUnitProgram!=null && orgUnitProgram.equals(program))
                    initProgram.add(orgUnitProgram);
            }
        }
        initProgram.add(0, programDefaultOption);
        programView = (Spinner)  llLayout.findViewById(R.id.program);
        programView.setAdapter(new ProgramArrayAdapter( getActivity(), initProgram));
        ProgramDB lastSelectedProgram= getLastSelectedProgram();

        String programUidFilter = filter.getSelectedProgramFilter();
        if(!programUidFilter.isEmpty()){
            ProgramDB filteredProgram = ProgramDB.getProgram(programUidFilter);
            programView.setSelection(getIndex(programView, filteredProgram.getName()));
        }else {
            if (lastSelectedProgram != null) {
                programView.setSelection(getIndex(programView, lastSelectedProgram.getName()));
            }
        }
        return initProgram;
    }

    private void saveOrgUnitList(String list){
        SharedPreferences.Editor editor = getEditor();
        editor.putString(getString(R.string.default_orgUnits), list);
        editor.commit();
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

    //Gets the default program/
    private ProgramDB getLastSelectedProgram() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        ProgramDB lastSelectedProgram = ProgramDB.getProgram(sharedPreferences.getString(getActivity().getApplicationContext().getResources().getString(R.string.default_program), ""));
        return lastSelectedProgram;
    }


    //Sets the default program
    private void setLastSelectedProgram(String uid) {
            SharedPreferences.Editor editor = getEditor();
            editor.putString(getString(R.string.default_program), uid);
            editor.commit();
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
            if(loadHierarchy)
                if (SurveyService.ALL_CREATE_SURVEY_DATA_ACTION.equals(intent.getAction())) {
                    BaseServiceBundle data=(BaseServiceBundle) Session.popServiceValue(SurveyService.ALL_CREATE_SURVEY_DATA_ACTION);
                    orgUnitList=(List<OrgUnitDB>)data.getModelList(OrgUnitDB.class.getName());
                    orgUnitLevelList=(List<OrgUnitLevelDB>)data.getModelList(OrgUnitLevelDB.class.getName());
                    allProgramList=(List<ProgramDB>)data.getModelList(ProgramDB.class.getName());
                    create();
                }
        }
    }

    public class OrgUnitHierarchy {

        //Used to control the active selectedHierarchy
        List<OrgUnitDB> selectedHierarchy;

        //Used to restore the saved org unit list in preferences
        String savedUidsList;

        public OrgUnitHierarchy() {
            selectedHierarchy = new ArrayList<OrgUnitDB>();
            //get and set the lastOrgUnit list from Preferences
            setLastOrgUnitStringList();
        }

        public String getSavedUidsList(){
            return savedUidsList;
        }

        //Add the last valid org unit and remove the unselected levels
        public void addOrgUnit(OrgUnitDB orgUnit) {
            for (int i = selectedHierarchy.size()-1; i > 0; i--) {
                //Remove the next levels from the active org unit selected list.
                    if (selectedHierarchy.get(i).getOrgUnitLevel().getId_org_unit_level() >= orgUnit.getOrgUnitLevel().getId_org_unit_level())
                        selectedHierarchy.remove(i);
            }
            //Save only the real org unit
            if(orgUnit.getUid()!=null)
                selectedHierarchy.add(orgUnit);
        }

        //If the uid is the last uid, the list was removed
        public void removeUid(String uid){
            if(!savedUidsList.contains(uid+TOKEN))
                savedUidsList ="";
        }

        //Saved the selectedHierarchy list in the preferences
        public String saveSelectionInPreferences() {
            String orgUnitList = "";
            for (OrgUnitDB orgUnit : selectedHierarchy) {
                orgUnitList += orgUnit.getUid() + TOKEN;
            }

            if (orgUnitList.lastIndexOf(TOKEN) != -1){
                orgUnitList=orgUnitList.substring(0,orgUnitList.lastIndexOf(TOKEN));
            }

            saveOrgUnitList(orgUnitList);
            return orgUnitList;
        }

        public void setLastOrgUnitStringList(){
            savedUidsList =getListOrgUnits();
        }

        //Return the first uid and remove from the list.
        public String getAndRemoveOrderedOrgUnitUid() {
            String[] list= savedUidsList.split(TOKEN);
            savedUidsList = savedUidsList.replace(list[0] + TOKEN, "");
            return list[0];
        }

        public OrgUnitDB getLastSelected() {

            OrgUnitDB filteredOrgUnit =OrgUnitDB.getOrgUnit(filter.getSelectedOrgUnitFilter());

            if(filteredOrgUnit!=null && filteredOrgUnit.getUid()!=null) {
                return filteredOrgUnit;
            }
            //old way
            if(selectedHierarchy.size()>0)
                return selectedHierarchy.get(selectedHierarchy.size()-1);
            else
                return null;
        }
    }
}
