package uk.co.markormesher.easymaps.mapperapp.activities

import android.app.FragmentTransaction
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.fragments.DestinationChooserFragment
import uk.co.markormesher.easymaps.mapperapp.fragments.RouteChooserFragment
import uk.co.markormesher.easymaps.mapperapp.fragments.RouteGuidanceFragment
import uk.co.markormesher.easymaps.mapperapp.services.LocationAndRouteGuidanceService
import uk.co.markormesher.easymaps.mapperapp.ui.LocationStatusBar
import uk.co.markormesher.easymaps.sdk.BaseActivity

class MainActivity: BaseActivity(), ServiceConnection, AnkoLogger {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		initDestinationChooser()
	}

	override fun onStart() {
		super.onStart()
		startService()
	}

	override fun onResume() {
		super.onResume()

		if (OfflineDatabase.isPopulated(this)) {
			OfflineDatabase.startBackgroundUpdate(this)
		} else {
			startActivity(Intent(this, OfflineDataDownloadActivity::class.java))
			finish()
		}

		updateLocationStatusFromService()

		registerLocationServiceReceivers()
		registerNavigationReceivers()
	}

	override fun onPause() {
		super.onPause()
		unregisterLocationServiceReceivers()
		unregisterNavigationReceivers()
	}

	override fun onStop() {
		super.onStop()
		stopService()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		// reinstate navigation receivers before passing this to fragments,
		// in case it triggers a navigation event
		registerNavigationReceivers()
		super.onActivityResult(requestCode, resultCode, data)
	}

	/* service binding */

	var locationAndRouteGuidanceService: LocationAndRouteGuidanceService? = null

	private fun startService() {
		val serviceIntent = Intent(this, LocationAndRouteGuidanceService::class.java)
		bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
		startService(serviceIntent)
		sendBroadcast(Intent(LocationAndRouteGuidanceService.START_SERVICE))
	}

	private fun stopService() {
		sendBroadcast(Intent(LocationAndRouteGuidanceService.STOP_SERVICE))
	}

	private fun registerLocationServiceReceivers() {
		registerReceiver(locationStateUpdatedReceiver, IntentFilter(LocationAndRouteGuidanceService.STATE_UPDATED))
		registerReceiver(locationServiceStoppedReceiver, IntentFilter(LocationAndRouteGuidanceService.SERVICE_STOPPED))
	}

	private fun unregisterLocationServiceReceivers() {
		unregisterReceiver(locationStateUpdatedReceiver)
		unregisterReceiver(locationServiceStoppedReceiver)
	}

	override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
		if (binder is LocationAndRouteGuidanceService.LocalBinder) {
			locationAndRouteGuidanceService = binder.getLocationAndRouteGuidanceService()
			sendBroadcast(Intent(LocationAndRouteGuidanceService.START_SERVICE))
		}
	}

	override fun onServiceDisconnected(name: ComponentName?) {
		locationAndRouteGuidanceService = null
	}

	private val locationStateUpdatedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			updateLocationStatusFromService()
		}
	}

	private val locationServiceStoppedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			finish()
		}
	}

	private fun updateLocationStatusFromService() {
		status_bar.setHeading(locationAndRouteGuidanceService?.locationStateHeader ?: "")
		status_bar.setMessage(locationAndRouteGuidanceService?.locationStateMessage ?: "")

		val status = locationAndRouteGuidanceService?.locationState ?: LocationAndRouteGuidanceService.LocationState.SEARCHING
		status_bar.setStatus(when (status) {
			LocationAndRouteGuidanceService.LocationState.NONE -> LocationStatusBar.Status.WAITING
			LocationAndRouteGuidanceService.LocationState.NO_WIFI -> LocationStatusBar.Status.WIFI_OFF
			LocationAndRouteGuidanceService.LocationState.NO_LOCATION -> LocationStatusBar.Status.LOCATION_OFF
			LocationAndRouteGuidanceService.LocationState.SEARCHING -> LocationStatusBar.Status.SEARCHING
			LocationAndRouteGuidanceService.LocationState.FOUND -> LocationStatusBar.Status.LOCATION_ON
		})
		if (status == LocationAndRouteGuidanceService.LocationState.NO_LOCATION) {
			status_bar.setOnClickListener { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
		} else if (status == LocationAndRouteGuidanceService.LocationState.NO_WIFI) {
			status_bar.setOnClickListener { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
		} else {
			status_bar.setOnClickListener { }
		}
	}

	/* navigation */

	companion object {
		val GOTO_ROUTE_CHOOSER = "activities.MainActivity:GOTO_ROUTE_CHOOSER"
		val GOTO_ROUTE_GUIDANCE = "activities.MainActivity:GOTO_ROUTE_GUIDANCE"
	}

	private fun registerNavigationReceivers() {
		registerReceiver(gotoRouteChooserReceiver, IntentFilter(GOTO_ROUTE_CHOOSER))
		registerReceiver(gotoRouteGuidanceReceiver, IntentFilter(GOTO_ROUTE_GUIDANCE))
	}

	private fun unregisterNavigationReceivers() {
		unregisterReceiver(gotoRouteChooserReceiver)
		unregisterReceiver(gotoRouteGuidanceReceiver)
	}

	private val gotoRouteChooserReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			gotoRouteChooser(intent?.getStringExtra(RouteChooserFragment.DESTINATION) ?: "none")
		}
	}

	private val gotoRouteGuidanceReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			gotoRouteGuidance()
		}
	}

	private fun initDestinationChooser() {
		supportFragmentManager.beginTransaction()
				.add(R.id.fragment_frame, DestinationChooserFragment())
				.commit()
	}

	private fun gotoRouteChooser(destination: String? = null) {
		val fragment = RouteChooserFragment.getInstance(destination)
		val key = "${RouteChooserFragment.KEY}:${destination ?: RouteChooserFragment.NO_DESTINATION}"

		supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_frame, fragment, key)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(key)
				.commit()
	}

	private fun gotoRouteGuidance() {
		val fragment = RouteGuidanceFragment.getInstance()
		val key = RouteGuidanceFragment.KEY

		supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_frame, fragment, key)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(key)
				.commit()
	}

}
