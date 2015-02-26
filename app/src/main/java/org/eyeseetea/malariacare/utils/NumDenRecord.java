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
    }

    public void deleteRecord(Question question){
        getNumDenRecord().remove(question);
    }

    public Map<Question, List<Float>> getNumDenRecord() {
        return numDenRecord;
    }

    public List<Float> calculateNumDenTotal(){
        Float num = 0.0F;
        Float den = 0.0F;
        for (List<Float> numDen : numDenRecord.values()){
            num += numDen.get(0);
            den += numDen.get(1);
        }
        return new ArrayList<Float>(Arrays.asList(num, den));
    }
}
