package uk.co.markormesher.easymaps.mapperapp.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_location_search.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.adapters.LocationListAdapter
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.sdk.BaseActivity

class LocationSearchActivity: BaseActivity() {

	private val locationListAdapter by lazy { LocationListAdapter(this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_location_search)
		location_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
		location_list.adapter = locationListAdapter
		loadLocations()
	}

	private fun loadLocations() {
		val db = OfflineDatabase(this)
		val locations = db.getLocations()
		locations.sortedBy(Location::title)
		locationListAdapter.locations.clear()
		locationListAdapter.locations.addAll(locations)
		locationListAdapter.notifyDataSetChanged()
	}

}
