package org.eyeseetea.malariacare.layout.adapters.dashboard;

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
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.sdk.presentation.views.DoubleRectChart;

public class AssessmentUnsentAdapterCosmeticsStrategy {
    public void decorateUnsentSurveyChart(View rowView, SurveyDB survey) {
        final CustomTextView surveyCompletion = (CustomTextView) rowView.findViewById(R.id.survey_completion);
        final CustomTextView surveyMandatoryCompletion = (CustomTextView) rowView.findViewById(R.id.survey_mandatory_completion);

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
                            if(surveyCompletion!=null) {
                                surveyCompletion.setText(surveyAnsweredRatio.getAnswered() + "%");
                            }
                            if(surveyMandatoryCompletion!=null) {
                                surveyMandatoryCompletion.setText(surveyAnsweredRatio.getCompulsoryAnswered() + "%");
                            }
                        }
                    }
                });
    }

    public static void decorateSentSurveyChart(View rowView, SurveyDB survey) {
        View view = rowView.findViewById(R.id.scoreChart);
        if(view==null || !(view instanceof DoubleRectChart)){
            return;
        }

        final DoubleRectChart doubleRectChart =
                (DoubleRectChart) view;
        if(doubleRectChart!=null){
            LayoutUtils.drawScore(survey.getMainScore(), doubleRectChart);

        }
    }
}
