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

package org.eyeseetea.malariacare.database.migration;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.model.OptionAttribute;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;


/**
 * Created by ignac on 30/11/2015.
 */
@Migration(version = 2, databaseName = AppDatabase.NAME)
public class Migration1PictureaApp extends BaseMigration {

    public Migration1PictureaApp() {
        super();
    }

    public void onPreMigrate() {
        FlowManager.getDatabase(AppDatabase.NAME).reset(PreferencesState.getInstance().getContext());
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        try {
            ModelAdapter myAdapter = FlowManager.getModelAdapter(OptionAttribute.class);
            database.execSQL("DROP TABLE IF EXISTS " + myAdapter.getTableName());
            database.execSQL(myAdapter.getCreationQuery());

            myAdapter = FlowManager.getModelAdapter(Survey.class);
            database.execSQL("DROP TABLE IF EXISTS " + myAdapter.getTableName());
            database.execSQL(myAdapter.getCreationQuery());
            ;
            myAdapter = FlowManager.getModelAdapter(Tab.class);
            database.execSQL("DROP TABLE IF EXISTS " + myAdapter.getTableName());
            database.execSQL(myAdapter.getCreationQuery());

            myAdapter = FlowManager.getModelAdapter(Value.class);
            database.execSQL("DROP TABLE IF EXISTS " + myAdapter.getTableName());
            database.execSQL(myAdapter.getCreationQuery());
        } catch (Exception e) {
            Log.i(".migrate", "Error");
            e.printStackTrace();
        }
    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }
}