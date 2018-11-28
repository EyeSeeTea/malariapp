package org.eyeseetea.malariacare.data.remote.api.data;

import static org.hamcrest.core.Is.is;

import org.eyeseetea.malariacare.common.ResourcesFileReader;
import org.eyeseetea.malariacare.data.remote.api.ApiMapper;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiMapperShould {
    String uid1="uid1";
    String uid2="uid2";
    String uid3="uid3";
    String expectedPathMultipleUid="/api/events.json?event=uid1;uid2;uid3";
    String expectedPathOneUid="/api/events.json?event=uid1";
    String expectedEmptyPath="/api/events.json?event=";

    private ResourcesFileReader mFileReader = new ResourcesFileReader();

    @Test
    public void return_formatted_enpoint_when_pass_more_than_two_uids() {
        //given
        List<String> uids = new ArrayList<>();
        uids.add(uid1);
        uids.add(uid2);
        uids.add(uid3);
        //when
        String path = ApiMapper.getFilteredEventPath(uids);

        //then
        Assert.assertThat(path.equals(expectedPathMultipleUid), is(true));
    }

    @Test
    public void return_formatted_enpoint_when_pass_only_one_uid() {
        //given
        List<String> uids = new ArrayList<>();
        uids.add(uid1);
        //when
        String path = ApiMapper.getFilteredEventPath(uids);

        //then
        Assert.assertThat(path. equals(expectedPathOneUid), is(true));
    }

    @Test
    public void return_null_when_pass_empty_list_of_uids() {
        //given
        List<String> uids = new ArrayList<>();
        //when
        String path = ApiMapper.getFilteredEventPath(uids);

        //then
        Assert.assertThat(path. equals(expectedEmptyPath), is(true));
    }

    @Test
    public void return_mapped_surveys_from_api_event_json() {
        //given
        List<Survey> surveys = new ArrayList<>();
        try {
            //when
            try {
                surveys = ApiMapper.mapSurveysFromJson(givenAJSONObjectofEvent("events_filtered_by_uid.json"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //then
        Assert.assertThat(surveys.size(), is(2));
        Assert.assertThat(surveys.get(0).getUId(), is("PpAn7NKUE1P"));
        Assert.assertThat(surveys.get(0).getUId(), is("T7nItoNHhOU"));
    }

    @Test
    public void return_empty_list_of_surveys_from_empty_api_event_json() {
        //given
        List<Survey> surveys = new ArrayList<>();
        try {
            //when
            surveys = ApiMapper.mapSurveysFromJson(givenAJSONObjectofEvent("events_filtered_by_uid_empty.json"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //then
        Assert.assertThat(surveys.size(), is(2));
        Assert.assertThat(surveys.get(0).getUId(), is("PpAn7NKUE1P"));
        Assert.assertThat(surveys.get(0).getUId(), is("T7nItoNHhOU"));
    }

    private String givenAJSONObjectofEvent(String fileName) throws IOException {
        return mFileReader.getStringFromFile(fileName);
    }
}
