package uk.co.markormesher.easymaps.scannerapp.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.support.v4.app.NotificationCompat
import uk.co.markormesher.easymaps.scannerapp.*
import uk.co.markormesher.easymaps.scannerapp.activities.EntryActivity
import uk.co.markormesher.easymaps.sdk.WifiScanResult
import uk.co.markormesher.easymaps.sdk.WifiScannerService
import uk.co.markormesher.easymaps.sdk.getLongPref
import uk.co.markormesher.easymaps.sdk.setLongPref

class ScannerService: WifiScannerService() {

	override fun onCreate() {
		super.onCreate()

		initResults()

		registerReceiver(toggleScanReceiver, IntentFilter(getString(R.string.intent_toggle_scan)))
		registerReceiver(stopScanReceiver, IntentFilter(getString(R.string.intent_stop_scan)))
	}

	override fun onDestroy() {
		super.onDestroy()

		unregisterReceiver(toggleScanReceiver)
		unregisterReceiver(stopScanReceiver)
	}

	override fun stop() {
		super.stop()
		closeScanResultsFile()
	}

	private val toggleScanReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			toggle()
		}
	}

	private val stopScanReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			stop()
		}
	}

	private val localBinder by lazy { LocalBinder() }

	inner class LocalBinder: Binder() {
		fun getScannerService() = this@ScannerService
	}

	override fun getLocalBinder(): Binder = localBinder

	/* scan results */

	private val LIFETIME_COUNT_KEY = "lifetime_data_points"
	var lifetimeDataPoints = 0L
	var sessionDataPoints = 0L

	override val scanInterval: Int
		get() = getScanInterval()

	fun initResults() {
		lifetimeDataPoints = getLongPref(LIFETIME_COUNT_KEY, 0)
	}

	override fun onNewScanResults(results: Set<WifiScanResult>) {
		if (!running) return

		val filteredResults = results.filter { it.ssid.contains(SSID_FILTER, true) }
		lifetimeDataPoints += filteredResults.size
		sessionDataPoints += filteredResults.size
		writeScanResultsToFile(filteredResults)
		stateUpdated()
	}

	override fun stateUpdated() {
		setLongPref(LIFETIME_COUNT_KEY, lifetimeDataPoints)
		updateNotification()
		sendBroadcast(Intent(getString(R.string.intent_scan_status_updated)))
	}

	/* notification */

	private val NOTIFICATION_ID = 15995
	private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
	private val notificationStyle = NotificationCompat.BigTextStyle()
	private val returnToAppIntent by lazy { PendingIntent.getActivity(this, 0, Intent(this, EntryActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT) }
	private val stopScannerIntent by lazy { PendingIntent.getBroadcast(this, 0, Intent().setAction(getString(R.string.intent_stop_scan)), PendingIntent.FLAG_UPDATE_CURRENT) }

	private fun updateNotification() {
		if (running) {
			val message = getString(R.string.scanning_notification_message, sessionDataPoints, if (sessionDataPoints == 1L) "" else "s")
			val nBuilder = NotificationCompat.Builder(this)
			with(nBuilder) {
				setContentTitle(getString(R.string.scanning_notification_title))
				setContentText(message)
				setStyle(notificationStyle.bigText(message))
				setSmallIcon(R.drawable.ic_scanner_app_white)
				setContentIntent(returnToAppIntent)
				addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.scan_toggle_stop), stopScannerIntent)
			}
			startForeground(NOTIFICATION_ID, nBuilder.build())
		} else {
			stopForeground(true)
			notificationManager.cancel(NOTIFICATION_ID)
		}
	}

}
