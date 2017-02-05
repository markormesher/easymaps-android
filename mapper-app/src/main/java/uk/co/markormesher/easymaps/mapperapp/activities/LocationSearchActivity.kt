package uk.co.markormesher.easymaps.mapperapp.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_location_search.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.adapters.LocationListAdapter
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.sdk.AbstractTextWatcher
import uk.co.markormesher.easymaps.sdk.BaseActivity

// TODO: pass result to activity

class LocationSearchActivity: BaseActivity() {

	private val iconSpinAnimation: Animation? by lazy { AnimationUtils.loadAnimation(this, R.anim.icon_spin) }

	private val locationListAdapter by lazy { LocationListAdapter(this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.activity_location_search)
		loading_icon.startAnimation(iconSpinAnimation)

		search_input.addTextChangedListener(object: AbstractTextWatcher() {
			override fun afterTextChanged(str: Editable?) {
				locationListAdapter.filter.filter(str?.trim() ?: "")
			}
		})

		location_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
		location_list.adapter = locationListAdapter
		loadLocations()
	}

	private fun loadLocations() {
		// load locations and set up adapter
		val db = OfflineDatabase(this)
		val locations = db.getLocations().sortedBy(Location::title)
		locationListAdapter.locations.clear()
		locationListAdapter.locations.addAll(locations)
		locationListAdapter.filter.filter("")

		// switch to main UI
		loading_icon.visibility = GONE
		loading_icon.clearAnimation()
		search_input.visibility = VISIBLE
		location_list.visibility = VISIBLE
	}

}
