package uk.co.markormesher.easymaps.mapperapp.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_route_guidance.*
import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.BaseFragment
import uk.co.markormesher.easymaps.mapperapp.R
import uk.co.markormesher.easymaps.mapperapp.activities.MainActivity
import uk.co.markormesher.easymaps.mapperapp.routing.Route

/*
TODO: pass route to recycler view adapter
TODO: create adapter to display basic copies of route components
TODO: persist session if a route is active
TODO: open to route guidance fragment is a route is active
 */

class RouteGuidanceFragment: BaseFragment(), AnkoLogger {

	private var activeRoute: Route? = null

	companion object {
		fun getInstance(): RouteGuidanceFragment {
			val fragment = RouteGuidanceFragment()
			val args = Bundle()
			fragment.arguments = args
			return fragment
		}

		val KEY = "fragments.RouteGuidanceFragment:KEY"
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		activeRoute = (activity as MainActivity).locationAndRouteGuidanceService?.activeRoute
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
	}

	/* views */

	private fun initViews() {
		no_route_message.setOnClickListener { activity.onBackPressed() }

		step_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
		//step_list.adapter = routeGuidanceAdapter
	}

	/* route guidance */


}
