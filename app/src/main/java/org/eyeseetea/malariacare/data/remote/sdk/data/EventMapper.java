package org.eyeseetea.malariacare.data.remote.sdk.data;

import android.content.Context;
import android.location.Location;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.ISyncData;
import org.hisp.dhis.client.sdk.models.common.Coordinates;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;

public abstract class EventMapper {

    protected final Context mContext;
    protected final String mUsername;

    public EventMapper(Context context, String username) {
        mContext = context;
        mUsername = username;
    }

    protected TrackedEntityDataValue createDataValue(Event event, String dataElement, String value) {
        TrackedEntityDataValue dataValue = new TrackedEntityDataValue();
        dataValue.setDataElement(dataElement);
        dataValue.setEvent(event);
        dataValue.setStoredBy(mUsername);
        dataValue.setValue(value);
        return dataValue;
    }

    protected Event buildEvent(String orgUnitUid, String programUid,
            boolean isNew) throws Exception {

        Event event = new Event();
        event.setStatus(Event.EventStatus.COMPLETED);

        event.setOrgUnit(orgUnitUid);
        event.setProgram(programUid);

        //TODO: jsanchez It's necessary
        //event.setProgramStage(relatedSurvey.getProgramUId());

        if (isNew) {
            updateEventLocation(event);
        }

        return event;
    }

    protected String createErrorConversionMessage(String errorMessageBase, ISyncData data,
            String orgUnitUid, String programUid) {

        return ": " + errorMessageBase + " surveyUid: " + data.getSurveyUid()
                + "program: " + programUid + " OrgUnit: "
                + orgUnitUid + "data: " + data.toString();
    }

    private void updateEventLocation(Event event) throws Exception {
        Location lastLocation = LocationMemory.get(event.getUId());
        //If location is required but there is no location -> exception
        if (PreferencesState.getInstance().isLocationRequired() && lastLocation == null) {
            throw new Exception(
                    mContext.getString(R.string.dialog_error_push_no_location_and_required));
        }

        //No location + not required -> done
        if (lastLocation == null) {
            return;
        }

        //location -> set lat/lng
        Coordinates coordinates =
                new Coordinates(lastLocation.getLatitude(),lastLocation.getLongitude());

        event.setCoordinate(coordinates);
    }
}
