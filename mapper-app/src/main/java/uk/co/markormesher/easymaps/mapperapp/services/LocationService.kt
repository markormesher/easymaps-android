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
import uk.co.markormesher.easymaps.sdk.WifiScanResult
import uk.co.markormesher.easymaps.sdk.WifiScannerService

// TODO: decide what quit button should do

class LocationService: WifiScannerService() {

	companion object {
		val STATE_UPDATED = "services.LocationDetectionService:STATE_UPDATED"
		val STOP_SERVICE = "services.LocationDetectionService:STOP_SERVICE"
	}

	override fun onCreate() {
		super.onCreate()
		registerReceiver(stopServiceReceiver, IntentFilter(STOP_SERVICE))
		initState()
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
		fun getLocationDetectionService() = this@LocationService
	}

	override fun getLocalBinder(): Binder = localBinder

	/* scan results */

	override val scanInterval = 10
	override val statusCheckInterval = 10

	var locationState = LocationState.NONE
	var locationStateHeader = ""
	var locationStateMessage = ""

	override fun onNewScanResults(results: Set<WifiScanResult>) {
		// TODO: actually determine location

		locationState = LocationState.SEARCHING
		locationStateHeader = getString(R.string.location_status_searching_header)
		locationStateMessage = "${results.size} MACs found"

		stateUpdated()
	}

	override fun onStatusChange(newStatus: ScannerStatus) = when (newStatus) {
		ScannerStatus.OKAY -> {
			locationState = LocationState.NONE
			locationStateHeader = getString(R.string.location_status_waiting_header)
			locationStateMessage = getString(R.string.location_status_waiting_message)
			stateUpdated()
		}

		ScannerStatus.NO_LOCATION -> {
			locationState = LocationState.NO_LOCATION
			locationStateHeader = getString(R.string.location_status_no_location_header)
			locationStateMessage = getString(R.string.location_status_no_location_message)
			stateUpdated()
		}

		ScannerStatus.NO_WIFI -> {
			locationState = LocationState.NO_WIFI
			locationStateHeader = getString(R.string.location_status_no_wifi_header)
			locationStateMessage = getString(R.string.location_status_no_wifi_message)
			stateUpdated()
		}
	}

	private fun initState() {
		locationState = LocationState.NONE
		locationStateHeader = getString(R.string.location_status_waiting_header)
		locationStateMessage = getString(R.string.location_status_waiting_message)
		stateUpdated()
	}

	override fun stateUpdated() {
		updateNotification()
		sendBroadcast(Intent(STATE_UPDATED))
	}

	enum class LocationState {
		NONE, SEARCHING, FOUND, NO_WIFI, NO_LOCATION
	}

	/* notification */

	private val NOTIFICATION_ID = 15995
	private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
	private val notificationStyle = NotificationCompat.BigTextStyle()
	private val returnToAppIntent by lazy { PendingIntent.getActivity(this, 0, Intent(this, EntryActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT) }
	private val stopScannerIntent by lazy { PendingIntent.getBroadcast(this, 0, Intent().setAction(STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT) }

	private fun updateNotification() {
		if (running) {
			with(NotificationCompat.Builder(this)) {
				setContentTitle(locationStateHeader)
				setContentText(locationStateMessage)
				setStyle(notificationStyle.bigText(locationStateMessage))
				setSmallIcon(R.drawable.ic_mapper_app_white)
				setContentIntent(returnToAppIntent)
				addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.quit), stopScannerIntent)
				startForeground(NOTIFICATION_ID, build())
			}
		} else {
			stopForeground(true)
			notificationManager.cancel(NOTIFICATION_ID)
		}
	}

}
