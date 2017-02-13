package uk.co.markormesher.easymaps.mapperapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_route_chooser.*
import uk.co.markormesher.easymaps.mapperapp.R

class RouteChooserFragment(val initialDestination: String): Fragment() {

	companion object {
		val DESTINATION = "fragments.RouteChooserFragment:DESTINATION"
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_route_chooser, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		destination.text = initialDestination
	}
}
