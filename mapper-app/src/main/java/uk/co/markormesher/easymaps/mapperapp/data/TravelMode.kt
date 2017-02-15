package uk.co.markormesher.easymaps.mapperapp.data

enum class TravelMode(val key: String) {

	WALK("walk"),
	UNKNOWN("unknown"),

	BAKERLOO("tube/bakerloo"),
	CENTRAL("tube/central"),
	CIRCLE("tube/circle"),
	DISTRICT("tube/district"),
	HAMMERSMITH_AND_CITY("tube/hammersmith-and-city"),
	JUBILEE("tube/jubilee"),
	METROPOLITAN("tube/metropolitan"),
	NORTHERN("tube/northern"),
	PICCADILLY("tube/piccadilly"),
	VICTORIA("tube/victoria"),
	WATERLOO_AND_CITY("tube/waterloo-and-city");

	companion object {
		fun fromKey(key: String) = when (key) {
			"walk" -> WALK
			"tube/bakerloo" -> BAKERLOO
			"tube/central" -> CENTRAL
			"tube/circle" -> CIRCLE
			"tube/district" -> DISTRICT
			"tube/hammersmith-and-city" -> HAMMERSMITH_AND_CITY
			"tube/jubilee" -> JUBILEE
			"tube/metropolitan" -> METROPOLITAN
			"tube/northern" -> NORTHERN
			"tube/piccadilly" -> PICCADILLY
			"tube/victoria" -> VICTORIA
			"tube/waterloo-and-city" -> WATERLOO_AND_CITY
			else -> UNKNOWN
		}
	}
}
