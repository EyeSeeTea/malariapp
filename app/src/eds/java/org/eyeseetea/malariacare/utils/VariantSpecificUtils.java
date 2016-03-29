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
import android.app.Dialog;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.eyeseetea.malariacare.BaseActivity;
import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.R;

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

    public static void createNewSurvey(final OrgUnit orgUnit, final TabGroup tabGroup) {
        final DashboardActivity activity = ((DashboardActivity) DashboardActivity.dashboardActivity);
        Survey survey = new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.ID_ORG_UNIT).eq(orgUnit.getId_org_unit()))
                .and(Condition.column(Survey$Table.ID_TAB_GROUP).eq(tabGroup.getId_tab_group()))
                .and(Condition.column(Survey$Table.STATUS).is(Constants.SURVEY_COMPLETED))
                .or(Condition.column(Survey$Table.STATUS).is(Constants.SURVEY_SENT))
                .or(Condition.column(Survey$Table.STATUS).is(Constants.SURVEY_CONFLICT))
                .orderBy(false,Survey$Table.COMPLETIONDATE).querySingle();
        new AlertDialog.Builder(DashboardActivity.dashboardActivity)
                .setTitle(null)
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
    }
}