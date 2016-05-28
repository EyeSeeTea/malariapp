/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.adapters.survey;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Media;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.feedback.CompositeScoreFeedback;
import org.eyeseetea.malariacare.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.database.utils.feedback.QuestionFeedback;
import org.eyeseetea.malariacare.network.CustomParser;
import org.eyeseetea.malariacare.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arrizabalaga on 14/09/15.
 */
public class FeedbackAdapter extends BaseAdapter {

    private List<Feedback> items;

    private Context context;

    private boolean onlyFailed;

    private boolean[] hiddenPositions;

    public FeedbackAdapter(Context context) {
        this(new ArrayList<Feedback>(), context);
    }

    public FeedbackAdapter(List<Feedback> items, Context context) {
        this.items = items;
        this.context = context;
        this.onlyFailed = true;
        this.hiddenPositions = new boolean[this.items.size()];
    }

    @Override
    public int getCount() {
        int hiddenItems = this.onlyFailed ? countHiddenUpTo(this.items.size()) : 0;
        return this.items.size() - hiddenItems;
    }

    @Override
    public Object getItem(int position) {
        //Show all -> direct
        if (!onlyFailed) {
            return this.items.get(position);
        }

        //Find the visible item number 'position'
        int visibleItems = 0;
        int i;
        for (i = 0; i < this.hiddenPositions.length; i++) {
            //Hidden, move on
            if (this.hiddenPositions[i]) {
                continue;
            }

            //Visible, count it and check
            visibleItems++;
            if (visibleItems == position + 1) {
                break;
            }
        }

        return this.items.get(i);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Feedback feedback = (Feedback) getItem(position);
        if (feedback instanceof CompositeScoreFeedback) {
            return getViewByCompositeScoreFeedback((CompositeScoreFeedback) feedback, convertView, parent);
        } else {
            return getViewByQuestionFeedback((QuestionFeedback) feedback, convertView, parent);
        }
    }

    private View getViewByCompositeScoreFeedback(CompositeScoreFeedback feedback, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout) inflater.inflate(R.layout.feedback_composite_score_row, parent, false);
        rowLayout.setBackgroundResource(feedback.getBackgroundColor());

        //CompositeScore title
        TextView textView = (TextView) rowLayout.findViewById(R.id.feedback_label);
        String pattern = "^[0-9]+[.][0-9]+.*"; // the format "1.1" for the second level header
        if (!PreferencesState.getInstance().isVerticalDashboard())
            if (feedback.getLabel().matches(pattern)) {
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkGrey));
                //Calculate the size of the second header, with the pixels size between question label and header label.
                LinearLayout questionLayout = (LinearLayout) inflater.inflate(R.layout.feedback_question_row, parent, false);
                TextView questionTextView = (TextView) questionLayout.findViewById(R.id.feedback_question_label);
                float size = (textView.getTextSize() + questionTextView.getTextSize()) / 2;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            }
        textView.setText(feedback.getLabel());

        //CompositeScore title
        textView = (TextView) rowLayout.findViewById(R.id.feedback_score_label);
        if (!PreferencesState.getInstance().isVerticalDashboard()) {
            if (feedback.getScore() < Constants.MAX_AMBER)
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.amber));
            else if (feedback.getScore() < Constants.MAX_RED)
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkRed));
            else
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.lightGreen));
        }
        textView.setText(feedback.getPercentageAsString());

        return rowLayout;
    }

    private View getViewByQuestionFeedback(QuestionFeedback feedback, View convertView, ViewGroup parent) {
        if (onlyFailed && feedback.isPassed()) {
            return null;
        }

        int layoutId = findLayoutByMedia(feedback);
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout) inflater.inflate(layoutId, parent, false);

        rowLayout.setTag(feedback);

        //Question label
        TextView textView = (TextView) rowLayout.findViewById(R.id.feedback_question_label);
        if (!PreferencesState.getInstance().isVerticalDashboard()) {
            textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkGrey));
        }
        if (feedback.isLabel()) {
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        textView.setText(feedback.getLabel());

        //Option label
        textView = (TextView) rowLayout.findViewById(R.id.feedback_option_label);
        if (!PreferencesState.getInstance().isVerticalDashboard())
            textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkGrey));
        textView.setText(feedback.getOption());

        //Score label
        textView = (TextView) rowLayout.findViewById(R.id.feedback_score_label);
        if (feedback.hasGrade()) {
            textView.setText(context.getString(feedback.getGrade()));
            textView.setTextColor(context.getResources().getColor(feedback.getColor()));
        }

        //Feedback
        textView = (TextView) rowLayout.findViewById(R.id.feedback_feedback_html);
        String feedbackText = feedback.getFeedback();
        if (feedbackText == null) {
            feedbackText = context.getString(R.string.feedback_info_no_feedback);
        }
        textView.setText(Html.fromHtml(feedbackText, new CustomParser(textView, this.context), new CustomParser(textView, this.context)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        //Hide/Show feedback according to its inner state
        toggleFeedback(rowLayout, feedback.isFeedbackShown());

        //Add listener to toggle feedback state
        rowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionFeedback questionFeedback = (QuestionFeedback) v.getTag();
                if (questionFeedback == null || questionFeedback.isLabel() || questionFeedback.getFeedback() == null) {
                    return;
                }
                toggleFeedback((LinearLayout) v, questionFeedback.toggleFeedbackShown());
            }
        });

        //media stuff
        if(layoutId!=R.layout.feedback_question_row){
            addMedia(rowLayout,feedback);
        }

        return rowLayout;
    }

    private void addMedia(LinearLayout rowLayout, QuestionFeedback feedback) {
        Media media=feedback.getMedia();

        File file=new File(media.getFilename());
        Uri uri = Uri.fromFile(file);
        //add video
        if(media.getMediaType()==Media.MEDIA_TYPE_VIDEO){
            MediaController mediaController=new MediaController(DashboardActivity.dashboardActivity);
            VideoView videoView=((VideoView)rowLayout.findViewById(R.id.feedback_media));
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            return;
        }

        //add image
        ((ImageView)rowLayout.findViewById(R.id.feedback_media)).setImageURI(uri);
    }

    private int findLayoutByMedia(QuestionFeedback feedback) {

        Media media = feedback.getMedia();

        //NO media
        if (media.isEmpty()) {
            return R.layout.feedback_question_row;
        }

        //image
        if (media.getMediaType() == Media.MEDIA_TYPE_IMAGE) {
            return R.layout.feedback_image_question_row;
        }

        //video
        return R.layout.feedback_video_question_row;
    }

    private void toggleFeedback(LinearLayout rowLayout, boolean visible) {
        View separator = rowLayout.findViewById(R.id.feedback_container);
        separator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Reloads items into the adapter
     *
     * @param newItems
     */
    public void setItems(List<Feedback> newItems) {
        this.items.clear();
        this.items.addAll(newItems);

        //init 'hiddenPositions'
        reloadHiddenPositions();
        notifyDataSetChanged();
    }

    /**
     * Toggles the state of the flag that determines if only 'failed' questions are shown
     */
    public void toggleOnlyFailed() {
        this.onlyFailed = !this.onlyFailed;
        notifyDataSetChanged();
    }

    public boolean isOnlyFailed() {
        return onlyFailed;
    }

    /**
     * Recalculates the array of hidden positions
     */
    private void reloadHiddenPositions() {
        //a brand new array
        this.hiddenPositions = new boolean[this.items.size()];

        for (int i = 0; i < this.hiddenPositions.length; i++) {
            //Passed items might get hidden
            this.hiddenPositions[i] = this.items.get(i).isPassed();
        }
    }

    /**
     * Counts the number of hidden items up to the given position or the whole array if the given position is greater.
     *
     * @param position Upper index to check (included)
     * @return
     */
    private int countHiddenUpTo(int position) {
        int iMax = (position < hiddenPositions.length - 1) ? position : (this.hiddenPositions.length - 1);
        int numHidden = 0;
        for (int i = 0; i <= iMax; i++) {
            numHidden += hiddenPositions[i] ? 1 : 0;
        }
        return numHidden;
    }

}
