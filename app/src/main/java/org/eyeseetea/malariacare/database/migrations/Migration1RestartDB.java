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
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.eyeseetea.malariacare.BaseActivity;
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
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.hisp.dhis.android.sdk.persistence.models.Attribute;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;

import java.io.IOException;

/**
 * Created by ignac on 30/11/2015.
 */
@Migration(version = 3, databaseName = AppDatabase.NAME)
public class Migration1RestartDB extends BaseMigration {

    private final static String TAG=".Migration";

    private final static Class APP_TABLES_TO_UPDATE[] = {
            Value.class,
            Score.class,
            Survey.class,
            OrgUnit.class,
            OrgUnitLevel.class,
            User.class,
            QuestionOption.class,
            Match.class,
            QuestionRelation.class,
            Question.class,
            CompositeScore.class,
            Option.class,
            Answer.class,
            Header.class,
            Tab.class,
            TabGroup.class,
            Program.class
    };

    private final static Class SDK_TABLES_TO_UPDATE[] = {
            Attribute.class,
            DataElement.class
    };

    public static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    public static final String ALTER_TABLE_ADD_COLUMN = "ALTER TABLE %s ADD COLUMN %s %s";

    public Migration1RestartDB() {
        super();
    }

    public void onPreMigrate() {
//        FlowManager.getDatabase(AppDatabase.NAME).reset(PreferencesState.getInstance().getContext());
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        addColumn(database,Question.class,"output","integer");
        addColumn(database, Survey.class, "creationDate", "integer");
        addColumn(database, Survey.class, "scheduledDate", "integer");
        recreateTables(database,SDK_TABLES_TO_UPDATE);
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