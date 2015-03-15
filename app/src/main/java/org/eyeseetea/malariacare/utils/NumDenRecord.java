package org.eyeseetea.malariacare.utils;

import org.eyeseetea.malariacare.data.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adrian on 26/02/15.
 */
public class NumDenRecord {

    private Map<Question,List<Float>> numDenRecord = new HashMap<Question,List<Float>>();

    public void addRecord(Question question, Float num, Float den){
        numDenRecord.put(question, new ArrayList<Float>(Arrays.asList(num, den)));
        CompositiveScoreRegister.addRecord(question, num, den);
    }

    public void deleteRecord(Question question){
        getNumDenRecord().remove(question);
        CompositiveScoreRegister.remove(question);
    }

    public Map<Question, List<Float>> getNumDenRecord() {
        return numDenRecord;
    }

    public List<Float> calculateTotal(){
        return calculateNumDenTotal(numDenRecord);

    }

    public static List<Float> calculateNumDenTotal(Map<Question,List<Float>> record) {

        Float num = 0.0F;
        Float den = 0.0F;
        for (List<Float> numDen : record.values()) {
            num += numDen.get(0);
            den += numDen.get(1);
        }
        return new ArrayList<Float>(Arrays.asList(num, den));
    }

}
