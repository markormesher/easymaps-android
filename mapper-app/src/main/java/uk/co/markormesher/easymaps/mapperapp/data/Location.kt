package uk.co.markormesher.easymaps.mapperapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import org.json.JSONObject
import uk.co.markormesher.easymaps.mapperapp.R

data class Location(
		val id: String,
		val title: String,
		val image: String?,
		val type: LocationType,
		val lat: Double,
		val lon: Double) {

	fun getDisplayTitle(context: Context): String = when (type) {
		LocationType.ATTRACTION -> context.getString(R.string.location_attraction_display_template, title)
		LocationType.STATION -> context.getString(R.string.location_station_display_template, title)
	}

	fun toContentValues(): ContentValues {
		val cv = ContentValues()
		cv.put(LocationSchema.id, id)
		cv.put(LocationSchema.title, title)
		cv.put(LocationSchema.image, image)
		cv.put(LocationSchema.type, type.id)
		return cv
	}

	companion object {
		fun fromCursor(cursor: Cursor): Location {
			return Location(
					id = cursor.getString(cursor.getColumnIndexOrThrow(LocationSchema.id)),
					title = cursor.getString(cursor.getColumnIndexOrThrow(LocationSchema.title)),
					image = cursor.getString(cursor.getColumnIndexOrThrow(LocationSchema.image)),
					type = LocationType.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(LocationSchema.type))),
					lat = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationSchema.lat)),
					lon = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationSchema.lon))
			)
		}

		fun fromJson(id: String, json: JSONObject): Location {
			return Location(
					id = id,
					title = json.getString("title"),
					image = if (json.has("image")) json.getString("image") else null,
					type = LocationType.fromString(json.getString("type")),
					lat = if (json.has("lat")) json.getDouble("lat") else 0.0,
					lon = if (json.has("lon")) json.getDouble("lon") else 0.0
			)
		}
	}

}

enum class LocationType(val id: Int) {

	ATTRACTION(0), STATION(1);

	companion object {
		fun fromId(id: Int): LocationType = when (id) {
			0 -> ATTRACTION
			1 -> STATION
			else -> throw IllegalArgumentException()
		}

		fun fromString(str: String): LocationType = when (str) {
			"attraction" -> ATTRACTION
			"station" -> STATION
			else -> STATION
		}
	}
}

object LocationSchema {

	val _tableName = "Location"
	val id = "id"
	val title = "title"
	val image = "image"
	val type = "type"
	val lat = "lat"
	val lon = "lon"

	object v1 {
		val createTable = "CREATE TABLE $_tableName (" +
				"$id TEXT PRIMARY KEY," +
				"$title TEXT," +
				"$image TEXT," +
				"$type INTEGER" +
				");"
	}

	object v2 {
		val addLat = "ALTER TABLE $_tableName ADD COLUMN lat REAL DEFAULT 0;"
		val addLon = "ALTER TABLE $_tableName ADD COLUMN lon REAL DEFAULT 0;"
	}
}
