package org.eyeseetea.malariacare.data.remote.api;

import android.content.Context;

import org.eyeseetea.dhis2.lightsdk.D2Response;
import org.eyeseetea.dhis2.lightsdk.attributes.AttributeValue;
import org.eyeseetea.dhis2.lightsdk.organisationunits.OrganisationUnit;
import org.eyeseetea.dhis2.lightsdk.programs.ProgramType;
import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.dataSources.D2LightSDKDataSource;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.utils.DateParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserAccountD2LightSDKDataSource
        extends D2LightSDKDataSource
        implements IUserAccountDataSource {

    public static final String ATTRIBUTE_USER_CLOSE_DATE = "USER_CLOSE_DATE";
    public static final String ATTRIBUTE_USER_ANNOUNCEMENT = "USER_ANNOUNCEMENT";

    public UserAccountD2LightSDKDataSource(Context context) {
        super(context);
    }

    @Override
    public UserAccount getUser() throws Exception {
        D2Response<org.eyeseetea.dhis2.lightsdk.useraccount.UserAccount> meResponse =
                getD2Api().me().get().execute();

        if (meResponse.isSuccess()) {
            D2Response.Success<org.eyeseetea.dhis2.lightsdk.useraccount.UserAccount> success =
                    (D2Response.Success<org.eyeseetea.dhis2.lightsdk.useraccount.UserAccount>)
                            meResponse;

            return mapToDomain(success.getValue());
        } else {
            D2Response.Error errorResponse = (D2Response.Error) meResponse;

            handleError(errorResponse);
        }

        return null;
    }

    private UserAccount mapToDomain(
            org.eyeseetea.dhis2.lightsdk.useraccount.UserAccount remoteUserAccount) {

        String announcement = "";
        Date closedDate = null;

        for (AttributeValue attributeValue : remoteUserAccount.getAttributeValues()) {
            if (attributeValue.getAttribute().getCode().equals(ATTRIBUTE_USER_ANNOUNCEMENT)) {
                announcement = attributeValue.getValue();
            } else if (attributeValue.getAttribute().getCode().equals(ATTRIBUTE_USER_CLOSE_DATE)) {
                closedDate = parseClosedDate(attributeValue.getValue());
            }
        }

        List<String> assignedOrgUnits = new ArrayList<>();
        List<String> assignedPrograms = new ArrayList<>();

        for (OrganisationUnit organisationUnit : remoteUserAccount.getOrganisationUnits()) {
            if (!assignedOrgUnits.contains(organisationUnit.getId())) {
                assignedOrgUnits.add(organisationUnit.getId());
            }

            for (org.eyeseetea.dhis2.lightsdk.programs.Program program :
                    organisationUnit.getPrograms()) {
                if (!assignedPrograms.contains(program.getId()) &&
                        program.getProgramType() == ProgramType.WITHOUT_REGISTRATION) {
                    assignedPrograms.add(program.getId());
                }
            }
        }

        UserAccount userAccount = new UserAccount(
                remoteUserAccount.getName(),
                remoteUserAccount.getUserCredentials().getUsername(),
                remoteUserAccount.getId(), announcement, closedDate,
                assignedPrograms, assignedOrgUnits);

        return userAccount;
    }

    @Override
    public void saveUser(UserAccount user) {
        //On the future implement this method to update server user account if is needed
    }

    private Date parseClosedDate(String closedDate) {
        if (closedDate == null || closedDate.equals("")) {
            return null;
        }
        DateParser dateParser = new DateParser();
        return dateParser.parseDate(closedDate, DateParser.LONG_DATE_FORMAT);
    }

}
