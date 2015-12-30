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

package org.eyeseetea.malariacare.database.migrations;

import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.hisp.dhis.android.sdk.persistence.models.Attribute;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;

/**
 * Created by ignac on 30/11/2015.
 */
@Migration(version = 4, databaseName = AppDatabase.NAME)
public class Migration2RestartDBPictureapp extends BaseMigration {

    public static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    public static final String ALTER_TABLE_ADD_COLUMN = "ALTER TABLE %s ADD COLUMN %s %s";

    public Migration2RestartDBPictureapp() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        addColumn(database,Option.class,"path","string");
        addColumn(database,Option.class,"id_optionAttribute","long");
        addColumn(database,Score.class,"value","float");
        addColumn(database,Score.class,"id_tab","long");
        addColumn(database,Survey.class,"id_program","long");
        addColumn(database,Tab.class,"id_program","long");
    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }

    private void addColumn(SQLiteDatabase database, Class model, String columnName,String type){
        ModelAdapter myAdapter = FlowManager.getModelAdapter(model);
        database.execSQL(String.format(ALTER_TABLE_ADD_COLUMN, myAdapter.getTableName(),columnName,type) );
    }

    private void recreateTables(SQLiteDatabase database,Class[] tables){
        for(int i=0;i<tables.length;i++){
            ModelAdapter myAdapter = FlowManager.getModelAdapter(tables[i]);
            database.execSQL(DROP_TABLE_IF_EXISTS + myAdapter.getTableName());
            database.execSQL(myAdapter.getCreationQuery());
        }
    }
}