package uk.co.markormesher.easymaps.mapperapp.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.fragment_destination_chooser.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.activities.LocationSearchActivity
import uk.co.markormesher.easymaps.mapperapp.activities.MainActivity
import uk.co.markormesher.easymaps.mapperapp.adapters.LocationListAdapter
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase

// TODO: better handling of init on new instance vs. re-visit

class DestinationChooserFragment: Fragment(), LocationListAdapter.OnClickListener {

	private val COLUMN_WIDTH = 110 // dp

	private val locationListAdapter by lazy { LocationListAdapter(context, this) }
	private var locationsLoaded = false

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.fragment_destination_chooser, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		loading_icon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.icon_spin))

		val screenWidthInDp = resources.configuration.screenWidthDp
		val columns = screenWidthInDp / COLUMN_WIDTH
		val gridLayoutManager = GridLayoutManager(context, columns)
		gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
			override fun getSpanSize(position: Int): Int = if (position == 0) columns else 1
		}
		location_grid.layoutManager = gridLayoutManager
		location_grid.adapter = locationListAdapter

		loadLocations()
	}

	/* attraction list */

	// TODO: this is not neat
	private fun loadLocations() {
		if (locationsLoaded) {
			loading_icon.clearAnimation()
			loading_icon.visibility = View.GONE
			location_grid.visibility = View.VISIBLE
			return
		}

		val attractions = OfflineDatabase(context).getAttractions()
		locationListAdapter.attractions.clear()
		locationListAdapter.attractions.addAll(attractions)
		locationListAdapter.notifyDataSetChanged()

		loading_icon.clearAnimation()
		loading_icon.visibility = View.GONE
		location_grid.visibility = View.VISIBLE

		locationsLoaded = true
	}

	override fun onAttractionClick(type: Int, location: Location?) {
		when (type) {
			LocationListAdapter.TYPE_SEARCH -> startActivityForResult(
					Intent(context, LocationSearchActivity::class.java),
					LocationSearchActivity.REQUEST_CODE
			)

			LocationListAdapter.TYPE_ATTRACTION -> if (location != null) {
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
		val intent = Intent(MainActivity.GOTO_ROUTE_CHOOSER)
		intent.putExtra(RouteChooserFragment.DESTINATION, locationId)
		context.sendBroadcast(intent)
	}

}
