package org.eyeseetea.malariacare.data.remote.sdk.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eyeseetea.malariacare.common.ResourcesFileReader;
import org.eyeseetea.malariacare.domain.entity.CompositeScore;
import org.eyeseetea.malariacare.domain.entity.Option;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.Question;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SurveyMapperShould {

    private ResourcesFileReader mFileReader = new ResourcesFileReader();

    @Test
    public void mapToSurveys() throws IOException {
        List<Survey> expectedSurveys = givenAnExpectedSurveys();

        List<Event> events = givenAnEventsDownloaded("events.json");
        ServerMetadata serverMetadata = givenAServerMetadata();
        List<CompositeScore> compositeScores = givenACompositeScores();
        List<Question> questions = givenAQuestions();
        List<Option> options = givenAnOptions();
        List<OrgUnit> orgUnits = givenAnOrgUnits();

        SurveyMapper surveyMapper =
                new SurveyMapper(serverMetadata, orgUnits, compositeScores, questions, options);

        List<Survey> mappedSurveys = surveyMapper.mapSurveys(events);

        assertThat(mappedSurveys, is(expectedSurveys));
    }

    @Test
    public void doesNotMapToSurveysIfNotExistsUserCDE() throws IOException {
        List<Event> events = givenAnEventsDownloaded("events_without_user_cde.json");
        ServerMetadata serverMetadata = givenAServerMetadata();
        List<CompositeScore> compositeScores = givenACompositeScores();
        List<Question> questions = givenAQuestions();
        List<Option> options = givenAnOptions();
        List<OrgUnit> orgUnits = givenAnOrgUnits();

        SurveyMapper surveyMapper =
                new SurveyMapper(serverMetadata, orgUnits, compositeScores, questions, options);

        List<Survey> mappedSurveys = surveyMapper.mapSurveys(events);

        assertThat(mappedSurveys.size(), is(0));
    }

    private ServerMetadata givenAServerMetadata() throws IOException {
        String stringJson = mFileReader.getStringFromFile("server_metadata.json");
        Gson gson = mFileReader.createGson();

        return gson.fromJson(stringJson, ServerMetadata.class);
    }

    private List<Survey> givenAnExpectedSurveys() throws IOException {
        String stringJson = mFileReader.getStringFromFile("surveys.json");

        Gson gson = mFileReader.createGson();

        Type listType = new TypeToken<ArrayList<Survey>>(){}.getType();
        return gson.fromJson(stringJson, listType);
    }


    private List<Event> givenAnEventsDownloaded(String fileName) throws IOException {
        String stringJson = mFileReader.getStringFromFile(fileName);

        Gson gson = mFileReader.createGson();

        Type listType = new TypeToken<ArrayList<Event>>(){}.getType();
        return gson.fromJson(stringJson, listType);
    }

    private List<Question> givenAQuestions() throws IOException {
        String stringJson = mFileReader.getStringFromFile("questions.json");
        Gson gson = mFileReader.createGson();

        Type listType = new TypeToken<ArrayList<Question>>(){}.getType();
        return gson.fromJson(stringJson, listType);
    }

    private List<Option> givenAnOptions() throws IOException {
        String stringJson = mFileReader.getStringFromFile("options.json");
        Gson gson = mFileReader.createGson();

        Type listType = new TypeToken<ArrayList<Option>>(){}.getType();
        return gson.fromJson(stringJson, listType);
    }

    private List<OrgUnit> givenAnOrgUnits() throws IOException {
        String stringJson = mFileReader.getStringFromFile("orgUnits.json");
        Gson gson = mFileReader.createGson();

        Type listType = new TypeToken<ArrayList<OrgUnit>>(){}.getType();
        return gson.fromJson(stringJson, listType);
    }

    private List<CompositeScore> givenACompositeScores() throws IOException {
        String stringJson = mFileReader.getStringFromFile("composite_scores.json");

        Gson gson = mFileReader.createGson();

        Type listType = new TypeToken<ArrayList<CompositeScore>>() {
        }.getType();
        return gson.fromJson(stringJson, listType);
    }
}
