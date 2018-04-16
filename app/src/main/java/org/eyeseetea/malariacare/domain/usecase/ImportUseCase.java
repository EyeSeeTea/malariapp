/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.domain.usecase;

import android.net.Uri;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullDemoController;
import org.eyeseetea.malariacare.domain.boundary.IImportController;
import org.eyeseetea.malariacare.domain.boundary.IPullDemoController;

public class ImportUseCase {

    Uri uri;

    public interface Callback {
        void onComplete();

        void onImportError();
    }

    IImportController mImportController;

    public ImportUseCase(Uri uri, IImportController importController) {
        mImportController = importController;
        this.uri = uri;
    }

    public void execute(final Callback callback) {
        mImportController.importDB(uri, new IImportController.IImportControllerCallback() {
            @Override
            public void onComplete() {
                callback.onComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                callback.onImportError();
            }
        });
    }
}
