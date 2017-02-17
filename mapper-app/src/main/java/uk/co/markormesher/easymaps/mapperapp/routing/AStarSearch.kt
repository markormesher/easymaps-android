package uk.co.markormesher.easymaps.mapperapp.routing

import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.helpers.distanceInKm
import java.util.*

/*
A* search based on h(s) = travel time + change time + walking distance to goal.
 */
class AStarSearch(val data: RouteSearchManager, val penalty: Int = 0): RouteSearchManager.RoutingAlgorithm {

	override fun findRoutes(from: Location, to: Location): ArrayList<Route> {
		val open = PriorityQueue<Route>(20, Comparator<Route> { a, b ->
			val aVal = a.duration + distanceInKm(a.locations.last(), to)
			val bVal = b.duration + distanceInKm(b.locations.last(), to)
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
				val nextState = state.clone()
				nextState.locations.add(edge.destination)
				nextState.modes.add(edge.mode)
				nextState.duration += edge.cost

				if (state.modes.isNotEmpty() && state.modes.last() != edge.mode) {
					nextState.duration += penalty
				}

				open.offer(nextState)
			}
		}

		return output
	}

}
