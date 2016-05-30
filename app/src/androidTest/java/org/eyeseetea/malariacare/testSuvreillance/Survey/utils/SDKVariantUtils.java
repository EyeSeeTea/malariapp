package org.eyeseetea.malariacare.testSuvreillance.Survey.utils;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.NoMatchingViewException;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.layout.dashboard.controllers.AssessModuleController;
import org.eyeseetea.malariacare.test.utils.ElapsedTimeIdlingResource;
import org.hamcrest.Matchers;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by idelcano on 17/05/2016.
 */
public class SDKVariantUtils {
    private static final String TAG = "TestingSuvreillance";

    public static void selectSurvey(int idxOrgUnit, int idxProgram, int seconds) {
        //when: click on assess tab + plus button

        IdlingResource idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);

        try {
            onView(withText(android.R.string.ok)).perform(click());
        }catch(NoMatchingViewException e){
            e.printStackTrace();
            Log.d(TAG, "the enduser licence was accepted.");
        }
        finally {
            Espresso.unregisterIdlingResources(idlingResource);
        }

        onView(withId(R.id.plusButton)).perform(click());


        idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);

        onView(withId(R.id.org_unit)).perform(click());

        Espresso.unregisterIdlingResources(idlingResource);

        onData(is(instanceOf(OrgUnit.class))).atPosition(idxOrgUnit).perform(click());


        idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);

        onView(withId(R.id.program)).perform(click());

        Espresso.unregisterIdlingResources(idlingResource);
        onData(is(instanceOf(Program.class))).atPosition(idxProgram).perform(click());
        onView(withText(R.string.create_info_ok)).perform(click());
    }



    public static void fillSurvey(int numQuestions, String optionValue,int seconds) {
        //when: answer NO to every question
        //Wait for fragment load data from SurveyService
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);

        onView(withTagValue(Matchers.is((Object) AssessModuleController.getSimpleName()))).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        for (int i = 0; i < numQuestions; i++) {
            try {
                idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
                Espresso.registerIdlingResources(idlingResource);
                onData(is(instanceOf(Question.class)))
                        .inAdapterView(withId(R.id.listView))
                        .atPosition(i)
                        .onChildView(withId(R.id.answer)).onChildView(withText(optionValue))
                        .perform(click());
            } catch (NoMatchingViewException e) {
                Log.e(TAG,"Exception selecting option value " + optionValue);
            }
            finally{
                Espresso.unregisterIdlingResources(idlingResource);
            }

        }
        //then: back + confirm
        Espresso.pressBack();
        idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withText(android.R.string.ok)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public static void fillNegativeSurvey(int seconds) {
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withId(R.string.option + 1)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public static void fillNoTestSurvey(int seconds) {
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withId(R.string.option + 2)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public static void fillCompleteSurvey(int seconds) {

        //First option positive
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withId(R.string.option + 0)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        //Second option woman
        idlingResource = new ElapsedTimeIdlingResource(seconds *2 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withId(R.string.option + 1)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        //third option 22
        idlingResource = new ElapsedTimeIdlingResource(seconds *2 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withId(R.id.dynamic_positiveInt_edit)).perform(typeText("22"));
        onView(withId(R.id.dynamic_positiveInt_btn)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        //First option (mixed)
        idlingResource = new ElapsedTimeIdlingResource(seconds *2 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withId(R.string.option + 0)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        //Option five, Referral
        idlingResource = new ElapsedTimeIdlingResource(seconds *2 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withId(R.string.option + 5)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        //Option four, Other
        idlingResource = new ElapsedTimeIdlingResource(seconds *2 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withId(R.string.option + 3)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        //Phone
        idlingResource = new ElapsedTimeIdlingResource(seconds *2 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        //bad number
        onView(withId(R.id.dynamic_phone_edit)).perform(typeText("22"));
        onView(withId(R.id.dynamic_phone_btn)).perform(click());
        //correct number
        onView(withId(R.id.dynamic_phone_edit)).perform(clearText());
        onView(withId(R.id.dynamic_phone_edit)).perform(typeText("0123222222"));
        onView(withId(R.id.dynamic_phone_btn)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public static void clickSend(int seconds) {
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(seconds * 1000);
        Espresso.registerIdlingResources(idlingResource);
        onView(withText(R.string.send)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);
    }
}
