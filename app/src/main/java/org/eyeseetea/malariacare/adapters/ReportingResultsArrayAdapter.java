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
public class ReportingResultsArrayAdapter extends ArrayAdapter<ReportingResults>{

    private final List<ReportingResults> list;
    private final Activity context;

    private static int score = 0;

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

            LayoutInflater inflater = (LayoutInflater) context.getLayoutInflater();
            final int[] backgrounds = {R.drawable.background_even, R.drawable.background_odd};
            view = inflater.inflate(R.layout.resultsreporting, null);
            final ViewHolder viewHolder = new ViewHolder();

            view.setBackgroundResource(backgrounds[position % backgrounds.length]);
            viewHolder.question = (TextView) view.findViewById(R.id.question);
            viewHolder.answer1 = (EditText) view.findViewById(R.id.answer1);
            viewHolder.answer2 = (EditText) view.findViewById(R.id.answer2);
            viewHolder.score = (TextView) view.findViewById(R.id.score);

            viewHolder.answer1.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ReportingResults model = (ReportingResults) viewHolder.score.getTag();
                    model.setAnswer1(s.toString());
                    String answer2 = viewHolder.answer2.getText().toString();
                    String answer1 = s.toString();
                    updateScore(model, answer2, answer1, viewHolder);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            viewHolder.answer2.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ReportingResults model = (ReportingResults) viewHolder.score.getTag();
                    model.setAnswer2(s.toString());
                    String answer1 = viewHolder.answer1.getText().toString();
                    String answer2 = s.toString();
                    updateScore(model, answer2, answer1, viewHolder);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            view.setTag(viewHolder);
            viewHolder.score.setTag(list.get(position));

        }

        else {

            view=convertView;
            ((ViewHolder) view.getTag()).score.setTag(list.get(position));

        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.question.setText(list.get(position).getQuestion());
        holder.answer1.setText(list.get(position).getAnswer1());
        holder.answer2.setText(list.get(position).getAnswer2());
        holder.score.setText(list.get(position).getScore());


        return view;
    }

    private void updateScore(ReportingResults model, String answer2, String answer1, ViewHolder viewHolder) {
        float score_total;

        if (!answer2.equals("") || !answer1.equals(""))
            if (answer1.equals(answer2)) {
                viewHolder.score.setText("1");
                score = score + 1 - Integer.parseInt(model.getScore());
                model.setScore("1");
            } else {
                viewHolder.score.setText("0");
                score = score - Integer.parseInt(model.getScore());
                model.setScore("0");
            }
        else
        {
            viewHolder.score.setText("0");
        }

        TextView scoreView = (TextView) context.findViewById(R.id.reportingScore);
        score_total = ((float) score / Constants.REPORTING_MAX_SCORE)*100.0F;
        scoreView.setText(Utils.round(score_total));
    }

}
