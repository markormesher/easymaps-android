package uk.co.markormesher.easymaps.mapperapp.routing

import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import java.util.*

data class Route(
		val locations: MutableList<Location> = ArrayList<Location>(),
		val modes: MutableList<TravelMode> = ArrayList<TravelMode>(),
		var duration: Int = 0) {

	fun clone(): Route {
		val clone = Route()
		clone.locations.addAll(locations)
		clone.modes.addAll(modes)
		clone.duration = duration
		return clone
	}

}
