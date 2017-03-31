package uk.co.markormesher.easymaps.mapperapp.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.co.markormesher.easymaps.mapperapp.data.Location
import uk.co.markormesher.easymaps.mapperapp.data.LocationType

class Maths {

	val mockLocation1 = Location("mock-loc-1", "Mock Location 1", null, LocationType.STATION, 1.0, 2.0)
	val mockLocation2 = Location("mock-loc-2", "Mock Location 2", null, LocationType.STATION, 3.0, 4.0)

	@Test
	fun distanceInKmShouldBeAccurate() {
		assertEquals(0.0, distanceInMetres(mockLocation1, mockLocation1), 0.0)
		assertEquals(0.0, distanceInMetres(mockLocation2, mockLocation2), 0.0)

		assertEquals(0.0, distanceInMetres(mockLocation1, mockLocation2), 314402.95102362486)
	}

	@Test
	fun distanceInKmShouldNotDependOnOrder() {
		assertTrue(distanceInMetres(mockLocation1, mockLocation2) == distanceInMetres(mockLocation2, mockLocation1))
	}

}
