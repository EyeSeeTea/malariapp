package org.eyeseetea.malariacare.data.remote.api;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.IUserAccountRemoteDataSource;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.exception.GetUserAccountException;
import org.eyeseetea.malariacare.domain.usecase.UserFilter;
import org.eyeseetea.malariacare.utils.DateParser;

import java.util.Date;

public class UserAccountAPIRemoteDataSource extends OkHttpClientDataSource implements IUserAccountRemoteDataSource {

    private static final String DHIS_PULL_API="/api/";

    private static String QUERY_USER_ATTRIBUTES =
            "/%s?fields=name,userCredentials[username],attributeValues[value,attribute[code]]id&paging=false";


    private static final String TAG = ".PullDhisApiDataSource";

    public static final String EVENT = "event";
    public static final String ATTRIBUTEVALUES = "attributeValues";
    public static final String ATTRIBUTE = "attribute";
    public static final String VALUE = "value";
    public static final String CODE = "code";
    private static final String USER = "users";
    private static final String NAME = "name";
    private static final String USERCREDENTIALS = "userCredentials";
    private static final String USERNAME = "username";

    public UserAccountAPIRemoteDataSource(Credentials credentials) {
        super(credentials);
    }

    @Override
    public UserAccount getUser(UserFilter userFilter) throws GetUserAccountException {
        String username = "";
        String name = "";
        String announcement;
        String closedDateAsString;
        String data = USER + String.format(QUERY_USER_ATTRIBUTES, userFilter.getUid());
        Log.d(TAG, String.format("getUserAttributesApiCall(%s) -> %s", USER, data));
        try {
            String response = executeCall(DHIS_PULL_API+data);
            JsonNode jsonNode = parseResponse(response);
            JsonNode jsonNodeArray = jsonNode.get(ATTRIBUTEVALUES);
            String newMessage = "";
            String closeDate = "";
            for (int i = 0; i < jsonNodeArray.size(); i++) {
                newMessage =
                        getUserAnnouncement(jsonNodeArray, newMessage, i,
                                UserDB.ATTRIBUTE_USER_ANNOUNCEMENT);
                closeDate = getUserCloseDate(jsonNodeArray, closeDate, i);
            }
            announcement =newMessage;
            closedDateAsString=closeDate;
            username = getUsername(jsonNode);
            name = getName(jsonNode);
        } catch (Exception ex) {
            Log.e(TAG, "Cannot read user last updated from server with");
            ex.printStackTrace();
            throw new GetUserAccountException();
        }
        UserAccount userAccount = new UserAccount(name, username, userFilter.getUid(), announcement, parseClosedDate(closedDateAsString));
        return userAccount;
    }


    private Date parseClosedDate(String closedDate) {
        if (closedDate == null || closedDate.equals("")) {
            return null;
        }
        return DateParser.parseNewLongDate(closedDate);
    }

    private static String getName(JsonNode jsonNodeArray) {
        return jsonNodeArray.get(NAME).textValue();
    }

    private static String getUsername(JsonNode jsonNodeArray) {
        return jsonNodeArray.get(USERCREDENTIALS).get(USERNAME).textValue();
    }

    private static String getUserCloseDate(JsonNode jsonNodeArray, String closeDate, int i) {
        if (jsonNodeArray.get(i).get(ATTRIBUTE).get(CODE).textValue().equals(
                UserDB.ATTRIBUTE_USER_CLOSE_DATE)) {
            closeDate = jsonNodeArray.get(i).get(VALUE).textValue();
        }
        return closeDate;
    }

    private static String getUserAnnouncement(JsonNode jsonNodeArray, String newMessage, int i,
            String attributeUserAnnouncement) {
        if (jsonNodeArray.get(i).get(ATTRIBUTE).get(CODE).textValue().equals(
                attributeUserAnnouncement)) {
            newMessage = jsonNodeArray.get(i).get(VALUE).textValue();
        }
        return newMessage;
    }
}
