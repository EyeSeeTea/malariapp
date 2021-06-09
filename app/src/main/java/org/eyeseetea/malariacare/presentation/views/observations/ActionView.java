package org.eyeseetea.malariacare.presentation.views.observations;

import android.app.AlertDialog;
import android.content.Context;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.presentation.presenters.observations.ActionPresenter;
import org.eyeseetea.malariacare.presentation.viewmodels.observations.ActionViewModel;
import org.eyeseetea.malariacare.presentation.views.CustomTextWatcher;
import org.eyeseetea.malariacare.utils.DateParser;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.Calendar;
import java.util.Date;

public class ActionView extends LinearLayout implements ActionPresenter.View {

    public interface OnActionChangedListener {
        void onActionChanged(ActionViewModel actionViewModel);
    }

    private ActionPresenter presenter;

    private OnActionChangedListener onActionChangedListener;

    private CustomTextView titleView;
    private CustomEditText descriptionView;
    private CustomEditText dueDateView;
    private CustomEditText responsibleView;
    private ImageView expandCollapseView;
    private LinearLayout responsibleContainer;
    private LinearLayout dueDateContainer;

    public ActionView(Context context) {
        super(context);

        initialize(context);
    }

    public ActionView(Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);

        initialize(context);
    }

    public void setOnActionChangedListener(OnActionChangedListener listener) {
        onActionChangedListener = listener;
    }

    public void setAction(ActionViewModel actionViewModel) {
        presenter.setAction(actionViewModel);
    }

    public void setTitle(String title) {
        if (titleView == null) {
            titleView = findViewById(R.id.action_title_view);
        }
        titleView.setText(title);
    }

    @Override
    public void setEnabled(boolean value) {
        descriptionView.setEnabled(value);
        responsibleView.setEnabled(value);
        dueDateView.setEnabled(value);
    }

    @Override
    public void showActionData(ActionViewModel actionViewModel) {
        descriptionView.setText(actionViewModel.getDescription());
        responsibleView.setText(actionViewModel.getResponsible());

        showDueDate(actionViewModel.getDueDate());
    }

    @Override
    public void notifyOnActionChanged(ActionViewModel actionViewModel) {
        if (onActionChangedListener != null) {
            onActionChangedListener.onActionChanged(actionViewModel);
        }
    }

    @Override
    public void expand() {
        expandCollapseView.setRotation(180);
        descriptionView.setVisibility(VISIBLE);
        responsibleContainer.setVisibility(VISIBLE);
        dueDateContainer.setVisibility(VISIBLE);
    }

    @Override
    public void collapse() {
        expandCollapseView.setRotation(0);
        descriptionView.setVisibility(GONE);
        responsibleContainer.setVisibility(GONE);
        dueDateContainer.setVisibility(GONE);
    }

    private void initialize(final Context context) {
        inflate(context, R.layout.view_observation_action, this);

        initializeDescription();
        initializeResponsibleView();
        initializeDueDateView();
        initializeExpandCollapseView();
        initializePresenter();
    }

    private void showDueDate(Date dueDate) {
        if (dueDate != null) {
            String dueDateText = new DateParser().format(dueDate, DateParser.AMERICAN_DATE_FORMAT);
            dueDateView.setText(dueDateText);
        }
    }

    private void initializeExpandCollapseView() {
        expandCollapseView = findViewById(R.id.expand_collapse_view);

        expandCollapseView.setOnClickListener(v -> presenter.expandOrCollapse());
    }

    private void initializeDescription() {
        descriptionView = findViewById(R.id.description_view);

        descriptionView.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                presenter.onDescriptionChange(editable.toString());
            }
        });
    }

    private void initializeResponsibleView() {
        responsibleContainer = findViewById(R.id.responsible_container_view);
        responsibleView = findViewById(R.id.responsible_view);

        responsibleView.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                presenter.onResponsibleChange(editable.toString());
            }
        });
    }

    private void initializeDueDateView() {
        dueDateContainer = findViewById(R.id.due_date_container_view);
        dueDateView = findViewById(R.id.due_date_view);

        dueDateView.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_date, null);
        DatePicker datePicker = v.findViewById(R.id.dialog_date_date_picker);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(v)
                //.setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok,
                        (dialog1, which) -> {
                            int year = datePicker.getYear();
                            int month = datePicker.getMonth();
                            int day = datePicker.getDayOfMonth();

                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, month, day);
                            Date dueDate = calendar.getTime();

                            presenter.onDueDateChange(dueDate);

                            showDueDate(dueDate);

                            dialog1.dismiss();
                        })
                .create();

        dialog.show();
    }

    private void initializePresenter() {
        presenter = new ActionPresenter();
        presenter.attachView(this);
    }
}
