package org.eyeseetea.malariacare.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;

import java.util.List;

/**
 * Created by Jose on 01/03/2015.
 */
public class AdherenceAdapter extends ArrayAdapter<String> {

    private final List<String> list;
    private final Activity context;

    public AdherenceAdapter(Activity context, List<String> list) {
        super(context, R.layout.resultsreporting, list);
        this.context=context;
        this.list=list;
    }

    static class ViewHolder {
        protected TextView question;
        protected EditText patienid;
        protected Spinner gender;
        protected EditText age;
        protected Spinner results;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.adherenceresults, parent, false);

        final ViewHolder viewHolder = new ViewHolder();

        viewHolder.question = (TextView) rowView.findViewById(R.id.question);
        viewHolder.patienid = (EditText) rowView.findViewById(R.id.patientid);
        viewHolder.gender = (Spinner) rowView.findViewById(R.id.gender);
        viewHolder.age = (EditText) rowView.findViewById(R.id.age);
        viewHolder.results = (Spinner) rowView.findViewById(R.id.results);

        viewHolder.question.setText(list.get(position).toString());

        ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.gender.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(context, R.array.adherence_testresults, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.results.setAdapter(adapter);


        return rowView;



    }
}
