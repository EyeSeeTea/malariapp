package org.eyeseetea.malariacare.domain.usecase;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AMonitorDialogAdapter;
import org.eyeseetea.sdk.presentation.views.CustomTextView;

import java.util.ArrayList;

public class ShowListOfSurveyUseCase {
    DashboardActivity mDashboardActivity;
    IMainExecutor mMainExecutor;

    public ShowListOfSurveyUseCase(DashboardActivity dashboardActivity, IMainExecutor mainExecutor) {
        mDashboardActivity = dashboardActivity;
        mMainExecutor = mainExecutor;
    }

    public void execute(final ArrayList<SurveyDB> surveys) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mDashboardActivity);
        LayoutInflater inflater = mDashboardActivity.getLayoutInflater();

        View v = inflater.inflate(R.layout.monitor_model_dialog, null);

        builder.setView(v);
        CustomTextView orgUnit = (CustomTextView) v.findViewById(R.id.org_unit);
        CustomTextView program = (CustomTextView) v.findViewById(R.id.program);
        program.setText(surveys.get(0).getProgram().getName());
        orgUnit.setText(surveys.get(0).getOrgUnit().getName());
        Button cancel = (Button) v.findViewById(R.id.cancel);
        ListView listView = (ListView) v.findViewById(R.id.surveys_list);

        final AlertDialog alertDialog = builder.create();
        AMonitorDialogAdapter monitorAdapter = new AMonitorDialogAdapter(mDashboardActivity);
        monitorAdapter.setItems(surveys);
        listView.setAdapter(monitorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                // call feedbackselected function(and it call surveyfragment)
                alertDialog.dismiss();
                mMainExecutor.run(new Runnable() {
                    @Override
                    public void run() {
                        mDashboardActivity.onFeedbackSelected(surveys.get(position - 1));
                    }
                });
            }
        });
        cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                }
        );

        alertDialog.show();
    }
}
