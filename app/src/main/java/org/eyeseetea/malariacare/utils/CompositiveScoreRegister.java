package org.eyeseetea.malariacare.utils;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.CompositiveScore;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.layout.LayoutUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adrian on 26/02/15.
 */
public class CompositiveScoreRegister {

    private static final SparseArray<NumDenRecord> compositiveScoreRegister = new SparseArray<NumDenRecord>();

    public static void addRecord(Question question, Float num, Float den){
        if (getNumDenRecord(question) != null) {
            getNumDenRecord(question).getNumDenRecord().put(question, new ArrayList<Float>(Arrays.asList(num, den)));
        }
    }

    public static NumDenRecord getNumDenRecord(Question question){
        if (question.getCompositiveScore() == null) return null;
        return compositiveScoreRegister.get((int)(long)question.getCompositiveScore().getId());
    }

    public static void registerScore(CompositiveScore compositiveScore){
        compositiveScoreRegister.put((int)(long)compositiveScore.getId(), new NumDenRecord());
    }


    public static void updateCompositivesScore(Question question, View gridView){
        if (getNumDenRecord(question) != null) {
            List<Float> numDenTotal = NumDenRecord.calculateNumDenTotal(getNumDenRecord(question).getNumDenRecord());
            float score;
            float average = 0.0F, totalAverage = 0.0F;
            if (numDenTotal.get(0) == 0 && numDenTotal.get(1) == 0){
                score = 100;
            } else {
                score = (numDenTotal.get(0) / numDenTotal.get(1)) * 100;
            }
            List<View> compositiveScoreRow = LayoutUtils.getChildrenByTag((ViewGroup) gridView.findViewById(R.id.compositivescoreTable), null, "CompositiveScore_" + question.getCompositiveScore().getId().toString());
            ((TextView) ((ViewGroup) ((ViewGroup) compositiveScoreRow.get(0)).getChildAt(2)).getChildAt(0)).setText(Float.toString(score));
        }
    }

    public static void remove(Question question) {
        if (getNumDenRecord(question) != null){
            getNumDenRecord(question).getNumDenRecord().remove(question);
        }
    }
}
