package uk.co.markormesher.easymaps.mapperapp.routing

import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.data.Connection
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import java.util.*

class GreedySearchRouteFinder: RouteFinder(), AnkoLogger {

	private val changePenalties = arrayOf(180, 240)

	private val locations = HashMap<String, Location>()
	private val edges = HashMap<String, ArrayList<Edge>>()

	override fun loadLocation(location: Location) {
		locations.put(location.id, location)
	}

	override fun loadConnection(connection: Connection) {
		edges.getOrPut(connection.from, { ArrayList<Edge>() }).add(Edge(
				locations[connection.to]!!,
				connection.mode,
				connection.cost
		))
	}

	override fun findRoute(from: Location, to: Location): ArrayList<Route> {
		val setOfRoutes = HashSet<Route>()
		changePenalties.forEach { penalty ->
			val route = findRouteWithChangePenalty(from, to, penalty)
			if (route != null) {
				setOfRoutes.add(route)
			}
		}
		return ArrayList(setOfRoutes)
	}

	private fun findRouteWithChangePenalty(from: Location, to: Location, penalty: Int): Route? {
		val open = PriorityQueue<Route>(20, Comparator<Route> { a, b -> a.duration.compareTo(b.duration) })
		val closed = HashSet<String>()

		val init = Route()
		init.locations.add(from)
		open.offer(init)

		while (open.isNotEmpty()) {
			val state = open.poll()

			if (state.locations.last() == to) {
				return state
			}

			val tip = state.locations.last()
			closed.add(tip.id)

			edges[tip.id]?.filter({ e -> !closed.contains(e.destination.id) })?.forEach { edge ->
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

		return null
	}

	data class Edge(val destination: Location, val mode: TravelMode, val cost: Int)

}
