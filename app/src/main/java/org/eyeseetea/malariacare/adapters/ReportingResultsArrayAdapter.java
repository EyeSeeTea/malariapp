package org.eyeseetea.malariacare.adapters;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.models.ReportingResults;

import java.util.List;

/**
 * Created by Jose on 28/02/2015.
 */
public class ReportingResultsArrayAdapter extends ArrayAdapter<ReportingResults>{

    private final List<ReportingResults> list;
    private final Activity context;


    public ReportingResultsArrayAdapter(Activity context, List<ReportingResults> list)
    {
        super(context, R.layout.resultsreporting, list);
        this.context=context;
        this.list=list;
    }



    static class ViewHolder {

        protected TextView question;
        protected EditText answer1;
        protected EditText answer2;
        protected TextView score;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.resultsreporting, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.question = (TextView) view.findViewById(R.id.question);
            viewHolder.answer1 = (EditText) view.findViewById(R.id.answer1);
            viewHolder.answer2 = (EditText) view.findViewById(R.id.answer2);
            viewHolder.score = (TextView) view.findViewById(R.id.score);

            viewHolder.answer1.addTextChangedListener(new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    String answer2=viewHolder.answer2.getText().toString();
                    String answer1=s.toString();

                    if (answer2!=null && answer1!=null)
                        if (answer1.equals(answer2)) viewHolder.score.setText("1"); else viewHolder.score.setText("0");
                    else
                        viewHolder.score.setText("");

                }
                @Override
                public void afterTextChanged(Editable s) {

                }


            });

            viewHolder.answer2.addTextChangedListener(new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    String answer1=viewHolder.answer1.getText().toString();
                    String answer2=s.toString();

                    if (answer2!=null && answer1!=null)
                        if (answer1.equals(answer2)) viewHolder.score.setText("1"); else viewHolder.score.setText("0");
                    else
                        viewHolder.score.setText("");


                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            view.setTag(viewHolder);
            //viewHolder.checkbox.setTag(list.get(position));
        } else {
            view = convertView;
            //((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.question.setText(list.get(position).getQuestion());
        holder.answer1.setText(list.get(position).getAnswer1());
        holder.answer2.setText(list.get(position).getAnswer2());
        holder.score.setText(list.get(position).getScore());
        //holder.text.setText(list.get(position).getName());
        //holder.checkbox.setChecked(list.get(position).isSelected());*/
        return view;


    }

}
