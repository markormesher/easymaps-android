package uk.co.markormesher.easymaps.mapperapp.routing

import uk.co.markormesher.easymaps.mapperapp.MODE_SWITCH_TIME
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.LocationType
import uk.co.markormesher.easymaps.mapperapp.helpers.distanceInMetres
import java.util.*

/*
A* search based on h(s) = travel time + change time + walking distance to goal.
 */
class AStarSearch(val data: RouteSearchManager): RouteSearchManager.RoutingAlgorithm {

	override fun findRoutes(from: Location, to: Location): ArrayList<Route> {
		val open = PriorityQueue<Route>(20, Comparator<Route> { a, b ->
			val aVal = a.duration + distanceInMetres(a.locations.last(), to)
			val bVal = b.duration + distanceInMetres(b.locations.last(), to)
			aVal.compareTo(bVal)
		})
		val closed = HashSet<String>()
		val output = ArrayList<Route>()

		val init = Route()
		init.locations.add(from)
		open.offer(init)

		while (open.isNotEmpty()) {
			val state = open.poll()

			if (state.locations.last() == to) {
				output.add(state)
				break
			}

			val tip = state.locations.last()
			closed.add(tip.id)

			data.edges[tip.id]?.filter({ e -> !closed.contains(e.destination.id) })?.forEach { edge ->
				// this edge is not useful if it goes to an attraction we're not looking for
				if (edge.destination.type == LocationType.ATTRACTION && edge.destination != to) {
					return@forEach
				}

				val nextState = state.clone()
				nextState.locations.add(edge.destination)
				nextState.modes.add(edge.mode)
				nextState.duration += edge.cost

				if (state.modes.isNotEmpty() && state.modes.last() != edge.mode) {
					nextState.duration += MODE_SWITCH_TIME
				}

				open.offer(nextState)
			}
		}

		return output
	}

}
