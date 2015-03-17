package org.eyeseetea.malariacare.layout.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by adrian on 17/03/15.
 */
public class GeneralNumDenRecord extends ANumDenRecord{

    public List<Float> calculateTotal(){
        return this.calculateNumDenTotal(new ArrayList<Float>(Arrays.asList(0F, 0F)));
    }
}
