package uk.co.markormesher.easymaps.mapperapp.routing

import android.content.Context
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.markormesher.easymaps.mapperapp.data.Connection
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.OfflineDatabase
import java.util.*

abstract class RouteFinder: AnkoLogger {

	fun loadData(context: Context) = doAsync {
		with(OfflineDatabase(context)) {
			getLocations().forEach { l -> loadLocation(l) }
			getConnections().forEach { c -> loadConnection(c) }
		}
	}

	protected abstract fun loadLocation(location: Location)

	protected abstract fun loadConnection(connection: Connection)

	fun findRoute(from: Location, to: Location, callback: (routes: ArrayList<Route>) -> Unit) {
		doAsync {
			val routes = findRoute(from, to)
			uiThread {
				callback(routes)
			}
		}
	}

	protected abstract fun findRoute(from: Location, to: Location): ArrayList<Route>

}
