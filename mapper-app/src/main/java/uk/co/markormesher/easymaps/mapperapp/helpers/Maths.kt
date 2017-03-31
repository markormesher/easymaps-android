package uk.co.markormesher.easymaps.mapperapp.helpers

import uk.co.markormesher.easymaps.mapperapp.data.Location

fun distanceInMetres(location1: Location, location2: Location): Double {
	return distanceInMetres(location1.lat, location1.lon, location2.lat, location2.lon)
}

fun distanceInMetres(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
	val earthRadius = 6371

	val latDistance = toRadians(lat2 - lat1)
	val lonDistance = toRadians(lon2 - lon1)
	val a = sin(latDistance / 2) * sin(latDistance / 2) + cos(toRadians(lat1)) * cos(toRadians(lat2)) * sin(lonDistance / 2) * sin(lonDistance / 2)
	val c = 2 * atan2(sqrt(a), sqrt(1 - a))
	val distance = earthRadius.toDouble() * c * 1000.0

	return distance
}

private fun sin(x: Double) = Math.sin(x)
private fun cos(x: Double) = Math.cos(x)
private fun sqrt(x: Double) = Math.sqrt(x)
private fun atan2(x: Double, y: Double) = Math.atan2(x, y)
private fun toRadians(x: Double) = Math.toRadians(x)
