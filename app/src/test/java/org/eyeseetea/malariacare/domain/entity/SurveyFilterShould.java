package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.usecase.SurveyFilter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

public class SurveyFilterShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createFilterGetQuarantine(){
        SurveyFilter surveyFilter = SurveyFilter.getQuarantineSurveys();

        Assert.assertNotNull(surveyFilter);
        Assert.assertTrue(surveyFilter.isQuarantineSurvey());
        Assert.assertTrue(surveyFilter.getReadPolicy().equals(ReadPolicy.CACHE));
    }

    @Test
    public void createCheckQuarantineOnServerFilter(){
        List<String> uids = new ArrayList<>();
        uids.add("uid1");
        uids.add("uid2");
        SurveyFilter surveyFilter = SurveyFilter.getSurveysUidsOnServer(uids);

        Assert.assertNotNull(surveyFilter);
        Assert.assertTrue(surveyFilter.getUids().equals(uids));
        Assert.assertTrue(surveyFilter.getReadPolicy().equals(ReadPolicy.NETWORK_NO_CACHE));
    }
}
