package uk.co.markormesher.easymaps.mapperapp.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_route_chooser.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.markormesher.easymaps.mapperapp.BaseFragment
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.activities.LocationSearchActivity
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase

class RouteChooserFragment: BaseFragment(), AnkoLogger {

	companion object {
		fun getInstance(destination: String?): RouteChooserFragment {
			val fragment = RouteChooserFragment()
			val args = Bundle()
			args.putString(RouteChooserFragment.DESTINATION, destination)
			fragment.arguments = args
			return fragment
		}

		val KEY = "fragments.RouteChooserFragment:KEY"
		val DESTINATION = "fragments.RouteChooserFragment:DESTINATION"
		val DEFAULT_DESTINATION = "none"
	}

	var initialDestinationId = DEFAULT_DESTINATION

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initialDestinationId = arguments.getString(DESTINATION, DEFAULT_DESTINATION)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_route_chooser, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		initViews()
		setRouteInput(Direction.TO, initialDestinationId)
	}

	/* views */

	private fun initViews() {
		loading_icon.visibility = View.GONE
		centre_message.visibility = View.GONE

		from_input.setOnClickListener {
			lastRequestDirection = Direction.FROM
			startActivityForResult(
					Intent(context, LocationSearchActivity::class.java),
					LocationSearchActivity.REQUEST_CODE
			)
		}
		to_input.setOnClickListener {
			lastRequestDirection = Direction.TO
			startActivityForResult(
					Intent(context, LocationSearchActivity::class.java),
					LocationSearchActivity.REQUEST_CODE
			)
		}
	}

	/* route input */

	var lastRequestDirection: Direction? = null

	var fromLocation: Location? = null
	var toLocation: Location? = null

	private fun setRouteInput(direction: Direction, id: String) {
		if (id == DEFAULT_DESTINATION) {
			setInputLocation(direction, null)
		} else {
			doAsync {
				val location = OfflineDatabase(context).getLocation(id)

				uiThread {
					setInputLocation(direction, location)
				}
			}
		}
	}

	private fun setInputLocation(direction: Direction, location: Location?) {
		when (direction) {
			Direction.FROM -> {
				fromLocation = location
				from_input.text = location?.getDisplayTitle(context) ?: ""
			}

			Direction.TO -> {
				toLocation = location
				to_input.text = location?.getDisplayTitle(context) ?: ""
			}
		}

		routeInputUpdated()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == LocationSearchActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK && lastRequestDirection != null) {
			setRouteInput(lastRequestDirection!!, data?.getStringExtra(LocationSearchActivity.LOCATION_ID_KEY)!!)
		}
	}

	private fun routeInputUpdated() {
		if (toLocation == null || fromLocation == null) {
			centre_message.text = getString(R.string.route_input_prompt)
			centre_message.visibility = View.VISIBLE
		} else {
			// TODO: start route finding task on background thread
			centre_message.text = "${fromLocation?.id} -> ${toLocation?.id}"
			centre_message.visibility = View.VISIBLE
		}
	}

	enum class Direction { TO, FROM }

}
