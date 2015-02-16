package org.eyeseetea.malariacare.data;

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

    public Float getReal() {
        return real;
    }

    public void setReal(Float real) {
        this.real = real;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id='" + id + '\'' +
                "real=" + real +
                ", tab=" + tab +
                '}';
    }
}
