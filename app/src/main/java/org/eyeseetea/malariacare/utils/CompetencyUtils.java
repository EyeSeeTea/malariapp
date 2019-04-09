package org.eyeseetea.malariacare.utils;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;

public class CompetencyUtils {
    public static String getTextByCompetency(
            CompetencyScoreClassification classification, Context context) {

        String competencyText = "";

        if (classification == CompetencyScoreClassification.NOT_AVAILABLE) {
            competencyText = context.getString(
                    R.string.competency_classification_not_available);
        } else if (classification == CompetencyScoreClassification.COMPETENT) {
            competencyText = context.getString(R.string.competency_classification_competent);
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            competencyText = context.getString(
                    R.string.competency_classification_competent_improvement);
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            competencyText = context.getString(
                    R.string.competency_classification_not_competent);
        }

        return competencyText;
    }

    public static int getColorByCompetency(
            CompetencyScoreClassification classification, Context context) {

        int color;

        if (classification == CompetencyScoreClassification.COMPETENT) {
            color = ContextCompat.getColor(context, R.color.competency_competent_background_color);
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            color = ContextCompat.getColor(context,
                    R.color.competency_competent_improvement_background_color);
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            color = ContextCompat.getColor(context,
                    R.color.competency_not_competent_background_color);
        } else {
            color = ContextCompat.getColor(context,
                    R.color.competency_not_available_background_color);
        }

        return color;
    }

    public static void setTextByCompetency(
            TextView textView, CompetencyScoreClassification classification) {

        String competencyText = getTextByCompetency(classification, textView.getContext());

        textView.setText(competencyText);
    }

    public static void setBackgroundByCompetency(
            TextView textView, CompetencyScoreClassification classification) {

        Context context = textView.getContext();

        if (classification == CompetencyScoreClassification.NOT_AVAILABLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textView.setBackground(context.getDrawable(R.drawable.background_black_border));
            } else {
                textView.setBackgroundDrawable(
                        ContextCompat.getDrawable(context, R.drawable.background_black_border));
            }
        } else {
            textView.setBackgroundColor(getColorByCompetency(classification, context));
        }
    }

    public static void setTextColorByCompetency(TextView textView,
            CompetencyScoreClassification classification) {
        Context context = textView.getContext();

        if (classification == CompetencyScoreClassification.COMPETENT) {
            textView.setTextColor(
                    ContextCompat.getColor(context, R.color.competency_competent_text_color));
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            textView.setTextColor(ContextCompat.getColor(context,
                    R.color.competency_competent_improvement_text_color));
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            textView.setTextColor(
                    ContextCompat.getColor(context, R.color.competency_not_competent_text_color));
        } else {
            textView.setTextColor(ContextCompat.getColor(context,
                    R.color.competency_not_available_text_color));
        }
    }
}
