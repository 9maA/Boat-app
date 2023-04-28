package com.ny.kystVarsel.aktiviteter

import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.ny.kystVarsel.R
import com.ny.kystVarsel.splash.SplashActivity
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class MainActivityTest {
    @Rule @JvmField var rule: ActivityTestRule<SplashActivity> = ActivityTestRule(SplashActivity::class.java)

    @Test
    fun sjekk_FarerTest() { //Antar at det er farer der ellers failer den
        Thread.sleep(4000)
        onView(withId(R.id.farerKnapp)).perform(click())
        onView(allOf(withId(R.id.fareCard), isDisplayed()))
    }
    @Test
    fun sjekk_LagretFarer() { //Antar at man har lagret en lokasjon tidligere ellers failer den
        Thread.sleep(4000)
        onView(withId(R.id.lagretKnapp)).perform(click())
        onView(allOf(withId(R.id.card_view), isDisplayed()))
    }
    @Test
    fun sjekk_VaerData() {
        Thread.sleep(4000)
        onView(withId(R.id.weatherdataKnapp)).perform(click())
        onView(withId(R.id.vindstyrkeBilde)).check(matches(isDisplayed()))
        Thread.sleep(4000)
        onView(withId(R.id.dataOgWeather)).check(matches(isDisplayed()))
    }
    @Test
    fun klikk_onMaps() {
        Thread.sleep(4000)
        onView(withId(R.id.searchTool)).perform(click())
        onView(withId(R.id.searchTool)).perform(typeText("Akerbrygge"))
        onView(withId(R.id.searchTool)).perform(pressKey(KeyEvent.KEYCODE_ENTER)) //klarte ikke aa faa til denne ettersom man mÃ¥tte skru av animasjoner i device
        Thread.sleep(3000)
        onView(withId(R.id.nav_host_fragment)).perform(click())
        Thread.sleep(4000)

    }
    @Test
    fun klikk_paaMeny_Nodetater(){
        Thread.sleep(4000)
        onView(withId(R.id.nav_host_fragment)).check(matches(isDisplayed()))
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
        onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click())
        onView(withId(R.id.nav_nodetater)).check(matches(isDisplayed()))
        onView(withId(R.id.nav_nodetater)).perform(click())
        Thread.sleep(2000)
    }
    @Test
    fun klikk_paaMeny_LagetAv(){
        Thread.sleep(4000)
        onView(withId(R.id.nav_host_fragment)).check(matches(isDisplayed()))
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
        onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click())
        onView(withId(R.id.nav_lagetAv)).check(matches(isDisplayed()))
        onView(withId(R.id.nav_lagetAv)).perform(click())
        Thread.sleep(2000)
    }
    @Test
    fun klikk_paaMeny_fareSymboler(){
        Thread.sleep(4000)
        onView(withId(R.id.nav_host_fragment)).check(matches(isDisplayed()))
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))
        onView(withContentDescription(R.string.nav_app_bar_open_drawer_description)).perform(click())
        onView(withId(R.id.nav_fareSymboler)).check(matches(isDisplayed()))
        onView(withId(R.id.nav_fareSymboler)).perform(click())
        Thread.sleep(2000)
    }
}
