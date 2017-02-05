package uk.co.markormesher.easymaps.mapperapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.adapters.AttractionListAdapter
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.ui.LocationStatusBar
import uk.co.markormesher.easymaps.sdk.BaseActivity

// TODO: new activity - route planner
// TODO: new server - location sensing

class MainActivity: BaseActivity(), AttractionListAdapter.OnClickListener {

	private val attractionListAdapter by lazy { AttractionListAdapter(this, this) }

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

		// set up status bar
		status_bar.setStatus(LocationStatusBar.Status.WAITING)
		status_bar.setHeading("Waiting for location")
		status_bar.setMessage("Mark, hurry up and code this bit")
	}

	override fun onResume() {
		super.onResume()

		if (OfflineDatabase.isPopulated(this)) {
			loadAttractions()
			OfflineDatabase.startBackgroundUpdate(this)
		} else {
			startActivity(Intent(this, OfflineDataDownloadActivity::class.java))
		}
	}

	private fun loadAttractions() {
		val attractions = OfflineDatabase(this).getAttractions()
		attractionListAdapter.attractions.addAll(attractions)
		attractionListAdapter.notifyDataSetChanged()

		loading_icon.clearAnimation()
		loading_icon.visibility = View.GONE
		status_bar.visibility = View.VISIBLE
		attraction_grid.visibility = View.VISIBLE
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
		Toast.makeText(this, "We're going to $locationId!", Toast.LENGTH_SHORT).show()
	}

}
