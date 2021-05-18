package com.vaultionizer.vaultapp.ui.auth.login

import android.provider.Settings.Global.getString
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.vaultionizer.vaultapp.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.rule.ActivityTestRule
import com.vaultionizer.vaultapp.ui.auth.AuthenticationActivity

import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Matchers.not

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not


@RunWith(AndroidJUnit4::class)
@LargeTest

class LoginFragmentTest{

    @get:Rule
    var activityTestRule: ActivityTestRule<AuthenticationActivity>
            = ActivityTestRule(AuthenticationActivity::class.java)

    @Before
    fun skipWelcomeScreen() {
        onView(withId(R.id.button_lets_go))
            .perform(click())
    }

    @Test
    fun loginSuccess() {
        onView(withId(R.id.input_host))
            .perform(typeText("api.vault.jatsqi.com"))
        onView(withId(R.id.input_username))
            .perform(typeText("username"))
        onView(withId(R.id.input_password))
            .perform(typeText("password000+++PASSWORD"))
        onView(withId(R.id.login))
            .perform(click())
        assertTrue(activityTestRule.activity.isFinishing)
    }

    @Test
    fun loginWrongUsername() {
        onView(withId(R.id.input_host))
            .perform(typeText("api.vault.jatsqi.com"))
        onView(withId(R.id.input_username))
            .perform(typeText("username"))
        onView(withId(R.id.input_password))
            .perform(typeText("password000+++PASSWORD--Invalid"))
        onView(withId(R.id.login))
            .perform(click())
        onView(withText(R.string.login_error_invalid_credentials)).inRoot(
            withDecorView(
                not(
                    activityTestRule.activity.window.decorView
                )

        )).check(matches(isDisplayed()))
    }

    @Test
    fun loginWrongPassword() {
        onView(withId(R.id.input_host))
            .perform(typeText("api.vault.jatsqi.com"))
        onView(withId(R.id.input_username))
            .perform(typeText("username--Invalid"))
        onView(withId(R.id.input_password))
            .perform(typeText("password000+++PASSWORD"))
        onView(withId(R.id.login))
            .perform(click())
        assertTrue(activityTestRule.activity.isFinishing)
    }
    
    @Test
    fun loginWrongHostname() {
        onView(withId(R.id.input_host))
            .perform(typeText("api.vault.jatsqi.af"))
        onView(withId(R.id.input_username))
            .perform(click())
        Thread.sleep(3000)
        val hostLayout = activityTestRule.activity.findViewById<TextInputLayout>(R.id.input_host_layout)
        assertNotNull(hostLayout.error)
        hostLayout.error?.let { assertFalse(it.isEmpty()) }


    }









}