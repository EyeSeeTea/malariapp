package org.eyeseetea.malariacare.views.strategy;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.strategies.ISettingsActivityStrategy;

public class SettingsActivityStrategy implements ISettingsActivityStrategy {

    @Override
    public void afterSetupPreferencesScreen(@NonNull PreferenceScreen preferenceScreen) {

        hideFontCustomisationOption(preferenceScreen);

    }

    private void hideFontCustomisationOption(@NonNull PreferenceScreen preferenceScreen) {
        Context context = preferenceScreen.getContext();

        Preference customizeFonts = preferenceScreen.findPreference(
                context.getString(R.string.customize_fonts));

        Preference fontSizes = preferenceScreen.findPreference(
                context.getString(R.string.font_sizes));

        preferenceScreen.removePreference(customizeFonts);
        preferenceScreen.removePreference(fontSizes);
    }

}
