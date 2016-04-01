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

package org.eyeseetea.malariacare.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.network.PushClient;
import org.hisp.dhis.android.sdk.persistence.models.Event;

/**
 * Created by nacho on 28/03/16.
 */
public class VariantSpecificUtils{

    public static void showAlert(int titleId, CharSequence text, Context context){
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(titleId))
                .setMessage(text)
                .setNeutralButton(android.R.string.ok, null).create();
        dialog.show();
        ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static OrgUnit orgUnit;
    public static TabGroup tabGroup;
    public static DashboardActivity activity;
    public void createNewSurvey(final OrgUnit orgUnit, final TabGroup tabGroup) {
        this.orgUnit=orgUnit;
        this.tabGroup=tabGroup;
        activity = ((DashboardActivity) DashboardActivity.dashboardActivity);
        askCreateOrModify();
        //reloadAllEvents();
    }

    private void askCreateOrModify() {

        RefreshEvents refreshEvents= new RefreshEvents();
        refreshEvents.execute(DashboardActivity.dashboardActivity);
    }

    private PushClient getPushClient(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String user=sharedPreferences.getString(context.getString(R.string.dhis_user), "");
        String password=sharedPreferences.getString(context.getString(R.string.dhis_password), "");
        return new PushClient(context, user,password);
    }
    @NonNull
    private DashboardActivity reloadAllEvents() {
        //activity.createNewSurvey(orgUnit, tabGroup);
        RefreshEvents refreshEvents=new RefreshEvents();
        refreshEvents.execute(activity.getApplicationContext());
        return activity;
    }


    private class RefreshEvents extends AsyncTask<Context, Void, Void> {
        Survey survey;
        @Override
        protected Void doInBackground(Context... params) {
            //sleep for wait the ontab change
            //PullController.refreshEvents(PreferencesState.getInstance().getContext());

            PushClient pushClient = getPushClient(DashboardActivity.dashboardActivity);
            //Push  data
            Event lastEvent=pushClient.getLastEvent(orgUnit, tabGroup.getProgram());
            survey=Survey.getLastSurvey(orgUnit, tabGroup);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            new AlertDialog.Builder(activity)
                    .setTitle("")
                    .setMessage(String.format(PreferencesState.getInstance().getContext().getResources().getString(R.string.create_or_patch), EventExtended.format(survey.getCompletionDate(), EventExtended.DHIS2_DATE_FORMAT ))+survey.getEventUid())
                    .setPositiveButton((R.string.create), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            activity.createNewSurvey(orgUnit, tabGroup);
                        }
                    })
                    .setNeutralButton((R.string.patch), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            activity.patchSurvey(orgUnit, tabGroup);
                        }
                    })
                    .setNegativeButton((R.string.cancel), null)
                    .setCancelable(true)
                    .create().show();
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(DashboardActivity.dashboardActivity);
            progressDialog.setMessage(PreferencesState.getInstance().getContext().getResources().getString(R.string.loading_last_surveys));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}