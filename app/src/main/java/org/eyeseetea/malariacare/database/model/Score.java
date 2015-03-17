package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Score extends SugarRecord<Score> {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Score score = (Score) o;

        if (real != null ? !real.equals(score.real) : score.real != null) return false;
        if (tab != null ? !tab.equals(score.tab) : score.tab != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = real != null ? real.hashCode() : 0;
        result = 31 * result + (tab != null ? tab.hashCode() : 0);
        return result;
    }
}
