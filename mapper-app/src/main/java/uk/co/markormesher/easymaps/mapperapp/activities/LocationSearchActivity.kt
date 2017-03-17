package uk.co.markormesher.easymaps.mapperapp.activities

import android.app.Activity
import android.content.Intent
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
import uk.co.markormesher.easymaps.mapperapp.adapters.LocationSearchListAdapter
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.sdk.AbstractTextWatcher
import uk.co.markormesher.easymaps.sdk.BaseActivity

class LocationSearchActivity: BaseActivity(), LocationSearchListAdapter.OnSelectListener {

	companion object {
		val REQUEST_CODE = "LocationSearchActivity".hashCode().and(0xffff)
		val LOCATION_ID_KEY = "activities.LocationSearchActivity:LOCATION_ID_KEY"
	}

	private val iconSpinAnimation: Animation? by lazy { AnimationUtils.loadAnimation(this, R.anim.icon_spin) }

	private val locationListAdapter by lazy { LocationSearchListAdapter(this, this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		setFinishOnTouchOutside(false)
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

	override fun onLocationSelected(locationId: String) {
		val data = Intent()
		data.putExtra(LOCATION_ID_KEY, locationId)
		setResult(Activity.RESULT_OK, data)
		finish()
	}
}
