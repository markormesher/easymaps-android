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
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.routing.Route
import uk.co.markormesher.easymaps.sdk.WifiScanResult
import uk.co.markormesher.easymaps.sdk.WifiScannerService

class LocationService: WifiScannerService(), AnkoLogger {

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
		fun getService() = this@LocationService
	}

	override fun getLocalBinder(): Binder = localBinder

	/* location scanning */

	override val scanInterval = 10
	override val statusCheckInterval = 10

	var currentLocation: Location? = null
	var locationState = LocationState.NONE
	var locationStateHeader = ""
	var locationStateMessage = ""

	override fun onNewScanResults(results: Set<WifiScanResult>) {
		// TODO: actually determine location
		locationState = LocationState.SEARCHING
		stateUpdated()
	}

	override fun onStatusChange(newStatus: ScannerStatus) {
		locationState = when (newStatus) {
			ScannerStatus.OKAY -> LocationState.NONE
			ScannerStatus.NO_LOCATION -> LocationState.NO_LOCATION
			ScannerStatus.NO_WIFI -> LocationState.NO_WIFI
		}
		stateUpdated()
	}

	private fun initState() {
		locationState = LocationState.NONE
		stateUpdated()
	}

	override fun stateUpdated() {
		when (locationState) {
			LocationService.LocationState.NONE -> {
				locationStateHeader = getString(R.string.location_status_waiting_header)
				locationStateMessage = getString(R.string.location_status_waiting_message)
			}

			LocationService.LocationState.SEARCHING -> {
				locationStateHeader = getString(R.string.location_status_searching_header)
				if (activeRoute == null) {
					locationStateMessage = getString(R.string.location_status_pick_route)
				} else {
					locationStateMessage = getString(R.string.location_status_destination, activeRoute?.locations?.last()?.getDisplayTitle(this) ?: "?")
				}
			}

			LocationService.LocationState.FOUND -> {
				locationStateHeader = getString(R.string.location_status_found_header, currentLocation?.getDisplayTitle(this) ?: "?")
				if (activeRoute == null) {
					locationStateMessage = getString(R.string.location_status_pick_route)
				} else {
					locationStateMessage = getString(R.string.location_status_destination, activeRoute?.locations?.last()?.getDisplayTitle(this) ?: "?")
				}
			}

			LocationService.LocationState.NO_WIFI -> {
				locationStateHeader = getString(R.string.location_status_no_wifi_header)
				locationStateMessage = getString(R.string.location_status_no_wifi_message)
			}

			LocationService.LocationState.NO_LOCATION -> {
				locationStateHeader = getString(R.string.location_status_no_location_header)
				locationStateMessage = getString(R.string.location_status_no_location_message)
			}
		}

		updateNotification()
		sendBroadcast(Intent(STATE_UPDATED))
	}

	enum class LocationState {
		NONE, SEARCHING, FOUND, NO_WIFI, NO_LOCATION
	}

	/* route guidance */

	var activeRoute: Route? = null
		set(value) {
			field = value
			stateUpdated()
		}

	/* notification */

	private val NOTIFICATION_ID = "LocationService".hashCode().and(0xffff)
	private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
	private val notificationStyle = NotificationCompat.BigTextStyle()
	private val returnToAppIntent by lazy { PendingIntent.getActivity(this, 0, Intent(this, EntryActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT) }
	private val stopServiceIntent by lazy { PendingIntent.getBroadcast(this, 0, Intent().setAction(STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT) }

	private fun updateNotification() {
		if (running) {
			with(NotificationCompat.Builder(this)) {
				setContentTitle(locationStateHeader)
				setContentText(locationStateMessage)
				setStyle(notificationStyle.bigText(locationStateMessage))
				setSmallIcon(R.drawable.ic_mapper_app_white)
				setContentIntent(returnToAppIntent)
				if (activeRoute != null) {
					addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.location_service_quit_nav), stopServiceIntent)
				} else {
					addAction(android.R.drawable.ic_menu_directions, getString(R.string.quit), returnToAppIntent)
				}
				startForeground(NOTIFICATION_ID, build())
			}
		} else {
			stopForeground(true)
			notificationManager.cancel(NOTIFICATION_ID)
		}
	}

}
