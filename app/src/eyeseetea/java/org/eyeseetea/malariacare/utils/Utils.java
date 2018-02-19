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

import android.app.Dialog;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.adapters.survey.FeedbackAdapter;
import org.eyeseetea.malariacare.presentation.presenters.ObsActionPlanPresenter;
import org.eyeseetea.malariacare.views.CustomRadioButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

/**
 * Created by nacho on 28/03/16.
 */
public class Utils extends AUtils {

    public static void showAlert(int titleId, CharSequence text, Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_about);
        dialog.setTitle(titleId);
        dialog.setCancelable(true);

        //set up text title
        TextView textTile = (TextView) dialog.findViewById(R.id.aboutTitle);
        textTile.setText(BuildConfig.FLAVOR.toUpperCase() + "(bb) " + BuildConfig.VERSION_NAME);
        textTile.setGravity(Gravity.RIGHT);

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

    public static void showFeedbackFilters(Context context, CustomRadioButton chkFailed, CustomRadioButton chkMedia, final FeedbackAdapter feedbackAdapter) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_feedback_filters);
        //dialog.setTitle(titleId);

        TextView title = (TextView) dialog.findViewById(R.id.programName);
        title.setText(SurveyDB.findById((long) feedbackAdapter.getIdSurvey()).getProgram().getName());
        dialog.setCancelable(false);
        Button button = (Button) dialog.findViewById(R.id.apply_filter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedbackAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        //now that the dialog is set up, it's time to show it

        chkFailed = (CustomRadioButton) dialog.findViewById(R.id.chkFailed);
        chkFailed.setText(context.getResources().getString(R.string.failed_questions_only)+"   ");
        chkFailed.setChecked(true);
        chkFailed.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             feedbackAdapter.toggleOnlyFailed(false);
                                             ((CustomRadioButton) v).setChecked(feedbackAdapter



                                                                                            .isOnlyFailed());
                                         }
                                     }
        );
        chkMedia = (CustomRadioButton) dialog.findViewById(R.id.chkMedia);
        chkMedia.setText(context.getResources().getString(R.string.failed_video_image_question_only)+"   ");
        chkMedia.setChecked(false);
        chkMedia.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            feedbackAdapter.toggleOnlyMedia(false);
                                            ((CustomRadioButton) v).setChecked(feedbackAdapter
                                                    .isOnlyMedia());
                                        }
                                    }
        );


        dialog.show();
    }
}