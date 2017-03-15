package uk.co.markormesher.easymaps.mapperapp.routing

import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import java.util.*

data class Route(
		val locations: MutableList<Location> = ArrayList<Location>(),
		val modes: MutableList<TravelMode> = ArrayList<TravelMode>(),
		var duration: Int = 0) {

	val quality: Double
		get() = duration * (modes.distinct().size * 0.2)

	fun clone(): Route {
		val clone = Route()
		clone.locations.addAll(locations)
		clone.modes.addAll(modes)
		clone.duration = duration
		return clone
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Route) return false
		if (other.locations.size != this.locations.size) return false
		if (other.modes.size != this.modes.size) return false
		for (i in 0..other.locations.size - 1) {
			if (other.locations[i] != this.locations[i]) return false
		}
		for (i in 0..other.modes.size - 1) {
			if (other.modes[i] != this.modes[i]) return false
		}
		return true
	}

	override fun hashCode(): Int {
		var hash = 17
		hash *= 31 + locations.hashCode()
		hash *= 31 + modes.hashCode()
		return hash
	}
}
