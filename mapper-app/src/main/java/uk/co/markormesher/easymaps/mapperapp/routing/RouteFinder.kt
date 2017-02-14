package uk.co.markormesher.easymaps.mapperapp.routing

import android.content.Context
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.markormesher.easymaps.mapperapp.data.Connection
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase

abstract class RouteFinder: AnkoLogger {

	fun loadConnections(context: Context) = doAsync {
		OfflineDatabase(context).getConnections().forEach { c -> loadConnection(c) }
	}

	protected abstract fun loadConnection(connection: Connection)

	fun findRoute(from: Location, to: Location, callback: (route: List<String>) -> Unit) {
		doAsync {
			val route = findRoute(from, to)
			uiThread {
				callback(route)
			}
		}
	}

	protected abstract fun findRoute(from: Location, to: Location): List<String>

}
