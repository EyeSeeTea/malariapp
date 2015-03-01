package org.eyeseetea.malariacare.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.models.DataHolder;

import java.util.List;

/**
 * Created by Jose on 28/02/2015.
 */
public class IQATestArrayAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final List<String> list;


    public IQATestArrayAdapter(Activity context, List<String> list)
    {
        super(context, R.layout.iqa_results, list);
        this.context=context;
        this.list=list;
    }

    static class ViewHolder {
        protected TextView question;
        protected Spinner spinner;
        protected EditText answer1;
        protected EditText answer2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.iqa_results, parent, false);

        final ViewHolder viewHolder = new ViewHolder();

        viewHolder.question = (TextView) rowView.findViewById(R.id.question1);
        viewHolder.answer1 = (EditText) rowView.findViewById(R.id.answer11);
        viewHolder.answer2 = (EditText) rowView.findViewById(R.id.answer21);
        viewHolder.spinner = (Spinner) rowView.findViewById(R.id.spinner1);

        viewHolder.question.setText(list.get(position).toString());

        ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.iqa_testresult, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spinner.setAdapter(adapter);

        viewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        return rowView;


    }
}
