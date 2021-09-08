package com.example.mobilestorage

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobilestorage.fragments.home.HomeFragment
import androidx.test.espresso.contrib.RecyclerViewActions
import com.example.mobilestorage.adapter.ProductsAdapter

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class MainMenuTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainMenu::class.java)

    @Test
    fun launchFragmentAndVerifyUI() {
        // use launchInContainer to launch the fragment with UI
        launchFragmentInContainer<HomeFragment>()

        // now use espresso to look for the fragment's text view and verify it is displayed

    }

}