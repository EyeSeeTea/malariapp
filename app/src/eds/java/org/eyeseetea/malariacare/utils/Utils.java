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
import android.os.AsyncTask;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.network.PullClient;
import org.hisp.dhis.android.sdk.persistence.models.Event;


/**
 * Created by nacho on 28/03/16.
 */
public class Utils extends AUtils {

    @Override
    public void createNewSurvey(OrgUnit orgUnit, TabGroup tabGroup) {
        ComboOrgUnitTabGroup comboOrgUnitTabGroup=new ComboOrgUnitTabGroup(orgUnit,tabGroup);
        LoadLastEvent loadLastEvent= new LoadLastEvent();
        loadLastEvent.execute(comboOrgUnitTabGroup);
    }

    private class LoadLastEvent extends AsyncTask<ComboOrgUnitTabGroup, Void, Event> {
        ComboOrgUnitTabGroup comboOrgUnitTabGroup;

        @Override
        protected Event doInBackground(ComboOrgUnitTabGroup... params) {
            comboOrgUnitTabGroup=params[0];
            PullClient pullClient = new PullClient(DashboardActivity.dashboardActivity);
            return pullClient.getLastEventInServerWith(comboOrgUnitTabGroup.getOrgUnit(), comboOrgUnitTabGroup.getTabGroup());
        }

        @Override
        protected void onPostExecute(Event lastEventInServer) {
            super.onPostExecute(lastEventInServer);
            // Dismiss the progress dialog
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            //Preference ask to modify or create
            if(PreferencesState.getInstance().isAskModifyOrCreate()){
                askCreateOrModify(comboOrgUnitTabGroup, lastEventInServer);
            }else{
                ((DashboardActivity)DashboardActivity.dashboardActivity).modifySurvey(comboOrgUnitTabGroup.getOrgUnit(),comboOrgUnitTabGroup.getTabGroup(), lastEventInServer);
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

    private void askCreateOrModify(final ComboOrgUnitTabGroup comboOrgUnitTabGroup, final Event lastEventInServer){
        String dialogMessage;
        if(lastEventInServer==null){
            dialogMessage=PreferencesState.getInstance().getContext().getResources().getString(R.string.no_previous_event_info);
        }else{
            dialogMessage=String.format(PreferencesState.getInstance().getContext().getResources().getString(R.string.create_or_modify), lastEventInServer.getEventDate());
        }
        final DashboardActivity activity= (DashboardActivity)DashboardActivity.dashboardActivity;
        new AlertDialog.Builder(DashboardActivity.dashboardActivity)
                .setTitle("")
                .setMessage(dialogMessage)
                .setPositiveButton((R.string.create), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        activity.createNewSurvey(comboOrgUnitTabGroup.getOrgUnit(),comboOrgUnitTabGroup.getTabGroup());
                    }
                })
                .setNeutralButton((R.string.modify), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        activity.modifySurvey(comboOrgUnitTabGroup.getOrgUnit(),comboOrgUnitTabGroup.getTabGroup(), lastEventInServer);
                    }
                })
                .setNegativeButton((R.string.cancel), null)
                .setCancelable(true)
                .create().show();
    }

    public class ComboOrgUnitTabGroup{
        OrgUnit orgUnit;
        TabGroup tabGroup;

        public ComboOrgUnitTabGroup(OrgUnit orgUnit, TabGroup tabGroup) {
            this.orgUnit = orgUnit;
            this.tabGroup = tabGroup;
        }

        public OrgUnit getOrgUnit() {
            return orgUnit;
        }

        public void setOrgUnit(OrgUnit orgUnit) {
            this.orgUnit = orgUnit;
        }

        public TabGroup getTabGroup() {
            return tabGroup;
        }

        public void setTabGroup(TabGroup tabGroup) {
            this.tabGroup = tabGroup;
        }
    }
}