package uk.co.markormesher.easymaps.mapperapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_route_chooser.*
import uk.co.markormesher.easymaps.mapperapp.R

class RouteChooserFragment: Fragment() {

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

	var destinationId = DEFAULT_DESTINATION

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		destinationId = arguments.getString(DESTINATION, DEFAULT_DESTINATION)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_route_chooser, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		destination.text = destinationId
	}

}
