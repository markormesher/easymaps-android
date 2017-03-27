package uk.co.markormesher.easymaps.scannerapp.activities

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.markormesher.easymaps.scannerapp.*

@RunWith(AndroidJUnit4::class)
class MainActivityTests {

	@Rule @JvmField
	val mainActivity = ActivityTestRule<MainActivity>(MainActivity::class.java)

	@Test
	fun errorMessageShouldNotBeShownWhenWifiIsEnabled() {
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
			mainActivity.wifiWarningShouldNotBeShown()
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
