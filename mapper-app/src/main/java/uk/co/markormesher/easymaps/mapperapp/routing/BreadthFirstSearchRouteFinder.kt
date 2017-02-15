package uk.co.markormesher.easymaps.mapperapp.routing

import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.data.Connection
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import java.util.*

class BreadthFirstSearchRouteFinder: RouteFinder(), AnkoLogger {

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

	override fun findRoute(from: Location, to: Location): List<Route> {
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

			if (tip.id == to.id) {
				output.add(state)
				return output
			}

			edges[tip.id]?.filter({ e -> !closed.contains(e.destination.id) })?.forEach { edge ->
				val nextState = state.clone()
				nextState.locations.add(edge.destination)
				nextState.modes.add(edge.mode)
				nextState.cost += edge.cost

				open.addLast(nextState)
			}
		}

		return output
	}

	data class Edge(val destination: Location, val mode: TravelMode, val cost: Int)

}
