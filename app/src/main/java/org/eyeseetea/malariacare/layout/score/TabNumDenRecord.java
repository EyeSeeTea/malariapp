package org.eyeseetea.malariacare.layout.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabNumDenRecord extends ANumDenRecord{

    public List<Float> calculateTotal(){
        return this.calculateNumDenTotal(new ArrayList<Float>(Arrays.asList(0F, 0F)));
    }
}
