package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eyeseetea.malariacare.utils.Constants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MediaShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void throw_exception_if_media_type_is_not_correct() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("mediaType is not correct");
        new Media(-1, "testFilename");
    }

    @Test
    public void return_correct_media_type_in_getMediaType() {
        int mediaType = Constants.MEDIA_TYPE_VIDEO;
        Media media = new Media(mediaType, "testFilename");
        assertThat(media.getMediaType(), is(mediaType));
    }

    @Test
    public void throw_exception_if_null_filename() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("filename is required");
        new Media(Constants.MEDIA_TYPE_VIDEO, null);
    }

    @Test
    public void throw_exception_if_empty_filename() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("filename is required");
        new Media(Constants.MEDIA_TYPE_VIDEO, "");
    }

    @Test
    public void return_correct_filename_in_getFilename() {
        String filename = "testFilename";
        Media media = new Media(Constants.MEDIA_TYPE_VIDEO, filename);
        assertThat(media.getFilename(), is(filename));
    }

    @Test
    public void return_isVideo_true_if_file_type_is_video() {
        Media media = new Media(Constants.MEDIA_TYPE_VIDEO, "testFilename");
        assertThat(media.isVideo(), is(true));
    }

    @Test
    public void return_isVideo_false_if_file_type_is_not_video() {
        Media media = new Media(Constants.MEDIA_TYPE_IMAGE, "testFilename");
        assertThat(media.isVideo(), is(false));
    }

    @Test
    public void return_isPicture_true_if_file_type_is_picture() {
        Media media = new Media(Constants.MEDIA_TYPE_IMAGE, "testFilename");
        assertThat(media.isPicture(), is(true));
    }

    @Test
    public void return_isPicture_false_if_file_type_is_not_picture() {
        Media media = new Media(Constants.MEDIA_TYPE_VIDEO, "testFilename");
        assertThat(media.isPicture(), is(false));
    }


}
