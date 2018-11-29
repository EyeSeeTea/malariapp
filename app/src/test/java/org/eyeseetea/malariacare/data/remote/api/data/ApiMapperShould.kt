package org.eyeseetea.malariacare.data.remote.api.data

import org.eyeseetea.malariacare.common.ResourcesFileReader
import org.eyeseetea.malariacare.data.remote.api.ApiMapper
import org.eyeseetea.malariacare.domain.entity.Survey
import org.hamcrest.core.Is.`is`
import org.json.JSONException
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.util.*

class ApiMapperShould {
    internal var uid1 = "uid1"
    internal var uid2 = "uid2"
    internal var uid3 = "uid3"
    internal var expectedPathMultipleUid = "/api/events.json?event=uid1;uid2;uid3"
    internal var expectedPathOneUid = "/api/events.json?event=uid1"
    internal var expectedEmptyPath = "/api/events.json?event="

    private val mFileReader = ResourcesFileReader()

    @Test
    fun return_formatted_endpoint_when_pass_more_than_two_uids() {
        //given
        val uids = ArrayList<String>()
        uids.add(uid1)
        uids.add(uid2)
        uids.add(uid3)
        //when
        val path = ApiMapper.getFilteredEventPath(uids)

        //then
        Assert.assertThat(path == expectedPathMultipleUid, `is`(true))
    }

    @Test
    fun return_formatted_endpoint_when_pass_only_one_uid() {
        //given
        val uids = ArrayList<String>()
        uids.add(uid1)
        //when
        val path = ApiMapper.getFilteredEventPath(uids)

        //then
        Assert.assertThat(path == expectedPathOneUid, `is`(true))
    }

    @Test
    fun return_null_when_pass_empty_list_of_uids() {
        //given
        val uids = ArrayList<String>()
        //when
        val path = ApiMapper.getFilteredEventPath(uids)

        //then
        Assert.assertThat(path == expectedEmptyPath, `is`(true))
    }

    @Test
    fun return_mapped_surveys_from_api_event_json() {
        //given
        var surveys: List<Survey> = ArrayList()
        try {
            val response = givenAEventResponse("events_filtered_by_uid.json")
            //when
            surveys = ApiMapper.mapSurveysFromJson(response)

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        //then
        Assert.assertThat(surveys.size, `is`(2))
        Assert.assertThat(surveys[0].uId, `is`("PpAn7NKUE1P"))
        Assert.assertThat(surveys[1].uId, `is`("T7nItoNHhOU"))
    }

    @Test
    fun return_empty_list_of_surveys_from_empty_api_event_json() {
        //given
        var surveys: List<Survey> = ArrayList()
        try {
            val response = givenAEventResponse("events_filtered_by_uid_empty.json")
            //when
            surveys = ApiMapper.mapSurveysFromJson(response)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        //then
        Assert.assertThat(surveys.size, `is`(0))
    }

    @Throws(IOException::class)
    private fun givenAEventResponse(fileName: String): String {
        return mFileReader.getStringFromFile(fileName)
    }
}
