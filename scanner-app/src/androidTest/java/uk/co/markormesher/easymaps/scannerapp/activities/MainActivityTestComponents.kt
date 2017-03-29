package uk.co.markormesher.easymaps.scannerapp.activities

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.not
import uk.co.markormesher.easymaps.scannerapp.R

// given

fun ActivityTestRule<MainActivity>.isRunning() {
}

fun ActivityTestRule<MainActivity>.scannerServiceIsNotRunning() {
	activity.scannerService?.stop()
}

// when

fun ActivityTestRule<MainActivity>.startStopScanningButtonIsPressed() {
	onView(withId(R.id.toggle_scanning_button)).perform(click())
}

// then

fun ActivityTestRule<MainActivity>.settingWarningShouldNotBeShown() {
	onView(withId(R.id.setting_warning))
			.check(matches(not(isDisplayed())))
}

fun ActivityTestRule<MainActivity>.scannerServiceShouldBeRunning() {
	assert(activity.scannerService != null)
	assert(activity.scannerService?.running ?: false)
}


fun ActivityTestRule<MainActivity>.scannerServiceShouldNotBeRunning() {
	assert(activity.scannerService != null)
	assert(!(activity.scannerService?.running ?: false))
}
