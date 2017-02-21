package uk.co.markormesher.easymaps.mapperapp.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.support.v4.app.NotificationCompat
import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.activities.EntryActivity
import uk.co.markormesher.easymaps.mapperapp.routing.Route
import uk.co.markormesher.easymaps.sdk.WifiScanResult
import uk.co.markormesher.easymaps.sdk.WifiScannerService

class LocationAndRouteGuidanceService: WifiScannerService(), AnkoLogger {

	companion object {
		val STATE_UPDATED = "services.LocationDetectionService:STATE_UPDATED"
		val START_SERVICE = "services.LocationDetectionService:START_SERVICE"
		val STOP_SERVICE = "services.LocationDetectionService:STOP_SERVICE"
		val SERVICE_STOPPED = "services.LocationDetectionService:SERVICE_STOPPED"
	}

	override fun onCreate() {
		super.onCreate()
		registerReceiver(startServiceReceiver, IntentFilter(START_SERVICE))
		registerReceiver(stopServiceReceiver, IntentFilter(STOP_SERVICE))
		initState()
	}

	override fun onDestroy() {
		super.onDestroy()
		unregisterReceiver(startServiceReceiver)
		unregisterReceiver(stopServiceReceiver)
	}

	private val startServiceReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) = start()
	}

	private val stopServiceReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			stop()
			stopSelf()
		}
	}

	override fun start() {
		if (!running) {
			super.start()
			initState()
		}
	}

	override fun stop() {
		super.stop()
		initState()
		sendBroadcast(Intent(SERVICE_STOPPED))
	}

	private val localBinder by lazy { LocalBinder() }

	inner class LocalBinder: Binder() {
		fun getLocationAndRouteGuidanceService() = this@LocationAndRouteGuidanceService
	}

	override fun getLocalBinder(): Binder = localBinder

	/* location scanning */

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

	/* route guidance */

	var activeRoute: Route? = null
		get() = field
		set(value) {
			field = value
			// TODO: route guidance mode
		}

	/* notification */

	private val NOTIFICATION_ID = "LocationAndRouteGuidanceService".hashCode().and(0xffff)
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
