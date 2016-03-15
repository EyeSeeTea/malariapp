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
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentUnsentAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;
import org.eyeseetea.malariacare.layout.listeners.SwipeDismissListViewTouchListener;
import org.eyeseetea.malariacare.network.PushClient;
import org.eyeseetea.malariacare.network.PushResult;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardUnsentFragment extends ListFragment implements IModuleFragment{


    public static final String TAG = ".DetailsFragment";
    private SurveyReceiver surveyReceiver;
    private List<Survey> surveys;
    protected IDashboardAdapter adapter;
    private static int index = 0;
    private static int selectedPosition=0;
    onSurveySelectedListener mCallback;


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
        if(!PreferencesState.getInstance().isVerticalDashboard())
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
    public interface onSurveySelectedListener {
        public void onSurveySelected(Survey survey);

        void dialogCompulsoryQuestionIncompleted();

        void alertOnComplete(Survey survey);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (onSurveySelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnUnsentDashboardListener");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Log.d(TAG, "id" + item.getItemId());
        final Survey survey=(Survey)adapter.getItem(selectedPosition-1);
        switch (item.getItemId()) {
            case R.id.option_edit:
                mCallback.onSurveySelected(survey);

                return true;
            case R.id.option_mark_completed:

                SurveyAnsweredRatio surveyAnsweredRatio=survey.getAnsweredQuestionRatio();
                if(surveyAnsweredRatio.getTotalCompulsory()>0) {
                    if(Float.valueOf(100 * surveyAnsweredRatio.getCompulsoryRatio()).intValue()>=100) {
                        survey.setCompleteSurveyState();
                        mCallback.alertOnComplete(survey);
                        removeSurveyFromAdapter(survey);
                        reloadToSend();
                    }
                    else{
                        mCallback.dialogCompulsoryQuestionIncompleted();
                    }
                }
                else {
                    survey.setCompleteSurveyState();
                    mCallback.alertOnComplete(survey);
                    removeSurveyFromAdapter(survey);
                    reloadToSend();
                }
                return true;
            case R.id.option_delete:
                Log.d(TAG, "removing item pos=" + selectedPosition);
                new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getString(R.string.dialog_title_delete_survey))
                        .setMessage(String.format(getActivity().getString(R.string.dialog_info_delete_survey), survey.getProgram().getName()))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                //this method create a new survey geting the getScheduledDate date of the oldsurvey, and remove it.
                                SurveyPlanner.getInstance().deleteSurveyAndBuildNext(survey);
                                removeSurveyFromAdapter(survey);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).create().show();

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    //Remove survey from the list and reload list.
    private void removeSurveyFromAdapter(Survey survey) {
        adapter.remove(survey);
        adapter.notifyDataSetChanged();
    }

    @Override
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
    public void onPause(){
        Log.d(TAG, "onPause");
        unregisterSurveysReceiver();

        super.onPause();
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
        if(PreferencesState.getInstance().isVerticalDashboard()) {
            CustomTextView title = (CustomTextView) getActivity().findViewById(R.id.titleInProgress);
            title.setText(adapter.getTitle());
        }
        ListView listView = getListView();
        listView.addHeaderView(header);
        listView.addFooterView(footer);
        setListAdapter((BaseAdapter) adapter);
        if(!PreferencesState.getInstance().isVerticalDashboard())
                Session.listViewUnsent = listView;
        else{

            // Create a ListView-specific touch listener. ListViews are given special treatment because
            // by default they handle touches for their list items... i.e. they're in charge of drawing
            // the pressed state (the list selector), handling list item clicks, etc.
            SwipeDismissListViewTouchListener touchListener =
                    new SwipeDismissListViewTouchListener(
                            listView,
                            new SwipeDismissListViewTouchListener.DismissCallbacks() {
                                @Override
                                public boolean canDismiss(int position) {
                                    return position>0 && position<=surveys.size();
                                }

                                @Override
                                public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                    for (final int position : reverseSortedPositions) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getActivity().getString(R.string.dialog_title_delete_survey))
                                                .setMessage(getActivity().getString(R.string.dialog_info_delete_survey))
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface arg0, int arg1) {
                                                        ((Survey)adapter.getItem(position-1)).delete();
                                                        //Reload data using service
                                                        Intent surveysIntent=new Intent(getActivity(), SurveyService.class);
                                                        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
                                                        getActivity().startService(surveysIntent);
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.no, null).create().show();
                                    }

                                }
                            });
            listView.setOnTouchListener(touchListener);
            // Setting this scroll listener is required to ensure that during ListView scrolling,
            // we don't look for swipes.
            listView.setOnScrollListener(touchListener.makeScrollListener());

            listView.setLongClickable(true);

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {


                    new AlertDialog.Builder(getActivity())
                            .setTitle("Pushing data")
                            .setMessage("Are you sure? You can not undo this action")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // We launch the login system, to authorize the push
                                    Intent authorizePush = new Intent(getActivity(), LoginActivity.class);
                                    authorizePush.putExtra("Action", Constants.AUTHORIZE_PUSH);
                                    authorizePush.putExtra("Survey", position);
                                    startActivityForResult(authorizePush, Constants.AUTHORIZE_PUSH);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).create().show();


                    return true;
                }
            });
        }


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
        AlarmPushReceiver.getInstance().setPushAlarm(getActivity());
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
    }

    public void reloadCompletedSurveys(){
        List<Survey> surveysCompletedFromService = (List<Survey>) Session.popServiceValue(SurveyService.ALL_COMPLETED_SURVEYS_ACTION);
        if(surveysCompletedFromService!=null) {
            if (surveysCompletedFromService.size() > 0) {
                manageSurveysAlarm(surveysCompletedFromService);
            } else
                AlarmPushReceiver.getInstance().cancelPushAlarm(getActivity().getApplicationContext());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "onActivityResult");
        // This captures the return code sent by Login Activity, to know whether or not the user got the authorization
        if(requestCode == Constants.AUTHORIZE_PUSH) {
            if(resultCode == Activity.RESULT_OK) {

                //Tell the activity NOT to reload on next resume since the push itself will do it
                ((DashboardActivity)getActivity()).setReloadOnResume(false);

                // In case authorization was ok, we launch push action
                Bundle extras = data.getExtras();
                int position = extras.getInt("Survey", 0);
                String user = extras.getString("User");
                String password = extras.getString("Password");
                final Survey survey = (Survey) adapter.getItem(position - 1);
                AsyncPush asyncPush = new AsyncPush(survey, user, password);
                asyncPush.execute((Void) null);
            } else {
                // Otherwise we notify and continue
                new AlertDialog.Builder(getActivity())
                        .setTitle("Authorization failed")
                        .setMessage("User or password introduced are wrong. Push aborted.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(android.R.string.no, null).create().show();
            }
        }
    }


    public class AsyncPush extends AsyncTask<Void, Integer, PushResult> {

        private Survey survey;
        private String user;
        private String password;


        public AsyncPush(Survey survey, String user, String password) {
            this.survey = survey;
            this.user = user;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //spinner
            setListShown(false);
        }

        @Override
        protected PushResult doInBackground(Void... params) {
            PushClient pushClient=new PushClient(survey, getActivity(), user, password);
            return pushClient.pushAPI();
        }

        @Override
        protected void onPostExecute(PushResult pushResult) {
            super.onPostExecute(pushResult);
            showResponse(pushResult);
        }

        /**
         * Shows the proper response message
         * @param pushResult
         */
        private void showResponse(PushResult pushResult){
            String msg="";
            if(pushResult.isSuccessful()){
                msg="Survey data pushed to server. Results: \n"+String.format("Imported: %s | Updated: %s | Ignored: %s",pushResult.getImported(),pushResult.getUpdated(),pushResult.getIgnored());
            }else{
                msg=pushResult.getException().getMessage();
            }

            new AlertDialog.Builder(getActivity())
                    .setTitle(getActivity().getString(R.string.dialog_title_push_response))
                    .setMessage(msg)
                    .setNeutralButton(android.R.string.yes,null).create().show();

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