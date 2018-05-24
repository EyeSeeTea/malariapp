package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;

public class CompositeScoreShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_composite_score_with_mandatory_fields() {
        CompositeScore compositeScore = new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", 1);

        Assert.assertNotNull(compositeScore);
        Assert.assertTrue(compositeScore.getUid().equals("UID"));
        Assert.assertTrue(compositeScore.getLabel().equals("LABEL"));
        Assert.assertTrue(compositeScore.getHierarchicalCode().equals("HIERARCHICAL_CODE"));
        Assert.assertTrue(compositeScore.getOrderPos() == 1);
    }

    @Test
    public void create_composite_score_with_mandatory_fields_and_null_not_mandatory() {
        CompositeScore compositeScore = new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", 1,
                null, null);

        Assert.assertNotNull(compositeScore);
        Assert.assertTrue(compositeScore.getUid().equals("UID"));
        Assert.assertTrue(compositeScore.getLabel().equals("LABEL"));
        Assert.assertTrue(compositeScore.getHierarchicalCode().equals("HIERARCHICAL_CODE"));
        Assert.assertTrue(compositeScore.getOrderPos() == 1);
    }

    @Test
    public void create_composite_score_with_all_fields() {
        CompositeScore compositeScore = new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", 1,
                "PARENT_UID", new ArrayList<>(Arrays.asList("CHILD_UID_1", "CHILD_UID_2")));

        Assert.assertNotNull(compositeScore);
        Assert.assertTrue(compositeScore.getUid().equals("UID"));
        Assert.assertTrue(compositeScore.getLabel().equals("LABEL"));
        Assert.assertTrue(compositeScore.getHierarchicalCode().equals("HIERARCHICAL_CODE"));
        Assert.assertTrue(compositeScore.getOrderPos() == 1);
        Assert.assertTrue(compositeScore.getParentUid().equals("PARENT_UID"));
        Assert.assertTrue(compositeScore.getChildrenUids().get(0).equals("CHILD_UID_1"));
        Assert.assertTrue(compositeScore.getChildrenUids().get(1).equals("CHILD_UID_2"));
    }

    @Test
    public void throw_exception_when_create_composite_with_null_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Uid is required");

        new CompositeScore(null, "LABEL", "HIERARCHICAL_CODE", 1);
    }

    @Test
    public void throw_exception_when_create_composite_with_null_label(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Label is required");

        new CompositeScore("UID", null, "HIERARCHICAL_CODE", 1);
    }

    @Test
    public void throw_exception_when_create_composite_with_null_hierarchicalCode(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("HierarchicalCode is required");

        new CompositeScore("UID", "LABEL", null, 1);
    }

    @Test
    public void throw_exception_when_create_composite_with_lower_than_0_orderPos(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("OrderPos has to be higher than 0");

        new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", -1);
    }





}
