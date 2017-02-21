package uk.co.markormesher.easymaps.mapperapp.routing

import android.content.Context
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import uk.co.markormesher.easymaps.mapperapp.data.TravelMode
import java.util.*

class RouteSearchManager: AnkoLogger {

	val algorithms = ArrayList<RoutingAlgorithm>()

	val locations = HashMap<String, Location>()
	val edges = HashMap<String, ArrayList<Edge>>()

	private var loaded = false

	fun loadData(context: Context) {
		if (loaded) {
			return
		}

		doAsync {
			locations.clear()
			edges.clear()
			with(OfflineDatabase(context)) {
				getLocations().forEach { location -> locations.put(location.id, location) }
				getConnections().forEach { connection ->
					edges.getOrPut(connection.from, { ArrayList<Edge>() }).add(Edge(
							locations[connection.from]!!,
							locations[connection.to]!!,
							connection.mode,
							connection.cost
					))
				}
			}
			loaded = true
		}
	}

	fun findRoutes(from: Location, to: Location, callback: (routes: ArrayList<Route>) -> Unit) {
		doAsync {
			val routes = ArrayList<Route>()
			algorithms.forEach { algorithm -> routes.addAll(algorithm.findRoutes(from, to)) }
			uiThread {
				callback(routes)
			}
		}
	}

	data class Edge(val source: Location, val destination: Location, val mode: TravelMode, val cost: Int)

	interface RoutingAlgorithm {
		fun findRoutes(from: Location, to: Location): ArrayList<Route>
	}

}
