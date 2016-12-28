package org.eyeseetea.malariacare;

import android.app.Application;
import android.content.res.AssetManager;
import android.support.test.InstrumentationRegistry;
import android.test.ApplicationTestCase;

import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.utils.PopulateDB;

import java.io.IOException;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }
}