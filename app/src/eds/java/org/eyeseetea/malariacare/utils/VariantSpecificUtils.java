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
import android.view.Gravity;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.PreferencesState;


/**
 * Created by nacho on 28/03/16.
 */
public class VariantSpecificUtils {

    public static void showAlert(int titleId, CharSequence text, Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_about);
        dialog.setTitle(titleId);
        dialog.setCancelable(true);

        //set up text title
        TextView textTile = (TextView) dialog.findViewById(R.id.aboutTitle);
        textTile.setText(BuildConfig.VERSION_NAME);
        textTile.setGravity(Gravity.RIGHT);

        //set up image view
        ImageView img = (ImageView) dialog.findViewById(R.id.aboutImage);
        img.setImageResource(R.drawable.psi);

        //set up text title
        TextView textContent = (TextView) dialog.findViewById(R.id.aboutMessage);
        textContent.setMovementMethod(LinkMovementMethod.getInstance());
        textContent.setText(text);
        //set up button
        Button button = (Button) dialog.findViewById(R.id.aboutButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //now that the dialog is set up, it's time to show it
        dialog.show();
    }

    public void createNewSurvey(OrgUnit orgUnit, TabGroup tabGroup) {
        ComboOrgUnitTabGroup comboOrgUnitTabGroup=new ComboOrgUnitTabGroup(orgUnit,tabGroup);
        LoadLastEvent loadLastEvent= new LoadLastEvent();
        loadLastEvent.execute(comboOrgUnitTabGroup);
    }

    private class LoadLastEvent extends AsyncTask<ComboOrgUnitTabGroup, Void, Void> {
        ComboOrgUnitTabGroup comboOrgUnitTabGroup;
        PullClient.EventInfo eventInfo;

        @Override
        protected Void doInBackground(ComboOrgUnitTabGroup... params) {
            comboOrgUnitTabGroup=params[0];
            PullClient pullClient = new PullClient((DashboardActivity) DashboardActivity.dashboardActivity);
            eventInfo = pullClient.getLastEventUid(comboOrgUnitTabGroup.getOrgUnit(), comboOrgUnitTabGroup.getTabGroup());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            askCreateOrModify(comboOrgUnitTabGroup, eventInfo);
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

    private void askCreateOrModify(final ComboOrgUnitTabGroup comboOrgUnitTabGroup, final PullClient.EventInfo eventInfo){
        new AlertDialog.Builder(DashboardActivity.dashboardActivity)
                .setTitle("")
                .setMessage(String.format(PreferencesState.getInstance().getContext().getResources().getString(R.string.create_or_modify), EventExtended.format(eventInfo.getEventDate(), EventExtended.DHIS2_DATE_FORMAT )))
                .setPositiveButton((R.string.create), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        DashboardActivity activity= (DashboardActivity)DashboardActivity.dashboardActivity;
                        activity.createNewSurvey(comboOrgUnitTabGroup.getOrgUnit(),comboOrgUnitTabGroup.getTabGroup());
                    }
                })
                .setNeutralButton((R.string.modify), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        DashboardActivity activity= (DashboardActivity)DashboardActivity.dashboardActivity;
                        activity.modifySurvey(comboOrgUnitTabGroup.getOrgUnit(),comboOrgUnitTabGroup.getTabGroup(), eventInfo);
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