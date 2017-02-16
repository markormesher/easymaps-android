package uk.co.markormesher.easymaps.mapperapp.routing

import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.data.Connection
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.LocationType
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

			edges[tip.id]?.filter({ e -> !closed.contains(e.destination.id) })?.forEach { edge ->
				// this edge is useful if it doesn't go to an attraction, or if it goes to the attraction we're looking for
				if (edge.destination.type != LocationType.ATTRACTION || edge.destination == to) {
					val nextState = state.clone()
					nextState.locations.add(edge.destination)
					nextState.modes.add(edge.mode)
					nextState.cost += edge.cost

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

	data class Edge(val destination: Location, val mode: TravelMode, val cost: Int)

}
