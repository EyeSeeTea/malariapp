package org.eyeseetea.malariacare.data.remote.api;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.domain.boundary.IUserRemoteDataSource;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public class UserRemoteDataSource extends OkHttpClientDataSource implements IUserRemoteDataSource {

    private static final String DHIS_PULL_API="/api/";

    private static String QUERY_USER_ATTRIBUTES =
            "/%s?fields=attributeValues[value,attribute[code]]id&paging=false";


    private static final String TAG = ".PullDhisApiDataSource";

    public static final String EVENT = "event";
    public static final String ATTRIBUTEVALUES = "attributeValues";
    public static final String ATTRIBUTE = "attribute";
    public static final String VALUE = "value";
    public static final String CODE = "code";
    private static final String USER = "users";

    public UserRemoteDataSource(Credentials credentials) {
        super(credentials);
    }

    @Override
    public UserAccount getUser(UserAccount userAccount) {
        UserAccount userAccountUpdated = pullUserAttributes(userAccount.getUserUid());
        return userAccountUpdated;
    }

    private UserAccount pullUserAttributes(String userUId) {
        UserAccount userAccount = new UserAccount(userUId, userUId, "", null);
        String data = USER + String.format(QUERY_USER_ATTRIBUTES, userAccount.getUserUid());
        Log.d(TAG, String.format("getUserAttributesApiCall(%s) -> %s", USER, data));
        try {
            String response = executeCall(DHIS_PULL_API+data, "GET");
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
            userAccount.setAnnouncement(newMessage);
            userAccount.setClosedDate(closeDate);
        } catch (Exception ex) {
            Log.e(TAG, "Cannot read user last updated from server with");
            ex.printStackTrace();
        }
        return userAccount;
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
