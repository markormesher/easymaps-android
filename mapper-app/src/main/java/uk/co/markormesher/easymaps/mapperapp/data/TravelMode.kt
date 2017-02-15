package uk.co.markormesher.easymaps.mapperapp.data

import android.graphics.Color

enum class TravelMode(val key: String, val displayName: String, val colourCode: Int, val verb: String) {

	UNKNOWN("unknown", "", 0, ""),

	WALK("walk",
			"Walk",
			Color.parseColor("#dcdcdc"),
			"Walk to %s"),

	BAKERLOO("tube/bakerloo",
			"Bakerloo line",
			Color.parseColor("#b26400"),
			"Take the Bakerloo line to %s"),

	CENTRAL("tube/central",
			"Central line",
			Color.parseColor("#dc241f"),
			"Take the Central line to %s"),

	CIRCLE("tube/circle",
			"Circle line",
			Color.parseColor("#ffd329"),
			"Take the Circle line to %s"),

	DISTRICT("tube/district",
			"District line",
			Color.parseColor("#007d32"),
			"Take the District line to %s"),

	HAMMERSMITH_AND_CITY("tube/hammersmith-and-city",
			"Hammersmith and City line",
			Color.parseColor("#f4a9be"),
			"Take the Hammersmith and City line to %s"),

	JUBILEE("tube/jubilee",
			"Jubilee line",
			Color.parseColor("#a1a5a7"),
			"Take the Jubilee line to %s"),

	METROPOLITAN("tube/metropolitan",
			"Metropolitan line",
			Color.parseColor("#9b0058"),
			"Take the Metropolitan line to %s"),

	NORTHERN("tube/northern",
			"Northern line",
			Color.parseColor("#000000"),
			"Take the Northern line to %s"),

	PICCADILLY("tube/piccadilly",
			"Piccadilly line",
			Color.parseColor("#0019a8"),
			"Take the Piccadilly line to %s"),

	VICTORIA("tube/victoria",
			"Victoria line",
			Color.parseColor("#0098d8"),
			"Take the Victoria line to %s"),

	WATERLOO_AND_CITY("tube/waterloo-and-city",
			"Waterloo and City line",
			Color.parseColor("#93ceba"),
			"Take the Waterloo and City line to %s");

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
