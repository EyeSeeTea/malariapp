package org.eyeseetea.malariacare.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.models.DataHolder;

import java.util.List;

/**
 * Created by Jose on 28/02/2015.
 */
public class IQATestArrayAdapter extends ArrayAdapter<DataHolder> {

    final Activity context;
    final List<DataHolder> list;

    public IQATestArrayAdapter(Activity context, List<DataHolder> list)
    {
        super(context, R.layout.iqa_results, list);
        this.context=context;
        this.list=list;
    }

    static class ViewHolder {
        protected Spinner spinner;
        protected EditText answer1;
        protected EditText answer2;
        protected DataHolder data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view=null;

        // Check to see if this row has already been painted once.
        if (convertView == null) {

            // If it hasn't, set up everything:
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.iqa_results, null);

            // Make a new ViewHolder for this row, and modify its data and spinner:
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.answer1 = (EditText) view.findViewById(R.id.answer1);
            viewHolder.answer2 = (EditText) view.findViewById(R.id.answer2);
            //viewHolder.data = new DataHolder(myContext);
            viewHolder.spinner = (Spinner) view.findViewById(R.id.spinner);
            viewHolder.spinner.setAdapter(viewHolder.data.getAdapter());

            // Used to handle events when the user changes the Spinner selection:
            viewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });

            // Update the TextView to reflect what's in the Spinner
            //viewHolder.text.setText(viewHolder.data.getText());

            view.setTag(viewHolder);

            //Log.d("DBGINF", viewHolder.text.getText() + "");
        } else {
            view = convertView;
        }

        // This is what gets called every time the ListView refreshes
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.answer1.setText(getItem(position).getText());
        holder.spinner.setSelection(getItem(position).getSelected());

        return view;
    }
}
