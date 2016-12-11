package uk.co.markormesher.easymaps.sdk

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.*
import java.util.*

abstract class WifiScannerService: Service() {

	/* service bindings */

	override fun onTaskRemoved(rootIntent: Intent?) {
		super.onTaskRemoved(rootIntent)
		if (!running) stopSelf()
	}

	abstract fun getLocalBinder(): Binder

	override fun onBind(intent: Intent?): IBinder = getLocalBinder()

	abstract fun stateUpdated()

	/* scanning */

	var powerManager: PowerManager? = null
	var wakeLock: PowerManager.WakeLock? = null
	val WAKE_LOCK_TAG = "uk.co.markormesher.easymaps.sdk.WifiScannerService.wake_lock"

	var wifiManager: WifiManager? = null
	var running = false

	protected fun start() {
		if (running) return
		running = true

		powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
		wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
		wakeLock?.acquire()

		wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
		registerReceiver(scanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

		scheduleNextScan()

		stateUpdated()
	}

	protected fun stop() {
		if (!running) return
		running = false

		wakeLock?.release()
		powerManager = null
		wakeLock = null

		unregisterReceiver(scanReceiver)
		wifiManager = null

		clearScheduling()

		stateUpdated()
	}

	protected fun toggle() {
		if (running) {
			stop()
		} else {
			start()
		}
	}

	val scanReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val rawScanResults = wifiManager?.scanResults ?: return
			val scanResults = HashSet<WifiScanResult>()
			scanResults.addAll(rawScanResults.map { rawResult -> WifiScanResult(rawResult.SSID, rawResult.BSSID) })
			onNewScanResults(scanResults)
			scheduleNextScan()
		}
	}

	abstract fun onNewScanResults(results: Set<WifiScanResult>)

	/* scheduling */

	val handler by lazy { Handler(Looper.getMainLooper()) }
	val rescanRunnable by lazy { Runnable { wifiManager?.startScan() } }

	open val scanInterval = 20

	private fun scheduleNextScan() {
		clearScheduling()
		if (running) handler.postDelayed(rescanRunnable, scanInterval * 1000L)
	}

	private fun clearScheduling() {
		handler.removeCallbacks(rescanRunnable)
	}

}

data class WifiScanResult(val ssid: String, val mac: String)
