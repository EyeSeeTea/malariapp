package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Settings;

public interface ISettingsRepository {
    interface ISettingsRepositoryCallback {
        void onComplete(Settings settings);
    }
    Settings getSettings();
}
