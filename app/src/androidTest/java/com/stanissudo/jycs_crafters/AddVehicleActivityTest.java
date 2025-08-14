package com.stanissudo.jycs_crafters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Ysabelle Kim
 * created: 8/14/2025 - 2:53 PM
 * Explanation: Unit tests for AddVehicleActivity.
 * project: JYCS-Crafters
 * file: AddVehicleActivityTest.java
 */
@RunWith(AndroidJUnit4.class)
public class AddVehicleActivityTest extends TestCase {

    @Rule
    public ActivityScenarioRule<AddVehicleActivity> activityRule = new ActivityScenarioRule<>(AddVehicleActivity.class);

    @Test
    public void listGoesOverTheFold() {
        onView(withText("Add Vehicle")).check(matches(isDisplayed()));
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testVehicleIntentFactory() {
    }

    public void testEditVehicleIntentFactory() {
    }

    public void testOnCreate() {
    }

    @Test
    public void testGetDrawerLayout() {
//        DrawerLayout drawerLayout;
//        assertNotNull(drawerLayout);
    }

    public void testGetNavigationView() {
        //assertNotNull(navView);
    }

    public void testGetToolbar() {
        //assertNotNull(binding.toolbar);
    }
}