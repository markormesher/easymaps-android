package uk.co.markormesher.easymaps.mapperapp.data

import android.graphics.Color

enum class TravelMode(val key: String, val displayName: String, val colourCode: Int) {

	UNKNOWN("unknown", "", 0),

	WALK("walk", "Walk", Color.parseColor("#dcdcdc")),

	BAKERLOO("tube/bakerloo", "Bakerloo line", Color.parseColor("#b26400")),
	CENTRAL("tube/central", "Central line", Color.parseColor("#dc241f")),
	CIRCLE("tube/circle", "Circle line", Color.parseColor("#ffd329")),
	DISTRICT("tube/district", "District line", Color.parseColor("#007d32")),
	HAMMERSMITH_AND_CITY("tube/hammersmith-and-city", "Hammersmith and City line", Color.parseColor("#f4a9be")),
	JUBILEE("tube/jubilee", "Jubilee line", Color.parseColor("#a1a5a7")),
	METROPOLITAN("tube/metropolitan", "Metropolitan line", Color.parseColor("#9b0058")),
	NORTHERN("tube/northern", "Northern line", Color.parseColor("#000000")),
	PICCADILLY("tube/piccadilly", "Piccadilly line", Color.parseColor("#0019a8")),
	VICTORIA("tube/victoria", "Victoria line", Color.parseColor("#0098d8")),
	WATERLOO_AND_CITY("tube/waterloo-and-city", "Waterloo and City line", Color.parseColor("#93ceba"));

	fun isTube() = this != WALK && this != UNKNOWN

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
