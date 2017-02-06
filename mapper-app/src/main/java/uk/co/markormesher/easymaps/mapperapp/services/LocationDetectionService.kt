package uk.co.markormesher.easymaps.mapperapp.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.support.v4.app.NotificationCompat
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.activities.EntryActivity
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.sdk.WifiScanResult
import uk.co.markormesher.easymaps.sdk.WifiScannerService

// TODO: check for wifi/location services

class LocationDetectionService: WifiScannerService() {

	companion object {
		val STATE_UPDATED = "services.LocationDetectionService:STATE_UPDATED"
		val STOP_SERVICE = "services.LocationDetectionService:STOP_SERVICE"
	}

	override fun onCreate() {
		super.onCreate()
		registerReceiver(stopServiceReceiver, IntentFilter(STOP_SERVICE))
		start()
	}

	override fun onDestroy() {
		super.onDestroy()
		unregisterReceiver(stopServiceReceiver)
	}

	private val stopServiceReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) = stop()
	}

	private val localBinder by lazy { LocalBinder() }

	inner class LocalBinder: Binder() {
		fun getLocationDetectionService() = this@LocationDetectionService
	}

	override fun getLocalBinder(): Binder = localBinder

	/* scan results */

	var locationState = LocationState.NONE
	var currentLocation: Location? = null
	var locationDetail = ""

	override val scanInterval = 10

	override fun onNewScanResults(results: Set<WifiScanResult>) {
		// TODO: actually determine location

		locationState = LocationState.SEARCHING
		locationDetail = "${results.size} MACs found"

		stateUpdated()
	}

	override fun stateUpdated() {
		updateNotification()
		sendBroadcast(Intent(STATE_UPDATED))
	}

	enum class LocationState {
		NONE, SEARCHING, FOUND, NO_WIFI_OR_LOCATION
	}

	/* notification */

	// TODO: replace with real text

	private val NOTIFICATION_ID = 15995
	private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
	private val notificationStyle = NotificationCompat.BigTextStyle()
	private val returnToAppIntent by lazy { PendingIntent.getActivity(this, 0, Intent(this, EntryActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT) }
	private val stopScannerIntent by lazy { PendingIntent.getBroadcast(this, 0, Intent().setAction(STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT) }

	private fun updateNotification() {
		if (running) {
			val message = "Determining location..."
			val nBuilder = NotificationCompat.Builder(this)
			with(nBuilder) {
				setContentTitle("EasyMaps")
				setContentText(message)
				setStyle(notificationStyle.bigText(message))
				setSmallIcon(R.drawable.ic_mapper_app_white)
				setContentIntent(returnToAppIntent)
				addAction(android.R.drawable.ic_menu_close_clear_cancel, "Quit", stopScannerIntent)
			}
			startForeground(NOTIFICATION_ID, nBuilder.build())
		} else {
			stopForeground(true)
			notificationManager.cancel(NOTIFICATION_ID)
		}
	}

}
