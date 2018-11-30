package org.eyeseetea.malariacare.data.remote.api.data

import org.eyeseetea.malariacare.common.ResourcesFileReader
import org.eyeseetea.malariacare.data.remote.api.ApiMapper
import org.eyeseetea.malariacare.domain.entity.Survey
import org.hamcrest.core.Is.`is`
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
    fun `return a formatted endpoint passing more than two uids`() {
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
    fun `return a formatted endpoint passing only one uid`() {
        //given
        val uids = ArrayList<String>()
        uids.add(uid1)

        //when
        val path = ApiMapper.getFilteredEventPath(uids)

        //then
        Assert.assertThat(path == expectedPathOneUid, `is`(true))
    }

    @Test
    fun `return a empty path when pass a empty uid list`() {
        //given
        val uids = ArrayList<String>()

        //when
        val path = ApiMapper.getFilteredEventPath(uids)

        //then
        Assert.assertThat(path == expectedEmptyPath, `is`(true))
    }

    @Test
    fun `return valid survey list from a api json response with events`() {
        //given
        var surveys: List<Survey> = ArrayList()
        val response = givenAEventResponse("events_filtered_by_uid.json")

        //when
        surveys = ApiMapper.mapSurveysFromJson(response)

        //then
        Assert.assertThat(surveys.size, `is`(2))
        Assert.assertThat(surveys[0].uId, `is`("PpAn7NKUE1P"))
        Assert.assertThat(surveys[1].uId, `is`("T7nItoNHhOU"))
    }

    @Test
    fun `return valid empty survey list from a api response json without events`() {
        //given
        var surveys: List<Survey> = ArrayList()
        val response = givenAEventResponse("events_filtered_by_uid_empty.json")

        //when
        surveys = ApiMapper.mapSurveysFromJson(response)

        //then
        Assert.assertThat(surveys.size, `is`(0))
    }

    @Throws(IOException::class)
    private fun givenAEventResponse(fileName: String): String {
        return mFileReader.getStringFromFile(fileName)
    }
}
