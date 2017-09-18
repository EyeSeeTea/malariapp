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

import static org.eyeseetea.malariacare.services.SurveyService.PREPARE_FEEDBACK_ACTION_ITEMS;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomSpinner;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.List;

public class PlanActionFragment extends Fragment implements IModuleFragment {

    public static final String TAG = ".PlanActionFragment";

    private String moduleName;
    boolean isFABOpen;
    FloatingActionButton fabHtmlOption;
    CustomTextView mTextViewHtml;
    FloatingActionButton fabPlainTextOption;
    CustomTextView mTextViewPlainText;

    /**
     * Parent layout
     */
    RelativeLayout llLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        llLayout = (RelativeLayout) inflater.inflate(R.layout.plan_action_fragment, container, false);
        setLayoutHeaders(llLayout);
        prepareUI(moduleName);
        setSpinner(llLayout);
        setFAB(llLayout);
        setBackButton(llLayout);
        return llLayout; // We must return the loaded Layout
    }

    private void setBackButton(RelativeLayout llLayout) {
        CustomRadioButton goback = (CustomRadioButton) llLayout.findViewById(
                R.id.backToSentSurveys);
        goback.setText(Session.getSurveyByModule(moduleName).getOrgUnit().getName());
        goback.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          getActivity().onBackPressed();
                                      }
                                  }
        );
    }

    private void setFAB(RelativeLayout llLayout) {
        FloatingActionButton fab = (FloatingActionButton) llLayout.findViewById(R.id.fab);
        fabHtmlOption = (FloatingActionButton) llLayout.findViewById(R.id.fab1);
        mTextViewHtml = (CustomTextView) llLayout.findViewById(R.id.text1);
        fabPlainTextOption = (FloatingActionButton) llLayout.findViewById(R.id.fab2);
        mTextViewPlainText = (CustomTextView) llLayout.findViewById(R.id.text2);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });
        fabHtmlOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });
        fabPlainTextOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });
    }

    private void showFABMenu(){
        isFABOpen=true;
        fabHtmlOption.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mTextViewHtml.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mTextViewHtml.setVisibility(View.VISIBLE);
        fabPlainTextOption.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        mTextViewPlainText.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        mTextViewPlainText.setVisibility(View.VISIBLE);
    }

    private void closeFABMenu(){
        isFABOpen = false;
        mTextViewPlainText.animate().translationY(0);
        mTextViewHtml.animate().translationY(0);
        fabHtmlOption.animate().translationY(0);
        fabPlainTextOption.animate().translationY(0);
        mTextViewPlainText.setVisibility(View.GONE);
        mTextViewHtml.setVisibility(View.GONE);
    }

    public boolean onBackPressed() {
        if(!isFABOpen){
            return false;
        }else{
            closeFABMenu();
            return true;
        }
    }

    private void setSpinner(RelativeLayout llLayout) {
        CustomSpinner spinner = (CustomSpinner) llLayout.findViewById(R.id.plan_action_spinner);

        final CustomSpinner secondarySpinner = (CustomSpinner) llLayout.findViewById(R.id.plan_action_secondary_spinner);
        final CustomEditText othersEditText = (CustomEditText) llLayout.findViewById(R.id.plan_action_others_edit_text);

        ArrayAdapter<CharSequence> secondaryAdapter = ArrayAdapter.createFromResource(llLayout.getContext(),R.array.plan_action_dropdown_suboptions, android.R.layout.simple_spinner_item);
        secondarySpinner.setAdapter(secondaryAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(llLayout.getContext(),R.array.plan_action_dropdown_options, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String[] options = getResources().getStringArray(R.array.plan_action_dropdown_options);
                if(adapterView.getItemAtPosition(position).equals(options[1])){
                    secondarySpinner.setVisibility(View.VISIBLE);
                    othersEditText.setVisibility(View.GONE);
                }else if(adapterView.getItemAtPosition(position).equals(options[5])) {
                    secondarySpinner.setVisibility(View.GONE);
                    othersEditText.setVisibility(View.VISIBLE);
                }
                else{
                    secondarySpinner.setVisibility(View.GONE);
                    othersEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                secondarySpinner.setVisibility(View.GONE);
                othersEditText.setVisibility(View.GONE);
            }
        });

    }

    private void setLayoutHeaders(RelativeLayout llLayout) {
        Survey survey = Session.getSurveyByModule(moduleName);
        if (survey.hasMainScore()) {
            float average = survey.getMainScore();
            CustomTextView item = (CustomTextView) llLayout.findViewById(R.id.feedback_total_score);
            item.setText(String.format("%.1f%%", average));
            int colorId = LayoutUtils.trafficColor(average);
            item.setBackgroundColor(getResources().getColor(colorId));
        } else {
            CustomTextView item = (CustomTextView) llLayout.findViewById(R.id.feedback_total_score);
            item.setText(String.format("NaN"));
            float average = 0;
            int colorId = LayoutUtils.trafficColor(average);
            item.setBackgroundColor(getResources().getColor(colorId));
        }
        CustomTextView nextDate = (CustomTextView) llLayout.findViewById(R.id.plan_completion_day);
        String formattedCompletionDate="NaN";
        if(survey.getCompletionDate()!=null){
            formattedCompletionDate =  EventExtended.format(survey.getCompletionDate(),EventExtended.EUROPEAN_DATE_FORMAT);
        }
        nextDate.setText(String.format(getString(R.string.plan_action_today_date),formattedCompletionDate));

        CustomTextView completionDate = (CustomTextView) llLayout.findViewById(R.id.new_supervision_date);
        String formattedNextDate="NaN";
        if(survey.getScheduledDate()!=null){
            formattedNextDate =  EventExtended.format(survey.getScheduledDate(),EventExtended.EUROPEAN_DATE_FORMAT);
        }
        completionDate.setText(String.format(getString(R.string.plan_action_next_date),formattedNextDate));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        List<Feedback> feedbackList= new ArrayList<>();
        Session.putServiceValue(PREPARE_FEEDBACK_ACTION_ITEMS, feedbackList);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void prepareUI(String module) {
    }

    public void setModuleName(String simpleName) {
        this.moduleName = simpleName;
    }

    @Override
    public void reloadData() {
    }

}
