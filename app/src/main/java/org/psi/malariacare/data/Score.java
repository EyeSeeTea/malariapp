package org.psi.malariacare.data;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Score extends SugarRecord<Tab> {

    Float real;
    Tab tab;

    public Score() {
    }

    public Score(Float real, Tab tab) {
        this.real = real;
        this.tab = tab;
    }
}
