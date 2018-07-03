package org.eyeseetea.malariacare.dataSources;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.eyeseetea.malariacare.data.remote.api.OkHttpClientDataSource;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class OkHttpClientDataSourceShould {

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?program=%s&orgUnit=%s&startDate=%s&endDate=%s&skipPaging=true"
                    + "&fields=event,orgUnit,program,dataValues";
    private Credentials mCredentials;

    @Before
    public void loadCredentials(){
        //// TODO: 26/06/2018  add testing credentials in github
        mCredentials = new Credentials("https://data.psi-mis.org","", "");
    }

    @Test
    public void get_empty_event_list_when_execute_call_filtered_by_2009_start_and_enddate() {
        OkHttpClientDataSource okHttpClientDataSource = new OkHttpClientDataSource(mCredentials);
        String response = null;
        try {
            String apiCall = String.format(DHIS_CHECK_EVENT_API, "ggxhuvMBtxj", "A5HZNrJc2ir",
                    "2009-01-01", "2009-01-01");
            response = okHttpClientDataSource.executeCall(apiCall);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(response, is("{\"events\":[]}"));
    }
}
