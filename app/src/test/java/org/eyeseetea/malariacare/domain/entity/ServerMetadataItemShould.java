package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ServerMetadataItemShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_a_server_metadata_item_with_mandatory_fields(){
        ServerMetadataItem serverMetadata = new ServerMetadataItem("code",
                "uid");
        Assert.assertNotNull(serverMetadata);
        Assert.assertTrue(serverMetadata.getCode().equals("code"));
        Assert.assertTrue(serverMetadata.getUId().equals("uid"));
    }

    @Test
    public void throw_exception_when_create_a_server_metadata_with_null_code(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Code is required");
        new ServerMetadataItem(null,
                "UId");
    }


    @Test
    public void throw_exception_when_create_a_server_metadata_item_with_null_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("UId is required");
        new ServerMetadataItem("code",
                null);
    }
}
