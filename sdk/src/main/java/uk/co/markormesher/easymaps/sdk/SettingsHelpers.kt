package uk.co.markormesher.easymaps.sdk

import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings

@Suppress("DEPRECATION")

fun Context.deviceLocationEnabled(): Boolean {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
		val providers = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
		return !providers.isEmpty() && providers.contains(LocationManager.GPS_PROVIDER)
	} else {
		val status = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE)
		return status != Settings.Secure.LOCATION_MODE_OFF
	}
}

fun Context.deviceWifiScanningEnabled(): Boolean {
	val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
		return wifiManager.isWifiEnabled
	} else {
		return wifiManager.isWifiEnabled || wifiManager.isScanAlwaysAvailable
	}
}
