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

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;

import static org.eyeseetea.malariacare.database.migrations.MigrationUtils.addColumn;
import static org.eyeseetea.malariacare.database.migrations.MigrationUtils.recreateTables;

/**
 * Created by ignac on 30/11/2015.
 */
@Migration(version = 3, database = AppDatabase.class)
public class Migration1RestartDB extends BaseMigration {

    private final static String TAG=".Migration";

    private final static Class SDK_TABLES_TO_UPDATE[] = {
            AttributeFlow.class,
            DataElementFlow.class
    };

    public Migration1RestartDB() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addColumn(database, Survey.class, "creationDate", "integer");
        addColumn(database, Survey.class, "scheduledDate", "integer");
        addColumn(database, Question.class,"output","integer");
        recreateTables(database,SDK_TABLES_TO_UPDATE);
    }

    @Override
    public void onPostMigrate() {
    }

}