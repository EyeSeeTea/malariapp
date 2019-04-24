package org.eyeseetea.malariacare.presentation.views.observations;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.presentation.presenters.observations.ActionPresenter;
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel;
import org.eyeseetea.malariacare.presentation.views.CustomTextWatcher;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomSpinner;

public class ActionView extends LinearLayout implements ActionPresenter.View {

    public interface OnActionChangedListener{
        void onActionChanged (ActionViewModel actionViewModel);
    }

    private ActionPresenter presenter;

    private OnActionChangedListener onActionChangedListener;

    private CustomSpinner activitiesSpinner;
    private CustomSpinner subActivitiesSpinner;
    private ArrayAdapter<CharSequence> activitiesAdapter;
    private ArrayAdapter<CharSequence> subActivitiesAdapter;

    private View subActivityDividerView;
    private View otherSubActivityDividerView;

    private CustomEditText otherSubActivityEditText;

    public ActionView(Context context) {
        super(context);

        initialize(context);
    }

    public ActionView(Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);

        initialize(context);
    }

    public void setOnActionChangedListener (OnActionChangedListener listener){
        onActionChangedListener = listener;
    }

    public void setAction (ActionViewModel actionViewModel){
        presenter.setAction(actionViewModel);
    }

    @Override
    public void selectActivity(int position) {
        activitiesSpinner.setSelection(position);
    }

    @Override
    public void selectSubActivity(int position) {
        subActivitiesSpinner.setSelection(position);
    }

    @Override
    public void loadActivities(String[] actions) {
        activitiesAdapter.addAll(actions);
    }

    @Override
    public void loadSubActivities(String[] subActions) {
        subActivitiesAdapter.addAll(subActions);
    }

    @Override
    public void notifyOnActionChanged(ActionViewModel actionViewModel){
        if (onActionChangedListener != null){
            onActionChangedListener.onActionChanged(actionViewModel);
        }
    }

    @Override
    public void showSubActivitiesView() {
        subActivitiesSpinner.setVisibility(View.VISIBLE);
        subActivityDividerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSubActivitiesView() {
        subActivitiesSpinner.setVisibility(View.GONE);
        subActivityDividerView.setVisibility(View.GONE);
    }

    @Override
    public void showSubActivityOtherView() {
        otherSubActivityEditText.setVisibility(View.VISIBLE);
        otherSubActivityDividerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSubActivityOtherView() {
        otherSubActivityEditText.setVisibility(View.GONE);
        otherSubActivityDividerView.setVisibility(View.GONE);
    }

    @Override
    public void renderOtherSubActivity(String subActivityAction) {
        otherSubActivityEditText.setText(subActivityAction);
    }

    private void initialize(final Context context) {
        inflate(context, R.layout.view_observation_action, this);

        initializeActivities();
        initializeSubActivities();
        initializeOtherView();
        initializeResponsibleView();
        initializeDueDateView();
        initializePresenter();
    }

    private void initializeResponsibleView() {
    }

    private void initializeDueDateView() {
        TextView responsibleTextView = findViewById(R.id.due_date_text_view);

/*        responsibleTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                    }
                }, startYear, starthMonth, startDay);
            }
        });*/
    }

    private void initializePresenter() {

        String[] activities =
                getResources().getStringArray(R.array.plan_action_dropdown_options);

        String[] subActivities =
                getResources().getStringArray(R.array.plan_action_dropdown_suboptions);

        presenter = new ActionPresenter();
        presenter.attachView(this, activities, subActivities);
    }

    private void initializeActivities() {
        activitiesSpinner = findViewById(R.id.activities_spinner);

        activitiesAdapter =
                new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item);

        activitiesSpinner.setAdapter(activitiesAdapter);
        activitiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                    long l) {
                presenter.onActivitySelected(adapterView.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void initializeSubActivities() {
        subActivitiesSpinner = findViewById(R.id.sub_activities_spinner);

        subActivitiesAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item);


        subActivitiesSpinner.setAdapter(subActivitiesAdapter);


        subActivitiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                    long l) {
                presenter.onSubActivitySelected(adapterView.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void initializeOtherView() {
        subActivityDividerView = findViewById(R.id.sub_activity_divider_view);
        otherSubActivityEditText = findViewById(R.id.other_sub_activity_edit_text);
        otherSubActivityDividerView = findViewById(R.id.other_sub_activity_divider_view);

        otherSubActivityEditText.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                presenter.subActivityOtherChanged(editable.toString());
            }
        });
    }

}
