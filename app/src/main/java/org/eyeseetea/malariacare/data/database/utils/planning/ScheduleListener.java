package org.eyeseetea.malariacare.data.database.utils.planning;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.utils.DateParser;
import org.eyeseetea.sdk.presentation.views.CustomEditText;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleListener implements View.OnClickListener {
    private AlertDialog mAlertDialog;
    SurveyDB survey;
    Date newScheduledDate;
    Context context;
    OnReloadListener onReloadListener;
    List<SurveyDB> plannedSurveys;

    public ScheduleListener(SurveyDB survey, Context context, OnReloadListener onReloadListener) {
        this.survey=survey;
        this.context=context;
        this.onReloadListener=onReloadListener;
    }

    public ScheduleListener(List<SurveyDB> plannedSurveys, Context applicationContext,OnReloadListener onReloadListener) {
        this.context=applicationContext;
        this.plannedSurveys=plannedSurveys;
        survey=plannedSurveys.get(0);
        this.onReloadListener=onReloadListener;
        createScheduleDialog();
    }

    public void addAlertDialog(AlertDialog alertDialog){
        mAlertDialog = alertDialog;
    }

    @Override
    public void onClick(View v){
        createScheduleDialog();
        if(mAlertDialog!=null) {
            mAlertDialog.dismiss();
        }
    }

    public void createScheduleDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.planning_schedule_dialog);
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        String subtitle;
        if(plannedSurveys==null) {
            subtitle = survey.getProgram().getName() + "\n" + survey.getOrgUnit().getName();
        }else{
            if(plannedSurveys.size()>1) {
                subtitle = String.format(
                        context.getString(R.string.reschedule_title_multiple_survey),
                        plannedSurveys.size(), plannedSurveys.get(0).getOrgUnit().getName());
            }else{
                subtitle = survey.getProgram().getName() + "\n" + survey.getOrgUnit().getName();
            }
        }
        ((TextView) dialog.findViewById(R.id.schedule_title)).setText(subtitle);
        //Set current date
        final CustomEditText scheduleDatePickerButton=(CustomEditText)dialog.findViewById(R.id.planning_dialog_picker_button);
        final Date surveyDefaultDate = survey.getScheduledDate();

        String dateFormatted = formatDate(surveyDefaultDate, context);

        scheduleDatePickerButton.setText(dateFormatted);
        //On Click open an specific DatePickerDialog
        scheduleDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Init secondary datepicker with current date
                Calendar calendar = Calendar.getInstance();
                if(survey.getScheduledDate()!=null){
                    calendar.setTime(surveyDefaultDate);
                }
                //Show datepickerdialog -> updates newScheduledDate and button
                new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newCalendar = Calendar.getInstance();
                        newCalendar.set(year, monthOfYear, dayOfMonth);
                        newScheduledDate = newCalendar.getTime();
                        scheduleDatePickerButton.setText(formatDate(newScheduledDate, context));
                    }

                },calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //Listens to close button
        Button dialogButton = (Button) dialog.findViewById(R.id.planning_dialog_close_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialogButton = (Button) dialog.findViewById(R.id.planning_dialog_ok_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check fields are ok
                String comment = ((EditText) dialog.findViewById(R.id.planning_dialog_comment)).getText().toString();
                if (newScheduledDate == null) {
                    newScheduledDate = surveyDefaultDate;
                }
                //Reschedule survey
                if(plannedSurveys==null) {
                    survey.reschedule(newScheduledDate, comment);
                }
                else {
                    for(SurveyDB survey:plannedSurveys){
                        survey.reschedule(newScheduledDate,comment);
                    }
                }
                //Recalculate items
                if (onReloadListener != null){
                    onReloadListener.onReload();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        ((EditText) dialog.findViewById(R.id.planning_dialog_comment)).requestFocus();
    }

    private String formatDate(Date surveyDefaultDate, Context context) {
        Locale locale = context.getResources().getConfiguration()
                        .locale;
        DateParser dateParser = new DateParser();
        String dateFormatted = dateParser.userFormatDate(surveyDefaultDate, locale);
        if(dateFormatted.isEmpty()){
            return "-";
        }
        return dateFormatted;
    }

    public interface OnReloadListener {
        void onReload();
    }
}