package uk.co.markormesher.easymaps.sdk

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.*
import android.support.annotation.CallSuper
import java.util.*

abstract class WifiScannerService: Service() {

	private val handler by lazy { Handler(Looper.getMainLooper()) }

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

	@CallSuper
	open protected fun start() {
		if (running) return
		running = true

		powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
		wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
		wakeLock?.acquire()

		wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
		registerReceiver(scanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

		scheduleNextScan(true)
		scheduleNextStatusCheck(true)
		stateUpdated()
	}

	@CallSuper
	open protected fun stop() {
		if (!running) return
		running = false

		wakeLock?.release()
		powerManager = null
		wakeLock = null

		unregisterReceiver(scanReceiver)
		wifiManager = null

		clearScanScheduling()
		clearStatusCheckScheduling()
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

	/* scan scheduling */

	open val scanInterval = 20
	val rescanRunnable = Runnable { wifiManager?.startScan() }

	private fun scheduleNextScan(immediate: Boolean = false) {
		clearScanScheduling()
		if (!running) return

		if (immediate) {
			handler.post(rescanRunnable)
		} else {
			handler.postDelayed(rescanRunnable, scanInterval * 1000L)
		}
	}

	private fun clearScanScheduling() {
		handler.removeCallbacks(rescanRunnable)
	}

	/* status monitoring */

	enum class ScannerStatus {
		OKAY, NO_WIFI, NO_LOCATION
	}

	private var status = ScannerStatus.OKAY
	private var newStatus = ScannerStatus.OKAY

	abstract fun onStatusChange(newStatus: ScannerStatus)

	open val statusCheckInterval = 5
	val statusCheckRunnable = Runnable {
		if (!deviceLocationEnabled()) {
			newStatus = ScannerStatus.NO_LOCATION
		} else if (!deviceWifiScanningEnabled()) {
			newStatus = ScannerStatus.NO_WIFI
		} else {
			newStatus = ScannerStatus.OKAY
		}

		if (status != newStatus) {
			onStatusChange(newStatus)
			if (newStatus == ScannerStatus.OKAY) {
				// resume scanning
				scheduleNextScan(true)
			}
		}
		status = newStatus
		scheduleNextStatusCheck()
	}

	private fun scheduleNextStatusCheck(immediate: Boolean = false) {
		clearStatusCheckScheduling()
		if (!running) return

		if (immediate) {
			handler.post(statusCheckRunnable)
		} else {
			handler.postDelayed(statusCheckRunnable, statusCheckInterval * 1000L)
		}
	}

	private fun clearStatusCheckScheduling() {
		handler.removeCallbacks(statusCheckRunnable)
	}
}

data class WifiScanResult(val ssid: String, val mac: String)
