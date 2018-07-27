package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ServerMetadataShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServerMetadataItem nextAssessment;
    private ServerMetadataItem creationDate;
    private ServerMetadataItem completionDate;
    private ServerMetadataItem uploadDate;
    private ServerMetadataItem uploadBy;
    private ServerMetadataItem overallScore;
    private ServerMetadataItem mainScoreClass;
    private ServerMetadataItem mainScoreA;
    private ServerMetadataItem mainScoreB;
    private ServerMetadataItem mainScoreC;
    private ServerMetadataItem forwardOrder;
    private ServerMetadataItem pushDevice;
    private ServerMetadataItem overallProductivity;
    private ServerMetadataItem provider;
    private ServerMetadataItem gaps;
    private ServerMetadataItem planAction;
    private ServerMetadataItem action1;
    private ServerMetadataItem action2;

    @Before
    public void setUp(){
        nextAssessment = new ServerMetadataItem("code", "uid");
        creationDate = new ServerMetadataItem("code", "uid");
        completionDate = new ServerMetadataItem("code", "uid");
        uploadDate = new ServerMetadataItem("code", "uid");
        uploadBy = new ServerMetadataItem("code", "uid");
        overallScore = new ServerMetadataItem("code", "uid");
        mainScoreClass = new ServerMetadataItem("code", "uid");
        mainScoreA = new ServerMetadataItem("code", "uid");
        mainScoreB = new ServerMetadataItem("code", "uid");
        mainScoreC = new ServerMetadataItem("code", "uid");
        forwardOrder = new ServerMetadataItem("code", "uid");
        pushDevice = new ServerMetadataItem("code", "uid");
        overallProductivity = new ServerMetadataItem("code", "uid");
        provider = new ServerMetadataItem("code", "uid");
        gaps = new ServerMetadataItem("code", "uid");
        planAction = new ServerMetadataItem("code", "uid");
        action1 = new ServerMetadataItem("code", "uid");
        action2 = new ServerMetadataItem("code", "uid");
    }

    @Test
    public void create_a_server_metadata_with_mandatory_fields(){
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice, overallProductivity,
                provider, gaps, planAction, action1, action2);
        Assert.assertNotNull(serverMetadata);
        Assert.assertTrue(serverMetadata.getNextAssessment().equals(nextAssessment));
        Assert.assertTrue(serverMetadata.getCreationDate().equals(creationDate));
        Assert.assertTrue(serverMetadata.getCompletionDate().equals(completionDate));
        Assert.assertTrue(serverMetadata.getUploadDate().equals(uploadDate));
        Assert.assertTrue(serverMetadata.getUploadBy().equals(uploadBy));
        Assert.assertTrue(serverMetadata.getOverallScore().equals(overallScore));
        Assert.assertTrue(serverMetadata.getMainScoreClass().equals(mainScoreClass));
        Assert.assertTrue(serverMetadata.getMainScoreA().equals(mainScoreA));
        Assert.assertTrue(serverMetadata.getMainScoreB().equals(mainScoreB));
        Assert.assertTrue(serverMetadata.getMainScoreC().equals(mainScoreC));
        Assert.assertTrue(serverMetadata.getForwardOrder().equals(forwardOrder));
        Assert.assertTrue(serverMetadata.getPushDevice().equals(pushDevice));
        Assert.assertTrue(serverMetadata.getOverallProductivity().equals(overallProductivity));
        Assert.assertTrue(serverMetadata.getProvider().equals(provider));
        Assert.assertTrue(serverMetadata.getGaps().equals(gaps));
        Assert.assertTrue(serverMetadata.getPlanAction().equals(planAction));
        Assert.assertTrue(serverMetadata.getAction1().equals(action1));
        Assert.assertTrue(serverMetadata.getAction2().equals(action2));
    }

    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_next_assessment(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("nextAssessment is required");
        ServerMetadata serverMetadata = new ServerMetadata(null,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder,
                pushDevice, overallProductivity,
                provider, gaps, planAction, action1, action2);
    }


    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_creation_date(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("creationDate is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                null, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }


    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_completion_date(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("completionDate is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, null,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB
                ,mainScoreC, forwardOrder,
                pushDevice, overallProductivity,
                provider, gaps, planAction, action1, action2);
    }


    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_upload_date(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("uploadDate is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                null, uploadBy,overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }


    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_upload_by(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("uploadBy is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, null, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }


    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_overallScore(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("overallScore is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, null,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_main_score_class(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("mainScoreClass is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                null, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_main_score_a(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("mainScoreA is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, null, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_main_score_b(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("mainScoreB is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, null,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_main_score_c(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("mainScoreC is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                null, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_forward_order(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("forwardOrder is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, null, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_push_device(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("pushDevice is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, null,
                overallProductivity,
                provider, gaps, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_overall_productivity(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("overallProductivity is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                null,
                provider, gaps, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_provider(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("provider is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                null, gaps, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_gaps(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("gaps is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, null, planAction, action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_planAction(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("planAction is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, null , action1, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_action1(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("action1 is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, null, action2);
    }
    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_action2(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("action2 is required");
        ServerMetadata serverMetadata = new ServerMetadata(nextAssessment,
                creationDate, completionDate,
                uploadDate, uploadBy, overallScore,
                mainScoreClass, mainScoreA, mainScoreB,
                mainScoreC, forwardOrder, pushDevice,
                overallProductivity,
                provider, gaps, planAction, action1, null);
    }
}
