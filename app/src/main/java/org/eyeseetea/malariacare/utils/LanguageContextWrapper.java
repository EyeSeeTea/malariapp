package org.eyeseetea.malariacare.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import java.util.Locale;


public class LanguageContextWrapper extends android.content.ContextWrapper {
    public LanguageContextWrapper(Context base) {
        super(base);
    }

    public static LanguageContextWrapper wrap(Context context, String languageCode) {
        Resources res = context.getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocale(new Locale(languageCode));

            LocaleList localeList = new LocaleList(new Locale(languageCode));
            LocaleList.setDefault(localeList);
            conf.setLocales(localeList);

            context = context.createConfigurationContext(conf);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(new Locale(languageCode));
            context = context.createConfigurationContext(conf);

        } else {
            conf.locale = new Locale(languageCode);
            res.updateConfiguration(conf, res.getDisplayMetrics());
        }
        return new LanguageContextWrapper(context);
    }
}
