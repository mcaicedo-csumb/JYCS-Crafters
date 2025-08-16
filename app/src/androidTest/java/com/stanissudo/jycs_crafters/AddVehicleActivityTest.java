package com.stanissudo.jycs_crafters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLOutput;

/**
 * @author Ysabelle Kim
 * created: 8/14/2025 - 2:53 PM
 * Explanation: Instrumented tests for AddVehicleActivity.
 * project: JYCS-Crafters
 * file: AddVehicleActivityTest.java
 */
@RunWith(AndroidJUnit4.class)
public class AddVehicleActivityTest extends TestCase {
    Intent intent;
    Context context;
    Vehicle vehicle;
    private static final String EXTRA_TEST_ID = "com.stanissudo.jycs_crafters.EXTRA_TEST_ID";

    @Rule
    public ActivityScenarioRule<AddVehicleActivity> activityRule = new ActivityScenarioRule<>(AddVehicleActivity.class);

    public void setUp() throws Exception {
        super.setUp();
        System.out.println("=== AddVehicleActivityTest Setup Complete===");
    }

    public void tearDown() throws Exception {
        super.tearDown();
        System.out.println("=== AddVehicleActivityTest Teardown Complete===");
    }

    @Test
    public void testOnCreate() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertNotNull(context);
        assertEquals("com.stanissudo.jycs_crafters", context.getPackageName());
    }

    @Test
    public void testClick() {
        onView(withId(R.id.vehicleNameEditText)).perform(typeText("car"));
        onView(withId(R.id.vehicleMakeEditText)).perform(typeText("carmake"));
        onView(withId(R.id.vehicleModelEditText)).perform(typeText("carmodel"));
        onView(withId(R.id.vehicleYearEditText)).perform(typeText("1999"));
        onView(withId(R.id.vehicleSaveButton)).perform(click());
    }

    @Test
    public void testUserInput() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        vehicle = new Vehicle();
        vehicle.setVehicleID(1);
        vehicle.setYear(1999);
        onView(withId(R.id.vehicleNameEditText)).perform(typeText("car"));
        onView(withId(R.id.vehicleMakeEditText)).perform(typeText("carmake"));
        onView(withId(R.id.vehicleModelEditText)).perform(typeText("carmodel"));
        onView(withId(R.id.vehicleYearEditText)).perform(typeText("1999"));
        onView(withId(R.id.vehicleSaveButton)).perform(click());
        assertNotNull(vehicle.getVehicleID());
        assertTrue(vehicle.getYear() >= 1885);
    }

    @Test
    public void testVehicleIntentFactory() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertNotNull(context);
        intent = new Intent(context, AddVehicleActivity.class);
        assertNotNull(intent);
    }

    @Test
    public void testEditVehicleIntentFactory() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        boolean isEditTest = false;
        int vehicleIDTest = 1;
        assertNotNull(context);
        intent = new Intent(context, AddVehicleActivity.class).putExtra("EXTRA_TEST_ID", vehicleIDTest);
        Intent intent2 = new Intent(context, AddVehicleActivity.class);
        assertNotNull(intent);
        assertNotNull(intent2);
        isEditTest = vehicleIDTest > 0;
        assertTrue(isEditTest);
        assertNotEquals(intent, intent2);
    }
}