package uk.co.markormesher.easymaps.sdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import java.util.*

object WifiScanner {

	const val INTENT_SCAN_RESULTS_UPDATED = "uk.co.markormesher.easymaps.sdk.INTENT_SCAN_RESULTS_UPDATED"

	var interval: Long = 0
	var wifiManager: WifiManager? = null
	var running = false

	val scanResults by lazy { HashSet<WifiScanResult>() }
	val handler by lazy { Handler(Looper.getMainLooper()) }
	var lastScan = -1L

	val scanReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val rawScanResults = wifiManager?.scanResults ?: return
			scanResults.clear()
			rawScanResults.forEach { scanResults.add(WifiScanResult(it.SSID, it.BSSID)) }

			lastScan = System.currentTimeMillis()
			resetScheduler()
			if (context != null) sendUpdateBroadcast(context)
		}
	}

	val rescanRunnable = Runnable { wifiManager?.startScan() }

	fun start(context: Context, interval: Long) {
		if (running) return
		running = true

		this.interval = interval

		context.registerReceiver(scanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
		wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
		wifiManager?.startScan()
		resetScheduler()
	}

	fun stop(context: Context) {
		if (!running) return
		running = false

		context.unregisterReceiver(scanReceiver)
		wifiManager = null
	}

	private fun sendUpdateBroadcast(context: Context) {
		context.sendBroadcast(Intent(INTENT_SCAN_RESULTS_UPDATED))
	}

	private fun resetScheduler() {
		handler.removeCallbacks(rescanRunnable)
		if (running) handler.postDelayed(rescanRunnable, interval)
	}

}

data class WifiScanResult(val ssid: String, val mac: String)