package org.eyeseetea.malariacare.services;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItemBuilder;
import org.eyeseetea.malariacare.data.database.utils.services.PlannedServiceBundle;


public class PlannedSurveyService extends IntentService {

    /**
     * Constant added to the intent in order to reuse the service for different 'methods'
     */
    public static final String SERVICE_METHOD = "serviceMethod";

    /**
     * Name of the parameter that holds every survey that goes into the planned tab
     */
    public static final String PLANNED_SURVEYS_ACTION =
            "org.eyeseetea.malariacare.services.SurveyService.PLANNED_SURVEYS_ACTION";
    /**
     * Name of the parameter that holds every survey that goes into the org unit planned tab
     */
    public static final String PLANNED_PER_ORG_UNIT_SURVEYS_ACTION =
            "org.eyeseetea.malariacare.services.SurveyService.PLANNED_PER_ORG_UNIT_SURVEYS_ACTION";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PlannedSurveyService(String name) {
        super(name);
    }

    public PlannedSurveyService() {
        super(PlannedSurveyService.class.getSimpleName());
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getStringExtra(SERVICE_METHOD).equals(PLANNED_SURVEYS_ACTION)) {
            reloadPlannedSurveys();
        }
        if (intent.getStringExtra(SERVICE_METHOD).equals(PLANNED_PER_ORG_UNIT_SURVEYS_ACTION)){
            reloadPlannedSurveysPerOrgUnit();
        }
    }

    private void reloadPlannedSurveys() {
        Log.d(getClass().getName(), "reloadPlanningSurveys");
        PlannedServiceBundle plannedServiceBundle = new PlannedServiceBundle();
        plannedServiceBundle.setPlannedItems(new PlannedItemBuilder().buildPlannedItems());
        plannedServiceBundle.addModelList(OrgUnitDB.class.getName(), OrgUnitDB.getAllOrgUnit());
        plannedServiceBundle.addModelList(ProgramDB.class.getName(), ProgramDB.getAllPrograms());
        Session.putServiceValue(PLANNED_SURVEYS_ACTION, plannedServiceBundle);
        //Returning result to anyone listening
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PLANNED_SURVEYS_ACTION));
    }
    private void reloadPlannedSurveysPerOrgUnit() {
        Log.d(getClass().getName(), "reloadPlanningSurveys");
        PlannedServiceBundle plannedServiceBundle = new PlannedServiceBundle();
        plannedServiceBundle.setPlannedItems(new PlannedItemBuilder().buildPlannedItems());
        plannedServiceBundle.addModelList(OrgUnitDB.class.getName(), OrgUnitDB.getAllOrgUnit());
        plannedServiceBundle.addModelList(ProgramDB.class.getName(), ProgramDB.getAllPrograms());
        Session.putServiceValue(PLANNED_PER_ORG_UNIT_SURVEYS_ACTION, plannedServiceBundle);
        //Returning result to anyone listening
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PLANNED_PER_ORG_UNIT_SURVEYS_ACTION));
    }
}
