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
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentUnsentAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.services.SurveyService;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardUnsentFragment extends ListFragment {


    public static final String TAG = ".DetailsFragment";
    private SurveyReceiver surveyReceiver;
    private List<Survey> surveys;
    protected IDashboardAdapter adapter;
    private static int index = 0;
    private static int selectedPosition=0;
    private AlarmPushReceiver alarmPush;
    OnUnsentDashboardListener mCallback;


    public DashboardUnsentFragment(){
        this.adapter = Session.getAdapterUnsent();
        this.surveys = new ArrayList();
    }

    public static DashboardUnsentFragment newInstance(int index) {
        DashboardUnsentFragment f = new DashboardUnsentFragment();

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
        alarmPush = new AlarmPushReceiver();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (container == null) {
            return null;
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        initListView();
        registerForContextMenu(getListView());
    }

    @Override
    public void onResume(){
        Log.d(TAG, "onResume");
        //Loading...
        setListShown(false);
        //Listen for data
        registerSurveysReceiver();
        super.onResume();
    }

    /**
     * Inits adapter.
     * Most of times is just an AssessmentAdapter.
     * In a version with several adapters in dashboard (like in 'mock' branch) a new one like the one in session is created.
     */
    private void initAdapter(){
        IDashboardAdapter adapterInSession = Session.getAdapterUnsent();
        if(adapterInSession == null){
            adapterInSession = new AssessmentUnsentAdapter(this.surveys,getActivity());
        }else{
            adapterInSession = adapterInSession.newInstance(this.surveys,getActivity());
        }
        this.adapter = adapterInSession;
        Session.setAdapterUnsent(this.adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if(isPositionASurvey(selectedPosition)) {
            MenuInflater inflater=getActivity().getMenuInflater();
            inflater.inflate(R.menu.unsent_options,menu);
        }
    }

    // Container Activity must implement this interface
    public interface OnUnsentDashboardListener {
        public void onSurveySelected(Survey survey);

        void dialogCompulsoryQuestionIncompleted();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnUnsentDashboardListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnUnsentDashboardListener");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Log.d(TAG,"id"+item.getItemId());
        switch (item.getItemId()) {
            case R.id.option_edit:
                mCallback.onSurveySelected(surveys.get(selectedPosition-1));

                return true;
            case R.id.option_mark_completed:
                Survey survey=(Survey)adapter.getItem(selectedPosition-1);

                SurveyAnsweredRatio surveyAnsweredRatio=survey.getAnsweredQuestionRatio();

                if(surveyAnsweredRatio.getTotalCompulsory()>0) {
                    if(Float.valueOf(100 * surveyAnsweredRatio.getCompulsoryRatio()).intValue()>=100) {
                        survey.setCompleteSurveyState();
                        reloadData();
                    }
                    else{
                        mCallback.dialogCompulsoryQuestionIncompleted();
                    }
                }
                else {
                survey.setCompleteSurveyState();
                reloadData();
                }
                return true;
            case R.id.option_delete:
                Log.d(TAG, "removing item pos=" + selectedPosition);
                ((Survey)adapter.getItem(selectedPosition-1)).delete();
                reloadData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void reloadData(){
        //Reload data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
    }

    public void reloadToSend(){
        //Reload data using service
        Intent surveysIntent=new Intent(getActivity(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.ALL_COMPLETED_SURVEYS_ACTION);
        getActivity().startService(surveysIntent);
    }
    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        unregisterSurveysReceiver();

        super.onStop();
    }

    /**
     * Checks if the given position points to a real survey instead of a footer or header of the listview.
     * @param position
     * @return true|false
     */
    private boolean isPositionASurvey(int position){
        return !isPositionFooter(position) && !isPositionHeader(position);
    }

    /**
     * Checks if the given position is the header of the listview instead of a real survey
     * @param position
     * @return true|false
     */
    private boolean isPositionHeader(int position){
        return position<=0;
    }

    /**
     * Checks if the given position is the footer of the listview instead of a real survey
     * @param position
     * @return true|false
     */
    private boolean isPositionFooter(int position){
        return position==(this.surveys.size()+1);
    }

    /**
     * Initializes the listview component, adding a listener for swiping right
     */
    private void initListView(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View header = inflater.inflate(this.adapter.getHeaderLayout(), null, false);
        View footer = inflater.inflate(this.adapter.getFooterLayout(), null, false);
        ListView listView = getListView();
        listView.addHeaderView(header);
        listView.addFooterView(footer);
        setListAdapter((BaseAdapter) adapter);
        Session.listViewUnsent = listView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        //Discard clicks on header|footer (which is attendend on newSurvey via super)
        selectedPosition=position;
        l.showContextMenuForChild(v);
    }

    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    private void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if(surveyReceiver==null){
            surveyReceiver=new SurveyReceiver();
                LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION));
                LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.ALL_COMPLETED_SURVEYS_ACTION));
        }
    }

    public void manageSurveysAlarm(List<Survey> newListSurveys){
        Log.d(TAG, "setSurveysAlarm (Thread: " + Thread.currentThread().getId() + "): " + newListSurveys.size());
        //Fixme think other way to cancel the setPushAlarm in Malariaapp
        alarmPush.setPushAlarm(getActivity());
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterSurveysReceiver(){
        Log.d(TAG, "unregisterSurveysReceiver");
        if(surveyReceiver!=null){
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(surveyReceiver);
            surveyReceiver=null;
        }
    }
    public void reloadInProgressSurveys(){
        List<Survey> surveysInProgressFromService = (List<Survey>) Session.popServiceValue(SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION);
        reloadSurveys(surveysInProgressFromService);
        //set alarm if is malariaapp question
        reloadCompletedSurveys();
    }

    public void reloadCompletedSurveys(){
        List<Survey> surveysCompletedFromService = (List<Survey>) Session.popServiceValue(SurveyService.ALL_COMPLETED_SURVEYS_ACTION);
        if(surveysCompletedFromService!=null) {
            if (surveysCompletedFromService.size() > 0) {
                manageSurveysAlarm(surveysCompletedFromService);
            } else
                alarmPush.cancelPushAlarm(getActivity().getApplicationContext());
        }
    }

    public void reloadSurveys(List<Survey> newListSurveys){
        if(newListSurveys!=null) {
            Log.d(TAG, "reloadSurveys (Thread: " + Thread.currentThread().getId() + "): " + newListSurveys.size());
            this.surveys.clear();
            this.surveys.addAll(newListSurveys);
            this.adapter.notifyDataSetChanged();
            setListShown(true);
        }
    }

    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver{
        private SurveyReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            //Listening only intents from this method
            if(SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION.equals(intent.getAction())) {
                reloadInProgressSurveys();
            }
            //Listening only intents from this method
            //if the state is completed, the state is not sent.
            if (SurveyService.ALL_COMPLETED_SURVEYS_ACTION.equals(intent.getAction())) {
                reloadCompletedSurveys();
            }
        }
    }


}