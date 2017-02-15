package uk.co.markormesher.easymaps.mapperapp.routing

import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.data.Connection
import uk.co.markormesher.easymaps.mapperapp.data.Location
import java.util.*

class BreadthFirstSearchRouteFinder: RouteFinder(), AnkoLogger {

	private val adj = HashMap<String, ArrayList<Connection>>()

	override fun loadConnection(connection: Connection) {
		adj.getOrPut(connection.from, { ArrayList<Connection>() }).add(connection)
	}

	override fun findRoute(from: Location, to: Location): Route? {
		val open = LinkedList<Route>()
		val closed = HashSet<String>()

		val init = Route()
		init.locations.add(from.id)

		open.addLast(init)

		while (open.isNotEmpty()) {
			val state = open.removeFirst()
			val tip = state.locations.last()

			closed.add(tip)

			if (tip == to.id) {
				return state
			}

			adj[tip]?.filter({ s -> !closed.contains(s.to) })?.forEach { edge ->
				val nextState = state.clone()
				nextState.locations.add(edge.to)
				nextState.modes.add(edge.mode)
				open.addLast(nextState)
			}
		}

		return null
	}

}
