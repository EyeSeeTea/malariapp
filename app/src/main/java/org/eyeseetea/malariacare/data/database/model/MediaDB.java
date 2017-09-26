/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.List;

@Table(database = AppDatabase.class, name = "Media")
public class MediaDB extends BaseModel {

    /**
     * Null media value to express that a question has NO media without using querys
     */
    private static MediaDB noMedia = new MediaDB(Constants.NO_MEDIA_ID, null, null);

    @Column
    @PrimaryKey(autoincrement = true)
    long id_media;

    @Column
    int media_type;

    @Column
    String resource_url;

    @Column
    Long id_question_fk;

    @Column
    String filename;


    /**
     * Reference to the question
     */
    QuestionDB question;

    public MediaDB() {
    }

    public MediaDB(int media_type, String resource_url, QuestionDB question) {
        this.media_type = media_type;
        this.resource_url = resource_url;
        this.filename = null;
        this.setQuestion(question);
    }

    public QuestionDB getQuestion(){
        if(question==null){
            if(id_question_fk==null) return null;
            question = new Select()
                    .from(QuestionDB.class)
                    .where(QuestionDB_Table.id_question
                        .is(id_question_fk)).querySingle();
        }
        return question;
    }

    public int getMediaType() {
        return media_type;
    }

    public void setMediaType(int media_type) {
        this.media_type = media_type;
    }

    public String getResourceUrl() {
        return resource_url;
    }

    public void setResourceUrl(String resource_url) {
        this.resource_url = resource_url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public static List<MediaDB> getAllNotInLocal() {
        return new Select().
                from(MediaDB.class).
                where(MediaDB_Table.filename.isNull()).
                and(MediaDB_Table.resource_url.isNotNull()).
                orderBy(MediaDB_Table.id_media, true).
                queryList();
    }

    public static List<MediaDB> getAllInLocal() {
        return new Select().
                from(MediaDB.class).
                where(MediaDB_Table.filename.isNotNull()).
                and(MediaDB_Table.resource_url.isNotNull()).
                orderBy(MediaDB_Table.id_media, true).
                queryList();
    }

    public static List<MediaDB> getAllMedia() {
        return new Select().
                from(MediaDB.class).
                orderBy(MediaDB_Table.id_media, true).
                queryList();
    }
    public static List<MediaDB> findByQuestion(QuestionDB question) {
        if (question == null) {
            return new ArrayList<>();
        }

        return new Select().
                from(MediaDB.class).
                where(MediaDB_Table.id_question_fk.eq(question.id_question)).
                orderBy(MediaDB_Table.id_media, true).
                queryList();
    }

    public void setQuestion(QuestionDB question) {
        this.question = question;
        this.id_question_fk = (question!=null)?question.getId_question():null;
    }

    public void setQuestion(Long id_question){
        this.id_question_fk = id_question;
        this.question = null;
    }

    /**
     * Since questions are saved in batch the referenced question has no id_question when the setter
     * is called.
     * This method allows the media to refresh de id_question according to the question referenced
     * once it is persisted.
     */
    public void updateQuestion() {
        //No question nothing to update
        if (this.question == null) {
            return;
        }

        this.id_question_fk = question.getId_question();
    }

    /**
     * Returns a media that holds a reference to the same resource with an already downloaded copy
     * of the file.
     */
    public MediaDB findLocalCopy() {
        return new Select().from(MediaDB.class)
                .where(MediaDB_Table.filename.isNotNull())
                .and(MediaDB_Table.id_media.isNot(this.id_media))
                .and(MediaDB_Table.resource_url.is(this.resource_url))
                .querySingle();
    }

    /**
     * Returns if is a picture
     */
    public boolean isPicture() {
        return (media_type == Constants.MEDIA_TYPE_IMAGE);
    }
    /**
     * Returns if is video
     */
    public boolean isVideo() {
        return (media_type == Constants.MEDIA_TYPE_VIDEO);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaDB media = (MediaDB) o;

        if (id_media != media.id_media) return false;
        if (media_type != media.media_type) return false;
        if (filename != media.filename) return false;
        if (resource_url != null ? !resource_url.equals(media.resource_url)
                : media.resource_url != null) {
            return false;
        }
        return id_question_fk != null ? id_question_fk.equals(media.id_question_fk) : media.id_question_fk == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id_media ^ (id_media >>> 32));
        result = 31 * result + media_type;
        result = 31 * result + (resource_url != null ? resource_url.hashCode() : 0);
        result = 31 * result + (id_question_fk != null ? id_question_fk.hashCode() : 0);
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Media{" +
                "id_media=" + id_media +
                ", media_type=" + media_type +
                ", resource_url='" + resource_url + '\'' +
                ", id_question=" + id_question_fk +
                ", filename=" + filename +
                '}';
    }
}
