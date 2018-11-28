package org.eyeseetea.malariacare.data.remote.api.data;

import static org.hamcrest.core.Is.is;

import org.eyeseetea.malariacare.data.remote.api.ApiMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ApiMapperShould {
    String uid1="uid1";
    String uid2="uid2";
    String uid3="uid3";
    String expectedPathMultipleUid="/api/events.json?event=uid1;uid2;uid3";
    String expectedPathOneUid="/api/events.json?event=uid1";
    String expectedEmptyPath="/api/events.json?event=";

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
}
