package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Score extends SugarRecord<Score> {

    Float value;
    Tab tab;
    String uid;

    public Score() {
    }

    public Score(Float real, Tab tab, String uid) {
        this.value = real;
        this.tab = tab;
        this.uid = uid;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float real) {
        this.value = real;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id='" + id + '\'' +
                "real=" + value +
                ", tab=" + tab +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Score score = (Score) o;

        if (value != null ? !value.equals(score.value) : score.value != null) return false;
        if (tab != null ? !tab.equals(score.tab) : score.tab != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (tab != null ? tab.hashCode() : 0);
        return result;
    }
}
