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
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.VideoActivity;
import org.eyeseetea.malariacare.database.model.Media;
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

    private boolean [] hiddenPositions;

    float idSurvey;

    String module;

    public FeedbackAdapter(Context context, float idSurvey, String module){
        this(new ArrayList<Feedback>(), context, idSurvey, module);
    }

    public FeedbackAdapter(List<Feedback> items, Context context, float idSurvey, String module){
        this.items=items;
        this.context=context;
        this.idSurvey=idSurvey;
        this.module=module;
        this.onlyFailed=true;
        this.hiddenPositions= new boolean[this.items.size()];
    }

    @Override
    public int getCount() {
        int hiddenItems=this.onlyFailed?countHiddenUpTo(this.items.size()):0;
        return this.items.size()-hiddenItems;
    }

    @Override
    public Object getItem(int position) {
        //Show all -> direct
        if(!onlyFailed){
            return this.items.get(position);
        }

        //Find the visible item number 'position'
        int visibleItems=0;
        int i;
        for(i=0;i<this.hiddenPositions.length;i++){
            //Hidden, move on
            if(this.hiddenPositions[i]){
                continue;
            }

            //Visible, count it and check
            visibleItems++;
            if(visibleItems==position+1){
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
        Feedback feedback=(Feedback)getItem(position);
        if (feedback instanceof CompositeScoreFeedback){
            return getViewByCompositeScoreFeedback((CompositeScoreFeedback)feedback, convertView, parent, module);
        }else{
            return getViewByQuestionFeedback((QuestionFeedback) feedback, convertView, parent);
        }
    }

    private View getViewByCompositeScoreFeedback(CompositeScoreFeedback feedback, View convertView, ViewGroup parent, String module){
        LayoutInflater inflater=LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout)inflater.inflate(R.layout.feedback_composite_score_row, parent, false);
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
        textView=(TextView)rowLayout.findViewById(R.id.feedback_score_label);
        if(!PreferencesState.getInstance().isVerticalDashboard()){
            if(feedback.getScore(idSurvey, module)< Constants.MAX_RED)
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkRed));
            else if(feedback.getScore(idSurvey, module)< Constants.MAX_AMBER)
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.amber));
            else
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.lightGreen));
        }
        textView.setText(feedback.getPercentageAsString(idSurvey, module));

        return rowLayout;
    }

    private View getViewByQuestionFeedback(QuestionFeedback feedback, View convertView, ViewGroup parent){
        if(onlyFailed && feedback.isPassed()){
            return null;
        }

        LayoutInflater inflater=LayoutInflater.from(context);
        LinearLayout rowLayout = (LinearLayout)inflater.inflate(R.layout.feedback_question_row, parent, false);
        rowLayout.setTag(feedback);

        //Question label
        TextView textView=(TextView)rowLayout.findViewById(R.id.feedback_question_label);
        if(!PreferencesState.getInstance().isVerticalDashboard()){
            textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkGrey));
        }
        if(feedback.isLabel()){
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        textView.setText(feedback.getLabel());

        //Option label
        textView=(TextView)rowLayout.findViewById(R.id.feedback_option_label);
        if(!PreferencesState.getInstance().isVerticalDashboard())
            textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkGrey));
        textView.setText(feedback.getOption());

        //Score label
        textView=(TextView)rowLayout.findViewById(R.id.feedback_score_label);
        if(feedback.hasGrade()) {
            textView.setText(context.getString(feedback.getGrade()));
            textView.setTextColor(context.getResources().getColor(feedback.getColor()));
        }

        //Feedback
        textView=(TextView)rowLayout.findViewById(R.id.feedback_feedback_html);
        String feedbackText=feedback.getFeedback();
        if(feedbackText==null){
            feedbackText=context.getString(R.string.feedback_info_no_feedback);
        }
        textView.setText( Html.fromHtml(feedbackText, new CustomParser(textView, this.context), new CustomParser(textView, this.context)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        //Hide/Show feedback according to its inner state
        toggleFeedback(rowLayout, feedback.isFeedbackShown());

        //Add listener to toggle feedback state
        rowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionFeedback questionFeedback=(QuestionFeedback)v.getTag();
                if(questionFeedback==null || questionFeedback.isLabel() || questionFeedback.getFeedback()==null){
                    return;
                }
                toggleFeedback((LinearLayout)v, questionFeedback.toggleFeedbackShown());
            }
        });

        //media stuff
        addAllMedia(rowLayout,feedback);


        return rowLayout;
    }

    /**
     * Adds N media items to the 1 feedback
     * @param rowLayout
     * @param feedback
     */
    private void addAllMedia(LinearLayout rowLayout, QuestionFeedback feedback) {
        LinearLayout feedbackContainer = (LinearLayout)rowLayout.findViewById(R.id.feedback_container);
        List<Media> mediaList = feedback.getMedia();
        for(Media media:mediaList){
            if(media.getMediaType()==Constants.MEDIA_TYPE_IMAGE){
                addImage(feedbackContainer,media);
            }else{
                addVideo(feedbackContainer,media);
            }
        }
    }


    /**
     * Adds a image media to the feedback
     * @param rowLayout
     * @param media
     */
    private void addImage(LinearLayout rowLayout, Media media) {
        if(media !=null && media.getFilename()==null){
            rowLayout.addView(setDrawableOnLayout(rowLayout, R.drawable.no_image));
        }
        else {
            if(media == null || media.getFilename() == null || media.getFilename().isEmpty()){
                return;
            }
            //Get image uri
            File file = new File(media.getFilename());
            Uri uri = Uri.fromFile(file);

            //Inflate media row
            LayoutInflater inflater = LayoutInflater.from(context);
            RelativeLayout mediaLayout = (RelativeLayout) inflater.inflate(R.layout.feedback_image_row, rowLayout, false);
            ((ImageView) mediaLayout.findViewById(R.id.feedback_media_preview)).setImageURI(uri);

            //Add media row to feedback layout
            rowLayout.addView(mediaLayout);
        }
    }

    /**
     * Adds a video media to the feedback
     * @param rowLayout
     * @param media
     */
    private void addVideo(LinearLayout rowLayout, Media media){
        if(media !=null && media.getFilename()==null){
            rowLayout.addView(setDrawableOnLayout(rowLayout, R.drawable.no_video));
        }
        else {
            if (media == null || media.getFilename() == null || media.getFilename().isEmpty()) {
                return;
            }

            //Inflate media row
            LayoutInflater inflater = LayoutInflater.from(context);
            RelativeLayout mediaLayout = (RelativeLayout) inflater.inflate(R.layout.feedback_video_row, rowLayout, false);
            //add video link
            mediaLayout.setTag(media.getFilename());
            mediaLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mediaLink = (String) v.getTag();
                    Intent videoIntent = new Intent(DashboardActivity.dashboardActivity, VideoActivity.class);
                    videoIntent.putExtra(VideoActivity.VIDEO_PATH_PARAM, mediaLink);
                    DashboardActivity.dashboardActivity.startActivity(videoIntent);
                }
            });

            //add preview frame
            addPreview((ImageView) mediaLayout.findViewById(R.id.feedback_media_preview), media);

            //Add media row to feedback layout
            rowLayout.addView(mediaLayout);
        }
    }


    private RelativeLayout setDrawableOnLayout(LinearLayout rowLayout, int drawableId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout mediaLayout = (RelativeLayout) inflater.inflate(R.layout.feedback_image_row, rowLayout, false);
        Drawable drawable= ResourcesCompat.getDrawable(PreferencesState.getInstance().getContext().getResources(), drawableId, null);
        ((ImageView) mediaLayout.findViewById(R.id.feedback_media_preview)).setImageDrawable(drawable);
        //Add media row to feedback layout
        return mediaLayout;
    }

    private void addPreview(ImageView viewMediaLink, Media media) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        File mediaFile=new File(media.getFilename());
        try {
            retriever.setDataSource(mediaFile.getAbsolutePath());
            viewMediaLink.setImageBitmap(retriever.getFrameAtTime(10000000,MediaMetadataRetriever.OPTION_CLOSEST));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
    }

    private void toggleFeedback(LinearLayout rowLayout, boolean visible) {
        View separator = rowLayout.findViewById(R.id.feedback_container);
        separator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Reloads items into the adapter
     * @param newItems
     */
    public void setItems(List<Feedback> newItems){
        this.items.clear();
        this.items.addAll(newItems);

        //init 'hiddenPositions'
        reloadHiddenPositions();
        notifyDataSetChanged();
    }

    /**
     * Toggles the state of the flag that determines if only 'failed' questions are shown
     */
    public void toggleOnlyFailed(){
        this.onlyFailed=!this.onlyFailed;
        notifyDataSetChanged();
    }

    public boolean isOnlyFailed() {
        return onlyFailed;
    }

    /**
     * Recalculates the array of hidden positions
     */
    private void reloadHiddenPositions(){
        //a brand new array
        this.hiddenPositions= new boolean[this.items.size()];

        for(int i=0;i<this.hiddenPositions.length;i++){
            //Passed items might get hidden
            this.hiddenPositions[i]=this.items.get(i).isPassed();
        }
    }

    /**
     * Counts the number of hidden items up to the given position or the whole array if the given position is greater.
     * @param position Upper index to check (included)
     * @return
     */
    private int countHiddenUpTo(int position){
        int iMax=(position<hiddenPositions.length-1)?position:(this.hiddenPositions.length-1);
        int numHidden=0;
        for(int i=0;i<=iMax;i++){
            numHidden+=hiddenPositions[i]?1:0;
        }
        return numHidden;
    }

}
