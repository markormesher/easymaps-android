package uk.co.markormesher.easymaps.mapperapp.routing

import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import java.util.*

data class Route(
		val locations: MutableList<String> = ArrayList<String>(),
		val modes: MutableList<TravelMode> = ArrayList<TravelMode>(),
		var cost: Int = 0) {

	fun clone(): Route {
		val clone = Route()
		clone.locations.addAll(locations)
		clone.modes.addAll(modes)
		clone.cost = cost
		return clone
	}

}
