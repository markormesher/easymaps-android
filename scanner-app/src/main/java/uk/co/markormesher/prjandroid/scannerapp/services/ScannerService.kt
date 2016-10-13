package uk.co.markormesher.prjandroid.scannerapp.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.NotificationCompat
import uk.co.markormesher.prjandroid.scannerapp.R
import uk.co.markormesher.prjandroid.scannerapp.activities.EntryActivity
import uk.co.markormesher.prjandroid.sdk.getLongPref
import uk.co.markormesher.prjandroid.sdk.setLongPref

// TODO: actually scan wifi, rather than incrementing a pointless counter
// TODO: save wifi scans to disk
// TODO: kill service after a long period of scanning

class ScannerService : Service() {

	private val localBinder by lazy { LocalBinder() }

	var scannerRunning = false
	var lifetimeDataPoints = 0L
	private val LIFETIME_COUNT_KEY = "lifetime_data_points"

	override fun onCreate() {
		super.onCreate()

		lifetimeDataPoints = getLongPref(LIFETIME_COUNT_KEY, 0)

		registerReceiver(toggleScanReceiver, IntentFilter(getString(R.string.intent_toggle_scan)))
		registerReceiver(stopScanReceiver, IntentFilter(getString(R.string.intent_stop_scan)))
	}

	override fun onDestroy() {
		super.onDestroy()

		unregisterReceiver(toggleScanReceiver)
		unregisterReceiver(stopScanReceiver)
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		super.onTaskRemoved(rootIntent)
		if (!scannerRunning) stopSelf()
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

	private val stopScanReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			stop()
		}
	}

	private fun start() {
		if (scannerRunning) return
		scannerRunning = true

		dummyFunction()
		stateUpdated()
	}

	private fun stop() {
		if (!scannerRunning) return
		scannerRunning = false

		dummyHandler.removeCallbacks(dummyRunnable)
		stateUpdated()
	}

	fun toggle() {
		if (scannerRunning) {
			stop()
		} else {
			start()
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
		if (scannerRunning) {
			val message = "$lifetimeDataPoints data point${if (lifetimeDataPoints == 1L) "" else "s"} so far!"
			val nBuilder = NotificationCompat.Builder(this)
			with(nBuilder) {
				setContentTitle("PRJ Scanner is running")
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

	// dummy code

	private val dummyHandler by lazy { Handler(Looper.getMainLooper()) }
	private val dummyRunnable by lazy { Runnable { dummyFunction() } }
	private fun dummyFunction() {
		++lifetimeDataPoints
		stateUpdated()
		if (scannerRunning) dummyHandler.postDelayed(dummyRunnable, 1000)
	}

}