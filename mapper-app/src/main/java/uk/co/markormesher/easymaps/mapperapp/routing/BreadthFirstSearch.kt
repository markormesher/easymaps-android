package uk.co.markormesher.easymaps.mapperapp.routing

import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.LocationType
import java.util.*

class BreadthFirstSearch(val manager: RouteSearchManager): RouteSearchManager.RoutingAlgorithm, AnkoLogger {

	override fun findRoutes(from: Location, to: Location): ArrayList<Route> {
		val open = LinkedList<Route>()
		val closed = HashSet<String>()
		val output = ArrayList<Route>()

		val init = Route()
		init.locations.add(from)
		open.addLast(init)

		while (open.isNotEmpty()) {
			val state = open.removeFirst()
			val tip = state.locations.last()

			closed.add(tip.id)

			manager.edges[tip.id]?.filter({ e -> !closed.contains(e.destination.id) })?.forEach { edge ->
				// this edge is useful if it doesn't go to an attraction, or if it goes to the attraction we're looking for
				if (edge.destination.type != LocationType.ATTRACTION || edge.destination == to) {
					val nextState = state.clone()
					nextState.locations.add(edge.destination)
					nextState.modes.add(edge.mode)
					nextState.duration += edge.cost

					if (edge.destination == to) {
						output.add(nextState)
						return output
					} else {
						open.addLast(nextState)
					}
				}
			}
		}

		return output
	}

}
