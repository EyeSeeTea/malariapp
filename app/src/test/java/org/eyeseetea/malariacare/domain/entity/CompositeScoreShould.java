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
    public void create_composite_score_and_add_parent() {
        CompositeScore compositeScore = new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", 1);
        compositeScore.addParent("PARENT_UID");
        Assert.assertTrue(compositeScore.getParentUid().equals("PARENT_UID"));
    }

    @Test
    public void create_composite_score_and_add_child() {
        CompositeScore compositeScore = new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", 1);
        compositeScore.addChild("CHILD_UID");
        Assert.assertTrue(compositeScore.getChildrenUids().get(0).equals("CHILD_UID"));
    }

    @Test
    public void create_composite_score_and_add_children() {
        CompositeScore compositeScore = new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", 1);
        compositeScore.addChildren(
                new ArrayList<>(Arrays.asList("CHILD_UID1", "CHILD_UID2", "CHILD_UID3")));
        Assert.assertTrue(compositeScore.getChildrenUids().get(0).equals("CHILD_UID1"));
        Assert.assertTrue(compositeScore.getChildrenUids().get(1).equals("CHILD_UID2"));
        Assert.assertTrue(compositeScore.getChildrenUids().get(2).equals("CHILD_UID3"));
    }

    @Test
    public void throw_exception_adding_empty_parent() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ParentUid is required and not empty");
        CompositeScore compositeScore = new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", 1);
        compositeScore.addParent("");
    }

    @Test
    public void throw_exception_adding_empty_child() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ChildUid is required an not empty");
        CompositeScore compositeScore = new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", 1);
        compositeScore.addChild("");
    }

    @Test
    public void throw_exception_adding_empty_children() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ChildUid is required an not empty");
        CompositeScore compositeScore = new CompositeScore("UID", "LABEL", "HIERARCHICAL_CODE", 1);
        compositeScore.addChildren(
                new ArrayList<>(Arrays.asList("CHILD_UID1", "", "CHILD_UID3")));
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
