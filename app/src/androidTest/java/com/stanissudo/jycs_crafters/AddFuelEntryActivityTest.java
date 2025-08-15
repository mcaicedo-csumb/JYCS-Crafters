package com.stanissudo.jycs_crafters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * project: JYCS-Crafters
 * file: AddFuelEntryActivityTest.java
 * @author Ysabelle Kim
 * created: 8/15/2025 - 3:34 AM
 * Explanation: Instrumented tests for AddFuelEntryActivity.java.
 */
@RunWith(AndroidJUnit4.class)
public class AddFuelEntryActivityTest extends TestCase {
    Intent intent;
    Context context;
    FuelEntry fuelEntry;
    private static final String EXTRA_TEST_ID = "com.stanissudo.jycs_crafters.EXTRA_TEST_ID";

    @Rule
    public ActivityScenarioRule<AddFuelEntryActivity> activityRule = new ActivityScenarioRule<>(AddFuelEntryActivity.class);

    public void setUp() throws Exception {
        super.setUp();
        fuelEntry = new FuelEntry();
        context = ApplicationProvider.getApplicationContext();
        System.out.println("=== AddFuelEntryActivityTest Setup Complete===");
    }

    public void tearDown() throws Exception {
        super.tearDown();
        System.out.println("=== AddFuelEntryActivityTest Teardown Complete===");
    }

    @Test
    public void testUserInput() {
        onView(withId(R.id.odometerInputEditText)).perform(typeText("30000"));
        onView(withId(R.id.gasVolumeInputEditText)).perform(typeText("12"));
        onView(withId(R.id.pricePerGallonInputEditText)).perform(typeText("5"));
        onView(withText("Save!")).perform(click());
        assertNotNull(fuelEntry.getLogID());
        double d = fuelEntry.getGallons() * fuelEntry.getPricePerGallon();
        assertEquals(60.00, d);
    }

    @Test
    public void testAddFuelIntentFactory() {
        assertNotNull(context);
        intent = new Intent(context, AddFuelEntryActivity.class);
        assertNotNull(intent);
    }

    @Test
    public void testEditIntentFactory() {
        boolean isEditTest = false;
        int fuelEntryIDTest = 1;
        assertNotNull(context);
        intent = new Intent(context, AddFuelEntryActivity.class).putExtra("EXTRA_TEST_ID", fuelEntryIDTest);
        Intent intent2 = new Intent(context, AddFuelEntryActivity.class);
        assertNotNull(intent);
        assertNotNull(intent2);
        isEditTest = fuelEntryIDTest > 0;
        assertTrue(isEditTest);
        assertNotEquals(intent, intent2);
    }

    @Test
    public void testOnCreate() {
        assertNotNull(context);
        assertEquals("com.stanissudo.jycs_crafters", context.getPackageName());
    }
}