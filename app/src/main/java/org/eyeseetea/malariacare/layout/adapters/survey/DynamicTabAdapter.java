/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.adapters.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.SurveyActivity;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.survey.progress.ProgressTabStatus;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jose on 21/04/2015.
 */
public class DynamicTabAdapter  extends ATabAdapter {

    private final static String TAG=".DynamicTabAdapter";

    /**
     * Hold the progress of completion
     */
    public ProgressTabStatus progressTabStatus;

    /**
     * Flag that indicates if the swipe listener has been already added to the listview container
     */
    private boolean isSwipeAdded;

    /**
     * Listener that detects taps on buttons & swipe
     */
    private OnSwipeTouchListener swipeTouchListener;

    //List of Headers and Questions. Each position contains an object to be showed in the listview
    Tab tab;



    /**
     * Flag that indicates if the current survey in session is already sent or not (it affects readonly settings)
     */
    private boolean readOnly;

    public DynamicTabAdapter(Tab tab, Context context) {
        super(tab,context,R.layout.form_without_score_pictureapp);


        super.setItems(initItems(tab));
        List<Question> questions= initHeaderAndQuestions();
        this.progressTabStatus=initProgress(questions);
        this.readOnly = !Session.getSurvey().isInProgress();
        this.isSwipeAdded=false;
    }

    /**
     * Turns a tab into an ordered list of headers+questions
     * @param tab
     */
    private List<? extends BaseModel> initItems(Tab tab){
        this.tab=tab;
        return Utils.convertTabToArrayCustom(tab);
    }

    /**
     * Initializes the clean list of questions (without headers)
     */
    private List<Question> initHeaderAndQuestions() {
        List<Question> questions=new ArrayList<Question>();

        for(int i=1;i<getItems().size();i++){
            questions.add((Question)getItems().get(i));
        }

        return questions;
    }

    /**
     * Builds a progress status based on the current list of questions
     * @param questions
     * @return
     */
    private ProgressTabStatus initProgress(List<Question> questions){
        return new ProgressTabStatus(questions);
    }

    public void addOnSwipeListener(final ListView listView){
        if(isSwipeAdded){
            return;
        }

        swipeTouchListener=new OnSwipeTouchListener(getContext()) {
            /**
             * Click listener for image option
             * @param view
             */
            public void onClick(View view) {
                Log.d(TAG, "onClick");
                Option selectedOption=(Option)view.getTag();
                Question question=progressTabStatus.getCurrentQuestion();
                ReadWriteDB.saveValuesDDL(question, selectedOption);

                ViewGroup vgTable = (ViewGroup) view.getParent().getParent();
                for (int rowPos = 0; rowPos < vgTable.getChildCount(); rowPos++) {
                    ViewGroup vgRow = (ViewGroup) vgTable.getChildAt(rowPos);
                    for (int itemPos = 0; itemPos < vgRow.getChildCount(); itemPos++) {
                        View childItem = vgRow.getChildAt(itemPos);
                        if (childItem instanceof ImageView) {
                            Option otherOption=(Option)childItem.getTag();
                            if(selectedOption.getId_option() != otherOption.getId_option()){
                                overshadow((ImageView) childItem, otherOption);
                            }
                        }
                    }
                }

                highlightSelection(view, selectedOption);
                finishOrNext();
            }

            /**
             * Swipe right listener moves to previous question
             */
            public void onSwipeRight() {
                Log.d(TAG, "onSwipeRight(previous)");

                //Hide keypad
                hideKeyboard(listView.getContext(), listView);

                previous();
            }

            /**
             * Swipe left listener moves to next question
             */
            public void onSwipeLeft() {
                Log.d(TAG,"onSwipeLeft(next)");
                if(readOnly || progressTabStatus.isNextAllowed()) {

                    //Hide keypad
                    hideKeyboard(listView.getContext(), listView);

                    next();
                }
            }
        };

        listView.setOnTouchListener(swipeTouchListener);
    }


    public Tab getTab() {
        return this.tab;
    }

    /**
     * No scores required
     */
    @Override
    public void initializeSubscore() {
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return this.progressTabStatus.getCurrentQuestion();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflate the layout
        View rowView = getInflater().inflate(R.layout.dynamic_tab_grid_question, parent, false);
        rowView.getLayoutParams().height=parent.getHeight();
        rowView.requestLayout();

        Question question=this.progressTabStatus.getCurrentQuestion();
        Value value=question.getValueBySession();

        //Question
        CustomTextView headerView=(CustomTextView) rowView.findViewById(R.id.question);
        //Load a font which support Khmer character
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + "KhmerOS.ttf");
        headerView.setTypeface(tf);
        headerView.setText(question.getForm_name());

        //Progress
        ProgressBar progressView=(ProgressBar)rowView.findViewById(R.id.dynamic_progress);
        progressView.setMax(progressTabStatus.getTotalPages());
        progressView.setProgress(progressTabStatus.getCurrentPage()+1);
        TextView progressText=(TextView)rowView.findViewById(R.id.dynamic_progress_text);
        progressText.setText(getLocaleProgressStatus(progressView.getProgress(), progressView.getMax()));

        //Options
        TableLayout tableLayout=(TableLayout)rowView.findViewById(R.id.options_table);

        TableRow tableRow=null;
        int typeQuestion=question.getOutput();
        switch (typeQuestion){
            case Constants.IMAGES_2:
            case Constants.IMAGES_4:
            case Constants.IMAGES_6:
                List<Option> options = question.getAnswer().getOptions();
                swipeTouchListener.clearClickableViews();
                for(int i=0;i<options.size();i++){
                    Option currentOption = options.get(i);
                    int mod=i%2;
                    //First item per row requires a new row
                    if(mod==0){
                        tableRow=(TableRow)getInflater().inflate(R.layout.dynamic_tab_row,tableLayout,false);
                        tableLayout.addView(tableRow);
                    }
                    ImageView imageButton = (ImageView) tableRow.getChildAt(mod);
                    String backGColor = currentOption.getOptionAttribute() != null ? currentOption.getOptionAttribute().getBackground_colour() : currentOption.getBackground_colour();
                    imageButton.setBackgroundColor(Color.parseColor("#" + backGColor));

                    initOptionButton(imageButton, currentOption, value, parent);
                }
                break;
            case Constants.IMAGES_3:
                List<Option> opts = question.getAnswer().getOptions();
                swipeTouchListener.clearClickableViews();
                for(int i=0;i<opts.size();i++){

                    tableRow=(TableRow)getInflater().inflate(R.layout.dynamic_tab_row_singleitem,tableLayout,false);
                    tableLayout.addView(tableRow);

                    ImageView imageButton = (ImageView) tableRow.getChildAt(0);

                    Option currentOption = opts.get(i);

                    String backGColor = currentOption.getOptionAttribute() != null ? currentOption.getOptionAttribute().getBackground_colour() : currentOption.getBackground_colour();
                    imageButton.setBackgroundColor(Color.parseColor("#" + backGColor));

                    initOptionButton(imageButton, currentOption, value, parent);
                }
                break;
            case Constants.PHONE:
                swipeTouchListener.clearClickableViews();
                tableRow=(TableRow)getInflater().inflate(R.layout.dynamic_tab_phone_row, tableLayout, false);
                tableLayout.addView(tableRow);
                initPhoneValue(tableRow, value);
                break;
            case Constants.POSITIVE_INT:
                swipeTouchListener.clearClickableViews();
                tableRow=(TableRow)getInflater().inflate(R.layout.dynamic_tab_positiveint_row, tableLayout, false);
                tableLayout.addView(tableRow);
                initPositiveIntValue(tableRow, value);
                break;
        }
        rowView.requestLayout();
        return rowView;
    }

    /**
     * Get status progress in locale strings
     * @param currentPage
     * @param totalPages
     */
    private String getLocaleProgressStatus(int currentPage, int totalPages){

        String current = getContext().getResources().getString(getContext().getResources().getIdentifier("number_"+currentPage, "string", getContext().getPackageName()));
        String total = getContext().getResources().getString(getContext().getResources().getIdentifier("number_"+totalPages, "string", getContext().getPackageName()));
        return current.concat("/").concat(total);
    }


    private void showKeyboard(Context c, View v){
        InputMethodManager keyboard = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(v, 0);

    }

    private void hideKeyboard(Context c, View v) {
        InputMethodManager keyboard = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    /**
     * Inits NumberPicker and button to view/edit a integer between 0 and Constants.MAX_INT_AGE
     * @param tableRow
     * @param value
     */
    private void initPositiveIntValue(TableRow tableRow, Value value){
        Button button=(Button)tableRow.findViewById(R.id.dynamic_positiveInt_btn);
        final NumberPicker numberPicker=(NumberPicker)tableRow.findViewById(R.id.dynamic_positiveInt_edit);

        //Without setMinValue, setMaxValue, setValue in this order, the setValue is not displayed in the screen.
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(Constants.MAX_INT_AGE);

        //Has value? show it
        if(value!=null){
            numberPicker.setValue(Integer.parseInt(value.getValue()));
        }

        if (!readOnly) {
            //Save the numberpicker value in the DB, and continue to the next screen.
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String positiveIntValue = String.valueOf(numberPicker.getValue());
                    Question question = progressTabStatus.getCurrentQuestion();
                    ReadWriteDB.saveValuesText(question, positiveIntValue);
                    finishOrNext();
                }
            });

        }else{
            numberPicker.setEnabled(false);
            button.setEnabled(false);
        }

        //Add button to listener
        swipeTouchListener.addClickableView(button);

        //Take focus to this view
        numberPicker.requestFocus();
    }

    /**
     * Inits editText and button to view/edit the phone number
     * @param tableRow
     * @param value
     */
    private void initPhoneValue(TableRow tableRow, Value value){
        Button button=(Button)tableRow.findViewById(R.id.dynamic_phone_btn);
        final EditText editText=(EditText)tableRow.findViewById(R.id.dynamic_phone_edit);
        final Context ctx = tableRow.getContext();

        //Has value? show it
        if(value!=null){
            editText.setText(value.getValue());
        }

        //Editable? add listener
        if(!readOnly){

            editText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentView = (View) v.getParent();
                    EditText editText = (EditText) parentView.findViewById(R.id.dynamic_phone_edit);
                    String phoneValue = editText.getText().toString();

                    //Optional, empty values allowed
                    if (phoneValue == null || "".equals(phoneValue)) {
                        phoneValue = "";
                    }else {
                        // Check phone number format
                        Phonenumber.PhoneNumber phoneNumber = null;
                        try {
                            Locale locale = getContext().getResources().getConfiguration().locale;
                            phoneNumber = PhoneNumberUtil.getInstance().parse(phoneValue, locale.getCountry());
                        } catch (NumberParseException e) {
                            editText.setError(getContext().getString(R.string.dynamic_error_phone_format));
                            return;
                        }
                        if(!PhoneNumberUtil.getInstance().isValidNumber(phoneNumber)){
                            editText.setError(getContext().getString(R.string.dynamic_error_phone_format));
                            return;
                        }
                    }

                    //Hide keypad
                    hideKeyboard(ctx, v);

                    Question question = progressTabStatus.getCurrentQuestion();
                    ReadWriteDB.saveValuesText(question, phoneValue);
                    finishOrNext();
                }
            });
        }else{
            editText.setEnabled(false);
            button.setEnabled(false);
        }

        //Add button to listener
        swipeTouchListener.addClickableView(button);

        //Take focus to this view
        editText.requestFocus();
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Show keypad
                showKeyboard(ctx, editText);
            }
        }, 300);
    }

    /**
     * Attach an option with its button in view, adding the listener
     * @param button
     * @param option
     */
    private void initOptionButton(ImageView button, Option option, Value value, ViewGroup parent){

        // value = null --> first time calling initOptionButton
        //Highlight button
        if (value != null && value.getValue().equals(option.getName())) {
            highlightSelection(button, option);
        } else if (value != null) {
            overshadow(button, option);
        }

        //Put image
        try {
            InputStream inputStream = getContext().getAssets().open(option.getPath());
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            button.setImageBitmap(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Associate option
        button.setTag(option);

        //Readonly (not clickable, enabled)
        if(readOnly){
            button.setEnabled(false);
            return;
        }

        //Add button to listener
        swipeTouchListener.addClickableView(button);

    }

    /**
     * @param view
     * @param option
     */
    private void highlightSelection(View view, Option option){
        Drawable selectedBackground = getContext().getResources().getDrawable(R.drawable.background_dynamic_clicked_option);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {    //JELLY_BEAN=API16
            view.setBackground(selectedBackground);
        } else {
            view.setBackgroundDrawable(selectedBackground);
        }

        GradientDrawable bgShape = (GradientDrawable)view.getBackground();
        String backGColor = option.getOptionAttribute() != null ? option.getOptionAttribute().getBackground_colour() : option.getBackground_colour();
        bgShape.setColor(Color.parseColor("#" + backGColor));

        bgShape.setStroke(3, Color.WHITE);

        ImageView v = (ImageView) view;
        v.clearColorFilter();
    }

    /**
     * @param view
     */
    private void overshadow(ImageView view, Option option){

        //FIXME: (API17) setColorFilter for view.getBackground() has no effect...
        view.getBackground().setColorFilter(Color.parseColor("#805a595b"), PorterDuff.Mode.SRC_ATOP);
        view.setColorFilter(Color.parseColor("#805a595b"));

        Drawable bg = view.getBackground();
        if(bg instanceof GradientDrawable) {
            GradientDrawable bgShape = (GradientDrawable)bg;
            bgShape.setStroke(0, 0);
        }
    }

    /**
     * Advance to the next question with delay applied or finish survey according to question and value.
     */
    private void finishOrNext(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Question question = progressTabStatus.getCurrentQuestion();
                Value value = question.getValueBySession();
                if (isDone(value)) {
                    showDone();
                    return;
                }
                next();
            }
        }, 1000);
    }

    /**
     * Show a final dialog to announce the survey is over
     */
    private void showDone(){
        final Activity activity=(Activity)getContext();
        AlertDialog.Builder msgConfirmation = new AlertDialog.Builder((activity))
                .setTitle(R.string.survey_title_completed)
                .setMessage(R.string.survey_info_completed)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        ((SurveyActivity) activity).finishAndGo(DashboardActivity.class);
                    }
                });
        if(!progressTabStatus.isFirstQuestion()){
            msgConfirmation.setNegativeButton(R.string.review, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    review();
                }
            });
        }

        msgConfirmation.create().show();
    }

    /**
     * Checks if there are more questions to answer according to the given value + current status.
     * @param value
     * @return
     */
    private boolean isDone(Value value){
        //First question + NO => true
        if(progressTabStatus.isFirstQuestion() && !value.isAPositive()){
            return true;
        }

        //No more questions => true
        return !progressTabStatus.hasNextQuestion();
    }

    /**
     * Changes the current question moving forward
     */
    private void next(){
        if(!progressTabStatus.hasNextQuestion()){
            return;
        }

        progressTabStatus.getNextQuestion();
        notifyDataSetChanged();
    }

    /**
     * Changes the current question moving backward
     */
    private void previous(){
        if(!progressTabStatus.hasPreviousQuestion()){
            return;
        }

        progressTabStatus.getPreviousQuestion();
        notifyDataSetChanged();
    }

    /**
     * Back to initial question to review questions
     */
    private void review(){

        progressTabStatus.getFirstQuestion();
        notifyDataSetChanged();
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        /**
         * Custom gesture detector
         */
        private final GestureDetector gestureDetector;

        /**
         * List of clickable items inside the swipable view (buttons)
         */
        private final List<View> clickableViews;

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
            clickableViews =new ArrayList<>();
        }

        @Override
        /**
         * Delegates any touch into the our custom gesture detector
         */
        public boolean onTouch(View v, MotionEvent event) {
//            Log.d(TAG, "onTouch: " + v.toString() + "\t (" + this.toString() + ")");
            return gestureDetector.onTouchEvent(event);
        }

        /**
         * Adds a clickable view
         * @param view
         */
        public void addClickableView(View view){
            clickableViews.add(view);
        }

        /**
         * Clears the list of clickable items
         */
        public void clearClickableViews(){
            clickableViews.clear();
        }

        /**
         * Calculates de clickable view that has been 'clicked' in the given event
         * @param event
         * @return Returns de touched view or null otherwise
         */
        public View findViewByCoords(MotionEvent event){
            float x=event.getRawX();
            float y=event.getRawY();
            for(View v: clickableViews){
                Rect visibleRectangle = new Rect();
                v.getGlobalVisibleRect(visibleRectangle);
                //Image/Button clicked
//                Log.d(TAG,String.format("findViewByCoords(%d,%d,%d,%d)",visibleRectangle.left,visibleRectangle.top,visibleRectangle.right, visibleRectangle.bottom));
                if(x>=visibleRectangle.left && x<=visibleRectangle.right && y>=visibleRectangle.top && y<=visibleRectangle.bottom){
                    return v;
                }
            }

            return null;
        }

        public void onClick(View view){
//            Log.e(".DynamicTabAdapter", "empty onclick");
        }

        public void onSwipeRight(){
//            Log.e(TAG, "onSwipeRight(DEFAULT)");
        }

        public void onSwipeLeft(){
//            Log.e(TAG, "onSwipeLeft(DEFAULT)");
        }

        /**
         * Our own custom gesture detector that distinguishes between onFling and a SingleTap
         */
        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 50;
            private static final int SWIPE_VELOCITY_THRESHOLD = 50;

            private float lastX;

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event){
//              Log.d(TAG, String.format("onSingleTapConfirmed: %f %f", event.getX(), event.getY()));

                //Find the clicked button
                View clickedView=findViewByCoords(event);

                //If found
                if(clickedView!=null) {
                    //delegate onClick
                    onClick(clickedView);
                    return true;
                }

                //Not found, not consumed
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                lastX=e.getX();
//                Log.d(TAG, "onDown: "+lastX);
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    float diffX = e2.getX()-((e1==null)?lastX:e1.getX());
//                    Log.d(TAG, String.format("onFling (%f): diffX: %f, velocityX: %f",lastX, diffX, velocityX));
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    return true;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return false;
            }
        }

    }

}