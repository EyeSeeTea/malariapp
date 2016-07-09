/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.adapters.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.survey.navigation.NavigationBuilder;
import org.eyeseetea.malariacare.layout.adapters.survey.navigation.NavigationController;
import org.eyeseetea.malariacare.layout.adapters.survey.progress.ProgressTabStatus;
import org.eyeseetea.malariacare.layout.dashboard.controllers.AssessModuleController;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.filters.MinMaxInputFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jose on 21/04/2015.
 */
public class DynamicTabAdapter extends BaseAdapter implements ITabAdapter {

    private final static String TAG=".DynamicTabAdapter";

    /**
     * Formatted telephone mask: 0NN NNN NNN{N}
     */
    public static final String FORMATTED_PHONENUMBER_MASK = "0\\d{2} \\d{3} \\d{3,4}";

    /**
     * Formatted telephone mask: 0NN NNN NNN{N}
     */
    public static final String PLAIN_PHONENUMBER_MASK = "0\\d{8,9}";

    public NavigationController navigationController;
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
    List<Object> items;
    Tab tab;

    LayoutInflater lInflater;

    private final Context context;

    private String module;

    int id_layout;


    /**
     * Flag that indicates if the current survey in session is already sent or not (it affects readonly settings)
     */
    private boolean readOnly;

    /**
     * View needed to close the keyboard in methods with view
     */
    View keyboardView;

    public DynamicTabAdapter(Tab tab, Context context, float idSurvey, String module) {
        this.lInflater = LayoutInflater.from(context);
        this.context = context;
        this.id_layout = R.layout.form_without_score;
        this.module = module;

        this.navigationController = initNavigationController(tab, module);
        this.readOnly = Session.getSurveyByModule(module) != null && !Session.getSurveyByModule(module).isInProgress();
        this.isSwipeAdded=false;
        int totalPages=navigationController.getCurrentQuestion().getTotalQuestions();
        if(readOnly){
            if(Session.getSurveyByModule(module)!=null){
                Question lastQuestion=Session.getSurveyByModule(module).findLastSavedQuestion();
                if(lastQuestion!=null){
                    totalPages=lastQuestion.getTotalQuestions();
                }
            }
        }
        navigationController.setTotalPages(totalPages);
    }

    private NavigationController initNavigationController(Tab tab, String module) {
        NavigationController navigationController = NavigationBuilder.getInstance().buildController(tab, module);
        navigationController.next(null);
        return navigationController;
    }

    public void addOnSwipeListener(final ListView listView){
        if(isSwipeAdded){
            return;
        }

        swipeTouchListener=new OnSwipeTouchListener(context) {
            /**
             * Click listener for image option
             * @param view
             */
            public void onClick(View view) {
                Log.d(TAG, "onClick");

                Option selectedOption=(Option)view.getTag();
                Question question=navigationController.getCurrentQuestion();


                Value value = question.getValueBySession(module);
                //set new totalpages if the value is not null and the value change
                if(value!=null && !readOnly)
                    navigationController.setTotalPages(question.getTotalQuestions());
                ReadWriteDB.saveValuesDDL(question, selectedOption, value, module);

                ViewGroup vgTable = (ViewGroup) view.getParent().getParent();
                for (int rowPos = 0; rowPos < vgTable.getChildCount(); rowPos++) {
                    ViewGroup vgRow = (ViewGroup) vgTable.getChildAt(rowPos);
                    for (int itemPos = 0; itemPos < vgRow.getChildCount(); itemPos++) {
                        View childItem = vgRow.getChildAt(itemPos);
                        if (childItem instanceof ImageView) {
                            //We dont want the user to click anything else
                            swipeTouchListener.clearClickableViews();

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
                if(readOnly || navigationController.isNextAllowed()) {

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

    @Override
    public BaseAdapter getAdapter() {
        return this;
    }

    @Override
    public int getLayout() {
        return id_layout;
    }

    @Override
    public Float getScore() {
        return 0F;
    }

    /**
     * No scores required
     */
    @Override
    public void initializeSubscore() {
    }

    @Override
    public String getName() {
        return tab.getName();
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return this.navigationController.getCurrentQuestion();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflate the layout
        View rowView = lInflater.inflate(R.layout.dynamic_tab_grid_question, parent, false);
        rowView.getLayoutParams().height=parent.getHeight();
        rowView.requestLayout();
        Question question=(Question)this.getItem(position);
        // We get values from DB and put them in Session
        if(Session.getSurveyByModule(module)!=null)
            Session.getSurveyByModule(module).getValuesFromDB();
        // Se get the value from Session
        Value value=question.getValueBySession(module);

        //Question
        CustomTextView headerView=(CustomTextView) rowView.findViewById(R.id.question);
        //Load a font which support Khmer character
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/" + context.getResources().getString(R.string.specific_language_font));
        headerView.setTypeface(tf);
        headerView.setText(question.getForm_name());

        //Progress
        ProgressBar progressView=(ProgressBar)rowView.findViewById(R.id.dynamic_progress);
        TextView progressText=(TextView)rowView.findViewById(R.id.dynamic_progress_text);
        progressView.setMax(navigationController.getTotalPages());
        progressView.setProgress(navigationController.getCurrentPage()+1);
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
                        tableRow=(TableRow)lInflater.inflate(R.layout.dynamic_tab_row,tableLayout,false);
                        tableLayout.addView(tableRow);
                    }
                    ImageView imageButton = (ImageView) tableRow.getChildAt(mod);
                    imageButton.setBackgroundColor(Color.parseColor("#" + currentOption.getBackground_colour()));

                    initOptionButton(imageButton, currentOption, value, parent);
                }
                break;
            case Constants.IMAGES_3:
                List<Option> opts = question.getAnswer().getOptions();
                swipeTouchListener.clearClickableViews();
                for(int i=0;i<opts.size();i++){

                    tableRow=(TableRow)lInflater.inflate(R.layout.dynamic_tab_row_singleitem,tableLayout,false);
                    tableLayout.addView(tableRow);

                    ImageView imageButton = (ImageView) tableRow.getChildAt(0);

                    Option currentOption = opts.get(i);
                    imageButton.setBackgroundColor(Color.parseColor("#" + currentOption.getBackground_colour()));

                    initOptionButton(imageButton, currentOption, value, parent);
                }
                break;
            case Constants.IMAGES_5:
                List<Option> optns = question.getAnswer().getOptions();
                swipeTouchListener.clearClickableViews();
                for(int i=0;i<optns.size();i++) {
                    Option currentOption = optns.get(i);
                    int mod = i % 2;
                    //First item per row requires a new row
                    if (mod == 0) {
                        tableRow = (TableRow) lInflater.inflate(R.layout.dynamic_tab_row, tableLayout, false);
                        tableLayout.addView(tableRow);
                    }
                    //The last option in the last row is a single image
                    if (i == optns.size()-1) {
                        ImageView imageButton = null;
                        TableRow.LayoutParams params = new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT,1f);
                        imageButton = (ImageView) tableRow.getChildAt(mod);
                        //remove the innecesary second imageview.
                        tableRow.removeViewAt(mod+1);
                        imageButton.setLayoutParams(params);
                        imageButton.setBackgroundColor(Color.parseColor("#" + currentOption.getBackground_colour()));

                        initOptionButton(imageButton, currentOption, value, parent);
                    }
                    else{
                        ImageView imageButton = (ImageView) tableRow.getChildAt(mod);
                        imageButton.setBackgroundColor(Color.parseColor("#" + currentOption.getBackground_colour()));

                        initOptionButton(imageButton, currentOption, value, parent);
                    }
                }
                break;
            case Constants.PHONE:
                swipeTouchListener.clearClickableViews();
                tableRow=(TableRow)lInflater.inflate(R.layout.dynamic_tab_phone_row, tableLayout, false);
                tableLayout.addView(tableRow);
                initPhoneValue(tableRow, value);
                break;
            case Constants.POSITIVE_INT:
                swipeTouchListener.clearClickableViews();
                tableRow=(TableRow)lInflater.inflate(R.layout.dynamic_tab_positiveint_row, tableLayout, false);
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
        String current = context.getResources().getString(context.getResources().getIdentifier("number_"+currentPage, "string", context.getPackageName()));
        String total = context.getResources().getString(context.getResources().getIdentifier("number_"+totalPages, "string", context.getPackageName()));
        return current.concat("/").concat(total);
    }

    private void showKeyboard(Context c, View v){
        Log.d(TAG,"KEYBOARD SHOW ");
        keyboardView=v;
        InputMethodManager keyboard = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(v, 0);
    }

    /**
     * hide keyboard using a provided view
     */
    private void hideKeyboard(Context c, View v){
        Log.d(TAG,"KEYBOARD HIDE ");
        InputMethodManager keyboard = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(v!=null)
            keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    /**
     * hide keyboard using a keyboardView variable view
     */
    private void hideKeyboard(Context c){
        Log.d(TAG,"KEYBOARD HIDE ");
        InputMethodManager keyboard = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(keyboardView!=null)
            keyboard.hideSoftInputFromWindow(keyboardView.getWindowToken(), 0);
    }
    /**
     * Initialise NumberPicker and button to view/edit a integer between 0 and Constants.MAX_INT_AGE
     * @param tableRow
     * @param value
     */
    private void initPositiveIntValue(TableRow tableRow, Value value){
        Button button=(Button)tableRow.findViewById(R.id.dynamic_positiveInt_btn);

        final EditText numberPicker = (EditText)tableRow.findViewById(R.id.dynamic_positiveInt_edit);

        //Without setMinValue, setMaxValue, setValue in this order, the setValue is not displayed in the screen.
        numberPicker.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(Constants.MAX_INT_CHARS),
                new MinMaxInputFilter(0, 99)
        });

        //Has value? show it
        if(value!=null){
            numberPicker.setText(value.getValue());
        }

        if (!readOnly) {
            //Save the numberpicker value in the DB, and continue to the next screen.
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savePositiveIntValue(numberPicker);
                }
            });

        }else{
            numberPicker.setEnabled(false);
            button.setEnabled(false);
        }

        //Add button to listener
        swipeTouchListener.addClickableView(button);

        //Take focus and open keyboard
        openKeyboard(numberPicker);
    }

    private void savePositiveIntValue(EditText numberPicker) {
        String positiveIntValue = String.valueOf(numberPicker.getText());

        //Required, empty values rejected
        if(checkEditTextNotNull(positiveIntValue)){
            numberPicker.setError(context.getString(R.string.dynamic_error_age));
            return;
        }
        Question question = navigationController.getCurrentQuestion();
        ReadWriteDB.saveValuesText(question, positiveIntValue, module);
        finishOrNext();
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

            //Try to format on done
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String phoneValue = editText.getText().toString();
                        if (checkPhoneNumberByMask(phoneValue)) {
                            editText.setText(formatPhoneNumber(phoneValue));
                        }
                        savePhoneValue(editText);
                    }
                    return false;
                }
            });

            //Validate format on button click
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentView = (View) v.getParent();
                    EditText editText = (EditText) parentView.findViewById(R.id.dynamic_phone_edit);
                    savePhoneValue(editText);
                }
            });
        }else{
            editText.setEnabled(false);
            button.setEnabled(false);
        }

        //Add button to listener
        swipeTouchListener.addClickableView(button);

        //Take focus and open keyboard
        openKeyboard(editText);
    }

    private void savePhoneValue(EditText editText) {

        String phoneValue = editText.getText().toString();
        //Check phone ok
        if(!checkPhoneNumberByMask(phoneValue)){
            editText.setError(context.getString(R.string.dynamic_error_phone_format));
            return;
        }
        String value=editText.getText().toString();
        //Hide keypad
        hideKeyboard(PreferencesState.getInstance().getContext());
        editText.setText(value);
        Question question = navigationController.getCurrentQuestion();
        ReadWriteDB.saveValuesText(question, phoneValue, module);
        finishOrNext();
    }

    private void openKeyboard(final EditText editText){
        if(!readOnly) {
            editText.requestFocus();
            editText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Show keypad
                    showKeyboard(context, editText);
                }
            }, 300);
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if(v.getId()==R.id.dynamic_positiveInt_edit)
                            savePositiveIntValue((EditText) v);
                        else if(v.getId()==R.id.dynamic_phone_edit)
                            savePhoneValue((EditText)v);
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            });
        }
    }

    /**
     * Formats number according to mask 0NN NNN NNN{N}
     * @param phoneValue
     * @return
     */
    private String formatPhoneNumber(String phoneValue) {
        //Empty -> nothing to format
        if (phoneValue == null || "".equals(phoneValue)) {
            phoneValue = "";
        }

        //Already formatted -> done
        if(phoneValue.isEmpty() || phoneValue.matches(FORMATTED_PHONENUMBER_MASK)){
            return phoneValue;
        }

        //0NNNNNNNN{N} -> 0NN NNN NNN{N}
        String formattedNumber=phoneValue.substring(0,3)+" "+phoneValue.substring(3,6)+" "+phoneValue.substring(6,phoneValue.length());
        return  formattedNumber;
    }

    /**
     * Checks if the given string corresponds a correct phone number for the current country (by locale)
     * @param phoneValue
     * @return true|false
     */
    private boolean checkPhoneNumberByCountry(String phoneValue){

        //Empty  is ok
        if (phoneValue == null || "".equals(phoneValue)) {
            phoneValue = "";
        }

        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            Locale locale = context.getResources().getConfiguration().locale;
            phoneNumber = PhoneNumberUtil.getInstance().parse(phoneValue, locale.getCountry());
        } catch (NumberParseException e) {
            return false;
        }
        return PhoneNumberUtil.getInstance().isValidNumber(phoneNumber);
    }


    /**
     * Checks if the given string corresponds a correct phone number according to mask:
     *  0NN NNN NNN{N}
     * @param phoneValue
     * @return true|false
     */
    private boolean checkPhoneNumberByMask(String phoneValue){

        //Empty  is ok
        if (phoneValue == null) {
            phoneValue = "";
        }
        return phoneValue.isEmpty() || phoneValue.replace(" ", "").matches(FORMATTED_PHONENUMBER_MASK) || phoneValue.replace(" ", "").matches(PLAIN_PHONENUMBER_MASK);
    }

    /**
     * Checks if edit text is not null:
     * @param editValue
     * @return true|false
     */
    private boolean checkEditTextNotNull(String editValue){
        if (editValue == null) {
            editValue = "";
        }
        return editValue.isEmpty();
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
            InputStream inputStream = context.getAssets().open(option.getPath());
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
        Drawable selectedBackground = context.getResources().getDrawable(R.drawable.background_dynamic_clicked_option);
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {    //JELLY_BEAN=API16
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
                Question question = navigationController.getCurrentQuestion();
                Value value = question.getValueBySession(module);
                if (isDone(value)) {
                    showDone();
                    return;
                }
                next();
            }
        }, 750);
    }

    /**
     * Show a final dialog to announce the survey is over
     */
    private void showDone(){
        final Activity activity=(Activity)context;
        AlertDialog.Builder msgConfirmation = new AlertDialog.Builder((activity))
                .setTitle(R.string.survey_title_completed)
                .setMessage(R.string.survey_info_completed)
                .setCancelable(false)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        hideKeyboard(PreferencesState.getInstance().getContext());
                        closeSurvey();

                    }
                });
        msgConfirmation.setNegativeButton(R.string.review, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                hideKeyboard(PreferencesState.getInstance().getContext());
                review();
            }
        });

        msgConfirmation.create().show();
    }

    private void closeSurvey() {
        saveSurvey();
        AssessModuleController assessModuleController= (AssessModuleController) DashboardActivity.dashboardController.getModuleByName(module);
        assessModuleController.closeSurveyFragment();
    }

    public void saveSurvey(){
        Survey survey=Session.getSurveyByModule(module);
        survey.getValuesFromDB();
        survey.updateSurveyStatusAndMarkAsCompleted();
    }
    /**
     * Checks if there are more questions to answer according to the given value + current status.
     * @param value
     * @return
     */
    private boolean isDone(Value value){
        return !navigationController.hasNext(value!=null?value.getOption():null);
    }

    /**
     * Changes the current question moving forward
     */
    private void next(){
        Question question = navigationController.getCurrentQuestion();
        Value value = question.getValueBySession(module);
        if (isDone(value)) {
            return;
        }
        navigationController.next(value!=null?value.getOption():null);
        notifyDataSetChanged();
        hideKeyboard(PreferencesState.getInstance().getContext());

        question = navigationController.getCurrentQuestion();
        value = question.getValueBySession(module);
        //set new page number if the value is null
        if(value==null  && !readOnly)
            navigationController.setTotalPages(navigationController.getCurrentQuestion().getTotalQuestions());
    }

    /**
     * Changes the current question moving backward
     */
    private void previous(){
        if(!navigationController.hasPrevious()){
            return;
        }
        navigationController.previous();
        notifyDataSetChanged();
    }

    /**
     * Back to initial question to review questions
     */
    private void review(){
        navigationController.first();
        notifyDataSetChanged();
    }

    /**
     * Factory method to build a Adapter.
     *
     * @param tab
     * @param context
     * @return
     */
    public static DynamicTabAdapter build(Tab tab, Context context, float idSurvey, String module) {
       return new DynamicTabAdapter(tab, context, idSurvey, module);
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