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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.network.PullClient;

/**
 * Created by nacho on 28/03/16.
 */
public class VariantSpecificUtils{

    public static void showAlert(int titleId, CharSequence text, Context context){
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

    public void createNewSurvey(final OrgUnit orgUnit, final TabGroup tabGroup) {
        final DashboardActivity activity = ((DashboardActivity) DashboardActivity.dashboardActivity);
        activity.createNewSurvey(orgUnit, tabGroup);
    }
}