package uk.co.markormesher.easymaps.mapperapp.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.fragment_route_chooser.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.markormesher.easymaps.mapperapp.BaseFragment
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.activities.LocationSearchActivity
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import uk.co.markormesher.easymaps.mapperapp.routing.BreadthFirstSearchRouteFinder
import java.util.*

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
		routeFinder.loadConnections(context)
	}

	/* views */

	private fun initViews() {
		hideContentViews()

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

	private fun hideContentViews() {
		loading_icon.visibility = View.GONE
		loading_icon.clearAnimation()
		centre_message.visibility = View.GONE
		route_list_wrapper.visibility = View.GONE
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
		hideContentViews()

		if (toLocation == null || fromLocation == null) {
			centre_message.text = getString(R.string.route_input_prompt)
			centre_message.visibility = View.VISIBLE
		} else {
			loading_icon.visibility = View.VISIBLE
			loading_icon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.icon_spin))

			routeFinder.findRoute(fromLocation!!, toLocation!!, { routes ->
				if (routes.isEmpty()) {
					centre_message.text = getString(R.string.no_route_found)
					centre_message.visibility = View.VISIBLE
				} else {
					with(StringBuilder()) {
						// TODO: display route with better UI
						routes.forEach { route ->
							val uniqueModes = LinkedList<TravelMode>()
							var changes = 0

							route.modes.forEach { mode ->
								if (uniqueModes.isEmpty()) {
									uniqueModes.add(mode)
								} else if (uniqueModes.last != mode) {
									uniqueModes.add(mode)
									if (uniqueModes.last != TravelMode.WALK && mode != TravelMode.WALK) {
										++changes
									}
								}
							}

							append(uniqueModes.joinToString(", "))
							append("\n")
							append("Changes: $changes")
							append("\n")
							append("Cost: ${route.cost}")
							append("\n\n")
						}

						route_list.text = toString()
						route_list_wrapper.visibility = View.VISIBLE
					}
				}
				loading_icon.visibility = View.GONE
				loading_icon.clearAnimation()
			})
		}
	}

	enum class Direction { TO, FROM }

	/* route finding */

	val routeFinder = BreadthFirstSearchRouteFinder()

}
