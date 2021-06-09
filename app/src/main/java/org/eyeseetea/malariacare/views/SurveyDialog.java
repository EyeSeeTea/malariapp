package org.eyeseetea.malariacare.views;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public final class SurveyDialog extends AlertDialog {

    private final View rootView;

    private SurveyDialog(@NonNull final Context context, @NonNull final SurveyDB survey,
            @NonNull final GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase,
            @Nullable final View.OnClickListener editListener,
            @Nullable final View.OnClickListener completeListener,
            @Nullable final View.OnClickListener deleteListener,
            @Nullable final View.OnClickListener cancelListener,
            @StringRes int bodyTextId, boolean  completeEnabled) {

        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        if(inflater == null)
            throw new NullPointerException("Unable to get inflater "+SurveyDialog.class);

        rootView = inflater.inflate(R.layout.modal_menu, null);

        getWindow().setBackgroundDrawableResource(R.color.transparent);

        setView(rootView);

        initBodyText(bodyTextId);

        initButtons(context, survey, editListener, completeListener, deleteListener,
                cancelListener, completeEnabled);

        initPieChart(context, survey, getSurveyAnsweredRatioUseCase);

    }

    private void initBodyText(@StringRes int bodyTextId) {
        final CustomTextView textBody = (CustomTextView) rootView.findViewById(R.id.body_text);
        if (bodyTextId != 0) {
            textBody.setText(bodyTextId);
        } else {
            textBody.setVisibility(View.GONE);
        }
    }

    private void initPieChart(@NonNull final Context context, @NonNull SurveyDB survey,
            @NonNull GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase) {
        final TextView overall = (TextView) rootView.findViewById(R.id.overall_percent);
        final TextView mandatory = (TextView) rootView.findViewById(R.id.mandatory_percent);
        final DoublePieChart chart = (DoublePieChart) rootView.findViewById(R.id.pie_chart);

        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {
                        Log.d(getClass().getName(), "nextProgressMessage");
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                        Log.d(getClass().getName(), "onComplete");

                        if (surveyAnsweredRatio != null) {
                            overall.setText(surveyAnsweredRatio.getTotalStatus() +
                                    context.getString(R.string.percent));
                            overall.setBackgroundColor(chart.getOverAllColorByPercentage(
                                    surveyAnsweredRatio.getTotalStatus(), context));

                            mandatory.setText(surveyAnsweredRatio.getMandatoryStatus() +
                                    context.getString(R.string.percent));
                            mandatory.setBackgroundColor(chart.getMandatoryColorByPercentage(
                                    surveyAnsweredRatio.getMandatoryStatus(), context));

                            chart.createDoublePie(surveyAnsweredRatio.getMandatoryStatus(),
                                    surveyAnsweredRatio.getTotalStatus());

                            SurveyDialog.this.show();
                        }
                    }
                });
    }

    private void initButtons(@NonNull final Context context, @NonNull final SurveyDB survey,
            @Nullable final View.OnClickListener editListener,
            @Nullable final View.OnClickListener completeListener,
            @Nullable final View.OnClickListener deleteListener,
            @Nullable final View.OnClickListener cancelListener,
            @Nullable final boolean completeEnabled) {

        initEditButton(editListener);

        initMarkCompleteButton(completeListener, completeEnabled);

        initDeleteButton(context, survey, deleteListener);

        initCancelButton(cancelListener);

    }

    private void initCancelButton(@Nullable final View.OnClickListener cancelListener) {
        final Button cancel = (Button) rootView.findViewById(R.id.cancel);

        cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (cancelListener != null) {
                            cancelListener.onClick(v);
                        }

                        SurveyDialog.this.dismiss();
                    }
                }
        );
    }

    private void initDeleteButton(@NonNull final Context context, @NonNull final SurveyDB survey,
            @Nullable final View.OnClickListener deleteListener) {

        final Button delete = (Button) rootView.findViewById(R.id.delete);

        if (deleteListener != null) {
            delete.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            new AlertDialog.Builder(context)
                                    .setTitle(
                                            context.getString(R.string.dialog_title_delete_survey))
                                    .setMessage(String.format(
                                            "" + context.getString(
                                                    R.string.dialog_info_delete_survey),
                                            survey.getProgram().getName()))
                                    .setPositiveButton(android.R.string.yes,
                                            new OnClickListener() {
                                                public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                                    deleteListener.onClick(v);

                                                }
                                            })
                                    .setNegativeButton(android.R.string.no, null).create().show();
                            SurveyDialog.this.dismiss();
                        }
                    }
            );
        } else {
            delete.setVisibility(View.GONE);
        }
    }

    private void initMarkCompleteButton(@Nullable final View.OnClickListener completeListener,
            @Nullable final boolean completeEnabled) {
        final Button markComplete = (Button) rootView.findViewById(R.id.mark_completed);
        if(!completeEnabled) {
            markComplete.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            markComplete.setEnabled(false);
        }
        if (completeListener != null) {
            markComplete.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            completeListener.onClick(v);
                            dismiss();
                        }
                    }

            );
        } else {
            markComplete.setVisibility(View.GONE);
        }
    }

    public Button getMarkCompleteButton(){
        return (Button) rootView.findViewById(R.id.mark_completed);
    }

    private void initEditButton(@Nullable final View.OnClickListener editListener) {
        final Button edit = (Button) rootView.findViewById(R.id.edit);

        if (editListener != null) {
            edit.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            editListener.onClick(v);
                            dismiss();
                        }
                    }

            );
        } else {
            edit.setVisibility(View.GONE);
        }
    }


    public static Builder newBuilder(@NonNull final Context context,
            @NonNull final SurveyDB survey) {
        return new Builder(context, survey);
    }


    @SuppressWarnings("unused")
    public static final class Builder {
        private Context context;
        private SurveyDB survey;
        private GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase;
        private View.OnClickListener editListener;
        private View.OnClickListener completeListener;
        private View.OnClickListener deleteListener;
        private View.OnClickListener cancelListener;
        private boolean completeEnabled = true;
        private @StringRes
        int bodyTextId;

        private Builder(@NonNull final Context context, @NonNull final SurveyDB survey) {
            this.context = context;
            this.survey = survey;
        }

        public Builder editButton(View.OnClickListener editListener) {
            this.editListener = editListener;
            return this;
        }

        public Builder completeButton(View.OnClickListener completeListener, boolean completeEnabled) {
            this.completeListener = completeListener;
            this.completeEnabled = completeEnabled;
            return this;
        }

        public Builder deleteButton(View.OnClickListener deleteListener) {
            this.deleteListener = deleteListener;
            return this;
        }

        public Builder cancelButton(View.OnClickListener cancelListener) {
            this.cancelListener = cancelListener;
            return this;
        }

        public Builder getSurveyAnsweredRatioUseCase(
                GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase) {
            this.getSurveyAnsweredRatioUseCase = getSurveyAnsweredRatioUseCase;
            return this;
        }

        public Builder bodyTextID(@StringRes int bodyTextId) {
            this.bodyTextId = bodyTextId;
            return this;
        }

        public SurveyDialog build() {

            if (getSurveyAnsweredRatioUseCase == null) {
                getSurveyAnsweredRatioUseCase = defaultGetSurveyAnsweredRatioUseCase();
            }

            return new SurveyDialog(context, survey, getSurveyAnsweredRatioUseCase,
                    editListener, completeListener, deleteListener,
                    cancelListener, bodyTextId, completeEnabled);
        }

        private GetSurveyAnsweredRatioUseCase defaultGetSurveyAnsweredRatioUseCase() {
            ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository =
                    new SurveyAnsweredRatioRepository();
            IAsyncExecutor asyncExecutor = new AsyncExecutor();
            IMainExecutor mainExecutor = new UIThreadExecutor();
            return new GetSurveyAnsweredRatioUseCase(surveyAnsweredRatioRepository, mainExecutor,
                    asyncExecutor);
        }
    }
}
