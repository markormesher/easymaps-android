package uk.co.markormesher.easymaps.scannerapp.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import uk.co.markormesher.easymaps.scannerapp.*
import uk.co.markormesher.easymaps.scannerapp.activities.EntryActivity
import uk.co.markormesher.easymaps.sdk.WifiScanner
import uk.co.markormesher.easymaps.sdk.getLongPref
import uk.co.markormesher.easymaps.sdk.setLongPref

// TODO: kill service after a long period of scanning

class ScannerService : Service() {

	private val localBinder by lazy { LocalBinder() }

	var running = false
	var lifetimeDataPoints = 0L
	var sessionDataPoints = 0L
	private val LIFETIME_COUNT_KEY = "lifetime_data_points"

	override fun onCreate() {
		super.onCreate()

		lifetimeDataPoints = getLongPref(LIFETIME_COUNT_KEY, 0)

		registerReceiver(toggleScanReceiver, IntentFilter(getString(R.string.intent_toggle_scan)))
		registerReceiver(stopScanReceiver, IntentFilter(getString(R.string.intent_stop_scan)))
		registerReceiver(scanResultReceiver, IntentFilter(WifiScanner.INTENT_SCAN_RESULTS_UPDATED))
	}

	override fun onDestroy() {
		super.onDestroy()

		unregisterReceiver(toggleScanReceiver)
		unregisterReceiver(stopScanReceiver)
		unregisterReceiver(scanResultReceiver)
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		super.onTaskRemoved(rootIntent)
		if (!running) stopSelf()
	}

	override fun onBind(intent: Intent?): IBinder = localBinder

	inner class LocalBinder : Binder() {
		fun getScannerService(): ScannerService = this@ScannerService
	}

	private val toggleScanReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			toggle()
		}
	}

	fun toggle() {
		if (running) {
			stop()
		} else {
			start()
		}
	}

	private fun start() {
		if (running) return
		running = true
		sessionDataPoints = 0L
		WifiScanner.start(this, if (isHighFrequencyMode()) HIGH_FREQ_SCAN_INTERVAL else SCAN_INTERVAL)
		stateUpdated()
	}

	private val stopScanReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			stop()
		}
	}

	private fun stop() {
		if (!running) return
		running = false
		WifiScanner.stop(this)
		closeScanResultsFile()
		stateUpdated()
	}

	private val scanResultReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (!running) return
			val latestResults = WifiScanner.scanResults.filter { it.ssid.contains(SSID_FILTER, true) }
			lifetimeDataPoints += latestResults.size
			sessionDataPoints += latestResults.size
			writeScanResultsToFile(latestResults)
			stateUpdated()
		}
	}

	private fun stateUpdated() {
		setLongPref(LIFETIME_COUNT_KEY, lifetimeDataPoints)
		updateNotification()
		sendStateUpdatedBroadcast()
	}

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
				setSmallIcon(R.mipmap.ic_launcher) // TODO: app icon
				setContentIntent(returnToAppIntent)
				addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.scan_toggle_stop), stopScannerIntent)
			}
			startForeground(NOTIFICATION_ID, nBuilder.build())
		} else {
			stopForeground(true)
			notificationManager.cancel(NOTIFICATION_ID)
		}
	}

	private fun sendStateUpdatedBroadcast() {
		sendBroadcast(Intent(getString(R.string.intent_scan_status_updated)))
	}

}