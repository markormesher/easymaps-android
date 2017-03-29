package uk.co.markormesher.easymaps.mapperapp.routing

import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import uk.co.markormesher.easymaps.mapperapp.activities.MainActivity
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.LocationType
import java.util.*

class RouteSearchManagerTests {

	// used for context
	@Rule @JvmField
	val mainActivity = ActivityTestRule<MainActivity>(MainActivity::class.java)

	val mockLocation1 = Location("mock-loc-1", "Mock Location 1", null, LocationType.STATION, 0.0, 0.0)
	val mockLocation2 = Location("mock-loc-2", "Mock Location 2", null, LocationType.STATION, 0.0, 0.0)

	@Test
	fun routeSearchManagerShouldSuspendSearchUntilDataIsLoaded() {
		var timer = -System.currentTimeMillis()
		val searchManager = RouteSearchManager()
		searchManager.loadData(mainActivity.activity.applicationContext)
		searchManager.findRoutes(mockLocation1, mockLocation2, {
			timer += System.currentTimeMillis()
			assert(timer > 100) // it should take at least 100 ms to load the data
		})
	}

	@Test
	fun routeSearchManagerShouldExecuteAllAlgorithms() {
		var executionCount = 0
		val mockRoutingAlgorithm1 = object: RouteSearchManager.RoutingAlgorithm {
			override fun findRoutes(from: Location, to: Location): ArrayList<Route> {
				++executionCount
				return ArrayList()
			}
		}
		val mockRoutingAlgorithm2 = object: RouteSearchManager.RoutingAlgorithm {
			override fun findRoutes(from: Location, to: Location): ArrayList<Route> {
				++executionCount
				return ArrayList()
			}
		}
		val mockRoutingAlgorithm3 = object: RouteSearchManager.RoutingAlgorithm {
			override fun findRoutes(from: Location, to: Location): ArrayList<Route> {
				++executionCount
				return ArrayList()
			}
		}

		val searchManager = RouteSearchManager()
		searchManager.algorithms.add(mockRoutingAlgorithm1)
		searchManager.algorithms.add(mockRoutingAlgorithm2)
		searchManager.algorithms.add(mockRoutingAlgorithm3)
		searchManager.loadData(mainActivity.activity.applicationContext)
		searchManager.findRoutes(mockLocation1, mockLocation2, {
			assert(executionCount == 3)
		})
	}

}
