/*
 * Copyright (c) 2016.
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
import org.eyeseetea.malariacare.database.model.Value;

import static org.eyeseetea.malariacare.database.migrations.MigrationUtils.addColumn;

/**
 * Created by idelcano on 23/02/2016.
 */
@Migration(version = 6, database = AppDatabase.class)
public class Migration6AddConflictValue extends BaseMigration {

    public Migration6AddConflictValue() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addColumn(database, Value.class, "conflict", "boolean");
    }

    @Override
    public void onPostMigrate() {
    }

}
