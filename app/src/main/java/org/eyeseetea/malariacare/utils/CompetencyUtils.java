package org.eyeseetea.malariacare.utils;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;

public class CompetencyUtils {
    public static String getTextByCompetencyName(
            CompetencyScoreClassification classification, Context context) {

        String competencyText = "";

        if (classification == CompetencyScoreClassification.NOT_AVAILABLE) {
            competencyText = context.getString(
                    R.string.competency_classification_not_available_name);
        } else if (classification == CompetencyScoreClassification.COMPETENT) {
            competencyText = context.getString(R.string.competency_classification_competent_name);
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            competencyText = context.getString(
                    R.string.competency_classification_competent_improvement_name);
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            competencyText = context.getString(
                    R.string.competency_classification_not_competent_name);
        }

        return competencyText;
    }

    public static String getTextByCompetencyDescription(
            CompetencyScoreClassification classification, Context context) {

        String competencyText = "";

        if (classification == CompetencyScoreClassification.NOT_AVAILABLE) {
            competencyText = context.getString(
                    R.string.competency_classification_not_available_description);
        } else if (classification == CompetencyScoreClassification.COMPETENT) {
            competencyText = context.getString(R.string.competency_classification_competent_description);
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            competencyText = context.getString(
                    R.string.competency_classification_competent_improvement_description);
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            competencyText = context.getString(
                    R.string.competency_classification_not_competent_description);
        }

        return competencyText;
    }

    public static String getTextByCompetencyAbbreviation(
            CompetencyScoreClassification classification, Context context) {

        String competencyText = "";

        if (classification == CompetencyScoreClassification.NOT_AVAILABLE) {
            competencyText = context.getString(
                    R.string.competency_classification_not_available_abbreviation);
        } else if (classification == CompetencyScoreClassification.COMPETENT) {
            competencyText = context.getString(
                    R.string.competency_classification_competent_abbreviation);
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            competencyText = context.getString(
                    R.string.competency_classification_competent_improvement_abbreviation);
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            competencyText = context.getString(
                    R.string.competency_classification_not_competent_abbreviation);
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

        String competencyText = getTextByCompetencyName(classification, textView.getContext());

        textView.setText(competencyText);
    }

    public static void setTextByCompetencyAbbreviation(
            TextView textView, CompetencyScoreClassification classification) {

        String competencyText = getTextByCompetencyAbbreviation(classification, textView.getContext());

        textView.setText(competencyText);
    }

    public static void setBackgroundByCompetency(
            TextView textView, CompetencyScoreClassification classification) {

        Context context = textView.getContext();

        textView.setBackgroundColor(getColorByCompetency(classification, context));
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
