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

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.AUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScheduleListener implements View.OnClickListener {
    private AlertDialog mAlertDialog;
    SurveyDB survey;
    Date newScheduledDate;
    Context context;
    List<SurveyDB> plannedSurveys;
    public ScheduleListener(SurveyDB survey, Context context){this.survey=survey; this.context=context;}

    public ScheduleListener(List<SurveyDB> plannedSurveys, Context applicationContext) {
        this.context=applicationContext;
        this.plannedSurveys=plannedSurveys;
        survey=plannedSurveys.get(0);
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
        dialog.setTitle(R.string.planning_title_dialog);

        //Set current date
        final Button scheduleDatePickerButton=(Button)dialog.findViewById(R.id.planning_dialog_picker_button);
        scheduleDatePickerButton.setText(AUtils.formatDate(survey.getScheduledDate()));
        //On Click open an specific DatePickerDialog
        scheduleDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Init secondary datepicker with current date
                Calendar calendar = Calendar.getInstance();
                if(survey.getScheduledDate()!=null){
                    calendar.setTime(survey.getScheduledDate());
                }
                //Show datepickerdialog -> updates newScheduledDate and button
                new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newCalendar = Calendar.getInstance();
                        newCalendar.set(year, monthOfYear, dayOfMonth);
                        newScheduledDate = newCalendar.getTime();
                        scheduleDatePickerButton.setText(AUtils.formatDate(newScheduledDate));
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
                if (!validateFields(newScheduledDate, comment)) {
                    return;
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
                reloadData();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void reloadData() {
        //Reload data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PLANNED_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
    }

    private boolean validateFields(Date newDate,String comment){
        return newDate!=null;
    }


}