package org.eyeseetea.malariacare.layout.score;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static NumDenRecord getNumDenRecord(CompositiveScore compositiveScore){
        return compositiveScoreRegister.get((int)(long)compositiveScore.getId());
    }

    public static void registerScore(CompositiveScore compositiveScore){
        compositiveScoreRegister.put((int)(long)compositiveScore.getId(), new NumDenRecord());
    }


    public static void updateCompositivesScore(Question question, View gridView){
        float score;
        List<Float> numDenTotal = new ArrayList<Float>(Arrays.asList(0.0F, 0.0F));
//        Float numTotal = 0.0F;
//        Float denTotal = 0.0F;
        if (getNumDenRecord(question) != null) {
            //Iterate to get the children
            CompositiveScore compositiveScore = question.getCompositiveScore();
            numDenTotal = readNumDen(compositiveScore, numDenTotal);

            if (numDenTotal.get(0) == 0 && numDenTotal.get(1) == 0){
                score = 0;
            }else if (numDenTotal.get(0) > 0 && numDenTotal.get(1) == 0){
                score = 0;
                //DialogDispatcher mf = DialogDispatcher.newInstance(null);
                //mf.showDialog(mainActivity.getFragmentManager(), DialogDispatcher.ERROR_DIALOG);
            }
            else {
                score = (numDenTotal.get(0) / numDenTotal.get(1)) * 100;
            }

            List<View> compositiveScoreRow = LayoutUtils.getChildrenByTag((ViewGroup) gridView.findViewById(R.id.compositivescoreTable), null, "CompositiveScore_" + compositiveScore.getId().toString());
            ((TextView) ((ViewGroup) ((ViewGroup) compositiveScoreRow.get(0)).getChildAt(2)).getChildAt(0)).setText(Utils.round(score));

            //Iterate to get the parents
            while (compositiveScore.hasParent()){
                compositiveScore = compositiveScore.getCompositive_score();
                numDenTotal = readNumDen(compositiveScore, numDenTotal);

                if (numDenTotal.get(0) == 0 && numDenTotal.get(1) == 0){
                    score = 0;
                }else if (numDenTotal.get(0) > 0 && numDenTotal.get(1) == 0){
                    score = 0;
                    //DialogDispatcher mf = DialogDispatcher.newInstance(null);
                    //mf.showDialog(mainActivity.getFragmentManager(), DialogDispatcher.ERROR_DIALOG);
                }
                else {
                    score = (numDenTotal.get(0) / numDenTotal.get(1)) * 100;
                }

                compositiveScoreRow = LayoutUtils.getChildrenByTag((ViewGroup) gridView.findViewById(R.id.compositivescoreTable), null, "CompositiveScore_" + compositiveScore.getId().toString());
                ((TextView) ((ViewGroup) ((ViewGroup) compositiveScoreRow.get(0)).getChildAt(2)).getChildAt(0)).setText(Utils.round(score));
            }
        }
    }
    public static List<Float> readNumDen(CompositiveScore compositiveScore, List<Float> numDenTotal){
        Float num = numDenTotal.get(0);
        Float den = numDenTotal.get(1);
        for (List<Float> numDen : getNumDenRecord(compositiveScore).getNumDenRecord().values()) {
            num += numDen.get(0);
            den += numDen.get(1);
        }

        numDenTotal.set(0, num);
        numDenTotal.set(1, den);

        if (compositiveScore.hasChildren()){
            for (CompositiveScore compositiveScoreChild: compositiveScore.getCompositiveScoreChildren()){
                numDenTotal = readNumDen(compositiveScoreChild, numDenTotal);
            }
        }

        return numDenTotal;
    }

    public static void remove(Question question) {
        if (getNumDenRecord(question) != null){
            getNumDenRecord(question).getNumDenRecord().remove(question);
        }
    }
}
