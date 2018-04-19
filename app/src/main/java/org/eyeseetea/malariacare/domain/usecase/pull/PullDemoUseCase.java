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

package org.eyeseetea.malariacare.domain.usecase.pull;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullDemoController;
import org.eyeseetea.malariacare.domain.boundary.IPullController;
import org.eyeseetea.malariacare.domain.boundary.IPullDemoController;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;

public class PullDemoUseCase {

    public interface Callback {
        void onComplete();

        void onPullError();
    }

    IPullDemoController mPullController;

    public PullDemoUseCase(IPullDemoController pullController) {
        mPullController = pullController;
    }

    public void execute(final Callback callback) {
        mPullController.pull(new PullDemoController.IPullDemoControllerCallback() {

            @Override
            public void onComplete() {
                callback.onComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                callback.onPullError();
            }
        });
    }
}
