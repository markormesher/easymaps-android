package uk.co.markormesher.easymaps.mapperapp.routing

import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.data.Location
import java.util.*

class GreedySearch(val data: RouteSearchManager, val penalty: Int): RouteSearchManager.RoutingAlgorithm, AnkoLogger {

	override fun findRoutes(from: Location, to: Location): ArrayList<Route> {
		val open = PriorityQueue<Route>(20, Comparator<Route> { a, b -> a.duration.compareTo(b.duration) })
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
