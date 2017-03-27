package uk.co.markormesher.easymaps.scannerapp

import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.support.test.rule.ActivityTestRule
import uk.co.markormesher.easymaps.scannerapp.activities.MainActivity

fun _given(exec: () -> Unit) = exec()
fun _when(exec: () -> Unit) = exec()
fun _then(exec: () -> Unit) = exec()

fun pause(ms: Long) = Thread.sleep(ms)

// given

fun ActivityTestRule<MainActivity>.wifiIsEnabled() {
	(activity.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled = true
	assert((activity.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled, { "Failed to enabled wifi" })
}

fun ActivityTestRule<MainActivity>.locationIsEnabled() {
	val locationManager = (activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
	assert(locationManager.allProviders.any { p -> locationManager.isProviderEnabled(p) }, { "Location must be enabled for testing!" })
}
