#Instrumented tests


The current version of malariacare has some 'instrumented with Espresso' (a Google library for testing Android) tests that can be run to ensure that nothing has been broken while programming.

##Where to find those tests

Under folder **'malariaapp/app/src/androidTest'**

##How to run them 
Instrumented tests require a physical device (or emulator) running in order to execute the tests.
In fact tests are converted into another **apk** (named app-debug) that is temporary installed into the device in order to manipulate the app under test.

Considering that, these tests can be run from:

 - Command Line:

```bash
./gradlew cC
```

 - Android Studio 
 	- Build Variants > Android Tests
 	- Select folder androidTest
 	- Run as Android Tests


When the tests are running you can see the changes on your device, and the results of those tests are reported under the following path:

```
malariapp/app/build/reports/androidTests/connected/index.html

```


##Understanding tests execution

One of the main pains while programming instrumented tests is dealing with the expected state of the app (activities, sessions, databases and so on).

In order to programme tests with ease it is fundamental its flow, let's consider the following example:

```java

@RunWith(AndroidJUnit4.class)
public class LoginActivityEspressoTest extends MalariaEspressoTest{

    @Rule
    public IntentsTestRule<LoginActivity> mActivityRule = new IntentsTestRule<>(
            LoginActivity.class);


    @BeforeClass
    public static void init(){
        MalariaEspressoTest.init();
    }

    @Before
    public void setup(){
        super.setup();
    }

    @Test
    public void form_views() {
        onView(withId(R.id.user)).check(matches(withText("")));
        onView(withId(R.id.password)).check(matches(withText("")));
    }

    @Test
    public void login_bad_credentials(){
        //GIVEN
        onView(withId(R.id.user)).perform(typeText("bad"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("bad"), closeSoftKeyboard());

        //WHEN
        onView(withId(R.id.email_sign_in_button)).perform(click());

        //THEN
        onView(withId(R.id.user)).check(matches(hasErrorText(res.getString(R.string.login_error_bad_credentials))));
    }
    ...
}
```

The sequence in order is:

    - @BeforeClass
    - For every @Test method in loop:
        - onCreate of the activity under test (configure with @Rule annotation)
        - @Before method(s) (could be many but order is not warranted)
        - @Test, with the proper test (following a GIVEN, WHEN, THEN sequence as a good practice)
        - @After method(s) (could be many but order is not warranted)
    - @AfterClass

##Be aware of the 'state'

Tests must be stateless otherwise they will or fail almost randomly which is awful because it causes false positives.    

Because of that it is really important to ensure that each tests with its preconditions and checks is run under the right context no matter what test has been run before.

    - cleanAll: Deletes database and session info.
        - cleanDB: Deletes each table from the database in the right order.
        - cleanSession: Removes info (user, survey, ..) from the singleton session class.
    - populateData: Load 'csv' files into tables.


#Unit tests

Lately some unit tests has been added to the project.

##Where to find those tests

Under folder **'malariaapp/app/src/test'**

##How to run them

Considering that, these tests can be run from:

 - Command Line:

```bash
./gradlew test --continue
```

 - Android Studio 
    - Build Variants > Test Artifact
    - Select folder(test), class or method
    - Run JUnit test


When the tests are running you can see the changes on your device, and the results of those tests are reported under the following path:

```
malariapp/app/build/reports/tests/debug/index.html

```


