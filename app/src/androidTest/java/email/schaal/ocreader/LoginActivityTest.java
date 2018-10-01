package email.schaal.ocreader;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import email.schaal.ocreader.api.API;
import email.schaal.ocreader.database.Queries;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockWebServer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by daniel on 13.10.16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {
    @Rule
    public final ActivityTestRule<LoginActivity> activityTestRule = new ActivityTestRule<>(LoginActivity.class);

    private final MockWebServer server = new MockWebServer();
    private final APIDispatcher dispatcher = new APIDispatcher();

    public LoginActivityTest() {
        server.setDispatcher(dispatcher);
    }

    @Before
    public void setUp() throws Exception {
        Queries.resetDatabase();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        Queries.resetDatabase();
        server.shutdown();
    }

    @Test
    public void testInsecureLogin() throws IOException {
        HttpUrl baseUrl = server.url("");

        onView(withId(R.id.url)).perform(clearText(), typeText(baseUrl.toString()));
        onView(withId(R.id.username)).perform(clearText(), typeText("admin"));
        onView(withId(R.id.password)).perform(clearText(), typeText("admin"));
        onView(withId(R.id.sign_in_button)).perform(scrollTo(), click());
        onView(withId(R.id.url)).check(matches(hasErrorText(activityTestRule.getActivity().getString(R.string.error_insecure_connection))));
        onView(withId(R.id.sign_in_button)).perform(scrollTo(), click());
    }

    @Test
    public void testUnknownHost() throws IOException {
        onView(withId(R.id.url)).perform(clearText(), typeText("https://unknown-host"));
        onView(withId(R.id.username)).perform(clearText(), typeText("admin"));
        onView(withId(R.id.password)).perform(clearText(), typeText("admin"));
        onView(withId(R.id.sign_in_button)).perform(scrollTo(), click());
        onView(withId(R.id.url)).check(matches(hasErrorText(activityTestRule.getActivity().getString(R.string.error_unknown_host))));
    }

    @Test
    public void testOutdatedVersion() throws IOException {
        String originalVersion = dispatcher.version;
        dispatcher.version = "8.8.0";

        HttpUrl baseUrl = server.url("");

        onView(withId(R.id.url)).perform(clearText(), typeText(baseUrl.toString()));
        onView(withId(R.id.username)).perform(clearText(), typeText("admin"));
        onView(withId(R.id.password)).perform(clearText(), typeText("admin"));
        onView(withId(R.id.sign_in_button)).perform(scrollTo(), click());
        onView(withId(R.id.url)).check(matches(hasErrorText(activityTestRule.getActivity().getString(R.string.error_insecure_connection))));
        onView(withId(R.id.sign_in_button)).perform(scrollTo(), click());

        onView(withId(R.id.status)).check(matches(withText(activityTestRule.getActivity().getString(R.string.ncnews_too_old, API.MIN_VERSION.toString()))));

        dispatcher.version = originalVersion;
    }
}
