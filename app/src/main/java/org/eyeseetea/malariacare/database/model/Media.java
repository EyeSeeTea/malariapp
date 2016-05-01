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

package org.eyeseetea.malariacare.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

@Table(databaseName = AppDatabase.NAME)
public class Media extends BaseModel{

     // Values to identify image/video on media_type column in the DB
    public static final int MEDIA_TYPE_IMAGE = 0;

    public static final int MEDIA_TYPE_VIDEO = 1;

    @Column
    @PrimaryKey(autoincrement = true)
    long id_media;

    @Column
    int media_type;

    @Column
    String resource_url;

    @Column
    Long id_question;

    /**
     * Reference to the question
     */
    Question question;

    public Media(){}

    public Media(int media_type, String resource_url, Question question){
        this.media_type = media_type;
        this.resource_url = resource_url;
        this.setQuestion(question);
    }

    public Question getQuestion(){
        if(question==null){
            if(id_question==null) return null;
            question = new Select()
                    .from(Question.class)
                    .where(Condition.column(Question$Table.ID_QUESTION)
                        .is(id_question)).querySingle();
        }
        return question;
    }

    public int getMedia_type() {
        return media_type;
    }

    public void setMedia_type(int media_type) {
        this.media_type = media_type;
    }

    public String getResource_url() {
        return resource_url;
    }

    public void setResource_url(String resource_url) {
        this.resource_url = resource_url;
    }

    public void setQuestion(Question question){
        this.question = question;
        this.id_question = (question!=null)?question.getId_question():null;
    }

    public void setQuestion(Long id_question){
        this.id_question = id_question;
        this.question = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        if (id_media != media.id_media) return false;
        if (getMedia_type() != media.getMedia_type()) return false;
        if (getResource_url() != null ? !getResource_url().equals(media.getResource_url()) : media.getResource_url() != null)
            return false;
        return id_question.equals(media.id_question);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_media ^ (id_media >>> 32));
        result = 31 * result + getMedia_type();
        result = 31 * result + (getResource_url() != null ? getResource_url().hashCode() : 0);
        result = 31 * result + id_question.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Media{" +
                "id_media=" + id_media +
                ", media_type=" + media_type +
                ", resource_url='" + resource_url + '\'' +
                ", id_question=" + id_question +
                '}';
    }
}
