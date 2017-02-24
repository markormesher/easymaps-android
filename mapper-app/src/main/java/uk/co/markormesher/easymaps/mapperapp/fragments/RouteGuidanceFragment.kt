package uk.co.markormesher.easymaps.mapperapp.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_route_guidance.*
import uk.co.markormesher.easymaps.mapperapp.BaseFragment
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.activities.MainActivity
import uk.co.markormesher.easymaps.mapperapp.adapters.RouteDisplayAdapter
import uk.co.markormesher.easymaps.mapperapp.routing.AugmentedRoute
import uk.co.markormesher.easymaps.mapperapp.services.LocationService

class RouteGuidanceFragment: BaseFragment() {

	private var activeRoute: AugmentedRoute? = null
	private var routeAdapter: RouteDisplayAdapter? = null

	companion object {
		fun getInstance(): RouteGuidanceFragment {
			val fragment = RouteGuidanceFragment()
			val args = Bundle()
			fragment.arguments = args
			return fragment
		}

		val TAG = "fragments.RouteGuidanceFragment:TAG"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		activeRoute = (activity as MainActivity).locationService?.activeRoute
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_route_guidance, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		initViews()
	}

	override fun onResume() {
		super.onResume()
		if (activeRoute == null) {
			no_route_message.visibility = View.VISIBLE
			step_list.visibility = View.GONE
		} else {
			no_route_message.visibility = View.GONE
			step_list.visibility = View.VISIBLE
		}

		context.registerReceiver(locationStateUpdatedReceiver, IntentFilter(LocationService.STATE_UPDATED))
	}

	override fun onPause() {
		super.onPause()
		context.unregisterReceiver(locationStateUpdatedReceiver)
	}

	private fun initViews() {
		no_route_message.setOnClickListener { activity.onBackPressed() }

		if (activeRoute != null) {
			routeAdapter = RouteDisplayAdapter(context, activeRoute!!)
			step_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
			step_list.adapter = routeAdapter
			updateCurrentLocation()
		}
	}

	private fun updateCurrentLocation() {
		routeAdapter?.currentLocation = (activity as MainActivity).locationService?.currentLocation
	}

	private val locationStateUpdatedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) = updateCurrentLocation()
	}

}
