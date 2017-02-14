package uk.co.markormesher.easymaps.mapperapp.routing

import org.jetbrains.anko.AnkoLogger
import uk.co.markormesher.easymaps.mapperapp.data.Connection
import uk.co.markormesher.easymaps.mapperapp.data.Location
import java.util.*

class BreadthFirstSearchRouteFinder: RouteFinder(), AnkoLogger {

	val adj = HashMap<String, ArrayList<String>>()

	override fun loadConnection(connection: Connection) {
		adj.getOrPut(connection.from, { ArrayList<String>() }).add(connection.to)
	}

	override fun findRoute(from: Location, to: Location): List<String> {
		Thread.sleep(4000)

		val route = ArrayList<String>()

		route.add("This")
		route.add("thing")
		route.add("can't")
		route.add("compute")
		route.add("routes")
		route.add("yet")

		return route
	}
}
