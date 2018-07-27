package org.eyeseetea.malariacare.strategies;

import android.content.Context;
import android.util.Log;
import android.view.View;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.repositories.SurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.usecase.GetSurveyAnsweredRatioUseCase;
import org.eyeseetea.malariacare.domain.usecase.ISurveyAnsweredRatioCallback;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.DoublePieChart;


public class AssessmentUnsentAdapterCosmeticsStrategy {
    public static void decorateSentSurveyChart(View rowView, SurveyDB survey) {
    }
    public static void decorateUnsentSurveyChart(View rowView, SurveyDB survey) {
    }

    public static void decorateCustomColumns(SurveyDB survey, View rowView) {
        final CustomTextView overall =
                (CustomTextView) rowView.findViewById(R.id.label_overall);

        final CustomTextView  mandatory = (CustomTextView) rowView.findViewById(R.id.label_mandatory_completed);

        ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository =
                new SurveyAnsweredRatioRepository();
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();
        GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase =
                new GetSurveyAnsweredRatioUseCase(surveyAnsweredRatioRepository, mainExecutor, asyncExecutor);

        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {
                        Log.d(getClass().getName(), "nextProgressMessage");
                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                        Log.d(getClass().getName(), "onComplete");

                        if (surveyAnsweredRatio != null) {
                            int mandatoryStatus = surveyAnsweredRatio.getMandatoryStatus();
                            int totalStatus = surveyAnsweredRatio.getTotalStatus();

                            setPercentage(mandatory,mandatoryStatus,
                                    DoublePieChart.getMandatoryColorByPercentage(mandatoryStatus, mandatory.getContext()));
                            setPercentage(overall,totalStatus,DoublePieChart.getOverAllColorByPercentage(totalStatus, overall.getContext()));
                        }
                    }
                });
    }

    private static void setPercentage(CustomTextView textView, int percentage, int color){
        Context context = textView.getContext();
        textView.setText(context.getString(R.string.template_percentage_number,percentage));
        textView.setTextColor(color);
    }
}
