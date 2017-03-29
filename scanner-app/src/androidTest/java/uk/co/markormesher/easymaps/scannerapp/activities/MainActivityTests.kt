package uk.co.markormesher.easymaps.scannerapp.activities

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.markormesher.easymaps.scannerapp.*

@RunWith(AndroidJUnit4::class)
class MainActivityTests {

	/*
	MANUAL TESTS

	Given: the main activity is running
	When: WiFi scanning is disabled
	Then: a warning message should be shown

	Given: the main activity is running
	When: location services are disabled
	Then: a warning message should be shown

	Given: the main activity is running
	When: the "Did I Win?" menu option is selected
	Then: an API call should be issued

	Given: the main activity is running
	When: the "Display User ID" menu option is selected
	Then: a dialog should be shown with the user ID

	Given: the main activity is running
	When: the "Display User ID" menu option is selected
	Then: the user ID should be copied to the clipboard

	Given: the main activity is running
	When: the "Send Debug Report" menu option is selected
	Then: an email intent should be issued

	Given: the main activity is running
	When: the "Send Debug Report" menu option is selected
	Then: the debug report should be copied to the clipbord

	Given: the main activity is running
	When: the "Contact Mark" menu option is selected
	Then: an email intent should be issued

	Given: the main activity is running
	When: the "Change Scan Interval" menu option is selected
	Then: a dialog should be shown

	Given: the main activity is running
	When: the "Withdraw Data" menu option is selected
	Then: a confirmation message should be shown

	Given: the main activity is running
	Given: the "Change Scan Interval" dialog is shown
	When: a new interval is selected
	Then: the interval should be saved

	Given: the main activity is running
	Given: the "Withdraw Data" confirmation dialog is shown
	When: the "Confirm" button is selected
	Then: an API call should be issued

	Given: the main activity is running
	Given: the "Withdraw Data" confirmation dialog is shown
	When: the "Cancel" button is selected
	Then: no API call should be issued
	 */

	@Rule @JvmField
	val mainActivity = ActivityTestRule<MainActivity>(MainActivity::class.java)

	@Test
	fun warningMessagesShouldNotBeShownWhenWifiAndLocationEnabled() {
		_given {
			mainActivity.isRunning()
			mainActivity.wifiIsEnabled()
			mainActivity.locationIsEnabled()
			mainActivity.scannerServiceIsNotRunning()
		}
		_then {
			pause(10000)
		}
		_then {
			mainActivity.settingWarningShouldNotBeShown()
		}
	}

	@Test
	fun scanningButtonShouldStartScanning() {
		_given {
			mainActivity.isRunning()
			mainActivity.wifiIsEnabled()
			mainActivity.locationIsEnabled()
			mainActivity.scannerServiceIsNotRunning()
		}
		_when {
			mainActivity.startStopScanningButtonIsPressed()
		}
		_then {
			mainActivity.scannerServiceShouldBeRunning()
		}
	}

	@Test
	fun scanningButtonTwiceShouldStopScanning() {
		_given {
			mainActivity.isRunning()
			mainActivity.wifiIsEnabled()
			mainActivity.locationIsEnabled()
			mainActivity.scannerServiceIsNotRunning()
		}
		_when {
			mainActivity.startStopScanningButtonIsPressed()
			pause(1000)
			mainActivity.startStopScanningButtonIsPressed()
		}
		_then {
			mainActivity.scannerServiceShouldNotBeRunning()
		}
	}

}
