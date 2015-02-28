package org.eyeseetea.malariacare.models;

import android.content.Context;
import android.widget.ArrayAdapter;

import org.eyeseetea.malariacare.R;

/**
 * Created by Jose on 28/02/2015.
 */
public class DataHolder {

    int selected;
    ArrayAdapter<CharSequence> adapter;

    public DataHolder(Context parent) {
        adapter = ArrayAdapter.createFromResource(parent, R.array.iqa_testresult, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    public ArrayAdapter<CharSequence> getAdapter() {
        return adapter;
    }

    public String getText() {
        return (String) adapter.getItem(selected);
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
}
