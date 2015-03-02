package org.eyeseetea.malariacare.adapters;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.models.ReportingResults;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.List;

/**
 * Created by Jose on 28/02/2015.
 */
public class ReportingResultsArrayAdapter extends ArrayAdapter<String>{

    private final List<String> list;
    private final Activity context;

    private static int score = 0;

    public ReportingResultsArrayAdapter(Activity context, List<String> list)
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


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final int [] backgrounds = {R.drawable.background_even, R.drawable.background_odd};

        View rowView = inflater.inflate(R.layout.resultsreporting, parent, false);

        final ViewHolder viewHolder = new ViewHolder();

        rowView.setBackgroundResource(backgrounds[position % backgrounds.length]);
        viewHolder.question = (TextView) rowView.findViewById(R.id.question);
        viewHolder.answer1 = (EditText) rowView.findViewById(R.id.answer1);
        viewHolder.answer2 = (EditText) rowView.findViewById(R.id.answer2);
        viewHolder.score = (TextView) rowView.findViewById(R.id.score);

        viewHolder.question.setText(list.get(position).toString());
        viewHolder.question.setTag(0);

        viewHolder.answer1.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String answer2=viewHolder.answer2.getText().toString();
                String answer1=s.toString();
                float score_total;

                if (answer2!=null && answer1!=null)
                    if (answer1.equals(answer2)) {
                        viewHolder.score.setText("1");

                        score=score + 1 - (int) viewHolder.question.getTag();

                        viewHolder.question.setTag(1);

                    } else {
                        viewHolder.score.setText("0");

                        score=score - (int) viewHolder.question.getTag();

                        viewHolder.question.setTag(0);

                    }
                else
                    viewHolder.score.setText("");

                TextView scoreView = (TextView) context.findViewById(R.id.reportingScore);
                score_total = (float)score / Constants.REPORTING_MAX_SCORE;
                scoreView.setText(Utils.round(score_total).toString());

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
                float score_total;

                if (answer2!=null && answer1!=null)
                    if (answer1.equals(answer2)){
                        viewHolder.score.setText("1");

                        score=score + 1 - (int) viewHolder.question.getTag();

                        viewHolder.question.setTag(1);

                    } else{
                        viewHolder.score.setText("0");

                        score=score - (int) viewHolder.question.getTag();

                        viewHolder.question.setTag(0);
                    }
                else
                    viewHolder.score.setText("");

                TextView scoreView = (TextView) context.findViewById(R.id.reportingScore);
                score_total = (float)score / Constants.REPORTING_MAX_SCORE;
                scoreView.setText(Utils.round(score_total).toString());


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return rowView;
    }

}
