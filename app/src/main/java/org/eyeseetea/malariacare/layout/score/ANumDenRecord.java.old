package org.eyeseetea.malariacare.layout.score;

import org.eyeseetea.malariacare.database.model.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ANumDenRecord {

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

    public List<Float> calculateNumDenTotal(List<Float> numDenTotal){
        Float num = numDenTotal.get(0);
        Float den = numDenTotal.get(1);
        for (List<Float> numDen : getNumDenRecord().values()) {
            num += numDen.get(0);
            den += numDen.get(1);
        }
        return new ArrayList<Float>(Arrays.asList(num, den));

    }

}
