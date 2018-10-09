package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Settings;

public interface ISettingsRepository {
    interface ISettingsRepositoryCallback<Settings> {
        void onComplete(Settings settings);
    }
    Settings getSettings();
}
