package uk.co.markormesher.easymaps.mapperapp.fragments

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.fragment_route_chooser.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.enabled
import org.jetbrains.anko.uiThread
import uk.co.markormesher.easymaps.mapperapp.BaseFragment
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.activities.LocationSearchActivity
import uk.co.markormesher.easymaps.mapperapp.activities.MainActivity
import uk.co.markormesher.easymaps.mapperapp.adapters.RouteListAdapter
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.routing.*
import uk.co.markormesher.easymaps.mapperapp.services.LocationService
import java.util.*

class RouteChooserFragment: BaseFragment(), RouteListAdapter.OnSelectListener {

	companion object {
		fun getInstance(destination: String?): RouteChooserFragment {
			val fragment = RouteChooserFragment()
			val args = Bundle()
			args.putString(RouteChooserFragment.DESTINATION, destination)
			fragment.arguments = args
			return fragment
		}

		val TAG = "fragments.RouteChooserFragment:TAG"
		val DESTINATION = "fragments.RouteChooserFragment:DESTINATION"
		val NO_DESTINATION = "none"
	}

	private var initialDestinationId = NO_DESTINATION
	private var fromLocation: Location? = null
	private var toLocation: Location? = null
	private var lastSearchLocation: Direction? = null

	private val routeSearchManager = RouteSearchManager()
	private var busySearching = false

	private val routeListAdapter by lazy { RouteListAdapter(context, this) }
	private val activeRoutes = ArrayList<Route>()

	init {
		with(routeSearchManager) {
			algorithms.add(BreadthFirstSearch(this))
			algorithms.add(GreedySearch(this))
			algorithms.add(AStarSearch(this))
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initialDestinationId = arguments.getString(DESTINATION, NO_DESTINATION)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_route_chooser, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		initViews()
		restoreState()
	}

	override fun onResume() {
		super.onResume()
		context.registerReceiver(locationStateUpdatedReceiver, IntentFilter(LocationService.STATE_UPDATED))
	}

	override fun onPause() {
		super.onPause()
		context.unregisterReceiver(locationStateUpdatedReceiver)
	}

	private fun initViews() {
		hideContentViews()

		route_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
		route_list.adapter = routeListAdapter

		from_input.setOnClickListener {
			if (!busySearching) {
				lastSearchLocation = Direction.FROM
				startActivityForResult(
						Intent(context, LocationSearchActivity::class.java),
						LocationSearchActivity.REQUEST_CODE
				)
			}
		}
		to_input.setOnClickListener {
			if (!busySearching) {
				lastSearchLocation = Direction.TO
				startActivityForResult(
						Intent(context, LocationSearchActivity::class.java),
						LocationSearchActivity.REQUEST_CODE
				)
			}
		}
		use_current.setOnClickListener {
			val current = (activity as MainActivity).locationService?.currentLocation
			if (current != null) {
				setInputLocation(Direction.FROM, current)
			}
		}
		swap_to_from.setOnClickListener {
			if (!busySearching) {
				swapRouteInput()
			}
		}
	}

	private fun hideContentViews() {
		loading_icon.visibility = View.GONE
		loading_icon.clearAnimation()
		centre_message.visibility = View.GONE
		route_list.visibility = View.GONE
	}

	private fun hideOrShowUseCurrentButton() {
		if ((activity as MainActivity).locationService?.currentLocation != null) {
			use_current.visibility = View.VISIBLE
		} else {
			use_current.visibility = View.GONE
		}
	}

	private fun restoreState() {
		if (fromLocation == null && toLocation == null) {
			// initial state
			routeSearchManager.loadData(context)
			setInputLocation(Direction.FROM, (activity as MainActivity).locationService?.currentLocation, false)
			setInputLocationWithId(Direction.TO, initialDestinationId)
		} else {
			setInputLocation(Direction.FROM, fromLocation, false)
			setInputLocation(Direction.TO, toLocation, false)
			onRouteSearchResult(activeRoutes)
		}
		hideOrShowUseCurrentButton()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == LocationSearchActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK && lastSearchLocation != null) {
			setInputLocationWithId(lastSearchLocation!!, data?.getStringExtra(LocationSearchActivity.LOCATION_ID_KEY)!!)
		}
	}

	private fun setInputLocationWithId(direction: Direction, locationId: String) {
		if (locationId == NO_DESTINATION) {
			setInputLocation(direction, null)
		} else {
			doAsync {
				val location = OfflineDatabase(context).getLocation(locationId)
				uiThread {
					setInputLocation(direction, location)
				}
			}
		}
	}

	private fun setInputLocation(direction: Direction, location: Location?, update: Boolean = true) {
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

		if (update) {
			onRouteInputUpdated()
		}
	}

	private fun onRouteInputUpdated() {
		hideContentViews()

		if (toLocation == null || fromLocation == null) {
			centre_message.text = getString(R.string.route_input_prompt)
			centre_message.visibility = View.VISIBLE
		} else if (toLocation == fromLocation) {
			centre_message.text = getString(R.string.no_route_same_start_end)
			centre_message.visibility = View.VISIBLE
		} else {
			startSearch()
		}
	}

	private fun swapRouteInput() {
		val oldTo = toLocation
		val oldFrom = fromLocation
		setInputLocation(Direction.TO, oldFrom, false)
		setInputLocation(Direction.FROM, oldTo, false)
		onRouteInputUpdated()
	}

	private val locationStateUpdatedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) = hideOrShowUseCurrentButton()
	}

	private fun startSearch() {
		if (busySearching) {
			return
		}
		busySearching = true

		loading_icon.visibility = View.VISIBLE
		loading_icon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.icon_spin))
		to_input.enabled = false
		to_input.alpha = 0.8f
		from_input.enabled = false
		from_input.alpha = 0.8f
		swap_to_from.isEnabled = false
		swap_to_from.alpha = 0.8f

		routeSearchManager.findRoutes(fromLocation!!, toLocation!!, { routes -> onRouteSearchResult(routes) })
	}

	private fun finishSearch() {
		if (!busySearching) {
			return
		}
		busySearching = false

		loading_icon.visibility = View.GONE
		loading_icon.clearAnimation()
		to_input.enabled = true
		to_input.alpha = 1f
		from_input.enabled = true
		from_input.alpha = 1f
		swap_to_from.isEnabled = true
		swap_to_from.alpha = 1f
	}

	private fun onRouteSearchResult(allRoutes: ArrayList<Route>) {
		val routes = allRoutes.distinct().toMutableList().sortedBy(Route::quality)

		// only overwrite if the update is not from the internal set
		if (routes != activeRoutes) {
			activeRoutes.clear()
			activeRoutes.addAll(routes)
		}

		if (routes.isEmpty()) {
			centre_message.text = getString(R.string.no_route_found)
			centre_message.visibility = View.VISIBLE
		} else {
			routeListAdapter.updateRoutes(routes)
			route_list.visibility = View.VISIBLE
		}

		finishSearch()
	}

	override fun onRouteSelected(index: Int) {
		(activity as MainActivity).locationService?.setActiveRoute(activeRoutes[index])
		context.sendBroadcast(Intent(MainActivity.GOTO_ROUTE_GUIDANCE))
	}

	enum class Direction {
		TO,
		FROM
	}
}
