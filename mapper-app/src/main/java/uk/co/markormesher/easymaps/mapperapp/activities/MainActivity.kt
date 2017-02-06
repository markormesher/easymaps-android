package uk.co.markormesher.easymaps.mapperapp.activities

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.adapters.AttractionListAdapter
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.services.LocationService
import uk.co.markormesher.easymaps.mapperapp.ui.LocationStatusBar
import uk.co.markormesher.easymaps.sdk.BaseActivity

class MainActivity: BaseActivity(), ServiceConnection, AttractionListAdapter.OnClickListener {

	private val attractionListAdapter by lazy { AttractionListAdapter(this, this) }
	private var attractionsLoaded = false

	private var locationService: LocationService? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		loading_icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.icon_spin))

		// set up attraction list
		val screenWidthInDp = resources.configuration.screenWidthDp
		val columns = screenWidthInDp / 110
		val gridLayoutManager = GridLayoutManager(this, columns)
		gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
			override fun getSpanSize(position: Int): Int = if (position == 0) columns else 1
		}
		attraction_grid.layoutManager = gridLayoutManager
		attraction_grid.adapter = attractionListAdapter
	}

	override fun onResume() {
		super.onResume()

		if (OfflineDatabase.isPopulated(this)) {
			loadAttractions()
			OfflineDatabase.startBackgroundUpdate(this)
		} else {
			startActivity(Intent(this, OfflineDataDownloadActivity::class.java))
		}

		val serviceIntent = Intent(baseContext, LocationService::class.java)
		baseContext.startService(serviceIntent)
		baseContext.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)

		registerReceiver(locationStateUpdatedReceiver, IntentFilter(LocationService.STATE_UPDATED))
		updateLocationStatusFromService()
	}

	override fun onPause() {
		super.onPause()

		unregisterReceiver(locationStateUpdatedReceiver)
	}

	/* service binding */

	override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
		if (binder is LocationService.LocalBinder) {
			locationService = binder.getLocationDetectionService()
			updateLocationStatusFromService()
		}
	}

	override fun onServiceDisconnected(name: ComponentName?) {
		locationService = null
	}

	private val locationStateUpdatedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			updateLocationStatusFromService()
		}
	}

	private fun updateLocationStatusFromService() {
		val status = locationService?.locationState ?: LocationService.LocationState.SEARCHING
		status_bar.setStatus(when (status) {
			LocationService.LocationState.NONE -> LocationStatusBar.Status.WAITING
			LocationService.LocationState.SEARCHING -> LocationStatusBar.Status.SEARCHING
			LocationService.LocationState.NO_WIFI -> LocationStatusBar.Status.WIFI_OFF
			LocationService.LocationState.NO_LOCATION -> LocationStatusBar.Status.LOCATION_OFF
			LocationService.LocationState.FOUND -> LocationStatusBar.Status.LOCATION_ON
		})
		status_bar.setHeading(locationService?.locationStateHeader ?: "")
		status_bar.setMessage(locationService?.locationStateMessage ?: "")
	}

	/* attraction list */

	private fun loadAttractions() {
		if (attractionsLoaded) {
			return
		}

		val attractions = OfflineDatabase(this).getAttractions()
		attractionListAdapter.attractions.clear()
		attractionListAdapter.attractions.addAll(attractions)
		attractionListAdapter.notifyDataSetChanged()

		loading_icon.clearAnimation()
		loading_icon.visibility = View.GONE
		status_bar.visibility = View.VISIBLE
		attraction_grid.visibility = View.VISIBLE

		attractionsLoaded = true
	}

	override fun onAttractionClick(type: Int, location: Location?) {
		when (type) {
			AttractionListAdapter.TYPE_SEARCH -> startActivityForResult(
					Intent(this, LocationSearchActivity::class.java),
					LocationSearchActivity.REQUEST_CODE
			)

			AttractionListAdapter.TYPE_ATTRACTION -> if (location != null) {
				onDestinationSelected(location.id)
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == LocationSearchActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			onDestinationSelected(data?.getStringExtra(LocationSearchActivity.LOCATION_ID_KEY)!!)
		}
	}

	private fun onDestinationSelected(locationId: String) {
		val intent = Intent(this, RoutePlanningActivity::class.java)
		intent.putExtra("DESTINATION", locationId)
		startActivity(intent)
	}

}
